package org.reujdon.jtp.shared.messaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Abstract base class representing a message in the transfer protocol.
 *
 * <p>All messages contain:</p>
 * <ul>
 *   <li>A unique identifier (UUID by default)</li>
 *   <li>A message type (from {@link MessageType})</li>
 *   <li>Optional parameters as key-value pairs</li>
 * </ul>
 *
 *
 * @see MessageType
 * @see JSONObject
 */
abstract class Message {
    private String id;
    private final MessageType type;

    protected final Map<String, Object> params = new HashMap<>();

    /**
     * Constructs a new Message with a randomly generated UUID and specified message type.
     *
     * @param type The type of message to create
     * @throws IllegalArgumentException if the message type is null
     * @see MessageType
     */
    protected Message(MessageType type) {
        this(UUID.randomUUID().toString(), type);
    }

    /**
     * Constructs a new Message with the specified ID and type.
     *
     * @param id The unique identifier for this message
     * @param type The type of message
     * @throws IllegalArgumentException if either id is null/empty or type is null
     * @see MessageType
     */
    protected Message(String id, MessageType type) {
        if (id == null || id.trim().isEmpty())
            throw new IllegalArgumentException("Id is null or empty");

        if (type == null)
            throw new IllegalArgumentException("Type is null");

        this.id = id;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MessageType getType() {
        return type;
    }

    /**
     * Adds a parameter to this message with the specified key and value.
     *
     * @param key the parameter key (cannot be empty or contain only whitespace)
     * @param value the parameter value to associate with the key
     * @throws IllegalArgumentException if the key is invalid
     */
    public void addParam(String key, Object value) {
        if (key.trim().isEmpty())
            throw new IllegalArgumentException("Key cannot be empty or null.");

        params.put(key, value);
    }

    /**
     * Adds multiple parameters from a {@code Map<String, Object>} to this response.
     *
     * @param params Map containing parameters to add (can be null)
     */
    public void addParams(Map<String, ?> params) {
        if (params == null)
            return;

        this.params.putAll(params);
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

    /**
     * Removes the parameter with the specified key from this message.
     *
     * <p>If the key does not exist in the parameters map, the method completes silently
     * without throwing an exception or modifying the map.</p>
     *
     * @param key the key of the parameter to remove
     * @see #addParam(String, Object)
     */
    protected void removeParam(String key) {
        params.remove(key);
    }

    /**
     * Retrieves the value associated with the specified parameter key.
     *
     * @param key the key whose associated value is to be returned
     * @return the keys value
     * @throws IllegalArgumentException if the key is not found in the parameters map
     * @see #getParam(String, Object)
     */
    public Object getParam(String key) throws IllegalArgumentException {
        if (!params.containsKey(key))
            throw new IllegalArgumentException("Key not found: " + key);

        return params.get(key);
    }

    /**
     * Retrieves the value associated with the specified key, returning the default value
     * if the key is not found in the parameters map.
     *
     * @param key the key whose associated value is to be returned
     * @param defaultValue the value to return if the key is not found
     * @return the value to which the specified key is mapped, or the defaultValue if not found
     *
     * @see Map#getOrDefault(Object, Object)
     */
    public Object getParam(String key, Object defaultValue) {
        return params.getOrDefault(key, defaultValue);
    }

    /**
     * Converts this message into a JSONObject representation.
     *
     * <p>The returned JSON structure contains the following fields:</p>
     * <ul>
     *   <li><b>type</b> - The message type ({@link MessageType})</li>
     *   <li><b>id</b> - The unique message identifier</li>
     *   <li><b>params</b> - (Optional) The message parameters as a nested JSONObject,
     *       only included if parameters exist</li>
     * </ul>
     *
     * @return a JSONObject containing the complete message structure
     * @throws JSONException if there is an error during JSON serialization
     *
     * @see MessageType
     * @see JSONObject
     *
     * @Example:
     * <pre>
     * {@code
     * {
     *      "type": "COMMAND",
     *      "id": "550e8400-e29b-41d4-a716-446655440000",
     *      "params": {
     *          "username": "john_doe",
     *          "timestamp": 1625097600
     *      }
     * }
     * }
     * </pre>
     */
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("type", type);
        json.put("id", id);

        if (!params.isEmpty())
            json.put("params", new JSONObject(params));

        return json;
    }
}
