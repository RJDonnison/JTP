package org.reujdon.jtp.shared;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResponseTest {
    private Response response;

    @BeforeEach
    void setUp() {
        response = new Response("1");
    }

    @Test
    void testInitialization() {
        assertNotNull(response.getId());
        assertEquals(MessageType.RESPONSE, response.getType());
        assertNotNull(response.params);
        assertTrue(response.params.isEmpty());
    }

    @Test
    void testAddParams() {
        JSONObject jsonParams = new JSONObject();
        jsonParams.put("key1", "value1");
        jsonParams.put("key2", 123);

        response.addParams(jsonParams);

        assertEquals(2, response.params.size());
        assertEquals("value1", response.params.get("key1"));
        assertEquals(123, response.params.get("key2"));
    }

    @Test
    void testAddNullParams() {
        response.addParams(null);

        assertEquals(0, response.params.size());
    }
}