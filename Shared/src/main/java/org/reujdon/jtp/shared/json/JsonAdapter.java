package org.reujdon.jtp.shared.json;

import com.google.gson.JsonElement;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

/**
 * An abstraction over JSON operations, allowing consistent access and manipulation
 * of JSON data regardless of the underlying library.
 */
public interface JsonAdapter {
    /**
     * Gets the string value associated with the specified key.
     *
     * @param key the key to look up
     * @return the string value, or null if not found or not a string
     */
    String getString(String key);

    /**
     * Gets the integer value associated with the specified key.
     *
     * @param key the key to look up
     * @return the integer value, or null if not found or not a number
     */
    Integer getInt(String key);


    /**
     * Gets the long value associated with the specified key.
     *
     * @param key the key to look up
     * @return the long value, or null if not found or not a number
     */
    Long getLong(String key);

    /**
     * Gets the double value associated with the specified key.
     *
     * @param key the key to look up
     * @return the double value, or null if not found or not a number
     */
    Double getDouble(String key);


    /**
     * Gets the boolean value associated with the specified key.
     *
     * @param key the key to look up
     * @return the boolean value, or null if not found or not a boolean
     */
    Boolean getBoolean(String key);

    /**
     * Gets a JSON object as a map associated with the specified key.
     *
     * @param key the key to look up
     * @return a map representation of the JSON object, or null if not found or not an object
     */
    Map<String, Object> getMap(String key);

    /**
     * Deserializes the value associated with the key into the given class type.
     *
     * @param key   the key to look up
     * @param clazz the class to deserialize into
     * @return the deserialized object, or null if the key is missing
     */
    <T> T get(String key, Class<T> clazz);

    /**
     * Gets the raw value associated with the specified key.
     *
     * @param key the key to look up
     * @return the object value, or null if not found
     */
    Object get(String key);

    /**
     * Checks whether the JSON object contains the given key.
     *
     * @param key the key to check
     * @return true if the key exists, false otherwise
     */
    boolean has(String key);

    /**
     * Returns the set of keys in the JSON object.
     *
     * @return a set of keys
     */
    Set<String> keySet();

    /**
     * Converts the entire JSON object to a map.
     *
     * @return a map representation of the JSON object
     */
    Map<String, Object> asMap();

    /**
     * Retrieves the value as an enum constant.
     *
     * @param key       the key to look up
     * @param enumClass the enum class
     * @param <E>       the enum type
     * @return the enum constant, or null if not found or invalid
     */
    <E extends Enum<E>> E getEnum(String key, Class<E> enumClass);

    /**
     * Deserializes a JSON string into the specified class.
     *
     * @param json  the JSON string
     * @param clazz the target class
     * @param <T>   the type of the result
     * @return the deserialized object
     */
    <T> T deserialize(String json, Class<T> clazz);

    /**
     * Deserializes a JSON string into the specified type (useful for generics).
     *
     * @param json     the JSON string
     * @param typeOfT  the target type
     * @param <T>      the type of the result
     * @return the deserialized object
     */
    <T> T deserialize(String json, Type typeOfT);

    /**
     * Serializes an object to a JSON string.
     *
     * @param obj the object to serialize
     * @return the resulting JSON string
     */
    String serialize(Object obj);

    /**
     * Returns the raw JSON string associated with this adapter.
     *
     * @return the raw JSON string
     */
    String getRawJson();

    /**
     * Returns the root {@link JsonElement} for the parsed JSON.
     *
     * @return the parsed JSON element
     */
    JsonElement getJsonElement();

    /**
     * Sets the raw JSON string for this adapter to work with.
     *
     * @param json the JSON string
     */
    void setJsonString(String json);
}