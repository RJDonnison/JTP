package org.reujdon.jtp.client.commands;

import org.reujdon.jtp.shared.messaging.Request;

import java.util.Map;

public class HelpCommand extends Request {
    public HelpCommand() {
        super("Help", 500);
    }

    @Override
    public void onSuccess(Map<String, Object> response) {
        logger.info("Commands available:");

        for (Map.Entry<String, Object> entry : response.entrySet())
            logger.info("{}: {}", entry.getKey(), entry.getValue());
    }

    @Override
    public void onTimeout() {
        logger.error("Getting help commands timed out.");
    }
}
