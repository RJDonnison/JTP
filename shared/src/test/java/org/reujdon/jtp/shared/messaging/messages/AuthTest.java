package org.reujdon.jtp.shared.messaging.messages;

import org.junit.jupiter.api.Test;
import org.reujdon.jtp.shared.messaging.MessageType;

import static org.junit.jupiter.api.Assertions.*;

class AuthTest {
    @Test
    void testConstructorInitializesCorrectly() {
        String key = "secretKey123";
        Auth auth = new Auth(key);

        assertEquals(MessageType.AUTH, auth.getType());
        assertEquals("*", auth.getId());
        assertEquals(key, auth.getKey());
        assertFalse(auth.getSuccess());
    }

    @Test
    void testSetAndGetToken() {
        Auth auth = new Auth("initKey");
        assertNull(auth.getToken());

        auth.setToken("user-token");
        assertEquals("user-token", auth.getToken());
    }

    @Test
    void testSuccessMethodSetsSuccessTrue() {
        Auth auth = new Auth("initKey");
        assertFalse(auth.getSuccess());

        auth.success();
        assertTrue(auth.getSuccess());
    }

    @Test
    void testGetKeyReturnsCorrectValue() {
        Auth auth = new Auth("abc123");
        assertEquals("abc123", auth.getKey());
    }

    @Test
    void testSetTokenToNull() {
        Auth auth = new Auth("abc123");
        auth.setToken(null);
        assertNull(auth.getToken());
    }
}