package org.reujdon.jtp.client;


import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.KeyStore;

public class Client {
    private final String HOST;
    private final int PORT;
    private static final String TRUSTSTORE_PATH = "Client/client_truststore.jks";
    private static final String TRUSTSTORE_PASSWORD = "clientpassword";

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
            SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(HOST, PORT);

            BufferedReader in = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
            PrintWriter out = new PrintWriter(sslSocket.getOutputStream(), true);
            BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Connected to server. Type 'exit' to quit.");

            // Communication loop
            String userInput;
            while ((userInput = consoleIn.readLine()) != null) {
                out.println(userInput);

                if ("exit".equalsIgnoreCase(userInput)) {
                    break;
                }

                String serverResponse = in.readLine();
                System.out.println("Server says: " + serverResponse);
            }



            consoleIn.close();
            in.close();
            out.close();
            sslSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public static void main(String[] args) {
        new Client();
    }
}
