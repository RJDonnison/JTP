package org.reujdon.jtp.shared.json;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

/**
 * A JsonAdapter implementation for Gson.
 */
//TODO: error handling
public class GsonAdapter implements JsonAdapter {
    private final Gson gson;
    private String jsonString;

    public GsonAdapter() {
        this.gson = new Gson();
    }

    public GsonAdapter(String jsonString) {
        this();
        this.jsonString = jsonString;
    }

    private JsonObject getJsonObject() {
        return gson.fromJson(jsonString, JsonObject.class);
    }

    @Override
    public String getString(String key) {
        JsonElement el = getJsonObject().get(key);
        return (el != null && el.isJsonPrimitive() && el.getAsJsonPrimitive().isString()) ? el.getAsString() : null;
    }


    @Override
    public Integer getInt(String key) {
        JsonElement el = getJsonObject().get(key);
        return (el != null && el.isJsonPrimitive() && el.getAsJsonPrimitive().isNumber()) ? el.getAsInt() : null;
    }

    @Override
    public Long getLong(String key) {
        JsonElement el = getJsonObject().get(key);
        return (el != null && el.isJsonPrimitive() && el.getAsJsonPrimitive().isNumber()) ? el.getAsLong() : null;
    }

    @Override
    public Double getDouble(String key) {
        JsonElement el = getJsonObject().get(key);
        return (el != null && el.isJsonPrimitive() && el.getAsJsonPrimitive().isNumber()) ? el.getAsDouble() : null;
    }

    @Override
    public Boolean getBoolean(String key) {
        JsonElement el = getJsonObject().get(key);
        return (el != null && el.isJsonPrimitive() && el.getAsJsonPrimitive().isBoolean()) ? el.getAsBoolean() : null;
    }

    @Override
    public Map getMap(String key) {
        JsonElement el = getJsonObject().get(key);
        if (el != null && el.isJsonObject())
            return gson.fromJson(el, Map.class);
        return null;
    }

    @Override
    public <T> T get(String key, Class<T> clazz) {
        JsonElement el = getJsonObject().get(key);
        return el != null ? gson.fromJson(el, clazz) : null;
    }

    @Override
    public Object get(String key) {
        return get(key, Object.class);
    }

    @Override
    public boolean has(String key) {
        return getJsonObject().has(key);
    }

    @Override
    public Set<String> keySet() {
        return getJsonObject().keySet();
    }

    @Override
    public Map<String, Object> asMap() {
        return gson.fromJson(getJsonObject(), Map.class);
    }

    @Override
    public <E extends Enum<E>> E getEnum(String key, Class<E> enumClass) {
        JsonElement el = getJsonObject().get(key);
        if (el == null || !el.isJsonPrimitive()) return null;
        try {
            return Enum.valueOf(enumClass, el.getAsString());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public <T> T deserialize(String json, Class<T> clazz) throws JsonParseException {
        try {
            return gson.fromJson(json, clazz);
        } catch (JsonParseException e) {
            throw new JsonParseException("Failed to deserialize into " + clazz.getSimpleName(), e);
        }
    }

    @Override
    public <T> T deserialize(String json, Type typeOfT) throws JsonParseException {
        try {
            return gson.fromJson(json, typeOfT);
        } catch (JsonParseException e) {
            throw new JsonParseException("Failed to deserialize into type " + typeOfT.getTypeName(), e);
        }
    }

    @Override
    public String serialize(Object obj) {
        return gson.toJson(obj);
    }

    @Override
    public String extractType(String json) {
        JsonElement el = gson.fromJson(json, JsonElement.class);
        return (el.isJsonObject() && el.getAsJsonObject().has("type"))
                ? el.getAsJsonObject().get("type").getAsString()
                : null;
    }

    @Override
    public String getRawJson() {
        return jsonString;
    }

    @Override
    public JsonElement getJsonElement() {
        return gson.fromJson(jsonString, JsonElement.class);
    }

    @Override
    public void setJsonString(String jsonString) {
        this.jsonString = jsonString;
    }
}
