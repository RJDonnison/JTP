package org.reujdon.jtp.client;


import org.json.JSONObject;
import org.reujdon.jtp.client.commands.TestCommand;
import org.reujdon.jtp.shared.MessageType;
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

public class Client {
    private static final String TRUSTSTORE_PATH = "Client/client_truststore.jks";
    private static final String TRUSTSTORE_PASSWORD = "clientpassword";

    private final String HOST;
    private final int PORT;

    private SSLSocket sslSocket;

    private BufferedReader in;
    private PrintWriter out;

    private volatile boolean running = true;
    private Thread listeningThread;

    private final HashMap<String, Request> pendingResponses = new HashMap<>();

    public Client(){
        this.PORT = 8080;
        this.HOST = "localhost";

        start();
    }

    public Client(String host, int port) {
        if(port < 0 || port > 65536)
            throw new IllegalArgumentException("Port must be between 0 and 65536");

        this.PORT = port;

        if(host == null || host.isEmpty())
            throw new IllegalArgumentException("Host cannot be null or empty");

        this.HOST = host;

        start();
    }

    private void start() {
        try{
            SSLContext sslContext = createSSLContext();
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            sslSocket = (SSLSocket) sslSocketFactory.createSocket(HOST, PORT);

            sslSocket.setEnabledCipherSuites(sslSocket.getSupportedCipherSuites());

            in = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
            out = new PrintWriter(sslSocket.getOutputStream(), true);

            System.out.println("\nConnected to server at " + HOST + ":" + PORT + "\n");
        } catch (Exception e) {
            System.err.println("\nFailed to start client: " + e.getMessage());
            close();
            throw new RuntimeException("Client initialization failed", e);
        }

        listeningThread = new Thread(this::handlePendingResponses);
        listeningThread.start();
    }

//    TODO: update
    private static SSLContext createSSLContext() throws Exception {
        // Load the truststore
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(new FileInputStream(TRUSTSTORE_PATH), TRUSTSTORE_PASSWORD.toCharArray());

        // Set up trust manager factory
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
        trustManagerFactory.init(trustStore);

        // Initialize SSL context
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

        return sslContext;
    }

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
                } else {
                    System.err.println("Unmatched response: " + response);
                }
            }
        } catch (IOException e) {
            if (running)
                System.err.println("Error while listening for responses: " + e.getMessage());
            else
                System.out.println("\nListening thread closed.");
        }
    }

    private void handleResponse(JSONObject response, Request request){
        MessageType type = response.getEnum(MessageType.class, "type");

        //Read params
        Map<String, Object> params = new HashMap<>();
        if (response.has("params")) {
            JSONObject paramJson = response.getJSONObject("params");
            for (String key : paramJson.keySet())
                params.put(key, paramJson.get(key));
        }

        if (type == MessageType.ERROR) {
            request.onError(params.get("message").toString());
            return;
        }

        request.onSuccess(params);
    }

    public void sendCommand(Request req) {
        if (req == null)
            throw new IllegalArgumentException("Request cannot be null");

        String id = req.getId();
        JSONObject json = req.toJSON();

        pendingResponses.put(id, req);

        out.println(json);
        out.flush();

        Task<Void> timeout = Task.of(() -> handleTimeout(req, id));
        timeout.run();
    }

    private void handleTimeout(Request req, String id) {
        Async.waitFor(req.getTimeout());

        if (pendingResponses.containsKey(id))
            req.onTimeout();

        pendingResponses.remove(id);
    }

    public void close() {
        running = false;

        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (sslSocket != null && !sslSocket.isClosed()) sslSocket.close();

            System.out.println("Client closed successfully.");
        } catch (IOException e) {
            System.err.println("Error while closing the client SSL socket: " + e.getMessage());
        }

        if (listeningThread != null && listeningThread.isAlive()) {
            try {
                listeningThread.join(1000); // Optional: wait for the thread to clean up
            } catch (InterruptedException e) {
                System.err.println("Interrupted while waiting for listening thread to stop.");
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();

        client.sendCommand(new TestCommand());

        Async.waitFor(5, TimeUnit.SECONDS); //TODO: improve

        client.close();
    }
}
