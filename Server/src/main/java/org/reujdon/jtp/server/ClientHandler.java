package org.reujdon.jtp.server;

import org.json.JSONObject;
import org.reujdon.jtp.shared.Error;
import org.reujdon.jtp.shared.Parse;
import org.reujdon.jtp.shared.Response;

import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Map;

class ClientHandler implements Runnable {
    private final SSLSocket clientSocket;
    private final Server server;

    private final String id;

    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler(SSLSocket socket, Server server) {
        this.clientSocket = socket;
        this.server = server;
        this.id = socket.getRemoteSocketAddress().toString();
    }

    @Override
    public void run() {
        try{
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            String message;

            while ((message = in.readLine()) != null) {
                JSONObject json = new JSONObject(message);
                String commandID = json.getString("id");

                if (commandID == null || commandID.trim().isEmpty())
                    throw new RuntimeException("ID is null or empty");

                Map<String, Object> params = Parse.Params(json);

                if (!params.containsKey("command")) {
                    System.err.println("No command.");
                    continue;
                }

                String command = params.get("command").toString();

                System.out.println("Client: " + id + "Sent command: " + command);

//                TODO: Async command handling
                CommandHandler handler = CommandRegistry.getHandler(command);
                if (handler == null) {
                    sendError(commandID, "Unknown command");
                    continue;
                }

                // TODO: implement errors in handler
                sendResponse(commandID, handler.handle(params));
            }
        }
        catch (IOException e){
            System.err.println("IOException during client communication: " + e.getMessage());
            e.printStackTrace();
        } finally {
            close();
        }
    }

    private void sendResponse(String commandID, JSONObject params) {
        Response response = new Response(commandID, params);

        out.println(response.toJSON());
    }

    private void sendError(String id, String message) {
        System.err.println("Error with id: " + id + "\nMessage: " + message);
        out.println(new Error(id, message).toJSON());
    }

    private void sendAuth(){

    }

    public void close() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();

            if (clientSocket != null && !clientSocket.isClosed())
                clientSocket.close();

            if (server != null) server.removeClient(id);
        } catch (IOException e) {
            System.err.println("Error closing client connection: " + e.getMessage());
        }
    }
}
