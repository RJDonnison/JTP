package org.reujdon.jtp.client;

import org.json.JSONException;
import org.json.JSONObject;
import org.reujdon.jtp.client.commands.AuthCommand;
import org.reujdon.jtp.shared.Parse;
import org.reujdon.jtp.shared.PropertiesUtil;
import org.reujdon.jtp.shared.messaging.MessageType;
import org.reujdon.jtp.shared.messaging.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reujdon.async.Async;
import reujdon.async.Task;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A secure client that connects to a server over SSL/TLS.
 *
 * <p>The client provides the following features:</p>
 * <ul>
 *   <li>Secure communication using SSL/TLS protocol</li>
 *   <li>Asynchronous handling of server responses</li>
 *   <li>Timeout management for requests</li>
 *   <li>Ability to send custom commands to the server using the {@link #sendCommand(Request)} method</li>
 * </ul>
 *
 * @see SSLContext
 */
public class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    private final String TRUSTSTORE_PATH;
    private final String TRUSTSTORE_PASSWORD;

    private final String HOST;
    private final int PORT;

    private SSLSocket sslSocket;

    private BufferedReader in;
    private PrintWriter out;

    private volatile boolean running = false;
    private Thread listeningThread;

    private final HashMap<String, Request> pendingResponses = new HashMap<>();

    /**
     * Constructs a new {@code Client} with default connection parameters.
     * <p>
     * Equivalent to calling {@code new Client("localhost", 8080)}.
     *
     * @param configFile the path to the configFile
     *
     * @see #Client(String, int, String)
     */
    public Client(String configFile){
        this("localhost", 8080, configFile);
    }

    /**
     * Constructs a new {@code Client} with default port.
     * <p>
     * Equivalent to calling {@code new Client(host, 8080)}.
     *
     * @param host the hostname or IP address of the server
     * @param configFile the path to the configFile
     * @throws IllegalArgumentException if the host is {@code null} or empty
     *
     * @see #Client(String, int, String)
     */
    public Client(String host, String configFile){ this(host, 8080, configFile); }

    /**
     * Constructs a new {@code Client} with default host.
     * <p>
     * Equivalent to calling {@code new Client("localhost", port)}.
     * @param port the port number on which the server is listening (0–65536)
     * @param configFile the path to the configFile
     * @throws IllegalArgumentException if the port is out of range
     *
     * @see #Client(String, int, String)
     */
    public Client(int port, String configFile){ this("localhost", port, configFile); }

    /**
     * Constructs a new {@code Client} and attempts to connect to the specified host and port.
     *
     * @param host the hostname or IP address of the server
     * @param port the port number on which the server is listening (0–65536)
     * @param configFile the path to the configFile
     * @throws IllegalArgumentException if the port is out of range or the host is {@code null} or empty
     */
    public Client(String host, int port, String configFile) {
        if(port < 0 || port > 65536)
            throw new IllegalArgumentException("Port must be between 0 and 65536");

        this.PORT = port;

        if(host == null || host.trim().isEmpty())
            throw new IllegalArgumentException("Host cannot be null or empty");

        this.HOST = host;

        if (configFile == null || !configFile.trim().endsWith(".properties"))
            throw new IllegalArgumentException("Config file cannot be null or empty and must end with '.properties'");

        TRUSTSTORE_PATH = PropertiesUtil.getProperty(configFile, "client.path");
        TRUSTSTORE_PASSWORD = PropertiesUtil.getProperty(configFile, "client.password");

        start();
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
            sslSocket = (SSLSocket) sslSocketFactory.createSocket(HOST, PORT);

            sslSocket.setEnabledCipherSuites(sslSocket.getSupportedCipherSuites());

            in = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
            out = new PrintWriter(sslSocket.getOutputStream(), true);

            logger.info("Connected to server at {} : {}\n", HOST, PORT);

            running = true;
        } catch (Exception e) {
            logger.error("Failed to start client: {}", e.getMessage());
            close();
            throw new RuntimeException("Client initialization failed", e);
        }

        listeningThread = new Thread(this::handlePendingResponses);
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
            if (TRUSTSTORE_PATH == null || TRUSTSTORE_PATH.trim().isEmpty()) {
                logger.warn("No truststore configured - using default SSLContext with standard certificate validation");
                return SSLContext.getDefault();
            }

            File truststoreFile = new File(TRUSTSTORE_PATH);
            if (!truststoreFile.exists())
                throw new FileNotFoundException("Truststore file not found at: " + TRUSTSTORE_PATH);

            try (FileInputStream fis = new FileInputStream(TRUSTSTORE_PATH)) {
                // Load the truststore
                KeyStore trustStore = KeyStore.getInstance("JKS");
                trustStore.load(fis, TRUSTSTORE_PASSWORD.toCharArray());

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
    private void handlePendingResponses() {
        String line;

        try {
            while (running && (line = in.readLine()) != null) {
                JSONObject response = new JSONObject(line);
                String id = response.optString("id", null);

                if (Objects.equals(id, "*"))
                {
                    Map<String, Object> params = Parse.Params(response);
                    logger.error("Server error with unknown id: {}", params.get("message").toString());
                }

                if (id != null && pendingResponses.containsKey(id)) {
                    Request request = pendingResponses.get(id);
                    if (request == null)
                        throw new RuntimeException("Request missing: " + id);

                    Task.of(() -> handleResponse(response, request)).run();

                    pendingResponses.remove(id);
                } else
                    logger.warn("Unmatched response: {}", response);
            }
        } catch (IOException e) {
            if (running)
                logger.error("Error while listening for responses: {}", e.getMessage());
            else
                logger.info("Listening thread closed.");
        } catch (JSONException e) {
            logger.error("Error parsing JSON: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error while handling response: {}", e.getMessage());
        }
    }

    /**
     * Handles the response from the server and passes to suitable {@link Request} function.
     *
     * @param response the {@link JSONObject} containing the server's response data
     * @param request  the {@link Request} associated with the response
     * @throws IllegalArgumentException if {@code response} or {@code request} is null
     */
    private void handleResponse(JSONObject response, Request request){
        if (response == null)
            throw new IllegalArgumentException("Response cannot be null");

        if (request == null)
            throw new IllegalArgumentException("Request cannot be null");

        MessageType type = response.getEnum(MessageType.class, "type");

        Map<String, Object> params = Parse.Params(response);

        switch (type) {
            case ERROR ->
                request.onError(params.get("message").toString());

            case RESPONSE ->
                request.onSuccess(params);

            case null, default ->
                logger.warn("Unsupported message type: {}", response);
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

        JSONObject json = request.toJSON();

        pendingResponses.put(id, request);

        out.println(json);
        out.flush();

        Task<Void> timeout = Task.of(() -> handleTimeout(request, id));
        timeout.run();
    }

    /**
     * Handles the timeout for a request if a response is not received within the specified timeout period.
     *
     * @param request the {@link Request} object that timed out
     * @param id the unique identifier of the request
     * @throws IllegalArgumentException if request or id is null or empty
     */
    private void handleTimeout(Request request, String id) {
        if (request == null)
            throw new IllegalArgumentException("Request cannot be null");

        if (id == null || id.trim().isEmpty())
            throw new IllegalArgumentException("Id cannot be null or empty");

        Async.waitFor(request.getTimeout());

        if (pendingResponses.containsKey(id))
            request.onTimeout();

        pendingResponses.remove(id);
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
        Client client = new Client("Client/myConfig.properties");

        client.sendCommand(new AuthCommand("test"));

        Async.waitFor(5000);

        client.close();
    }
}
