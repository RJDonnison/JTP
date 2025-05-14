package org.reujdon.jtp.server;

import jdk.jfr.Description;
import org.reujdon.jtp.server.handlers.HelpCommandHandler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Registry for managing command handlers in JTP.
 *
 * <p>Maintains a mapping between command strings and their corresponding
 * {@link CommandHandler} implementations, including both static and dynamic commands.</p>
 *
 * @author Reuben Donnison
 * @version 0.2
 * @see CommandHandler
 */
public class CommandRegistry {
    private static final Map<String, CommandHandler> handlers = new HashMap<>();
    private static final Set<String> STATIC_COMMANDS = new HashSet<>();

//    Base command initialization
    static {
        addStaticCommand("Help", new HelpCommandHandler());
        validateDescriptions();
    }

    /**
     * Registers a command as a statically defined command.
     * <p>
     * This method adds the given command and its handler to the internal command registry
     * and marks it as a static (core) command that should not be removed when clearing
     * dynamically registered commands.
     * </p>
     *
     * @param command the command keyword to register (must be non-null and non-empty)
     * @param handler the command handler instance to associate with the command (must not be null)
     * @throws IllegalArgumentException if the command is null, empty, or the handler is null
     */
    private static void addStaticCommand(String command, CommandHandler handler) {
        if (command == null || command.trim().isEmpty())
            throw new IllegalArgumentException("Command cannot be null or empty");

        if (handler == null)
            throw new IllegalArgumentException("Handler cannot be null");

        STATIC_COMMANDS.add(command);
        handlers.put(command, handler);
    }

    /**
     * Removes all dynamically registered commands while preserving static commands.
     */
    public static void clear() {
        handlers.keySet().removeIf(command -> !STATIC_COMMANDS.contains(command));
    }

    /**
     * Gets the command handler for the specified command.
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
     * Gets the description of a specific command.
     *
     * @param command the command to describe
     * @return the description or empty string if not found/missing
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
     * Gets descriptions for all registered commands.
     *
     * @return map of command names to their descriptions
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
     * @param command the command to register
     * @param handler the handler to execute
     * @param override if true, allows overwriting existing commands
     * @throws IllegalArgumentException for invalid inputs or existing commands when override=false
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
