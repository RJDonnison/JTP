package org.reujdon.jtp.shared.messaging.messages;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reujdon.jtp.shared.json.GsonAdapter;
import org.reujdon.jtp.shared.json.JsonAdapter;
import org.reujdon.jtp.shared.messaging.MessageType;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ResponseTest {
    JsonAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new GsonAdapter();
    }

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
    void testConstructorWithIdAndData() {
        String id = "response-id";

        adapter.put("username", "alice");
        adapter.put("status", "ok");

        Response response = new Response(id, adapter);
        assertEquals(id, response.getId());
        assertEquals("alice", response.getParam("username"));
        assertEquals("ok", response.getParam("status"));
    }

    @Test
    void testConstructorWithJsonDataOnly() {
        adapter.put("username", "bob");
        adapter.put("active", true);

        Response response = new Response(adapter);
        assertEquals(MessageType.RESPONSE, response.getType());
        assertEquals("bob", response.getParam("username"));
        assertEquals(true, response.getParam("active"));
    }

    @Test
    void testConstructorWithMapDataOnly() {
        Map<String, Object> data = new HashMap<>();
        data.put("result", 42);
        data.put("success", true);

        Response response = new Response(data);
        assertEquals(42, response.getParam("result"));
        assertEquals(true, response.getParam("success"));
    }

    @Test
    void testConstructorWithMapDataAndId() {
        String id = "response-id";
        Map<String, Object> data = new HashMap<>();
        data.put("result", 42);
        data.put("success", true);

        Response response = new Response(id, data);
        assertEquals(id, response.getId());
        assertEquals(42, response.getParam("result"));
        assertEquals(true, response.getParam("success"));
    }

    @Test
    void testConstructorWithNullJsonAdapter() {
        assertDoesNotThrow(() -> new Response((JsonAdapter) null));
    }

    @Test
    void testConstructorWithNullMap() {
        assertDoesNotThrow(() -> new Response((Map<String, ?>) null));
    }

    @Test
    void testConstructorWithNullIdThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new Response((String) null));
    }

    @Test
    void testConstructorWithEmptyIdThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new Response("   "));
    }
}