package org.reujdon.jtp.shared;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

class RequestTest {
    private Request request;

    @BeforeEach
    void setUp() {
        request = new TestRequest("testCommand", "testToken", 1000L);
    }

    @Test
    void testInitialization() {
        assertEquals(MessageType.REQUEST, request.getType());
        assertEquals("testToken", request.getToken());
        assertEquals(1000L, request.getTimeout());

        assertEquals("testCommand", request.getParam("command"));
        assertEquals("testToken", request.getParam("token"));
    }

    @Test
    void testInitializationTrimToken() {
        Request request = new TestRequest("cmd", "  tokenWithSpaces  ", 200L);
        assertEquals("tokenWithSpaces", request.getToken());
    }

    @Test
    void testInitializationWithNullToken() {
        Request nullTokenRequest = new TestRequest("cmd", null, 500L);
        assertNull(nullTokenRequest.getToken());
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, -100})
    void testInitializationWithNegativeTimeout(long invalidTimeout) {
        assertThrows(IllegalArgumentException.class, () -> new TestRequest("cmd", null, invalidTimeout));
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

    @Test
    void testAbstractMethods() {
        TestRequest testRequest = new TestRequest("cmd", 100L);

        testRequest.onSuccess(new HashMap<>());
        testRequest.onError("Test");
        testRequest.onTimeout();
    }

    // Concrete implementation for testing abstract Request class
    private static class TestRequest extends Request {
        public TestRequest(String command, String token, long timeout) {
            super(command, token, timeout);
        }

        public TestRequest(String command, long timeout) {
            super(command, timeout);
        }

        @Override
        public void onSuccess(Map<String, Object> response) {
            // Mock implementation
        }

        @Override
        public void onError(String error) {
            // Mock implementation
        }

        @Override
        public void onTimeout() {
            // Mock implementation
        }
    }
}