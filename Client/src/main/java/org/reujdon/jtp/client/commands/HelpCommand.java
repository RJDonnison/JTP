package org.reujdon.jtp.client.commands;

import java.util.Map;

public class HelpCommand extends Command {
    public HelpCommand() {
        super("Help");
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
