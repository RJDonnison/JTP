package org.reujdon.jtp.client;

import org.json.JSONObject;
import org.reujdon.jtp.client.commands.TestCommand;
import org.reujdon.jtp.shared.MessageType;
import org.reujdon.jtp.shared.Parse;
import org.reujdon.jtp.shared.Request;
import reujdon.async.Async;
import reujdon.async.Task;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
    private static final String TRUSTSTORE_PATH = "Client/client_truststore.jks";
    private static final String TRUSTSTORE_PASSWORD = "clientpassword";

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
     * @see #Client(String, int)
     */
    public Client(){
        this("localhost", 8080);
    }

    /**
     * Constructs a new {@code Client} with default port.
     * <p>
     * Equivalent to calling {@code new Client(host, 8080)}.
     *
     * @param host the hostname or IP address of the server
     * @throws IllegalArgumentException if the host is {@code null} or empty
     *
     * @see #Client(String, int)
     */
    public Client(String host){ this(host, 8080); }

    /**
     * Constructs a new {@code Client} with default host.
     * <p>
     * Equivalent to calling {@code new Client("localhost", port)}.
     * @param port the port number on which the server is listening (0–65536)
     * @throws IllegalArgumentException if the port is out of range
     *
     * @see #Client(String, int)
     */
    public Client(int port){ this("localhost", port); }

    /**
     * Constructs a new {@code Client} and attempts to connect to the specified host and port.
     *
     * @param host the hostname or IP address of the server
     * @param port the port number on which the server is listening (0–65536)
     * @throws IllegalArgumentException if the port is out of range or the host is {@code null} or empty
     */
    public Client(String host, int port) {
        if(port < 0 || port > 65536)
            throw new IllegalArgumentException("Port must be between 0 and 65536");

        this.PORT = port;

        if(host == null || host.trim().isEmpty())
            throw new IllegalArgumentException("Host cannot be null or empty");

        this.HOST = host;

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

            System.out.println("Connected to server at " + HOST + ":" + PORT + "\n");

            running = true;
        } catch (Exception e) {
            System.err.println("Failed to start client: " + e.getMessage());
            close();
            throw new RuntimeException("Client initialization failed", e);
        }

        listeningThread = new Thread(this::handlePendingResponses);
        listeningThread.start();
    }

    /**
     * Creates and initializes an SSLContext for secure communication using TLS protocol.
     * The SSLContext is configured with trust managers loaded from a JKS truststore.
     *
     * @return Initialized SSLContext ready for use in secure communications
     */
    private static SSLContext createSSLContext() {
        // Validate inputs
        //noinspection ConstantValue
        if (TRUSTSTORE_PATH.trim().isEmpty())
            throw new IllegalArgumentException("Truststore path must not be null or empty");

        FileInputStream fis = null;

        try{
            // Load the truststore
            KeyStore trustStore = KeyStore.getInstance("JKS");
            fis = new FileInputStream(TRUSTSTORE_PATH);
            trustStore.load(fis, TRUSTSTORE_PASSWORD.toCharArray());

            // Set up trust manager factory
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(trustStore);

            // Initialize SSL context
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

            return sslContext;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize the SSL context", e);
        }finally {
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
     * Listens for and processes pending responses from the server.
     */
    private void handlePendingResponses() {
        String line;

        try {
            while (running && (line = in.readLine()) != null) {
                JSONObject response = new JSONObject(line);
                String id = response.optString("id", null);

                if (id != null && pendingResponses.containsKey(id)) {
                    Request request = pendingResponses.get(id);
                    if (request == null)
                        throw new RuntimeException("Request missing: " + id);

                    Task.of(() -> handleResponse(response, request)).run();

                    pendingResponses.remove(id);
                } else
                    System.err.println("Unmatched response: " + response);
            }
        } catch (IOException e) {
            if (running)
                System.err.println("Error while listening for responses: " + e.getMessage());
            else
                System.out.println("Listening thread closed.");
        } catch (Exception e) {
            System.err.println("Unexpected error while handling response: " + e.getMessage());
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
                System.err.println("Unsupported message type: " + response);
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
        System.out.println("\nClosing connection...");

        running = false;

        try {
            if (out != null) {
                out.close();
                System.out.println("Output stream closed.");
            }
            if (in != null) {
                in.close();
                System.out.println("Input stream closed.");
            }
            if (sslSocket != null && !sslSocket.isClosed()) {
                sslSocket.close();
                System.out.println("SSL socket closed.");
            }
        } catch (IOException e) {
            System.err.println("Error while closing the client SSL socket or streams: " + e.getMessage());
        }

        if (listeningThread != null && listeningThread.isAlive()) {
            try {
                listeningThread.join(1000); // Optional: wait for the thread to clean up
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Interrupted while waiting for listening thread to stop.");
            }
        }

        System.out.println("\nClient resources closed successfully.");
    }

    public static void main(String[] args) {
        Client client = new Client();

        client.sendCommand(new TestCommand());

        Async.waitFor(5, TimeUnit.SECONDS); //TODO: improve

        client.close();
    }
}
