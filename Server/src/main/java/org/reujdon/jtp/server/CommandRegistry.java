package org.reujdon.jtp.server;

import org.reujdon.jtp.server.handlers.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * A registry for managing command handlers in the transfer protocol.
 * This class maintains a mapping between command strings and their corresponding
 * {@link CommandHandler} implementations.
 *
 * @see CommandHandler
 */
class CommandRegistry {
    private static final Map<String, CommandHandler> handlers = new HashMap<>();

//    Base command initialization
    static {
        handlers.put("test", new Test());
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

        command = command.trim().toLowerCase();

        if (!handlers.containsKey(command))
            return null;

        return handlers.get(command);
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
     */
    public void register(String command, CommandHandler handler, boolean override) {
        if (command == null || command.trim().isEmpty())
            throw new IllegalArgumentException("Command cannot be null or empty");

        if (handler == null)
            throw new IllegalArgumentException("Handler cannot be null");

        command = command.trim().toLowerCase();

        if (!override && handlers.containsKey(command))
            throw new IllegalArgumentException("Command " + command + " already exists");

        handlers.put(command.toLowerCase(), handler);
    }
}
