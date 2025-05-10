package org.reujdon.jtp.shared.json;

import com.google.gson.JsonElement;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

public interface JsonAdapter {
    String getString(String key);
    Integer getInt(String key);
    Long getLong(String key);
    Double getDouble(String key);
    Boolean getBoolean(String key);
    Map<String, Object> getMap(String key);

    <T> T get(String key, Class<T> clazz);
    Object get(String key);
    boolean has(String key);

    Set<String> keySet();
    Map<String, Object> asMap();

    <E extends Enum<E>> E getEnum(String key, Class<E> enumClass);

    <T> T deserialize(String json, Class<T> clazz);
    <T> T deserialize(String json, Type typeOfT);
    String serialize(Object obj);

    String extractType(String json); // get "type" field
    String getRawJson();
    JsonElement getJsonElement();

    void setJsonString(String json);
}