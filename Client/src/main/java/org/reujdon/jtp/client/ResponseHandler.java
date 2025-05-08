package org.reujdon.jtp.client;

import org.json.JSONException;
import org.json.JSONObject;
import org.reujdon.jtp.shared.Parse;
import org.reujdon.jtp.shared.messaging.Auth;
import org.reujdon.jtp.shared.messaging.MessageType;
import org.reujdon.jtp.shared.messaging.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reujdon.async.Async;
import reujdon.async.Task;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

class ResponseHandler {
    private static final Logger logger = LoggerFactory.getLogger(ResponseHandler.class);

    private final HashMap<String, Request> pendingResponses;
//    private Request cachedRequest;

    private final PrintWriter out;

    //    TODO: look at secure storage
    private String sessionToken;

    public ResponseHandler(PrintWriter out) {
        this.pendingResponses = new HashMap<>();
        this.out = out;
    }

    public void processResponse(JSONObject response) {
        try {
            String id = response.optString("id", null);
            String typeStr = response.optString("type", null);

            if (typeStr == null) {
                logger.warn("Missing 'type' in response: {}", response);
                return;
            }

            MessageType type;
            try {
                type = MessageType.valueOf(typeStr);
            } catch (IllegalArgumentException e) {
                logger.warn("Unknown MessageType '{}': {}", typeStr, response);
                return;
            }

            if ("*".equals(id)){
                if (type == MessageType.ERROR)
                    Task.of(() -> handleGlobalError(response)).run();
                else if (type == MessageType.AUTH)
                    Task.of(() -> handleAuthResponse(response)).run();

                return;
            }

            if (type == MessageType.AUTH){
                Task.of(() -> handleAuthRequest(id, response)).run();
                return;
            }

            Task.of(() -> handleRegularResponse(id, response)).run();
        } catch (JSONException e) {
            logger.error("Failed to parse response JSON: {}", e.getMessage());
        }
    }

    private void handleGlobalError(JSONObject response) {
        Map<String, Object> params = Parse.Params(response);
        logger.error("Server error: {}", params.getOrDefault("message", "No message"));
    }

    private void handleAuthRequest(String id, JSONObject response) {
        Request request = pendingResponses.remove(id);

        if (request == null)
            logger.warn("AUTH response without matching request: {}", response);

        logger.info("Authenticating...");

//        cachedRequest = request;

//        TODO: put in key
        out.println(new Auth("*", "test").toJSON());
    }

//    TODO: handle multiple AUTH request and response at once
//    TODO: handle send command when AUTH
//    TODO: handle failed AUTH
    private void handleAuthResponse(JSONObject response) {
        String token = Parse.Params(response).getOrDefault("key", null).toString();

        if (token == null) {
            logger.warn("Auth response without token: {}", response);
            return;
        }

        sessionToken = token;

//        TODO: resend last command
//        sendCommand(cachedRequest);
//        cachedRequest = null;

        logger.info("Auth successful");
    }

    /**
     * Handles the response from the server and passes to suitable {@link Request} function.
     *
     * @param response the {@link JSONObject} containing the server's response data
     * @throws IllegalArgumentException if {@code response} or {@code request} is null
     */
    private void handleRegularResponse(String id, JSONObject response){
        if (id == null || !pendingResponses.containsKey(id)) {
            logger.warn("Unmatched response: {}", response);
            return;
        }

        Request request = pendingResponses.remove(id);
        if (request == null)
            throw new RuntimeException("Request was null for ID: " + id);

        if (response == null)
            throw new IllegalArgumentException("Response cannot be null");

        MessageType type = response.getEnum(MessageType.class, "type");

        Map<String, Object> params = Parse.Params(response);

        switch (type) {
            case ERROR ->
                    request.onError(params.get("message").toString());

            case RESPONSE ->
                    request.onSuccess(params);

            case null, default ->
                    logger.warn("Unsupported message type: {}", response);
        }
    }

    /**
     * Handles the timeout for a request if a response is not received within the specified timeout period.
     *
     * @param request the {@link Request} object that timed out
     * @param id the unique identifier of the request
     * @throws IllegalArgumentException if request or id is null or empty
     */
    private void handleTimeout(Request request, String id) {
        if (request == null)
            throw new IllegalArgumentException("Request cannot be null");

        if (id == null || id.trim().isEmpty())
            throw new IllegalArgumentException("Id cannot be null or empty");

        Async.waitFor(request.getTimeout());

        if (pendingResponses.containsKey(id))
            request.onTimeout();

        pendingResponses.remove(id);
    }

    public void addPendingRequest(String id, Request request) {
        pendingResponses.put(id, request);

        Task<Void> timeout = Task.of(() -> handleTimeout(request, id));
        timeout.run();
    }

    public void removePendingRequest(String id) {
        pendingResponses.remove(id);
    }

    public String getSessionToken() {
        return sessionToken;
    }
}
