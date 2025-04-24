package org.reujdon.jtp.server;

import javax.net.ssl.*;
import java.io.*;
import java.security.KeyStore;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final String KEYSTORE_PATH = "Server/server_keystore.jks";
    private static final String KEYSTORE_PASSWORD = "serverpassword";

    private final int PORT;

    private SSLServerSocket serverSocket;
    private final ExecutorService clientThreadPool;
    private final ConcurrentHashMap<String, ClientHandler> activeClients = new ConcurrentHashMap<>();

    private boolean running;

    public Server() {
        this(8080);
    }

    public Server(int port) {
        if(port < 0 || port > 65536)
            throw new IllegalArgumentException("Port must be between 0 and 65536");

        this.PORT = port;

        this.clientThreadPool = Executors.newCachedThreadPool();
    }

    public void start() {
        running = true;

        try {
            SSLContext sslContext = createSSLContext();
            SSLServerSocketFactory ssf = sslContext.getServerSocketFactory();
            serverSocket = (SSLServerSocket) ssf.createServerSocket(this.PORT);

            // Add shutdown hook to close server socket on Ctrl+C
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nShutting down server...");
                close();
            }));

            System.out.println("Server started on port " + this.PORT);

            handleClients();
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

    private void handleClients() throws IOException {
        System.out.println("Waiting for clients to connect...\n");

        while (running) {
            try {
                SSLSocket clientSocket = (SSLSocket) serverSocket.accept();

                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                String clientId = clientSocket.getRemoteSocketAddress().toString();

                activeClients.put(clientId, clientHandler);
                clientThreadPool.execute(clientHandler);

                System.out.println("New client connected, ID: " + clientId + ". Active clients: " + activeClients.size());
            } catch (SSLException e) {
                System.err.println("SSL handshake failed with client: " + e.getMessage());
            } catch (IOException e) {
                throw new IOException(e);
            }
        }
    }

    void removeClient(String clientId) {
        activeClients.remove(clientId);
        System.out.println("Client " + clientId + " disconnected. Active clients: " + activeClients.size());
    }

    public void close() {
        running = false;

        try {
            for (ClientHandler handler : activeClients.values())
                handler.close();

            activeClients.clear();

            if (clientThreadPool != null)
                clientThreadPool.shutdownNow();

            if (serverSocket != null && !serverSocket.isClosed())
                serverSocket.close();

            System.out.println("Server closed successfully. All clients disconnected.");
        } catch (Exception e) {
            System.err.println("Error while closing the Server SSL socket: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Server server = new Server();

        server.start();
    }
}
