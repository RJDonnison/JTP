package org.reujdon.jtp.shared.messaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reujdon.jtp.shared.json.GsonAdapter;
import org.reujdon.jtp.shared.json.JsonAdapter;
import org.reujdon.jtp.shared.messaging.messages.Auth;
import org.reujdon.jtp.shared.messaging.messages.Error;
import org.reujdon.jtp.shared.messaging.messages.Request;
import org.reujdon.jtp.shared.messaging.messages.Response;

import static org.junit.jupiter.api.Assertions.*;

class MessageFactoryTest {
    private JsonAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new GsonAdapter();
    }

    @Test
    void testDeserializeValidRequestMessage() {
        Request request = new Request("test");
        request.addParam("testKey", "testValue");

        String json = adapter.serialize(request);
        Message message = MessageFactory.deserialize(json);

        assertInstanceOf(Message.class, message);
        assertEquals("testValue", message.getParam("testKey"));
        assertEquals(MessageType.REQUEST, message.getType());
    }

    @Test
    void testDeserializeValidResponseMessage() {
        Response response = new Response();
        response.addParam("responseKey", 123);

        String json = adapter.serialize(response);
        Message message = MessageFactory.deserialize(json);

        assertInstanceOf(Response.class, message);
        assertEquals(123.0, message.getParam("responseKey"));
        assertEquals(MessageType.RESPONSE, message.getType());
    }

    @Test
    void testDeserializeValidErrorMessage() {
        Error error = new Error("message");
        error.addParam("key", 123);

        String json = adapter.serialize(error);
        Message message = MessageFactory.deserialize(json);

        assertInstanceOf(Error.class, message);
        assertEquals(123.0, message.getParam("key"));
        assertEquals(MessageType.ERROR, message.getType());
    }

    @Test
    void testDeserializeValidAuthMessage() {
        Auth auth = new Auth("test");
        auth.addParam("key", 123);

        String json = adapter.serialize(auth);
        Message message = MessageFactory.deserialize(json);

        assertInstanceOf(Auth.class, message);
        assertEquals(123.0, message.getParam("key"));
        assertEquals(MessageType.AUTH, message.getType());
    }

    @Test
    void testDeserializeUnknownTypeThrowsException() {
        String invalidJson = "{\"type\":\"NON_EXISTENT\",\"data\":\"whatever\"}";

        assertThrows(IllegalArgumentException.class, () -> MessageFactory.deserialize(invalidJson));
    }

    @Test
    void testDeserializeWithBlankJsonReturnsNull() {
        assertNull(MessageFactory.deserialize(""));
        assertNull(MessageFactory.deserialize("   "));
        assertNull(MessageFactory.deserialize(null));
    }

    @Test
    void testDeserializeWhenNotRegisteredThrows() {
        // Temporarily unregister a type to simulate the error
        MessageFactory.register(MessageType.REQUEST, null);
        String json = "{\"type\":\"REQUEST\",\"param\":\"data\"}";

        assertThrows(IllegalArgumentException.class, () -> MessageFactory.deserialize(json));

        // Re-register to avoid side effects
        MessageFactory.register(MessageType.REQUEST, Request.class);
    }
}