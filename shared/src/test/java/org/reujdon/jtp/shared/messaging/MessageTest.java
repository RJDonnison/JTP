package org.reujdon.jtp.shared.messaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.reujdon.jtp.shared.json.GsonAdapter;
import org.reujdon.jtp.shared.json.JsonAdapter;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MessageTest {
    private Message message;

    @BeforeEach
    void setUp() {
        message = new Message(MessageType.REQUEST);
    }

    @Test
    void testInitialization() {
        assertNotNull(message.getId());
        assertEquals(MessageType.REQUEST, message.getType());
        assertNotNull(message.params);
        assert(message.params.isEmpty());
    }

    @Test
    void testId() {
        Message message1 = new Message(MessageType.REQUEST);
        Message message2 = new Message(MessageType.RESPONSE);

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

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t", "\n"})
    void testAddParameterWithBlankKey(String blankKey) {
        assertThrows(IllegalArgumentException.class, () -> message.addParam(blankKey, "value"));
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
    void testGetParam() {
        message.addParam("param1", "value1");

        assertEquals( "value1", message.getParam("param1"));
    }

    @Test
    void testGetParamDefault() {
        message.addParam("param1", "value1");

        assertNull( message.getParam("param2", null));
    }

    @Test
    void testAddParamsMap() {
        Map<String, Object> input = new HashMap<>();
        input.put("key1", "value1");
        input.put("key2", 42);
        message.addParams(input);

        assertEquals("value1", message.getParam("key1"));
        assertEquals(42, message.getParam("key2"));
    }

    @Test
    void testAddParamsMapNull() {
        message.addParams((Map<String, ?>) null);
        assertTrue(message.getParams().isEmpty());
    }

    @Test
    void testAddParamsJsonAdapter() {
        JsonAdapter mockAdapter = new GsonAdapter();
        mockAdapter.put("key1", "value1");
        mockAdapter.put("key2", 123);

        message.addParams(mockAdapter);

        assertEquals("value1", message.getParam("key1"));
        assertEquals(123.0, message.getParam("key2"));
    }

    @Test
    void testAddParamsFromJsonAdapterNull() {
        message.addParams((JsonAdapter) null);
        assertTrue(message.getParams().isEmpty());
    }

    @Test
    void testContainsParam() {
        message.addParam("param1", "value1");

        assertTrue(message.containsParam("param1"));
        assertFalse(message.containsParam("param2"));
        assertFalse(message.containsParam(null));
        assertFalse(message.containsParam(""));
    }
}