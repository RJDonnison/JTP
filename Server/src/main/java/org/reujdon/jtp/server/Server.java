package org.reujdon.jtp.server;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.KeyStore;

public class Server {
    private static final String KEYSTORE_PATH = "Server/server_keystore.jks";
    private static final String KEYSTORE_PASSWORD = "serverpassword";

    private final int PORT;

    public Server() {
        this.PORT = 8080;
        startServer();
    }

    public Server(int port){
        if(port < 0 || port > 65536)
            throw new IllegalArgumentException("Port must be between 0 and 65536");

        this.PORT = port;

        startServer();
    }

    private void startServer() {
        try {
            SSLContext sslContext = createSSLContext();

            SSLServerSocketFactory ssf = sslContext.getServerSocketFactory();

            SSLServerSocket sslServerSocket = (SSLServerSocket) ssf.createServerSocket(this.PORT);

            System.out.println("Server started. Waiting for client to connect...");

            SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();

            BufferedReader in = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
            PrintWriter out = new PrintWriter(sslSocket.getOutputStream(), true);

            // Communication loop
            String clientMessage;
            while ((clientMessage = in.readLine()) != null) {
                System.out.println("Client says: " + clientMessage);

                if ("exit".equalsIgnoreCase(clientMessage)) {
                    break;
                }

                // Send response to client
                out.println(clientMessage);
            }

            in.close();
            out.close();
            sslSocket.close();
            sslServerSocket.close();

        } catch (Exception e) {
            e.printStackTrace();
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

    public static void main(String[] args) {
        Server server = new Server();
    }
}
