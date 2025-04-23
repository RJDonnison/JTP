package org.reujdon.jtp.shared;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErrorTest {
    @Test
    void testInitialization() {
        Error error = new Error("Test Error");

        assertNotNull(error.getId());
        assertEquals(MessageType.ERROR, error.getType());
        assertNotNull(error.params);
        assertFalse(error.params.isEmpty());
        assertEquals(1, error.params.size());
        assertEquals("Test Error", error.getParam("message"));
    }

    @Test
    void testInitializationWithId() {
        Error error = new Error("Test", "Test Error");

        assertEquals("Test", error.getId());
        assertEquals(MessageType.ERROR, error.getType());
        assertNotNull(error.params);
        assertFalse(error.params.isEmpty());
        assertEquals(1, error.params.size());
        assertEquals("Test Error", error.getParam("message"));
    }
}