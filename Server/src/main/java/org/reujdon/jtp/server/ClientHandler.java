package org.reujdon.jtp.server;

import org.reujdon.jtp.shared.json.JsonException;
import org.reujdon.jtp.shared.messaging.Message;
import org.reujdon.jtp.shared.messaging.MessageFactory;
import org.reujdon.jtp.shared.messaging.messages.Error;
import org.reujdon.jtp.shared.messaging.messages.Request;
import org.reujdon.jtp.shared.messaging.messages.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reujdon.async.Task;

import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

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
 * Instances of this class are typically managed by the {@link JTPServer} and
 * executed on separate threads to allow concurrent client handling.
 *
 * @see Runnable
 */
class ClientHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    private final SSLSocket clientSocket;
    private final JTPServer server;

    private final String clientId;

    private BufferedReader in;
    private PrintWriter out;

    /**
     * Constructs a new {@code ClientHandler} with the specified SSL socket and server.
     *
     * @param socket the {@link SSLSocket} representing the client's connection
     * @param server the {@link JTPServer} instance that this client is connecting to
     * @throws IllegalArgumentException if:
     * <ul>
     *     <li>the socket is {@code null} or closed</li>
     *     <li>the server is {@code null} or not running</li>
     * </ul>
     */
    public ClientHandler(SSLSocket socket, JTPServer server) {
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
                Message deserilaizedMessage = MessageFactory.deserialize(message);

//                TODO: remove
                Task.of(() -> handleMessage(deserilaizedMessage)).run();
            }
        }
        catch (IOException e){
            logger.error("IOException during client communication: {}", e.getMessage());
        } catch (JsonException e) {
            sendError("*", "Invalid json: " + e.getMessage());
        }
        finally {
            close();
        }
    }

    /**
     * Handles an incoming message from the client.
     *
     * @throws NullPointerException if the {@link Message} is {@code null}
     */
    private void handleMessage(Message message) {
        if (message == null)
            throw new NullPointerException("Message cannot be null");

        // Extract and validate message ID
        String commandId = message.getId();
        if (commandId == null || commandId.isBlank()) {
            sendError("*", "Message ID is missing or blank");
            return;
        }

        switch (message.getType()) {
            case REQUEST:
                handleRequest((Request) message, commandId);
                break;
            case null, default:
                sendError(commandId, "Unknown message type: " + message.getType());
                break;
        }
    }

    private void handleRequest(Request message, String commandId) {
        // Verify command exists
        if (!message.containsParam("command")) {
            sendError(commandId, "No command specified");
            return;
        }

        String command = message.getParam("command").toString().trim();
        logger.info("Client: {}, Sent command: {}", clientId, command);

        // Get and execute handler
        CommandHandler handler = CommandRegistry.getHandler(command);
        try {
            if (handler == null) {
                sendError(commandId, "Unknown command, " + command);
                return;
            }

            Response response = handler.handle(message);
            logger.info("Command {} executed successfully for client {}", command, clientId);
            sendResponse(commandId, response);
        } catch (Exception e) {
            sendError(commandId, "Command execution failed: " + e.getMessage());
        }
    }

    /**
     * Sends a successful response to the client.
     *
     * @param commandID the id of the command this response is related to
     * @param response  the {@link Response} containing the response data
     */
    private void sendResponse(String commandID, Response response) {
        response.setId(commandID);

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
        logger.error("Error with client: {}, request: {} | {}", clientId, id, message);
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
            logger.error("Error closing client connection: {}", e.getMessage());
        }
    }
}
