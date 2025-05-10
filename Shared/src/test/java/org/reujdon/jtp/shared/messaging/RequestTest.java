package org.reujdon.jtp.shared.messaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reujdon.jtp.shared.messaging.messages.Request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RequestTest {
    private Request request;

    @BeforeEach
    void setUp() {
        request = new TestRequest("testCommand");
    }

    @Test
    void testInitialization() {
        assertEquals(MessageType.REQUEST, request.getType());

        assertEquals("testCommand", request.getParam("command"));
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

    // Concrete implementation for testing abstract Request class
    private static class TestRequest extends Request {
        public TestRequest(String command) {
            super(command);
        }
    }
}