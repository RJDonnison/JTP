package org.reujdon.jtp.server;

import org.reujdon.jtp.shared.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * A secure SSL/TLS server implementation that handles multiple client connections concurrently.
 *
 * <p>
 * Configuration can be provided through either:
 * <ul>
 *   <li>Environment variables (highest priority)</li>
 *   <li>Properties file (fallback)</li>
 * </ul>
 *
 * <table border="1">
 *   <caption>Configuration Options</caption>
 *   <thead>
 *     <tr>
 *       <th>Description</th>
 *       <th>Environment Variable</th>
 *       <th>Properties Key</th>
 *       <th>Required</th>
 *       <th>Default</th>
 *     </tr>
 *   </thead>
 *   <tbody>
 *     <tr>
 *       <td>Server port</td>
 *       <td>{@code SERVER_PORT}</td>
 *       <td>{@code server.port}</td>
 *       <td>Yes</td>
 *       <td>None</td>
 *     </tr>
 *     <tr>
 *       <td>Path to SSL keystore file</td>
 *       <td>{@code SERVER_KEYSTORE_PATH}</td>
 *       <td>{@code sever.path}</td>
 *       <td>Yes</td>
 *       <td>None</td>
 *     </tr>
 *     <tr>
 *       <td>Password for the keystore</td>
 *       <td>{@code SERVER_KEYSTORE_PASSWORD}</td>
 *       <td>{@code server.password}</td>
 *       <td>Yes</td>
 *       <td>None</td>
 *     </tr>
 *   </tbody>
 * </table>
 * <p>
 * <b>Note:</b> Port values must be between 0-65535.
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * Server server = new Server();
 * server.addCommand("test", params -> new JSONObject().put("status", "success"));
 * server.start();
 * }
 * </pre>
 *
 * @see ClientHandler
 * @see SSLServerSocket
 */
public class JTPServer {
    private static final Logger logger = LoggerFactory.getLogger(JTPServer.class);

    // Constants for environment variable keys
    private static final String ENV_PORT = "SERVER_PORT";
    private static final String ENV_KEYSTORE_PATH = "SERVER_KEYSTORE_PATH";
    private static final String ENV_KEYSTORE_PASSWORD = "SERVER_KEYSTORE_PASSWORD";
    private static final String DEFAULT_CONFIG_FILE = "server.properties";

    private int port = -1;
    private String keystorePath;
    private String keystorePassword;

    private SSLServerSocket serverSocket;

    private final ExecutorService clientThreadPool;
    private final ConcurrentHashMap<String, ClientHandler> activeClients = new ConcurrentHashMap<>();

    private boolean running;

//    TODO: add authentication setting
    /**
     * Constructs a new {@code Server} with default config file.
     * <p>
     * Configuration will be loaded from:
     * <ol>
     *   <li>Environment variables</li>
     *   <li>Default config file ({@code server.properties})</li>
     * </ol>
     *
     * @throws IllegalArgumentException if required configuration is missing or invalid
     *
     * @see #JTPServer(String) for details on server initialization
     */
    public JTPServer() {
        this(null);
    }

    /**
     * Constructs a new {@code Server} with configuration from the specified file.
     * <p>
     * Configuration will be loaded from:
     * <ol>
     *   <li>Environment variables</li>
     *   <li>Default config file ({@code server.properties})</li>
     * </ol>
     *
     * @throws IllegalArgumentException if required configuration is missing or invalid
     * @throws RuntimeException If there's an issue initializing server resources.
     *
     * @see #JTPServer(String) for details on server initialization
     */
    public JTPServer(String configFile) {
        loadConfig(configFile);

        this.running = false;

        try {
            this.clientThreadPool = Executors.newVirtualThreadPerTaskExecutor();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize server components", e);
        }
    }

    /**
     * Loads configuration from environment variables and properties file.
     *
     * @param configFile the path to the properties file (may be null)
     */
    private void loadConfig(String configFile){
        loadFromEnvVars();

        if (hasMissingConfig()) {
            if (configFile == null)
                configFile = DEFAULT_CONFIG_FILE;

            loadFromPropertiesFile(configFile);
        }

        validateConfig();
    }

    /**
     * Checks if any required configuration is missing.
     */
    private boolean hasMissingConfig() {
        return port == -1 || keystorePath == null || keystorePassword == null;
    }

    /**
     * Loads configuration from environment variables.
     */
    private void loadFromEnvVars() {
        String envPort = System.getenv(ENV_PORT);
        if (envPort != null) {
            try {
                this.port = Integer.parseInt(envPort);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid PORT in env vars", e);
            }
        }

        String envKeystorePath = System.getenv(ENV_KEYSTORE_PATH);
        if (envKeystorePath != null)
            this.keystorePath = envKeystorePath;

        String envKeystorePassword = System.getenv(ENV_KEYSTORE_PASSWORD);
        if (envKeystorePassword != null)
            this.keystorePassword = envKeystorePassword;
    }

    /**
     * Loads configuration from properties file.
     *
     * @param configFile the path to the properties file
     * @throws IllegalArgumentException if the file is invalid
     */
    private void loadFromPropertiesFile(String configFile) {
        if (this.port == -1)
            this.port = PropertiesUtil.getInteger(configFile, "server.port");

        if (this.keystorePath == null)
            this.keystorePath = PropertiesUtil.getString(configFile, "server.path");

        if (this.keystorePassword == null)
            this.keystorePassword = PropertiesUtil.getString(configFile, "server.password");
    }

    /**
     * Validates the loaded configuration.
     *
     * @throws IllegalArgumentException if any configuration is invalid
     */
    private void validateConfig() {
        if (this.port < 0 || this.port > 65536)
            throw new IllegalArgumentException("PORT must be between 0 and 65536 and set via " + ENV_PORT + " or properties file");

        if (this.keystorePath == null || this.keystorePath.trim().isEmpty())
            logger.warn("Keystore path not set");

        if (this.keystorePath != null && this.keystorePassword == null)
            throw new IllegalArgumentException("Truststore password must be set via " + ENV_KEYSTORE_PASSWORD + " or properties file");
    }

    /**
     * Starts the server and client connection handling.
     *
     * @throws IllegalStateException If the server is already running
     * @throws RuntimeException If server fails to start.
     *
     * @see #close()
     */
    public void start() {
        if (running)
            throw new IllegalStateException("Server is already running");

        try {
            SSLContext sslContext = createSSLContext();
            SSLServerSocketFactory ssf = sslContext.getServerSocketFactory();
            serverSocket = (SSLServerSocket) ssf.createServerSocket(this.port);

            // Register shutdown hook for graceful termination
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutdown triggered, closing server...");
                close();
            }));

            logger.info("Server started on port {}", this.port);

            running = true;
            handleClients();
        } catch (Exception e) {
            logger.error("Failed to start Server: {}", e.getMessage());
            throw new RuntimeException("Server initialization failed", e);
        } finally {
            close();
        }
    }

    /**
     * Creates an SSLContext, falling back to a basic context if no keystore is present.
     *
     * @return Configured SSLContext
     * @throws RuntimeException if SSLContext creation fails
     */
    private SSLContext createSSLContext() {
        // Validate inputs
        if (keystorePath == null || keystorePath.trim().isEmpty()) {
            try {
                // Create a basic SSL context without client authentication
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, null, new SecureRandom());
                logger.warn("No keystore provided - using basic SSLContext without certificate verification");
                return sslContext;
            } catch (Exception e) {
                throw new RuntimeException("Failed to create basic SSLContext", e);
            }
        }

        File keystoreFile = new File(keystorePath);
        if (!keystoreFile.exists())
            throw new IllegalArgumentException("Keystore file not found at: " + keystorePath);

        try (FileInputStream fis = new FileInputStream(keystorePath)) {
            // Load keystore
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(fis, keystorePassword.toCharArray());

            // Initialize key manager factory
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, keystorePassword.toCharArray());

            // Initialize trust manager factory
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(keyStore);

            // Create and initialize SSL context
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());

            return sslContext;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize server SSLContext", e);
        }
    }

    /**
     * Continuously accepts and handles incoming client connections in a loop while the server is running.
     * Each connected client is processed in a separate thread from the thread pool.
     *
     * @throws IOException If a fatal I/O error occurs while accepting connections
     * @throws IllegalStateException If the server socket is not properly initialized
     */
    private void handleClients() throws IOException {
        if (serverSocket == null || serverSocket.isClosed())
            throw new IllegalStateException("Server socket is closed so cannot handle clients.");

        logger.info("Waiting for clients to connect...");

        while (running && !serverSocket.isClosed()) {
            try {
                // Accept new client connection
                SSLSocket clientSocket = (SSLSocket) serverSocket.accept();

                String clientId = clientSocket.getRemoteSocketAddress().toString();
                logger.info("New connection attempt from: {}", clientId);

                // Create and register client handler
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                activeClients.put(clientId, clientHandler);
                clientThreadPool.execute(clientHandler);

                logger.info("New client connected, ID: {}. Active clients: {}", clientId, activeClients.size());
            } catch (SSLException e) {
                logger.error("SSL handshake failed with client: {}", e.getMessage());
            } catch (IOException e) {
                if (running)
                    logger.error("Fatal I/O error while accepting connections");
                else
                    logger.warn("I/O error occurred during server shutdown");

                throw e;
            }
        }
    }

    /**
     * Removes a client from the active clients registry and logs the disconnection.
     *
     * @param clientId The unique identifier of the client to remove
     * @throws IllegalArgumentException If clientId is null or empty
     *
     * @see #activeClients
     * @see ClientHandler
     */
    synchronized void removeClient(String clientId) {
        if (clientId == null || clientId.trim().isEmpty())
            throw new IllegalArgumentException("Client ID must not be null or empty");

        ClientHandler removedHandler = activeClients.remove(clientId);
        if (removedHandler != null)
            logger.info("Client {} disconnected. Active clients: {}", clientId, activeClients.size());
    }

    /**
     * @return if server is running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Gracefully shuts down the server.
     *
     * @see #activeClients
     * @see ClientHandler#close()
     */
    public void close() {
        if (!running)
            logger.info("Server is closing or closed");

        running = false;
        logger.info("\nClosing server...");

        closeAllClients();
        shutdownThreadPool();
        closeServerSocket();

        logger.info("Server shutdown completed successfully");
    }

    /**
     * Closes all active client connections and clears the registry.
     * Handles each client close operation individually to ensure maximum
     * connections get closed even if some fail.
     */
    private void closeAllClients() {
        int clientCount = activeClients.size();
        if (clientCount == 0) {
            logger.info("No active clients to close");
            return;
        }

        logger.info("Closing {} active client connections...", clientCount);
        int closedCount = 0;
        int failedCount = 0;

        for (Map.Entry<String, ClientHandler> entry : activeClients.entrySet()) {
            try {
                entry.getValue().close();
                closedCount++;
                logger.info("Closed connection for client: {}", entry.getKey());
            } catch (Exception e) {
                failedCount++;
                logger.warn("Failed to close client: {}", entry.getKey());
            }
        }

        activeClients.clear();
        logger.info("Client cleanup completed: {} closed, {} failed.", closedCount, failedCount);
    }

    /**
     * Shuts down the client thread pool with proper timeout handling.
     * Attempts graceful shutdown first, then forces shutdown if needed.
     */
    private void shutdownThreadPool() {
        if (clientThreadPool == null) {
            logger.warn("Thread pool not initialized");
            return;
        }

        try {
            logger.info("Initiating thread pool shutdown...");
            clientThreadPool.shutdown(); // Disable new tasks

            // Wait a while for existing tasks to terminate
            if (!clientThreadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                logger.warn("Forcing thread pool shutdown...");
                clientThreadPool.shutdownNow(); // Cancel currently executing tasks

                // Wait again for tasks to respond to cancellation
                if (!clientThreadPool.awaitTermination(5, TimeUnit.SECONDS))
                    logger.warn("Thread pool did not terminate properly");
            }
        } catch (InterruptedException e) {
            logger.error("Thread pool shutdown interrupted");
            Thread.currentThread().interrupt();
            clientThreadPool.shutdownNow();
        }
    }

    /**
     * Closes the server socket with proper error handling.
     */
    private void closeServerSocket() {
        if (serverSocket == null) {
            logger.warn("Server socket not initialized");
            return;
        }

        try {
            if (!serverSocket.isClosed()) {
                logger.info("Closing server socket...");
                serverSocket.close();
            }
        } catch (IOException e) {
            logger.error("Error closing server socket: {}", e.getMessage());
        }
    }

    /**
     * Registers a command with a {@link CommandHandler} implementation.
     * Will not override existing commands.
     *
     * @param command The command string to register
     * @param handler The command handler implementation to register
     * @throws IllegalArgumentException if command is null/empty or handler is null
     */
    public void addCommand(String command, CommandHandler handler) {
        this.addCommand(command, handler, false);
    }

    /**
     * Registers a command with a {@link CommandHandler} implementation
     * with option to override existing commands.
     *
     * @param command The command string to register
     * @param handler The command handler implementation to register
     * @param overrideExisting If true, will replace existing command
     * @throws IllegalArgumentException if command is null/empty or handler is null
     */
    public void addCommand(String command, CommandHandler handler, boolean overrideExisting) {
        if (command == null || command.trim().isEmpty())
            throw new IllegalArgumentException("Command must not be null or empty");

        if (handler == null)
            throw new IllegalArgumentException("Handler must not be null");

        CommandRegistry.register(command, handler, overrideExisting);
    }

    public static void main(String[] args) {
        JTPServer server = new JTPServer();
        server.start();
    }
}
