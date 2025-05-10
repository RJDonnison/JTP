package org.reujdon.jtp.shared.messaging;

import org.reujdon.jtp.shared.json.GsonAdapter;
import org.reujdon.jtp.shared.json.JsonAdapter;
import org.reujdon.jtp.shared.messaging.messages.Auth;
import org.reujdon.jtp.shared.messaging.messages.Error;
import org.reujdon.jtp.shared.messaging.messages.Request;
import org.reujdon.jtp.shared.messaging.messages.Response;

import java.util.HashMap;
import java.util.Map;

public class MessageFactory {
    private static final Map<MessageType, Class<? extends Message>> registry = new HashMap<>();
    private static final JsonAdapter adapter = new GsonAdapter();

    static {
        register(MessageType.REQUEST, Request.class);
        register(MessageType.RESPONSE, Response.class);
        register(MessageType.ERROR, Error.class);
        register(MessageType.AUTH, Auth.class);
    }

    public static void register(MessageType type, Class<? extends Message> clazz) {
        registry.put(type, clazz);
    }

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
