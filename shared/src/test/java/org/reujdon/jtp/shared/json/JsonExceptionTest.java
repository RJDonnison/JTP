package org.reujdon.jtp.shared.json;

import com.google.gson.JsonParseException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonExceptionTest {
    @Test
    void testConstructorWithMessage() {
        String message = "Test error message";

        JsonException exception = new JsonException(message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String message = "Test error with cause";
        Throwable cause = new JsonParseException("Invalid JSON");

        JsonException exception = new JsonException(message, cause);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals("Invalid JSON", exception.getCause().getMessage());
    }

    @Test
    void testExceptionChaining() {
        String rootCauseMessage = "Root cause";
        String intermediateMessage = "Intermediate";
        String topLevelMessage = "Top level";

        JsonException root = new JsonException(rootCauseMessage);
        JsonException intermediate = new JsonException(intermediateMessage, root);
        JsonException topLevel = new JsonException(topLevelMessage, intermediate);

        assertEquals(topLevelMessage, topLevel.getMessage());
        assertEquals(intermediate, topLevel.getCause());
        assertEquals(intermediateMessage, topLevel.getCause().getMessage());
        assertEquals(root, topLevel.getCause().getCause());
        assertEquals(rootCauseMessage, topLevel.getCause().getCause().getMessage());
    }

    @Test
    void testWithNullMessage() {
        JsonException exception = new JsonException(null);

        assertNull(exception.getMessage());
    }

    @Test
    void testWithNullCause() {
        JsonException exception = new JsonException("Message", null);

        assertEquals("Message", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testWithNullMessageAndCause() {
        JsonException exception = new JsonException(null, null);

        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testStackTracePreservation() {
        String message = "Stack trace test";
        Exception cause = new Exception("Cause exception");

        try {
            throw new JsonException(message, cause);
        } catch (JsonException e) {
            assertEquals(message, e.getMessage());
            assertEquals(cause, e.getCause());
            assertTrue(e.getStackTrace().length > 0);
            assertTrue(e.getCause().getStackTrace().length > 0);
        }
    }

    @Test
    void testToString() {
        String message = "ToString test";
        JsonException exception = new JsonException(message);

        String toStringResult = exception.toString();

        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("JsonException"));
        assertTrue(toStringResult.contains(message));
    }
}