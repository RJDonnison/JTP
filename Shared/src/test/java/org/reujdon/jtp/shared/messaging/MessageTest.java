package org.reujdon.jtp.shared.messaging;

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