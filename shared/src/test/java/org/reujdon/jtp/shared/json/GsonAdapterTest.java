package org.reujdon.jtp.shared.json;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GsonAdapterTest {
    private GsonAdapter adapter;
    private static final String TEST_JSON = """
                {
                  "name": "Dave",
                  "age": 124,
                  "active": true,
                  "score": 99.5,
                  "longValue": 1234567890123,
                  "role": "ADMIN",
                  "address": { "city": "Wellington", "zip": "6011" }
                }
                """;

    @BeforeEach
    void setUp() {
        adapter = new GsonAdapter(TEST_JSON);
    }

    @Test
    void testGetString() {
        assertEquals("Dave", adapter.getString("name"));
    }

    @Test
    void testGetInt() {
        assertEquals(124, adapter.getInt("age"));
    }

    @Test
    void testGetBoolean() {
        assertTrue(adapter.getBoolean("active"));
    }

    @Test
    void testGetDouble() {
        assertEquals(99.5, adapter.getDouble("score"));
    }

    @Test
    void testGetLong() {
        assertEquals(1234567890123L, adapter.getLong("longValue"));
    }

    enum Role { USER, ADMIN }

    @Test
    void testGetEnum() {
        assertEquals(Role.ADMIN, adapter.getEnum("role", Role.class));
    }

    @Test
    void testGetMap() {
        Map<String, Object> address = adapter.getMap("address");
        assertNotNull(address);
        assertEquals("Wellington", address.get("city"));
    }

    @Test
    void testHasAndKeySet() {
        assertTrue(adapter.has("name"));
        Set<String> keys = adapter.keySet();
        assertTrue(keys.contains("name"));
        assertTrue(keys.contains("age"));
    }

    @Test
    void testAsMap() {
        Map<String, Object> map = adapter.asMap();
        assertNotNull(map);
        assertEquals(124.0, map.get("age"));
    }

    @Test
    void testPutAndGet() {
        adapter.put("newKey", "newValue");
        assertEquals("newValue", adapter.getString("newKey"));
    }

    @Test
    void testSerializeDeserialize() {
        TestObject obj = new TestObject("test", 42);
        String json = adapter.serialize(obj);
        TestObject deserialized = adapter.deserialize(json, TestObject.class);
        assertEquals("test", deserialized.getName());
        assertEquals(42, deserialized.getValue());
    }

    @Test
    void testConstructorWithInvalidJson() {
        assertThrows(JsonException.class, () -> new GsonAdapter("{invalid}"));
    }

    @Test
    void testSetInvalidJsonString() {
        GsonAdapter adapter = new GsonAdapter();
        assertThrows(JsonException.class, () -> adapter.setJsonString("{invalid}"));
    }

    @Test
    void testGetStringWithInvalidKey() {
        assertNull(adapter.getString("nonexistent"));
    }

    @Test
    void testGetStringWithNonStringValue() {
        adapter.put("numberKey", 123);
        assertNull(adapter.getString("numberKey"));
    }

    @Test
    void testGetIntWithNonNumberValue() {
        adapter.put("stringKey", "not a number");
        assertNull(adapter.getInt("stringKey"));
    }

    @Test
    void testGetEnumWithInvalidValue() {
        assertThrows(JsonException.class, () ->
                adapter.getEnum("name", Role.class)); // "Dave" is not a Role value
    }

    @Test
    void testGetEnumWithNullValue() {
        assertNull(adapter.getEnum("nonexistent", Role.class));
    }

    @Test
    void testGetJsonElement() {
        JsonElement element = adapter.getJsonElement();
        assertNotNull(element);
        assertTrue(element.isJsonObject());
        assertEquals("Dave", element.getAsJsonObject().get("name").getAsString());
    }

    @Test
    void testGetRawJson() {
        String rawJson = adapter.getRawJson();
        assertNotNull(rawJson);
        assertTrue(rawJson.contains("Dave"));
        assertTrue(rawJson.contains("124"));
    }

    @Test
    void testDeserializeWithTypeToken() {
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> result = adapter.deserialize(TEST_JSON, type);
        assertNotNull(result);
        assertEquals("Dave", result.get("name"));
    }

    @Test
    void testDeserializeWithInvalidJson() {
        assertThrows(JsonException.class, () ->
                adapter.deserialize("{invalid}", TestObject.class));
    }

    @Test
    void testDeserializeWithIncompatibleType() {
        String json = "{\"name\":\"Dave\"}";
        assertThrows(JsonException.class, () ->
                adapter.deserialize(json, Integer.class));
    }

    @Test
    void testGetWithNonExistentKey() {
        assertNull(adapter.get("nonexistent"));
    }

    @Test
    void testGetWithSpecificClass() {
        String name = adapter.get("name", String.class);
        assertEquals("Dave", name);
    }

    @Test
    void testEmptyJson() throws JsonException {
        GsonAdapter emptyAdapter = new GsonAdapter("{}");
        assertNull(emptyAdapter.getString("any"));
        assertEquals(0, emptyAdapter.keySet().size());
    }

    @Test
    void testNullValues() {
        adapter.put("nullKey", null);
        assertNull(adapter.getString("nullKey"));
        assertNull(adapter.getInt("nullKey"));
        assertNull(adapter.get("nullKey"));
    }

    @Test
    void testPutComplexObject() {
        TestObject obj = new TestObject("complex", 100);
        adapter.put("complexObj", obj);
        TestObject retrieved = adapter.get("complexObj", TestObject.class);
        assertEquals("complex", retrieved.getName());
    }

    @Test
    void testGetJsonElementAfterModification() {
        adapter.put("newField", "value");
        JsonElement element = adapter.getJsonElement();
        assertTrue(element.getAsJsonObject().has("newField"));
    }

    private static class TestObject {
        private String name;
        private int value;

        public TestObject() {} // Required for Gson

        public TestObject(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() { return name; }
        public int getValue() { return value; }
    }
}