package org.reujdon.jtp.shared;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResponseTest {
    @Test
    void testInitialization() {
        Response response = new Response();

        assertNotNull(response.getId());
        assertEquals(MessageType.RESPONSE, response.getType());
        assertNotNull(response.params);
        assertTrue(response.params.isEmpty());
    }
}