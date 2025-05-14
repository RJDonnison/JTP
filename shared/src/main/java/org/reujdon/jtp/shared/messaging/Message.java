package org.reujdon.jtp.shared.messaging;

import org.reujdon.jtp.shared.json.GsonAdapter;
import org.reujdon.jtp.shared.json.JsonAdapter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Abstract base class representing a message in jtp.
 *
 * <p>All messages contain:</p>
 * <ul>
 *   <li>A unique identifier (UUID by default)</li>
 *   <li>A message type (from {@link MessageType})</li>
 *   <li>Optional parameters as key-value pairs</li>
 * </ul>
 *
 * <p>This class supports adding, removing, and retrieving parameters, serializing
 * to JSON, and contains utility methods for managing the message's structure.</p>
 *
 * @see MessageType
 * @author Reuben Donnison
 * @version 0.2
 */
//TODO: move away from params
public class Message {
    private String id;
    private final MessageType type;

    /**
     * Map containing all message parameters
     */
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
     * @param id The unique identifier for this message (ID="*" for global message)
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

    /**
     * Gets the message's unique identifier
     *
     * @return the message ID (ID="*" for global message)
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the message's unique identifier
     *
     * @param id the new ID to set (ID="*" for global message)
     * @throws IllegalArgumentException if ID is null or blank
     */
    public void setId(String id) {
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("Id cannot be null or blank");

        this.id = id;
    }

    /**
     * Gets the message type
     *
     * @return the message type
     */
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
        if (key.isBlank())
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
    protected void addParams(JsonAdapter data) {
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
     * Checks if the message contains a parameter with the specified key.
     *
     * @param key the key to check
     * @return true if the message contains the key, false otherwise
     */
    public boolean containsParam(String key) {
        return params.containsKey(key);
    }

    /**
     * Gets all message parameters
     *
     * @return map of all parameters
     */
    public Map<String, Object> getParams() {
        return params;
    }

    /**
     * Converts this message into a JSON representation.
     *
     * @return a JSON string containing the complete message structure
     */
    public String toJSON() {
        return new GsonAdapter().serialize(this);
    }

    @Override
    public String toString() {
        return "Message{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", params=" + params +
                '}';
    }
}
