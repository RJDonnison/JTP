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
 * <p>The server provides the following features:</p>
 * <ul>
 *   <li>Secure communication using SSL/TLS protocol</li>
 *   <li>Multithreaded client handling using a thread pool</li>
 *   <li>Custom command registration and processing</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * Server server = new Server(8080);
 * server.addCommand("test", params -> new JSONObject().put("status", "success"));
 * server.start();
 * }
 * </pre>
 *
 * @see ClientHandler
 * @see SSLContext
 * @see SSLServerSocket
 */
public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private final String KEYSTORE_PATH;
    private final String KEYSTORE_PASSWORD;

    private final int PORT;

    private SSLServerSocket serverSocket;

    private final ExecutorService clientThreadPool;
    private final ConcurrentHashMap<String, ClientHandler> activeClients = new ConcurrentHashMap<>();

    private boolean running;

//    TODO: rework configs
    /**
     * Constructs a Server instance with the default port 8080.
     *
     * @param configFile the path to the configFile
     *
     * @see #Server(int, String) for details on server initialization
     */
    public Server(String configFile) {
        this(8080, configFile);
    }

    /**
     * Constructs a Server instance with the specified port.
     * Initializes the command registry and a cached thread pool for handling client connections.
     *
     * @param port The port number on which the server will listen. Must be between 0 and 65535.
     * @param configFile the path to the configFile
     * @throws IllegalArgumentException If the port number is invalid (negative or greater than 65535)
     * @throws RuntimeException If there's an issue initializing server resources.
     */
    public Server(int port, String configFile) {
        if (port < 0 || port > 65535)
            throw new IllegalArgumentException(String.format("Invalid port number: %d. Port must be between 0 and 65535", port));

        this.PORT = port;

        if (configFile == null || !configFile.trim().endsWith(".properties"))
            throw new IllegalArgumentException(String.format("Invalid config file: %s", configFile));

        KEYSTORE_PATH = PropertiesUtil.getString(configFile, "server.path");
        KEYSTORE_PASSWORD = PropertiesUtil.getString(configFile, "server.password");

        this.running = false;

        try {
            this.clientThreadPool = Executors.newVirtualThreadPerTaskExecutor();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize server components", e);
        }
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
            serverSocket = (SSLServerSocket) ssf.createServerSocket(this.PORT);

            // Register shutdown hook for graceful termination
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutdown triggered, closing server...");
                close();
            }));

            logger.info("Server started on port {}", this.PORT);

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
        if (KEYSTORE_PATH == null || KEYSTORE_PATH.trim().isEmpty()) {
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

        File keystoreFile = new File(KEYSTORE_PATH);
        if (!keystoreFile.exists())
            throw new IllegalArgumentException("Keystore file not found at: " + KEYSTORE_PATH);

        try (FileInputStream fis = new FileInputStream(KEYSTORE_PATH)) {
            // Load keystore
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(fis, KEYSTORE_PASSWORD.toCharArray());

            // Initialize key manager factory
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, KEYSTORE_PASSWORD.toCharArray());

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
        Server server = new Server("Server/myConfig.properties");
        server.start();
    }
}
