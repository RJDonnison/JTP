package org.reujdon.jtp.server;

import org.reujdon.jtp.server.handlers.Test;

import java.util.HashMap;
import java.util.Map;

public class CommandRegistry {
    private static final Map<String, CommandHandler> handlers = new HashMap<>();

    static {
        handlers.put("test", new Test());
    }

    public static CommandHandler getHandler(String command) {
        if (!handlers.containsKey(command))
            return null;

        return handlers.get(command);
    }
}
