package org.reujdon.jtp.server;

import jdk.jfr.Description;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reujdon.jtp.shared.messaging.Response;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

//TODO: fix
class JTPServerTest {
    private JTPServer server;
    private CommandHandler handler;

    @BeforeEach
    void setup() {
        server = new JTPServer(0);
        handler = new TestCommandHandler();
    }

    @Test
    void testInitializationPortOutOfRangeThrows(){
        assertThrows(IllegalArgumentException.class, () -> new JTPServer(-1));
        assertThrows(IllegalArgumentException.class, () -> new JTPServer(65536));

        assertDoesNotThrow(() -> new JTPServer(0));
        assertDoesNotThrow(() -> new JTPServer(65535));
    }

    @Test
    void testRemoveClientUnknowIDThrows() {
        JTPServer server = new JTPServer();

        assertThrows(IllegalArgumentException.class, () -> server.removeClient(null));
        assertThrows(IllegalArgumentException.class, () -> server.removeClient(""));
        assertThrows(IllegalArgumentException.class, () -> server.removeClient("     "));
    }

    @Test
    void testAddCommandSuccessfullyHandler() {
        assertDoesNotThrow(() -> server.addCommand("ping", handler));
    }

    @Test
    void testAddCommandHandlerInvalidCommandThrows() {
        assertThrows(IllegalArgumentException.class, () -> server.addCommand(null, handler));
        assertThrows(IllegalArgumentException.class, () -> server.addCommand("   ", handler));
    }
}

@Description("Command for testing")
class TestCommandHandler implements CommandHandler {
    @Override
    public Response handle(Map<String, Object> params) {
        return null;
    }
}