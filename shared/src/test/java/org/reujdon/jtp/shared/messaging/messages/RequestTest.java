package org.reujdon.jtp.shared.messaging.messages;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reujdon.jtp.shared.messaging.MessageType;

import static org.junit.jupiter.api.Assertions.*;

class RequestTest {
    private Request request;

    @BeforeEach
    void setUp() {
        request = new Request("testCommand");
    }

    @Test
    void testInitialization() {
        assertEquals(MessageType.REQUEST, request.getType());
        assertEquals("testCommand", request.getParam("command"));
    }

    @Test
    void testConstructorWithNullCommandThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new Request(null));
    }

    @Test
    void testConstructorWithBlankCommandThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new Request("   "));
    }

    @Test
    void testSetTokenTrim() {
        request.setToken(" newToken ");
        assertEquals("newToken", request.getToken());
    }

    @Test
    void testSetTokenWithNull() {
        request.setToken(null);
        assertNull(request.getToken());
    }
}