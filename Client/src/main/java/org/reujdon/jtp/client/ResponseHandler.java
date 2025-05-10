package org.reujdon.jtp.client;

import org.reujdon.jtp.client.commands.Command;
import org.reujdon.jtp.shared.messaging.Message;
import org.reujdon.jtp.shared.messaging.messages.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reujdon.async.Task;

import java.util.HashMap;

class ResponseHandler {
    private static final Logger logger = LoggerFactory.getLogger(ResponseHandler.class);

    private final HashMap<String, Command> pendingResponses = new HashMap<>();

    public void processResponse(Message response) {
        if (response.getId() == null || !pendingResponses.containsKey(response.getId())) {
            logger.warn("Unmatched response: {}", response);
            return;
        }

        if (response.getId().equals("*")) {
            handleGlobalResponse(response);
            return;
        }

        Command command = pendingResponses.remove(response.getId());
        if (command == null)
            throw new RuntimeException("Request was null for ID: " + response.getId());

        switch (response.getType()) {
            case RESPONSE:
                command.onSuccess(response.getParams());
                break;
            case ERROR:
                Object message = response.getParam("message");
                command.onError(message != null ? message.toString() : "Unknown error");
                break;
            case null, default:
                logger.error("Unknown response type: {}", response.getType());
                break;
        }
    }

    private void handleGlobalResponse(Message response) {
        switch (response.getType()) {
            case ERROR:
                logger.error("Server error: {}", response.getParam("message"));
                break;
            case null, default:
                logger.warn("Unknown global response: {}", response);
                break;
        }
    }

    /**
     * Handles the timeout for a request if a response is not received within the specified timeout period.
     *
     * @param command the {@link Request} object that timed out
     * @param id the unique identifier of the request
     * @throws IllegalArgumentException if request or id is null or empty
     */
    private void handleTimeout(Command command, String id) {
        if (pendingResponses.containsKey(id))
            command.onTimeout();

        pendingResponses.remove(id);
    }

    public void addPendingRequest(String id, Command command) {
        pendingResponses.put(id, command);

        Task<Void> timeout = Task.of(() -> handleTimeout(command, id));
        timeout.delay(command.getTimeout());
    }

    public void removePendingRequest(String id) {
        pendingResponses.remove(id);
    }
}
