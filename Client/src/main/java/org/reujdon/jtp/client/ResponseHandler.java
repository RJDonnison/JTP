package org.reujdon.jtp.client;

import org.reujdon.jtp.client.commands.Command;
import org.reujdon.jtp.shared.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public void addPendingRequest(String id, Command command) {
        pendingResponses.put(id, command);

        Thread.startVirtualThread(() -> {
            try {
                Thread.sleep(command.getTimeout());
                if (pendingResponses.containsKey(id))
                    command.onTimeout();

                pendingResponses.remove(id);
            } catch (InterruptedException ignored) {
                // Thread was cancelled or interrupted before timeout
            }
        });
    }

    public void removePendingRequest(String id) {
        pendingResponses.remove(id);
    }
}
