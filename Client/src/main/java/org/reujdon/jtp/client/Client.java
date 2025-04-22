package org.reujdon.jtp.client;


import org.json.JSONObject;
import org.reujdon.jtp.client.commands.TestCommand;
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

public class Client {
    private final String HOST;
    private final int PORT;
    private static final String TRUSTSTORE_PATH = "Client/client_truststore.jks";
    private static final String TRUSTSTORE_PASSWORD = "clientpassword";

    private SSLSocket sslSocket;

    private BufferedReader in;
    private PrintWriter out;

    private final HashMap<String, Boolean> pendingResponses = new HashMap<>();

    public Client(){
        this.PORT = 8080;
        this.HOST = "localhost";

        startClient();
    }

    public Client(String host, int port) {
        if(port < 0 || port > 65536)
            throw new IllegalArgumentException("Port must be between 0 and 65536");

        this.PORT = port;

        if(host == null || host.isEmpty())
            throw new IllegalArgumentException("Host cannot be null or empty");

        this.HOST = host;

        startClient();
    }

    private void startClient() {
        try{
            SSLContext sslContext = createSSLContext();
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            sslSocket = (SSLSocket) sslSocketFactory.createSocket(HOST, PORT);

            sslSocket.setEnabledCipherSuites(sslSocket.getSupportedCipherSuites());

            in = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
            out = new PrintWriter(sslSocket.getOutputStream(), true);

            System.out.println("Connected to server at " + HOST + ":" + PORT);
        } catch (Exception e) {
            System.err.println("Failed to start client: " + e.getMessage());
            closeClient();
            throw new RuntimeException("Client initialization failed", e);
        }

        new Thread(this::handlePendingResponses).start();
    }

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

//    TODO: handle files
    private void sendRequest(Request req) {
        if (req == null)
            throw new IllegalArgumentException("Request cannot be null");

        String id = req.getId();
        JSONObject json = req.toJSON();

        Task<Boolean> waitTask = Task.of(() -> {
            long startTime = System.currentTimeMillis();
            while (!Thread.currentThread().isInterrupted() &&
                    !pendingResponses.get(id) &&
                    System.currentTimeMillis() - startTime < req.getTimeout()) {
                Async.waitFor(10);
            }

            if (!pendingResponses.get(id)) {
                req.onError();
                return false;
            }

            req.onSuccess();
            pendingResponses.remove(id);
            return true;
        });

        pendingResponses.put(id, false);

        System.out.println(json.toString(2));
        out.println(json);
        out.flush();

        waitTask.run();
    }

//    TODO: pass data to request handling.
    private void handlePendingResponses() {
        String line;

        try {
            while ((line = in.readLine()) != null) {
                JSONObject response = new JSONObject(line);
                String id = response.optString("id", null);

                if (id != null && pendingResponses.containsKey(id)) {
                    pendingResponses.put(id, true);
                } else {
                    System.err.println("Unmatched response: " + response);
                }
            }
        } catch (IOException e) {
            System.err.println("Error while listening for responses: " + e.getMessage());
        }
    }

//    TODO: handle closing of listening thread
    private void closeClient() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (sslSocket != null && !sslSocket.isClosed()) sslSocket.close();

            System.out.println("\nClient closed successfully.");
        } catch (IOException e) {
            System.err.println("Error while closing the client SSL socket: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Client client = new Client();

        client.sendRequest(new TestCommand());

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        client.closeClient();
    }
}
