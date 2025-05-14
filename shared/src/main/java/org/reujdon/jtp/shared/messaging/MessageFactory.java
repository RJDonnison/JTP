package org.reujdon.jtp.shared.messaging;

import org.reujdon.jtp.shared.json.GsonAdapter;
import org.reujdon.jtp.shared.json.JsonAdapter;
import org.reujdon.jtp.shared.messaging.messages.Auth;
import org.reujdon.jtp.shared.messaging.messages.Error;
import org.reujdon.jtp.shared.messaging.messages.Request;
import org.reujdon.jtp.shared.messaging.messages.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory for creating and deserializing messages based on their {@link MessageType}.
 * <p>
 * Maintains a registry mapping message types to their corresponding {@link Message} subclasses.
 * Uses a {@link JsonAdapter} (defaulting to {@link GsonAdapter}) to deserialize JSON representations.
 *
 * <p>
 * Typical usage involves calling {@link #deserialize(String)} to convert a JSON string into a typed message object.
 *
 * @author Reuben Donnison
 * @version 0.2
 */
public class MessageFactory {
    private static final Map<MessageType, Class<? extends Message>> registry = new HashMap<>();
    private static final JsonAdapter adapter = new GsonAdapter();

    static {
        register(MessageType.REQUEST, Request.class);
        register(MessageType.RESPONSE, Response.class);
        register(MessageType.ERROR, Error.class);
        register(MessageType.AUTH, Auth.class);
    }

    /**
     * Registers a message type and its corresponding class for deserialization.
     *
     * @param type  the {@link MessageType} to register
     * @param clazz the {@link Message} subclass associated with the type
     */
    public static void register(MessageType type, Class<? extends Message> clazz) {
        registry.put(type, clazz);
    }

    /**
     * Deserializes a JSON string into a {@link Message} object based on the embedded type field.
     *
     * @param json the JSON string representing the message
     * @return the deserialized {@code Message} instance, or {@code null} if the input is blank
     * @throws IllegalArgumentException if the message type is unknown or not registered
     */
    public static Message deserialize(String json) {
        if (json == null || json.isBlank()) return null;

        adapter.setJsonString(json);
        MessageType type = MessageType.fromString(adapter.getString("type"));
        Class<? extends Message> clazz = registry.get(type);
        if (clazz == null)
            throw new IllegalArgumentException("Unknown message type: " + type);

        return adapter.deserialize(json, clazz);
    }
}
