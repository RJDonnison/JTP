package org.reujdon.jtp.client;

import org.reujdon.jtp.client.commands.HelpCommand;
import org.reujdon.jtp.shared.PropertiesUtil;
import org.reujdon.jtp.shared.json.GsonAdapter;
import org.reujdon.jtp.shared.json.JsonAdapter;
import org.reujdon.jtp.shared.messaging.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reujdon.async.Async;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.security.KeyStore;
import java.security.SecureRandom;

/**
 * A secure client that connects to a server over SSL/TLS.
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
 *       <td>Server hostname or IP address</td>
 *       <td>{@code CLIENT_HOST}</td>
 *       <td>{@code client.host}</td>
 *       <td>Yes</td>
 *       <td>None</td>
 *     </tr>
 *     <tr>
 *       <td>Server port</td>
 *       <td>{@code CLIENT_PORT}</td>
 *       <td>{@code client.port}</td>
 *       <td>Yes</td>
 *       <td>None</td>
 *     </tr>
 *     <tr>
 *       <td>Path to SSL truststore file</td>
 *       <td>{@code CLIENT_TRUSTSTORE_PATH}</td>
 *       <td>{@code client.path}</td>
 *       <td>Yes</td>
 *       <td>None</td>
 *     </tr>
 *     <tr>
 *       <td>Password for the truststore</td>
 *       <td>{@code CLIENT_TRUSTSTORE_PASSWORD}</td>
 *       <td>{@code client.password}</td>
 *       <td>Yes</td>
 *       <td>None</td>
 *     </tr>
 *   </tbody>
 * </table>
 * <p>
 * <b>Note:</b> Port values must be between 0-65535.
 */
public class JTPClient {
    private static final Logger logger = LoggerFactory.getLogger(JTPClient.class);

    // Constants for environment variable keys
    private static final String ENV_HOST = "CLIENT_HOST";
    private static final String ENV_PORT = "CLIENT_PORT";
    private static final String ENV_TRUSTSTORE_PATH = "CLIENT_TRUSTSTORE_PATH";
    private static final String ENV_TRUSTSTORE_PASSWORD = "CLIENT_TRUSTSTORE_PASSWORD";
    private static final String DEFAULT_CONFIG_FILE = "client.properties";

    private String host;
    private int port = -1;
    private String truststorePath;
    private String truststorePassword;

    private SSLSocket sslSocket;

    private BufferedReader in;
    private PrintWriter out;

    private volatile boolean running = false;
    private Thread listeningThread;

    private ResponseHandler responseHandler;

    /**
     * Constructs a new {@code Client} with default config file.
     * <p>
     * Configuration will be loaded from:
     * <ol>
     *   <li>Environment variables</li>
     *   <li>Default config file ({@code client.properties})</li>
     * </ol>
     *
     * @throws IllegalArgumentException if required configuration is missing or invalid
     *
     * @see #JTPClient(String)
     */
    public JTPClient(){ this(null); }

    /**
     * Constructs a new {@code Client} with configuration from the specified file.
     * <p>
     * Configuration will be loaded from:
     * <ol>
     *   <li>Environment variables</li>
     *   <li>The specified config file</li>
     * </ol>
     *
     * @param configFile the path to the configuration file (maybe null)
     * @throws IllegalArgumentException if required configuration is missing or invalid
     */
    public JTPClient(String configFile) {
        loadConfig(configFile);

        start();
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
        return host == null || port == -1 || truststorePath == null || truststorePassword == null;
    }

    /**
     * Loads configuration from environment variables.
     */
    private void loadFromEnvVars() {
        String envHost = System.getenv(ENV_HOST);
        if (envHost != null && !envHost.trim().isEmpty())
            this.host = envHost.trim();

        String envPort = System.getenv(ENV_PORT);
        if (envPort != null) {
            try {
                this.port = Integer.parseInt(envPort);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid PORT in env vars", e);
            }
        }

        String envTruststorePath = System.getenv(ENV_TRUSTSTORE_PATH);
        if (envTruststorePath != null)
            this.truststorePath = envTruststorePath;

        String envTruststorePassword = System.getenv(ENV_TRUSTSTORE_PASSWORD);
        if (envTruststorePassword != null)
            this.truststorePassword = envTruststorePassword;
    }

    /**
     * Loads configuration from properties file.
     *
     * @param configFile the path to the properties file
     * @throws IllegalArgumentException if the file is invalid
     */
    private void loadFromPropertiesFile(String configFile) {
        if (this.host == null)
            this.host = PropertiesUtil.getString(configFile, "client.host");

        if (this.port == -1)
            this.port = PropertiesUtil.getInteger(configFile, "client.port");

        if (this.truststorePath == null)
            this.truststorePath = PropertiesUtil.getString(configFile, "client.path");

        if (this.truststorePassword == null)
            this.truststorePassword = PropertiesUtil.getString(configFile, "client.password");

    }

    /**
     * Validates the loaded configuration.
     *
     * @throws IllegalArgumentException if any configuration is invalid
     */
    private void validateConfig() {
        if (this.host == null || this.host.trim().isEmpty())
            throw new IllegalArgumentException("Host must be set via " + ENV_HOST + " or properties file");

        if (this.port < 0 || this.port > 65536)
            throw new IllegalArgumentException("PORT must be between 0 and 65536 and set via " + ENV_PORT + " or properties file");

        if (this.truststorePath == null || this.truststorePath.trim().isEmpty())
            logger.warn("Truststore path not set");

        if (this.truststorePath != null && this.truststorePassword == null)
            throw new IllegalArgumentException("Truststore password must be set via " + ENV_TRUSTSTORE_PASSWORD + " or properties file");
    }

    /**
     * Initializes the SSL connection to the server and sets up input/output streams.
     * <p>
     * This method creates an {@link SSLContext}, connects to the server at the specified
     * {@code HOST} and {@code PORT}, and opens buffered streams for communication.
     * It also starts a new thread to handle incoming responses asynchronously.
     *
     * @throws RuntimeException if:
     * <ul>
     *     <li>an exception occurs during initialization</li>
     *     <li>inability to create the SSL context or connect to the server</li>
     * </ul>
     */
    private void start() {
        try{
            SSLContext sslContext = createSSLContext();
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            sslSocket = (SSLSocket) sslSocketFactory.createSocket(host, port);

            sslSocket.setEnabledCipherSuites(sslSocket.getSupportedCipherSuites());

            in = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
            out = new PrintWriter(sslSocket.getOutputStream(), true);

            responseHandler = new ResponseHandler(out);

            logger.info("Connected to server at {} : {}\n", host, port);

            running = true;
        } catch (Exception e) {
            logger.error("Failed to start client: {}", e.getMessage());
            close();
            throw new RuntimeException("Client initialization failed", e);
        }

        listeningThread = new Thread(this::handleResponses);
        listeningThread.start();
    }

    /**
     * Creates and initializes an SSLContext for secure communication.
     * If no truststore is provided, creates a default SSLContext that performs basic certificate validation.
     *
     * @return Initialized SSLContext ready for use in secure communications
     * @throws RuntimeException if SSL context cannot be created
     */
    private SSLContext createSSLContext() {
        try {
            if (truststorePath == null || truststorePath.trim().isEmpty()) {
                logger.warn("No truststore configured - using default SSLContext with standard certificate validation");
                return SSLContext.getDefault();
            }

            File truststoreFile = new File(truststorePath);
            if (!truststoreFile.exists())
                throw new FileNotFoundException("Truststore file not found at: " + truststorePath);

            try (FileInputStream fis = new FileInputStream(truststorePath)) {
                // Load the truststore
                KeyStore trustStore = KeyStore.getInstance("JKS");
                trustStore.load(fis, truststorePassword.toCharArray());

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
     * Listens for and processes pending responses from the server.
     */
    private void handleResponses() {
        String line;

        try {
            while (running && (line = in.readLine()) != null) {
                JsonAdapter response = new GsonAdapter(line);

                responseHandler.processResponse(response);
//                TODO: handle invalid json
            }
        } catch (IOException e) {
            if (running)
                logger.error("Error while listening for responses: {}", e.getMessage());
            else
                logger.info("Listening thread closed.");
        } catch (Exception e) {
            logger.error("Unexpected error while handling response: {}", e.getMessage());
        }
    }

    /**
     * Sends a command to the server and stores the associated request for later response handling.
     *
     * @param request the {@link Request} object containing the command to be sent
     * @throws IllegalArgumentException if the request is {@code null} or request id is {@code null}
     */
    public void sendCommand(Request request) {
        if (request == null)
            throw new IllegalArgumentException("Request cannot be null");

        String id = request.getId();
        if (id == null || id.trim().isEmpty())
            throw new IllegalArgumentException("Request id cannot be null or empty");

        request.addParam("token", responseHandler.getSessionToken());

        responseHandler.addPendingRequest(id, request);

        out.println(request.toJSON());
        out.flush();
    }

    /**
     * Closes the client connection and associated resources.
     */
    public void close() {
        logger.info("Closing connection...");

        running = false;

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

        if (listeningThread != null && listeningThread.isAlive()) {
            try {
                listeningThread.join(1000); // Optional: wait for the thread to clean up
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Interrupted while waiting for listening thread to stop.");
            }
        }

        logger.info("Client resources closed successfully.");
    }

    public static void main(String[] args) {
        JTPClient client = new JTPClient("Client/myConfig.properties");

        client.sendCommand(new HelpCommand());

        Async.waitFor(5000);

        client.close();
    }
}
