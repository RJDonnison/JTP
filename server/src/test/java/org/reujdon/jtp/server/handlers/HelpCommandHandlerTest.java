package org.reujdon.jtp.server.handlers;

import org.junit.jupiter.api.Test;
import org.reujdon.jtp.shared.Permission;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HelpCommandHandlerTest {
    private final HelpCommandHandler handler = new HelpCommandHandler();

    @Test
    void testPermission() {
        assertEquals(Permission.NONE, handler.requiredPermission());
    }
}