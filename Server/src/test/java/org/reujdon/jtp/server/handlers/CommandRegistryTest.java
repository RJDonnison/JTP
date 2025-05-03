package org.reujdon.jtp.server.handlers;

import jdk.jfr.Description;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.reujdon.jtp.server.CommandHandler;
import org.reujdon.jtp.server.CommandRegistry;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CommandRegistryTest {
//    TODO: add final base commands
    @Test
    void testBaseCommandsAdded(){
        assertInstanceOf(HelpCommandHandler.class, CommandRegistry.getHandler("Help"));
    }

    @Test
    void testGetHandlerInvalidCommandThrows() {
        assertThrows(IllegalArgumentException.class, () -> CommandRegistry.getHandler(null));
        assertThrows(IllegalArgumentException.class, () -> CommandRegistry.getHandler(""));
        assertThrows(IllegalArgumentException.class, () -> CommandRegistry.getHandler("   "));
    }

    @Test
    void testGetHandlerReturnsNullIfNotFound() {
        assertNull(CommandRegistry.getHandler("nonexistent"));
    }

    @Test
    void testRegister(){
        CommandHandler handler = new TestCommandHandler();
        CommandRegistry.register("custom", handler, true);

        assertEquals(handler, CommandRegistry.getHandler("custom"));
        assertNotEquals(handler, CommandRegistry.getHandler("CUSTOM"));
        assertEquals(handler, CommandRegistry.getHandler("  custom  "));
    }

    @Test
    void testRegisterNullArgsThrows(){
        CommandHandler handler = new TestCommandHandler();
        CommandRegistry.register("x", handler, false);
        assertEquals(handler, CommandRegistry.getHandler("x"));
    }

    @Test
    void testRegisterFalseOverrideThrows() {
        CommandHandler first = new TestCommandHandler();
        CommandHandler second = new TestCommandHandler();
        CommandRegistry.register("overrideMe", first, false);
        assertThrows(IllegalArgumentException.class, () -> CommandRegistry.register("overrideMe", second, false));
    }

    @Test
    void testRegisterWithDescriptionlessHandlerThrows() {
        CommandHandler handler = new DescriptionlessCommandHandler();
        assertThrows(RuntimeException.class, () -> CommandRegistry.register("myCommand", handler, false));
    }

    @Test
    void testGetDescription(){
        CommandHandler handler = new TestCommandHandler();
        CommandRegistry.register("myCommand", handler, true);

        assertEquals("Command for testing", CommandRegistry.getDescription("myCommand"));
        assertEquals("", CommandRegistry.getDescription("nonexistent"));
    }

    @Test
    void testGetDescriptionInvalidArgsThrows(){
        assertThrows(IllegalArgumentException.class, () -> CommandRegistry.getHandler(""));
        assertThrows(IllegalArgumentException.class, () -> CommandRegistry.getHandler("    "));
        assertThrows(IllegalArgumentException.class, () -> CommandRegistry.getHandler(null));
    }

    @Test
    void testGetDescriptions(){
        CommandHandler handler = new TestCommandHandler();
        CommandRegistry.register("myCommand", handler, true);

        Map<String, String> descriptions = CommandRegistry.getDescriptions();

        assertFalse(descriptions.isEmpty());
        assertEquals("Command for testing", descriptions.get("myCommand"));
    }
}

@Description("Command for testing")
class TestCommandHandler implements CommandHandler {
    @Override
    public JSONObject handle(Map<String, Object> params) {
        return null;
    }
}

class DescriptionlessCommandHandler implements CommandHandler {
    @Override
    public JSONObject handle(Map<String, Object> params) {
        return null;
    }
}