package org.reujdon.jtp.client;

import org.reujdon.jtp.client.commands.Command;
import org.reujdon.jtp.shared.json.JsonException;
import org.reujdon.jtp.shared.messaging.Message;
import org.reujdon.jtp.shared.messaging.MessageFactory;
import org.reujdon.jtp.shared.messaging.messages.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Secure SSL/TLS client implementation for JTP communication.
 *
 * <p>Handles connection to server and command execution with response handling.
 * Configuration can be provided through environment variables or properties file.</p>
 *
 * @author Reuben Donnison
 * @version 0.2
 * @see JTPClientConfig
 * @see Runnable
 * @see AutoCloseable
 */
public class JTPClient implements Runnable, AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(JTPClient.class);

    private final JTPClientConfig config;

    private SSLSocket sslSocket;

    private BufferedReader in;
    private PrintWriter out;

    private volatile boolean running = false;
    private volatile boolean closing = false;

    private Thread listeningThread;

    private final ResponseHandler responseHandler = new ResponseHandler();
    private final ExecutorService responseExecutor = Executors.newVirtualThreadPerTaskExecutor();

    private final Queue<Command> pendingQueue = new ConcurrentLinkedQueue<>();

    /**
     * Constructs client with default configuration file.
     *
     * @throws IllegalArgumentException if required configuration is missing/invalid
     * @see #JTPClient(String)
     */
    public JTPClient(){ this(null); }

    /**
     * Constructs client with specified configuration file.
     *
     * @param configFile path to configuration file (null for default)
     * @throws IllegalArgumentException if required configuration is missing/invalid
     * @see #JTPClient()
     */
    public JTPClient(String configFile) {
        this.config = new JTPClientConfig();
        config.loadConfig(configFile, "client.properties");
    }

    /**
     * Initializes SSL connection and starts client operations.
     *
     * @throws RuntimeException if initialization fails
     * @see #close()
     */
    @Override
    public void run() {
        try{
            SSLContext sslContext = createSSLContext();
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            sslSocket = (SSLSocket) sslSocketFactory.createSocket(config.host, config.port);

            sslSocket.setEnabledCipherSuites(sslSocket.getSupportedCipherSuites());

            in = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
            out = new PrintWriter(sslSocket.getOutputStream(), true);

            logger.info("Connected to server at {} : {}\n", config.host, config.port);

            running = true;

            Thread.startVirtualThread(() -> {
                while (running) {
                    if (!pendingQueue.isEmpty() && responseHandler.isAuthenticated()){
                        flushPendingQueue();
                        break;
                    }
                }});
        } catch (Exception e) {
            logger.error("Failed to start client: {}", e.getMessage());
            close();
            throw new RuntimeException("Client initialization failed", e);
        }

        listeningThread = Thread.startVirtualThread(this::handleResponses);

        sendAuth();
    }

    /**
     * Creates SSLContext for secure communication.
     *
     * @return initialized SSLContext
     * @throws RuntimeException if SSL context cannot be created
     */
    private SSLContext createSSLContext() {
        try {
            if (config.truststorePath == null || config.truststorePath.isBlank()) {
                logger.warn("No truststore configured - using default SSLContext with standard certificate validation");
                return SSLContext.getDefault();
            }

            File truststoreFile = new File(config.truststorePath);
            if (!truststoreFile.exists())
                throw new FileNotFoundException("Truststore file not found at: " + config.truststorePath);

            try (FileInputStream fis = new FileInputStream(config.truststorePath)) {
                // Load the truststore
                KeyStore trustStore = KeyStore.getInstance("JKS");
                trustStore.load(fis, config.truststorePassword.toCharArray());

                // Initialize trust manager factory
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(trustStore);

                // Create SSL context with the trust managers
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());

                return sslContext;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize SSL context", e);
        }
    }

    /**
     * Sends authentication message to server.
     */
    private void sendAuth() {
        logger.info("Client authenticating...");

        Auth auth = new Auth(config.apiKey);
        out.println(auth.toJSON());
        out.flush();
    }

    /**
     * Listens for and processes server responses.
     */
    private void handleResponses() {
        String message;

        try {
            while (running && (message = in.readLine()) != null) {
                Message deserilaizedMessage = MessageFactory.deserialize(message);

                responseExecutor.submit(() -> responseHandler.processResponse(deserilaizedMessage));
            }
        } catch (IOException e) {
            if (running)
                logger.error("Error while listening for responses: {}", e.getMessage());
            else
                logger.info("Listening thread closed.");
        } catch (JsonException e) {
            logger.error("Error parsing response: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error while handling response: {}", e.getClass().getSimpleName());
        }
    }

    /**
     * Sends command to server for execution.
     *
     * @param command the command to send
     * @throws IllegalArgumentException if command is invalid
     */
    public void sendCommand(Command command) {
        if (closing || !running)
            throw new IllegalStateException("Client is shutting down");

        if (command == null)
            throw new IllegalArgumentException("Command cannot be null");

        String id = command.getId();
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("Command id cannot be null or blank");

        if (!responseHandler.isAuthenticated()){
            logger.warn("Client is not authenticated queuing command: {}", id);
            pendingQueue.add(command);
            return;
        }

        sendCommandNow(command);
    }

    /**
     * Sends queued commands after authentication.
     */
    private void flushPendingQueue() {
        Command command;
        while ((command = pendingQueue.poll()) != null) {
            logger.info("Sending queued command: {}", command.getId());
            sendCommandNow(command);
        }
    }

    /**
     * Immediately sends command to server.
     *
     * @param command the command to send
     */
    private void sendCommandNow(Command command) {
        responseHandler.addPendingRequest(command.getId(), command);

        command.setToken(responseHandler.getToken());

        try {
            out.println(command.toJSON());
            out.flush();
        } catch (Exception e) {
            logger.error("Failed to send command: {}", e.getMessage(), e);
        }
    }

    /**
     * Gracefully closes client connection and resources.
     *
     * @see #run()
     */
    @Override
    public void close() {
        logger.info("Closing connection...");

        closing = true;

        waitForPendingCommands();

        running = false;

        shutdownResponseExecutor();
        stopListeningThread();
        closeStreamsAndSocket();

        closing = false;
        logger.info("Client resources closed successfully.");
    }

    /**
     * Waits for completion of pending commands during graceful shutdown.
     *
     * <p>Performs the following actions:</p>
     * <ol>
     *   <li>Attempts to flush queued commands if authenticated</li>
     *   <li>Polls for pending responses at fixed intervals</li>
     *   <li>Respects maximum wait time (5000ms) and interrupt signals</li>
     * </ol>
     *
     * <p>Logs final status of unprocessed commands if timeout occurs.</p>
     *
     * @see #flushPendingQueue()
     * @see ResponseHandler#getPendingCommandCount()
     * @see ResponseHandler#isAuthenticated()
     */
    private void waitForPendingCommands() {
        logger.info("Waiting for pending responses to complete...");

        final int maxWaitMs = config.shutdownTimeout;
        final int checkIntervalMs = 100;
        final long endTime = System.currentTimeMillis() + maxWaitMs;

        while ((!pendingQueue.isEmpty() || responseHandler.getPendingCommandCount() > 0)
                && System.currentTimeMillis() < endTime) {
            if (responseHandler.isAuthenticated())
                flushPendingQueue();

            try {
                long remaining = endTime - System.currentTimeMillis();
                Thread.sleep(Math.min(checkIntervalMs, remaining));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Interrupted while waiting for pending commands");
                break;
            }
        }

        int remainingCommands = responseHandler.getPendingCommandCount();
        int queuedCommands = pendingQueue.size();

        if (remainingCommands > 0 || queuedCommands > 0)
            logger.warn("Shutdown completed with {} pending responses and {} queued commands unprocessed", remainingCommands, queuedCommands);
        else
            logger.info("All pending commands processed successfully");
    }

    /**
     * Shuts down response executor service.
     */
    private void shutdownResponseExecutor() {
        responseExecutor.shutdown();
        try {
            if (!responseExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                logger.warn("Response executor did not terminate in time; forcing shutdown.");
                responseExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted during response executor shutdown.");
        }
    }

    /**
     * Stops listening thread.
     */
    private void stopListeningThread() {
        if (listeningThread != null && listeningThread.isAlive()) {
            listeningThread.interrupt();
            try {
                listeningThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Interrupted while waiting for listening thread to stop.");
            }
        }
    }

    /**
     * Closes I/O streams and socket.
     */
    private void closeStreamsAndSocket() {
        try {
            if (out != null) {
                out.close();
                logger.info("Output stream closed.");
            }
            if (in != null) {
                in.close();
                logger.info("Input stream closed.");
            }
            if (sslSocket != null && !sslSocket.isClosed()) {
                sslSocket.close();
                logger.info("SSL socket closed.");
            }
        } catch (IOException e) {
            logger.error("Error while closing the client SSL socket or streams: {}", e.getMessage());
        }
    }
}
