package org.reujdon.jtp.server;

import jdk.jfr.Description;
import org.reujdon.jtp.server.handlers.HelpCommandHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * A registry for managing command handlers in the transfer protocol.
 * This class maintains a mapping between command strings and their corresponding
 * {@link CommandHandler} implementations.
 *
 * @see CommandHandler
 */
public class CommandRegistry {
    private static final Map<String, CommandHandler> handlers = new HashMap<>();

//    Base command initialization
    static {
        handlers.put("Help", new HelpCommandHandler());
        validateDescriptions();
    }

    /**
     * Retrieves the command handler for the specified command.
     * If no handler is found for the command, returns null.
     *
     * @param command the command to look up
     * @return the registered CommandHandler, or null if not found
     * @throws IllegalArgumentException if command is null or empty
     */
    public static CommandHandler getHandler(String command) {
        if (command == null || command.trim().isEmpty())
            throw new IllegalArgumentException("Command cannot be null or empty");

        command = command.trim();

        if (!handlers.containsKey(command))
            return null;

        return handlers.get(command);
    }

    /**
     * Gets the description of a specific command based on the {@link Description} annotation.
     *
     * @param command the command to describe
     * @return the description or an empty string if not found or missing
     * @throws IllegalArgumentException if command is null or empty
     */
    public static String getDescription(String command) {
        CommandHandler handler = getHandler(command);
        if (handler == null) return "";

        Description desc = handler.getClass().getAnnotation(Description.class);
        if (desc != null) return desc.value();

        return "";
    }

    /**
     * Gets the descriptions of all registered commands.
     *
     * @return map of command -> description
     */
    public static Map<String, String> getDescriptions() {
        Map<String, String> descriptions = new HashMap<>();
        for (String command : handlers.keySet())
            descriptions.put(command, getDescription(command));

        return descriptions;
    }

    /**
     * Registers a new command handler or replaces an existing one.
     *
     * <p>Command registration follows these rules:</p>
     * <ul>
     *   <li>Commands are stored in lowercase for case-insensitive matching</li>
     *   <li>Existing commands can only be overwritten if override=true</li>
     *   <li>Null/empty commands or null handlers are rejected</li>
     * </ul>
     *
     * @param command the command to register
     * @param handler the handler to execute for this command
     * @param override if true, allows overwriting existing commands
     * @throws IllegalArgumentException if:
     *         <ul>
     *           <li>command is null or empty</li>
     *           <li>handler is null</li>
     *           <li>command exists and override=false</li>
     *         </ul>
     * @throws RuntimeException if handler is missing @Description
     */
    public static void register(String command, CommandHandler handler, boolean override) {
        if (command == null || command.trim().isEmpty())
            throw new IllegalArgumentException("Command cannot be null or empty");

        if (handler == null)
            throw new IllegalArgumentException("Handler cannot be null");

        command = command.trim();

        if (!override && handlers.containsKey(command))
            throw new IllegalArgumentException("Command " + command + " already exists");

        validateDescription(handler.getClass());

        handlers.put(command, handler);
    }

    /**
     * Validates that all registered {@link CommandHandler} implementations
     * are annotated with {@link Description}.
     */
    private static void validateDescriptions() {
        for (CommandHandler c : handlers.values())
            validateDescription(c.getClass());
    }

    /**
     * Checks whether the given class is annotated with {@link Description}.
     *
     * @param clazz the class to check
     * @throws RuntimeException if the class is not annotated with {@link Description}
     */
    private static void validateDescription(Class<?> clazz) {
        if (clazz.getAnnotation(Description.class) == null)
            throw new RuntimeException("Missing @Description on command handler: " + clazz.getName());
    }
}
