package org.reujdon.jtp.client;

import org.reujdon.jtp.shared.json.JsonAdapter;
import org.reujdon.jtp.shared.messaging.MessageType;
import org.reujdon.jtp.shared.messaging.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reujdon.async.Async;
import reujdon.async.Task;

import java.util.HashMap;
import java.util.Map;

class ResponseHandler {
    private static final Logger logger = LoggerFactory.getLogger(ResponseHandler.class);

    private final HashMap<String, Request> pendingResponses;

    public ResponseHandler() {
        this.pendingResponses = new HashMap<>();
    }

    public void processResponse(JsonAdapter response) {
        String id = response.getString("id");
        String typeStr = response.getString("type");

        if (typeStr == null) {
            logger.warn("Missing 'type' in response: {}", response);
            return;
        }

        Task.of(() -> handleRegularResponse(id, response)).run();
    }

    /**
     * Handles the response from the server and passes to suitable {@link Request} function.
     *
     * @param response the {@link JsonAdapter} containing the server's response data
     * @throws IllegalArgumentException if {@code response} or {@code request} is null
     */
    private void handleRegularResponse(String id, JsonAdapter response){
        if (id == null || !pendingResponses.containsKey(id)) {
            logger.warn("Unmatched response: {}", response);
            return;
        }

        Request request = pendingResponses.remove(id);
        if (request == null)
            throw new RuntimeException("Request was null for ID: " + id);

        if (response == null)
            throw new IllegalArgumentException("Response cannot be null");

        MessageType type = response.getEnum("type", MessageType.class);

        Map<String, Object> params = response.getMap("params");

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
}
