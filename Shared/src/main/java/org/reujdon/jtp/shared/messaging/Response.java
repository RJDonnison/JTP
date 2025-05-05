package org.reujdon.jtp.shared.messaging;

import org.json.JSONObject;

import java.util.Map;

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
     * Constructs an empty Response message
     */
    public Response(){
        super(MessageType.RESPONSE);
    }

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
     * Constructs a Response message with the specified initial data.
     *
     * @param data Initial response data as a JSONObject (can be null)
     */
    public Response(JSONObject data){
        super(MessageType.RESPONSE);

        this.addParams(data);
    }

    /**
     * Constructs a Response message with the specified initial data.
     *
     * @param data Initial response data as a {@code Map<String, Object>} (can be null)
     */
    public Response(Map<String, ?> data){
        super(MessageType.RESPONSE);

        this.addParams(data);
    }
}
