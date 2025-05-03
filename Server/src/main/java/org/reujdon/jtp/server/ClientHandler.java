package org.reujdon.jtp.server;

import org.json.JSONException;
import org.json.JSONObject;
import org.reujdon.jtp.shared.Error;
import org.reujdon.jtp.shared.Parse;
import org.reujdon.jtp.shared.Response;
import reujdon.async.Task;

import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Handles communication with a connected client over a secure SSL socket.
 * <p>
 * This class is responsible for:
 * <ul>
 *     <li>Reading and parsing incoming JSON messages from the client</li>
 *     <li>Dispatching commands to the appropriate {@link CommandHandler}</li>
 *     <li>Sending back responses or errors based on execution results</li>
 *     <li>Cleaning up resources when the client disconnects</li>
 * </ul>
 *
 * Instances of this class are typically managed by the {@link Server} and
 * executed on separate threads to allow concurrent client handling.
 *
 * @see Runnable
 */
class ClientHandler implements Runnable {
    private final SSLSocket clientSocket;
    private final Server server;

    private final String clientId;

    private BufferedReader in;
    private PrintWriter out;

    /**
     * Constructs a new {@code ClientHandler} with the specified SSL socket and server.
     *
     * @param socket the {@link SSLSocket} representing the client's connection
     * @param server the {@link Server} instance that this client is connecting to
     * @throws IllegalArgumentException if:
     * <ul>
     *     <li>the socket is {@code null} or closed</li>
     *     <li>the server is {@code null} or not running</li>
     * </ul>
     */
    public ClientHandler(SSLSocket socket, Server server) {
        if (socket == null || socket.isClosed())
            throw new IllegalArgumentException("Socket is closed or null");

        if (server == null || !server.isRunning())
            throw new IllegalArgumentException("Server is closed or null");

        this.clientSocket = socket;
        this.server = server;
        this.clientId = socket.getRemoteSocketAddress().toString();
    }

    /**
     * Starts handling communication with the connected client.
     */
    @Override
    public void run() {
        try{
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            String message;

            while ((message = in.readLine()) != null) {
                JSONObject json = new JSONObject(message);

                Task.of(() -> handleMessage(json)).run();
            }
        }
        catch (IOException e){
            System.err.println("IOException during client communication: " + e.getMessage());
        } catch (JSONException e){
            sendError("*", "Invalid JSON response: " + e.getMessage()); //TODO: fix
        }
        finally {
            close();
        }
    }

    /**
     * Handles an incoming message from the client.
     *
     * @param json the {@link JSONObject} containing the message data from the client
     * @throws NullPointerException if the {@code json} is {@code null}
     * @throws IllegalStateException if the message ID is missing or empty
     */
    private void handleMessage(JSONObject json) {
        // Validate input
        if (json == null)
            throw new NullPointerException("Message JSON cannot be null");

        // Extract and validate message ID
        String commandId = json.getString("id");
        if (commandId == null || commandId.trim().isEmpty())
            throw new IllegalStateException("Message ID is missing or empty");

        // Parse parameters
        Map<String, Object> params = Parse.Params(json);

        // Verify command exists
        if (!params.containsKey("command")) {
            sendError(commandId, "No command specified");
            return;
        }

        String command = params.get("command").toString().trim();
        System.out.println("\nClient: " + clientId + ", Sent command: " + command);

        // Get and execute handler
        CommandHandler handler = CommandRegistry.getHandler(command);
        try {
            if (handler == null) {
                sendError(commandId, "Unknown command, " + command);
                return;
            }

            JSONObject response = handler.handle(params);
            System.out.println("Command " + command + " executed successfully for client " + clientId);
            sendResponse(commandId, response);
        } catch (Exception e) {
            sendError(commandId, "Command execution failed: " + e.getMessage());
        }
    }

    /**
     * Sends a successful response to the client.
     * <p>
     * Constructs a {@link Response} object using the given command ID and parameters.
     *
     * @param commandID the id of the command this response is related to
     * @param params    the {@link JSONObject} containing the response data
     */
    private void sendResponse(String commandID, JSONObject params) {
        Response response = new Response(commandID, params);

        out.println(response.toJSON());
    }

    /**
     * Sends an error response to the client.
     * <p>
     * Constructs an {@link Error} object with the given request ID and error message.
     *
     * @param id      the id of the request that caused the error
     * @param message a description of the error
     */
    private void sendError(String id, String message) {
        System.err.println("Error with client: " + clientId + ", request: " + id + "\nMessage: " + message);
        out.println(new Error(id, message).toJSON());
    }

    /**
     * Closes the connection to the client and performs cleanup.
     * <p>
     * This method shuts down the input and output streams, closes the client socket,
     * and notifies the server to remove the client from its active list.
     * Any {@link IOException} encountered during cleanup is logged to standard error.
     */
    public void close() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();

            if (clientSocket != null && !clientSocket.isClosed())
                clientSocket.close();

            if (server != null) server.removeClient(clientId);
        } catch (IOException e) {
            System.err.println("Error closing client connection: " + e.getMessage());
        }
    }
}
