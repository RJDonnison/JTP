package org.reujdon.jtp.shared.json;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

/**
 * Gson-based implementation of the JsonAdapter interface.
 *
 * <p>Provides JSON operations using Google's Gson library as the backend implementation.</p>
 *
 * @author Reuben Donnison
 * @version 0.2
 */
public class GsonAdapter implements JsonAdapter {
    private final Gson gson;
    private String jsonString;
    private JsonObject jsonObject;

    /**
     * Constructs a new GsonAdapter with an empty JSON object.
     */
    public GsonAdapter() {
        this.gson = new Gson();
        setJsonString("{}");
    }

    /**
     * Constructs a new GsonAdapter with the specified JSON string.
     *
     * @param jsonString The JSON string to parse and manage
     * @throws JsonException if the JSON string is invalid
     */
    public GsonAdapter(String jsonString) throws JsonException {
        this();
        setJsonString(jsonString);
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getInt(String key) {
        try {
            JsonElement el = jsonObject.get(key);
            return (el != null && el.isJsonPrimitive() && el.getAsJsonPrimitive().isNumber()) ? el.getAsInt() : null;
        } catch (Exception e) {
            throw new JsonException("Failed to get int for key: " + key, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getLong(String key) {
        try {
            JsonElement el = jsonObject.get(key);
            return (el != null && el.isJsonPrimitive() && el.getAsJsonPrimitive().isNumber()) ? el.getAsLong() : null;
        } catch (Exception e) {
            throw new JsonException("Failed to get long for key: " + key, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double getDouble(String key) {
        try {
            JsonElement el = jsonObject.get(key);
            return (el != null && el.isJsonPrimitive() && el.getAsJsonPrimitive().isNumber()) ? el.getAsDouble() : null;
        } catch (Exception e) {
            throw new JsonException("Failed to get double for key: " + key, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean getBoolean(String key) {
        try {
            JsonElement el = jsonObject.get(key);
            return (el != null && el.isJsonPrimitive() && el.getAsJsonPrimitive().isBoolean()) ? el.getAsBoolean() : null;
        } catch (Exception e) {
            throw new JsonException("Failed to get boolean for key: " + key, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getMap(String key) {
        try {
            JsonElement el = jsonObject.get(key);
            if (el != null && el.isJsonObject()) {
                Type type = new TypeToken<Map<String, Object>>(){}.getType();
                return gson.fromJson(el, type);
            }
            return null;
        } catch (Exception e) {
            throw new JsonException("Failed to get map for key: " + key, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T get(String key, Class<T> clazz) {
        try {
            JsonElement el = jsonObject.get(key);
            return el != null ? gson.fromJson(el, clazz) : null;
        } catch (Exception e) {
            throw new JsonException("Failed to deserialize key: " + key + " into " + clazz.getSimpleName(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object get(String key) {
        return get(key, Object.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean has(String key) {
        try {
            return jsonObject.has(key);
        } catch (Exception e) {
            throw new JsonException("Failed to check presence of key: " + key, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> keySet() {
        try {
            return jsonObject.keySet();
        } catch (Exception e) {
            throw new JsonException("Failed to get key set from JSON", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> asMap() {
        try {
            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            return gson.fromJson(jsonObject, type);
        } catch (Exception e) {
            throw new JsonException("Failed to convert JSON to Map<String, Object>", e);
        }
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T deserialize(String json, Class<T> clazz) throws JsonException {
        try {
            return gson.fromJson(json, clazz);
        } catch (Exception e) {
            throw new JsonException("Failed to deserialize JSON into " + clazz.getSimpleName(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T deserialize(String json, Type typeOfT) throws JsonException {
        try {
            return gson.fromJson(json, typeOfT);
        } catch (Exception e) {
            throw new JsonException("Failed to deserialize into type " + typeOfT.getTypeName(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String serialize(Object obj) {
        return gson.toJson(obj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRawJson() {
        return jsonString;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonElement getJsonElement() {
        try {
            return gson.fromJson(jsonString, JsonElement.class);
        } catch (Exception e) {
            throw new JsonException("Failed to parse raw JSON into JsonElement", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setJsonString(String jsonString) throws JsonException {
        this.jsonString = jsonString;

        try {
            this.jsonObject = gson.fromJson(jsonString, JsonObject.class);
            if (jsonObject == null)
                throw new JsonException("JSON string did not produce a valid JsonObject (null)");
        } catch (Exception e) {
            throw new JsonException("Failed to parse raw JSON into JsonObject", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void put(String key, Object value) {
        try {
            JsonElement element = gson.toJsonTree(value);
            jsonObject.add(key, element);
            jsonString = gson.toJson(jsonObject);
        } catch (Exception e) {
            throw new JsonException("Failed to put key: " + key + " with value: " + value, e);
        }
    }
}
