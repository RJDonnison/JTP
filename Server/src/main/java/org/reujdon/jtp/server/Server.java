package org.reujdon.jtp.server;

import org.json.JSONObject;
import org.reujdon.jtp.shared.Error;
import org.reujdon.jtp.shared.Response;

import javax.net.ssl.*;
import java.io.*;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

public class Server {
    private static final String KEYSTORE_PATH = "Server/server_keystore.jks";
    private static final String KEYSTORE_PASSWORD = "serverpassword";

    private final int PORT;

    private SSLSocket sslSocket;

    private BufferedReader in;
    private PrintWriter out;

    private boolean running;

    public Server() {
        this(8080);
    }

    public Server(int port){
        if(port < 0 || port > 65536)
            throw new IllegalArgumentException("Port must be between 0 and 65536");

        this.PORT = port;

        start();
    }

    public void start() {
        running = true;

        try {
            SSLContext sslContext = createSSLContext();
            SSLServerSocketFactory ssf = sslContext.getServerSocketFactory();
            SSLServerSocket sslServerSocket = (SSLServerSocket) ssf.createServerSocket(this.PORT);

            // Add shutdown hook to close server socket on Ctrl+C
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nShutting down server...");
                close();
            }));

            System.out.println("Server started on port " + this.PORT);

            while (running) {
                System.out.println("\nWaiting for a client to connect...");
                sslSocket = (SSLSocket) sslServerSocket.accept();

                in = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
                out = new PrintWriter(sslSocket.getOutputStream(), true);

                handleClient();
            }

        } catch (Exception e) {
            System.err.println("\nFailed to start Server: " + e.getMessage());
            close();
            throw new RuntimeException("Server initialization failed", e);
        }
    }

    //    TODO: update
    private static SSLContext createSSLContext() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(new FileInputStream(KEYSTORE_PATH), KEYSTORE_PASSWORD.toCharArray());

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
        keyManagerFactory.init(keyStore, KEYSTORE_PASSWORD.toCharArray());

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
        trustManagerFactory.init(keyStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

        return sslContext;
    }

    public void close() {
        running = false;

        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (sslSocket != null && !sslSocket.isClosed()) sslSocket.close();

            System.out.println("Server closed successfully.");
        } catch (IOException e) {
            System.err.println("Error while closing the Server SSL socket: " + e.getMessage());
        }
    }

    private void handleClient() {
        try{
            String message;

            while ((message = in.readLine()) != null) {
                JSONObject json = new JSONObject(message);
                String id = json.getString("id");

                if (id == null || id.trim().isEmpty())
                    throw new RuntimeException("ID is null or empty");

                //Read params
                Map<String, Object> params = new HashMap<>();
                if (json.has("params")) {
                    JSONObject paramJson = json.getJSONObject("params");
                    for (String key : paramJson.keySet())
                        params.put(key, paramJson.get(key));
                }

                if (!params.containsKey("command")) {
                    System.err.println("No command.");
                    continue;
                }

                String command = params.get("command").toString();

                CommandHandler handler = CommandRegistry.getHandler(command);
                if (handler != null) {
                    sendResponse(id);
                } else {
                    sendError(id, "Unknown command");
                }
            }
        }
        catch (IOException e){
            System.err.println("IOException during client communication: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendResponse(String id) {
        out.println(new Response(id).toJSON());
    }

    private void sendError(String id, String message) {
        System.err.println("Error with id: " + id + "\nMessage: " + message);
        out.println(new Error(id, message).toJSON());
    }

    public static void main(String[] args) {
        Server server = new Server();

        server.start();
    }
}
