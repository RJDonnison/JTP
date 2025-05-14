package org.reujdon.jtp.shared.messaging.messages;

import org.junit.jupiter.api.Test;
import org.reujdon.jtp.shared.messaging.MessageType;

import static org.junit.jupiter.api.Assertions.*;

class ResponseTest {
    @Test
    void testConstructor() {
        Response response = new Response("1");
        assertEquals(MessageType.RESPONSE, response.getType());
        assertEquals("1", response.getId());

        response = new Response();

        assertEquals(MessageType.RESPONSE, response.getType());
        assertNotNull(response.getId());
    }

    @Test
    void testConstructorWithNullIdThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new Response(null));
    }

    @Test
    void testConstructorWithEmptyIdThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new Response("   "));
    }
}