package org.reujdon.jtp.server;

import org.reujdon.jtp.server.handlers.CommandHandler;
import org.reujdon.jtp.server.handlers.CommandRegistry;

import javax.net.ssl.*;
import java.io.*;
import java.security.KeyStore;
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
    private static final String KEYSTORE_PATH = "Server/server_keystore.jks";
    private static final String KEYSTORE_PASSWORD = "serverpassword";

    private final int PORT;

    private SSLServerSocket serverSocket;

    private final ExecutorService clientThreadPool;
    private final ConcurrentHashMap<String, ClientHandler> activeClients = new ConcurrentHashMap<>();

    private boolean running;

    /**
     * Constructs a Server instance with the default port 8080.
     *
     * @see #Server(int) for details on server initialization
     */
    public Server() {
        this(8080);
    }

    /**
     * Constructs a Server instance with the specified port.
     * Initializes the command registry and a cached thread pool for handling client connections.
     *
     * @param port The port number on which the server will listen. Must be between 0 and 65535.
     * @throws IllegalArgumentException If the port number is invalid (negative or greater than 65535)
     * @throws RuntimeException If there's an issue initializing server resources.
     */
    public Server(int port) {
        if (port < 0 || port > 65535)
            throw new IllegalArgumentException(String.format("Invalid port number: %d. Port must be between 0 and 65535", port));

        this.PORT = port;
        this.running = false;

        try {
            this.clientThreadPool = Executors.newCachedThreadPool();
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
                System.out.println("Shutdown triggered, closing server...");
                close();
            }));

            System.out.println("Server started on port " + this.PORT);

            running = true;
            handleClients();
        } catch (Exception e) {
            System.err.println("\nFailed to start Server: " + e.getMessage());
            throw new RuntimeException("Server initialization failed", e);
        } finally {
            close();
        }
    }

    /**
     * Creates and initializes an SSLContext for secure communication using TLS protocol.
     * The SSLContext is configured with key and trust managers loaded from a JKS keystore.
     *
     * @return Initialized SSLContext ready for use in secure communications
     */
    private static SSLContext createSSLContext() {
        // Validate inputs
        //noinspection ConstantValue
        if (KEYSTORE_PATH.trim().isEmpty())
            throw new IllegalArgumentException("Keystore path must not be null or empty");

        FileInputStream fis = null;

        try {
            // Load keystore
            KeyStore keyStore = KeyStore.getInstance("JKS");
            fis = new FileInputStream(KEYSTORE_PATH);
            keyStore.load(fis, KEYSTORE_PASSWORD.toCharArray());

            // Initialize key manager factory
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, KEYSTORE_PASSWORD.toCharArray());

            // Initialize trust manager factory
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(keyStore);

            // Create and initialize SSL context
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

            return sslContext;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize server SSLContext", e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    System.err.println("Warning: Failed to close keystore file input stream: " + e.getMessage());
                }
            }
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

        System.out.println("Waiting for clients to connect...");

        while (running && !serverSocket.isClosed()) {
            try {
                // Accept new client connection
                SSLSocket clientSocket = (SSLSocket) serverSocket.accept();

                String clientId = clientSocket.getRemoteSocketAddress().toString();
                System.out.println("\nNew connection attempt from: " + clientId);

                // Create and register client handler
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                activeClients.put(clientId, clientHandler);
                clientThreadPool.execute(clientHandler);

                System.out.println("New client connected, ID: " + clientId + ". Active clients: " + activeClients.size());
            } catch (SSLException e) {
                System.err.println("SSL handshake failed with client: " + e.getMessage());
            } catch (IOException e) {
                if (running)
                    System.err.println("Fatal I/O error while accepting connections");
                else
                    System.out.println("I/O error occurred during server shutdown");

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
            System.out.println("\nClient " + clientId + " disconnected. Active clients: " + activeClients.size());
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
            System.out.println("Server is closing or closed");

        running = false;
        System.out.println("\nClosing server...");

        closeAllClients();
        shutdownThreadPool();
        closeServerSocket();

        System.out.println("Server shutdown completed successfully");
    }

    /**
     * Closes all active client connections and clears the registry.
     * Handles each client close operation individually to ensure maximum
     * connections get closed even if some fail.
     */
    private void closeAllClients() {
        int clientCount = activeClients.size();
        if (clientCount == 0) {
            System.out.println("No active clients to close");
            return;
        }

        System.out.println("Closing " + clientCount + " active client connections...");
        int closedCount = 0;
        int failedCount = 0;

        for (Map.Entry<String, ClientHandler> entry : activeClients.entrySet()) {
            try {
                entry.getValue().close();
                closedCount++;
                System.out.println("Closed connection for client: " + entry.getKey());
            } catch (Exception e) {
                failedCount++;
                System.err.println("Failed to close client: " + entry.getKey());
            }
        }

        activeClients.clear();
        System.out.println("Client cleanup completed: " + closedCount + " closed, " + failedCount + " failed.");
    }

    /**
     * Shuts down the client thread pool with proper timeout handling.
     * Attempts graceful shutdown first, then forces shutdown if needed.
     */
    private void shutdownThreadPool() {
        if (clientThreadPool == null) {
            System.out.println("Thread pool not initialized");
            return;
        }

        try {
            System.out.println("Initiating thread pool shutdown...");
            clientThreadPool.shutdown(); // Disable new tasks

            // Wait a while for existing tasks to terminate
            if (!clientThreadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                System.out.println("Forcing thread pool shutdown...");
                clientThreadPool.shutdownNow(); // Cancel currently executing tasks

                // Wait again for tasks to respond to cancellation
                if (!clientThreadPool.awaitTermination(5, TimeUnit.SECONDS))
                    System.err.println("Thread pool did not terminate properly");
            }
        } catch (InterruptedException e) {
            System.err.println("Thread pool shutdown interrupted");
            Thread.currentThread().interrupt();
            clientThreadPool.shutdownNow();
        }
    }

    /**
     * Closes the server socket with proper error handling.
     */
    private void closeServerSocket() {
        if (serverSocket == null) {
            System.out.println("Server socket not initialized");
            return;
        }

        try {
            if (!serverSocket.isClosed()) {
                System.out.println("Closing server socket...");
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket." + e.getMessage());
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
        Server server = new Server();

        server.start();
    }
}
