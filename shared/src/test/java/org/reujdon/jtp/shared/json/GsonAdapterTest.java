package org.reujdon.jtp.shared.json;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GsonAdapterTest {
    private GsonAdapter adapter;

    @BeforeEach
    void setUp() {
        String json = """
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
        adapter = new GsonAdapter(json);
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