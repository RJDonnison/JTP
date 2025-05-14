package org.reujdon.jtp.client;

import org.reujdon.jtp.client.commands.Command;
import org.reujdon.jtp.shared.messaging.Message;
import org.reujdon.jtp.shared.messaging.messages.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * Handles and processes responses from the JTP server.
 *
 * <p>Manages pending commands and routes responses to appropriate handlers.
 * Provides authentication state tracking and timeout handling.</p>
 *
 * @author Reuben Donnison
 * @version 0.2
 */
class ResponseHandler {
    private static final Logger logger = LoggerFactory.getLogger(ResponseHandler.class);

    /**
     * Map of pending commands awaiting responses, keyed by message ID
     */
    private final HashMap<String, Command> pendingResponses = new HashMap<>();

    private String token = null;
    private boolean authenticated = false;

    /**
     * Processes incoming server response messages.
     *
     * @param response the message to process
     */
    public void processResponse(Message response) {
        if (response.getId().equals("*")) {
            handleGlobalResponse(response);
            return;
        }

        if (response.getId() == null || !pendingResponses.containsKey(response.getId())) {
            logger.warn("Unmatched response: {}", response);
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

    /**
     * Handles global server responses (ID="*").
     *
     * @param response the global message
     */
    private void handleGlobalResponse(Message response) {
        switch (response.getType()) {
            case ERROR:
                logger.error("Server error: {}", response.getParam("message"));
                break;
            case AUTH:
                handleAuth((Auth) response);
                break;
            case null, default:
                logger.warn("Unknown global response: {}", response);
                break;
        }
    }

    /**
     * Processes authentication responses.
     *
     * @param auth the authentication message
     * @throws RuntimeException if authentication fails
     */
    private void handleAuth(Auth auth) {
        if (!auth.getSuccess())
            throw new RuntimeException("Client authentication failed");

        logger.info("Authentication success");
        this.token = auth.getToken();
        this.authenticated = true;
    }

    /**
     * Gets the current authentication token.
     *
     * @return the authentication token, or null if not authenticated
     */
    public String getToken() {
        return token;
    }

    /**
     * Checks authentication status.
     *
     * @return true if client is authenticated
     */
    public boolean isAuthenticated() {
        return authenticated;
    }

    /**
     * Adds a command to pending responses with timeout handling.
     *
     * @param id the message ID
     * @param command the command to track
     */
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

    /**
     * Removes a pending command from tracking.
     *
     * @param id the message ID to remove
     */
    public void removePendingRequest(String id) {
        pendingResponses.remove(id);
    }

    /**
     * Gets the current number of pending commands awaiting responses.
     *
     * @return the count of pending commands
     * @see #pendingResponses
     */
    public synchronized int getPendingCommandCount() {
        return pendingResponses.size();
    }
}
