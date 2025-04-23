package org.reujdon.jtp.shared;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Message {
    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    private final String id;
    private final MessageType type;

    protected String token = null;
    protected final Map<String, Object> params = new HashMap<>();

    protected Message(MessageType type) {
        this.type = type;
        this.id = String.valueOf(COUNTER.incrementAndGet());
    }

    public String getId() {
        return id;
    }

    public MessageType getType() {
        return type;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token.trim();
    }

    protected void addParam(String key, Object value) {
        if (params.containsKey(key))
            throw new IllegalArgumentException("Duplicate key: " + key);

        params.put(key, value);
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("type", type);
        json.put("id", id);

        if (token != null)
            json.put("token", token);

        if (!params.isEmpty())
            json.put("params", new JSONObject(params));

        return json;
    }
}
