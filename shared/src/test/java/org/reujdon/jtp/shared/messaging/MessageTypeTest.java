package org.reujdon.jtp.shared.messaging;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MessageTypeTest {
    @Test
    void testFromStringValidValues() {
        assertEquals(MessageType.REQUEST, MessageType.fromString("request"));
        assertEquals(MessageType.RESPONSE, MessageType.fromString("RESPONSE"));
        assertEquals(MessageType.ERROR, MessageType.fromString("Error"));
        assertEquals(MessageType.AUTH, MessageType.fromString("auth"));
    }

    @Test
    void testFromStringInvalidValue() {
        assertThrows(IllegalArgumentException.class, () -> MessageType.fromString("not-a-type"));
    }

    @Test
    void testFromStringEmptyString() {
        assertThrows(IllegalArgumentException.class, () -> MessageType.fromString(""));
    }

    @Test
    void testFromStringNullValue() {
        assertThrows(IllegalArgumentException.class, () -> MessageType.fromString(null));
    }
}