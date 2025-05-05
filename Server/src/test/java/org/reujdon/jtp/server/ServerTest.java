package org.reujdon.jtp.server;

import jdk.jfr.Description;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reujdon.jtp.shared.messaging.Response;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

//TODO: fix
class ServerTest {
    private Server server;
    private CommandHandler handler;

    @BeforeEach
    void setup() {
        server = new Server(0);
        handler = new TestCommandHandler();
    }

    @Test
    void testInitializationPortOutOfRangeThrows(){
        assertThrows(IllegalArgumentException.class, () -> new Server(-1));
        assertThrows(IllegalArgumentException.class, () -> new Server(65536));

        assertDoesNotThrow(() -> new Server(0));
        assertDoesNotThrow(() -> new Server(65535));
    }

    @Test
    void testRemoveClientUnknowIDThrows() {
        Server server = new Server();

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