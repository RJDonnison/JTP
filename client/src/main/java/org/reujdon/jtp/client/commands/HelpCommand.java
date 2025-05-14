package org.reujdon.jtp.client.commands;

import java.util.Map;

/**
 * Implementation of the help command for JTP.
 *
 * <p>Requests and displays all available commands and their descriptions
 * from the server.</p>
 *
 * @author Reuben Donnison
 * @version 0.2
 * @see Command
 */
public class HelpCommand extends Command {
    /**
     * Constructs a new help command with default timeout.
     */
    public HelpCommand() {
        super("Help");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSuccess(Map<String, Object> response) {
        logger.info("Commands available:");

        for (Map.Entry<String, Object> entry : response.entrySet())
            logger.info("   {}: {}", entry.getKey(), entry.getValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTimeout() {
        logger.error("Getting help commands timed out.");
    }
}
