package org.reujdon.jtp.server;

import org.reujdon.jtp.server.handlers.Test;

import java.util.HashMap;
import java.util.Map;

class CommandRegistry {
    private static final Map<String, CommandHandler> handlers = new HashMap<>();

    static {
        handlers.put("test", new Test());
    }

    public static CommandHandler getHandler(String command) {
        if (!handlers.containsKey(command))
            return null;

        return handlers.get(command);
    }

    public void register(String command, CommandHandler handler, boolean override) {
        if (command == null || command.trim().isEmpty())
            throw new IllegalArgumentException("Command cannot be null or empty");

        if (handler == null)
            throw new IllegalArgumentException("Handler cannot be null");

        if (!override && handlers.containsKey(command))
            throw new IllegalArgumentException("Command " + command + " already exists");

        handlers.put(command.toLowerCase(), handler);
    }
}
