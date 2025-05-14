package org.reujdon.jtp.client.commands;

import org.reujdon.jtp.shared.messaging.messages.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Abstract base class for JTP command implementations.
 *
 * <p>Provides core functionality for command execution including timeout handling
 * and response/error callbacks. All concrete commands must implement the success
 * and timeout handlers.</p>
 *
 * @author Reuben Donnison
 * @version 0.2
 * @see Request
 */
public abstract class Command extends Request {
    /**
     * Logger instance for command-related operations.
     *
     * @see org.slf4j.Logger
     */
    protected static final Logger logger = LoggerFactory.getLogger(Command.class);

    /**
     * Default timeout duration in milliseconds (1000ms)
     */
    protected long timeout = 1000;

    /**
     * Creates a command with default timeout.
     *
     * @param command the command string to execute
     * @see #Command(String, long)
     */
    public Command(String command) {
        super(command);
    }

    /**
     * Creates a command with specified timeout.
     *
     * @param command the command string to execute
     * @param timeout custom timeout in milliseconds
     * @see #Command(String)
     */
    public Command(String command, long timeout) {
        super(command);
        this.timeout = timeout;
    }

    /**
     * Gets the command timeout duration.
     *
     * @return timeout in milliseconds
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     * Handles command execution errors.
     *
     * @param error the error message received
     */
    public void onError(String error){
        logger.error("Error: {}", error);
    }

    /**
     * Handles successful command execution.
     *
     * @param response the response data from server
     */
    public abstract void onSuccess(Map<String, Object> response);

    /**
     * Handles command timeout scenarios.
     */
    public abstract void onTimeout();
}
