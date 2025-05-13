package org.reujdon.jtp.shared.messaging.messages;

import org.junit.jupiter.api.Test;
import org.reujdon.jtp.shared.messaging.MessageType;

import static org.junit.jupiter.api.Assertions.*;

class ErrorTest {
    @Test
    void testInitialization() {
        String errorMessage = "Something went wrong";

        Error error = new Error(errorMessage);

        assertEquals(MessageType.ERROR, error.getType());
        assertNotNull(error.getId());
        assertFalse(error.getId().isBlank());
        assertEquals(errorMessage, error.getParam("message"));
    }

    @Test
    void testInitializationWithId() {
        String id = "123e4567-e89b-12d3-a456-426614174000";
        String errorMessage = "Invalid authentication token";

        Error error = new Error(id, errorMessage);

        assertEquals(MessageType.ERROR, error.getType());
        assertEquals(id, error.getId());
        assertEquals(errorMessage, error.getParam("message"));
    }

    @Test
    void testConstructorWithNullMessageThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new Error(null));
    }

    @Test
    void testConstructorWithEmptyMessageThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new Error(""));
    }

    @Test
    void testConstructorWithIdAndEmptyMessageThrowsException() {
        String id = "123e4567-e89b-12d3-a456-426614174000";

        assertThrows(IllegalArgumentException.class, () -> new Error(id, "  "));
    }
}