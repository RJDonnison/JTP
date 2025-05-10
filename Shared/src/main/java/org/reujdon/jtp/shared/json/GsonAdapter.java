package org.reujdon.jtp.shared.json;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

/**
 * A JsonAdapter implementation for Gson.
 */
public class GsonAdapter implements JsonAdapter {
    private final Gson gson;
    private String jsonString;
    private JsonObject jsonObject;

    public GsonAdapter() {
        this.gson = new Gson();
    }

    public GsonAdapter(String jsonString) throws JsonException {
        this();
        this.jsonString = jsonString;

        try {
            this.jsonObject = gson.fromJson(jsonString, JsonObject.class);
            if (jsonObject == null)
                throw new JsonException("JSON string did not produce a valid JsonObject (null)");
        } catch (Exception e) {
            throw new JsonException("Failed to parse raw JSON into JsonObject", e);
        }
    }

    @Override
    public String getString(String key) {
        try {
            JsonElement el = jsonObject.get(key);
            if (el != null && el.isJsonPrimitive() && el.getAsJsonPrimitive().isString())
                return el.getAsString();
            return null;
        } catch (Exception e) {
            throw new JsonException("Failed to get string for key: " + key, e);
        }
    }


    @Override
    public Integer getInt(String key) {
        try {
            JsonElement el = jsonObject.get(key);
            return (el != null && el.isJsonPrimitive() && el.getAsJsonPrimitive().isNumber()) ? el.getAsInt() : null;
        } catch (Exception e) {
            throw new JsonException("Failed to get int for key: " + key, e);
        }
    }

    @Override
    public Long getLong(String key) {
        try {
            JsonElement el = jsonObject.get(key);
            return (el != null && el.isJsonPrimitive() && el.getAsJsonPrimitive().isNumber()) ? el.getAsLong() : null;
        } catch (Exception e) {
            throw new JsonException("Failed to get long for key: " + key, e);
        }
    }

    @Override
    public Double getDouble(String key) {
        try {
            JsonElement el = jsonObject.get(key);
            return (el != null && el.isJsonPrimitive() && el.getAsJsonPrimitive().isNumber()) ? el.getAsDouble() : null;
        } catch (Exception e) {
            throw new JsonException("Failed to get double for key: " + key, e);
        }
    }

    @Override
    public Boolean getBoolean(String key) {
        try {
            JsonElement el = jsonObject.get(key);
            return (el != null && el.isJsonPrimitive() && el.getAsJsonPrimitive().isBoolean()) ? el.getAsBoolean() : null;
        } catch (Exception e) {
            throw new JsonException("Failed to get boolean for key: " + key, e);
        }
    }

    @Override
    public Map getMap(String key) {
        try {
            JsonElement el = jsonObject.get(key);
            if (el != null && el.isJsonObject()) {
                return gson.fromJson(el, Map.class);
            }
            return null;
        } catch (Exception e) {
            throw new JsonException("Failed to get map for key: " + key, e);
        }
    }

    @Override
    public <T> T get(String key, Class<T> clazz) {
        try {
            JsonElement el = jsonObject.get(key);
            return el != null ? gson.fromJson(el, clazz) : null;
        } catch (Exception e) {
            throw new JsonException("Failed to deserialize key: " + key + " into " + clazz.getSimpleName(), e);
        }
    }

    @Override
    public Object get(String key) {
        return get(key, Object.class);
    }

    @Override
    public boolean has(String key) {
        try {
            return jsonObject.has(key);
        } catch (Exception e) {
            throw new JsonException("Failed to check presence of key: " + key, e);
        }
    }

    @Override
    public Set<String> keySet() {
        try {
            return jsonObject.keySet();
        } catch (Exception e) {
            throw new JsonException("Failed to get key set from JSON", e);
        }
    }

    @Override
    public Map<String, Object> asMap() {
        try {
            return gson.fromJson(jsonObject, Map.class);
        } catch (Exception e) {
            throw new JsonException("Failed to convert JSON to Map<String, Object>", e);
        }
    }

    @Override
    public <E extends Enum<E>> E getEnum(String key, Class<E> enumClass) {
        try {
            JsonElement el = jsonObject.get(key);
            if (el == null || !el.isJsonPrimitive()) return null;

            return Enum.valueOf(enumClass, el.getAsString());
        } catch (IllegalArgumentException e) {
            throw new JsonException("Invalid enum value for key: " + key, e);
        } catch (Exception e) {
            throw new JsonException("Failed to get enum for key: " + key, e);
        }
    }

    @Override
    public <T> T deserialize(String json, Class<T> clazz) throws JsonException {
        try {
            return gson.fromJson(json, clazz);
        } catch (JsonException e) {
            throw new JsonException("Failed to deserialize JSON into " + clazz.getSimpleName(), e);
        }
    }

    @Override
    public <T> T deserialize(String json, Type typeOfT) throws JsonException {
        try {
            return gson.fromJson(json, typeOfT);
        } catch (JsonException e) {
            throw new JsonException("Failed to deserialize into type " + typeOfT.getTypeName(), e);
        }
    }

    @Override
    public String serialize(Object obj) {
        return gson.toJson(obj);
    }

    @Override
    public String getRawJson() {
        return jsonString;
    }

    @Override
    public JsonElement getJsonElement() {
        try {
            return gson.fromJson(jsonString, JsonElement.class);
        } catch (Exception e) {
            throw new JsonException("Failed to parse raw JSON into JsonElement", e);
        }
    }

    @Override
    public void setJsonString(String jsonString) {
        this.jsonString = jsonString;
    }
}
