package org.reujdon.jtp.shared;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class Message {
    private final String id;
    private final MessageType type;


    protected final Map<String, Object> params = new HashMap<>();

    protected Message(MessageType type) {
        this.type = type;
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public MessageType getType() {
        return type;
    }

    protected void addParam(String key, Object value) {
        if (key.trim().isEmpty())
            throw new IllegalArgumentException("Key cannot be empty.");

        if (params.containsKey(key))
            throw new IllegalArgumentException("Duplicate key: " + key);

        params.put(key, value);
    }

    protected void setParam(String key, Object value) {
        if (!params.containsKey(key))
            throw new IllegalArgumentException("Map is missing key: " + key);

        params.put(key, value);
    }

    protected void removeParam(String key) {
        params.remove(key);
    }

    public Object getParam(String key) {
        if (!params.containsKey(key))
            throw new IllegalArgumentException("Key not found: " + key);

        return params.get(key);
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("type", type);
        json.put("id", id);

        if (!params.isEmpty())
            json.put("params", new JSONObject(params));

        return json;
    }
}
