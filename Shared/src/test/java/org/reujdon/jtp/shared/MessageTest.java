package org.reujdon.jtp.shared;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class MessageTest {
    private Message message;

    @BeforeEach
    void setUp() {
        message = new TestMessage(MessageType.REQUEST);
    }

    @Test
    void testInitialization() {
        assertNotNull(message.getId());
        assertEquals(MessageType.REQUEST, message.getType());
        assertNotNull(message.params);
        assert(message.params.isEmpty());
    }

    @Test
    void testIdIncrement() {
        Message message1 = new TestMessage(MessageType.REQUEST);
        Message message2 = new TestMessage(MessageType.RESPONSE);

        assertNotEquals(message1.getId(), message2.getId());
    }

    @Test
    void testAddParameter() {
        message.addParam("key1", "value1");
        message.addParam("key2", 123);
        message.addParam("key3", true);

        assertEquals("value1", message.getParam("key1"));
        assertEquals(123, message.getParam("key2"));
        assertEquals(true, message.getParam("key3"));
    }

    @Test
    void testAddDuplicateParameter() {
        message.addParam("key", "value");
        assertThrows(IllegalArgumentException.class, () -> message.addParam("key", "another-value"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t", "\n"})
    void testAddParameterWithBlankKey(String blankKey) {
        assertThrows(IllegalArgumentException.class, () -> message.addParam(blankKey, "value"));
    }

    @Test
    void testSetParameter() {
        message.addParam("key", "initialValue");
        message.setParam("key", "updatedValue");

        assertEquals("updatedValue", message.getParam("key"));
    }

    @Test
    void testSetParameterWithNonExistentKey() {
        assertThrows(IllegalArgumentException.class, () -> message.setParam("nonexistent", "value"));
    }

    @Test
    void testRemoveParameter() {
        message.addParam("key1", "value1");
        message.addParam("key2", "value2");

        message.removeParam("key1");

        assertFalse(message.params.containsKey("key1"));
        assertTrue(message.params.containsKey("key2"));
        assertEquals(1, message.params.size());
    }

    @Test
    void testRemoveNonExistentParameter() {
        assertDoesNotThrow(() -> message.removeParam("nonexistent"));
    }

    @Test
    void testToJSONStructure() {
        message.addParam("param1", "value1");
        message.addParam("param2", 42);

        JSONObject json = message.toJSON();

        assertEquals(3, json.length());

        assertEquals(MessageType.REQUEST, json.getEnum(MessageType.class, "type"));

        assertNotNull(json.get("id"));
        assertEquals(message.getId(), json.get("id"));

        JSONObject params = json.getJSONObject("params");
        assertEquals(2, params.length());
        assertEquals("value1", params.get("param1"));
        assertEquals(42, params.get("param2"));
    }

    @Test
    void testEmptyMessageToJSON() {
        JSONObject json = message.toJSON();

        assertEquals(2, json.length()); // Only type and id
        assertFalse(json.has("params"));
    }

    @Test
    void testDifferentMessageTypes() {
        Message request = new TestMessage(MessageType.REQUEST);
        Message response = new TestMessage(MessageType.RESPONSE);
        Message error = new TestMessage(MessageType.ERROR);

        assertEquals(MessageType.REQUEST, request.getType());
        assertEquals(MessageType.RESPONSE, response.getType());
        assertEquals(MessageType.ERROR, error.getType());

        assertEquals(MessageType.REQUEST, request.toJSON().getEnum(MessageType.class, "type"));
        assertEquals(MessageType.RESPONSE, response.toJSON().getEnum(MessageType.class, "type"));
        assertEquals(MessageType.ERROR, error.toJSON().getEnum(MessageType.class, "type"));
    }

    @Test
    void testGetParamsDefault() {
        message.addParam("param1", "value1");

        assertNull( message.getParam("param2", null));
    }

    // Concrete implementation for testing abstract Message class
    private static class TestMessage extends Message {
        public TestMessage(MessageType type) {
            super(type);
        }
    }
}