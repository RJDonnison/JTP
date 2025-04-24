package org.reujdon.jtp.shared;

import org.json.JSONObject;

/**
 * Represents a response message in the transfer protocol.
 *
 * <p>Response messages are used to return data from successful command executions.
 * They contain:</p>
 * <ul>
 *   <li>A message type of {@link MessageType#RESPONSE}</li>
 *   <li>Response data as key-value pairs</li>
 *   <li>An identifier matching the original request</li>
 * </ul>
 *
 <p>Example JSON representation:</p>
 * <pre>
 * {@code
 * {
 *   "type": "RESPONSE",
 *   "id": "550e8400-e29b-41d4-a716-446655440000",
 *   "params": {
 *     "username": "john_doe",
 *     "email": "john@example.com",
 *     "status": "active"
 *   }
 * }
 * }
 * </pre>
 *
 * @see Message
 * @see MessageType#RESPONSE
 */
public class Response extends Message {
    /**
     * Constructs a Response message with the specified ID.
     *
     * @param id The response identifier (should match the original request ID)
     * @throws IllegalArgumentException if id is null or empty
     */
    public Response(String id){
        super(id, MessageType.RESPONSE);
    }

    /**
     * Constructs a Response message with the specified ID and initial data.
     *
     * @param id The response identifier (should match the original request ID)
     * @param data Initial response data as a JSONObject (can be null)
     * @throws IllegalArgumentException if id is null or empty
     */
    public Response(String id, JSONObject data){
        this(id);

        this.addParams(data);
    }

    /**
     * Adds multiple parameters from a JSONObject to this response.
     *
     * <p>This method safely handles null input by doing nothing. All key-value pairs
     * from the JSONObject will be added to the response parameters.</p>
     *
     * @param data JSONObject containing parameters to add (can be null)
     */
    protected void addParams(JSONObject data) {
        if (data == null)
            return;

        for (String key : data.keySet())
            params.put(key, data.get(key));
    }
}
