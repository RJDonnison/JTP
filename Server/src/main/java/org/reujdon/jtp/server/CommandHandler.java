package org.reujdon.jtp.server;

import org.reujdon.jtp.shared.messaging.Response;

import java.util.Map;

/**
 * A functional interface representing a handler for processing commands in the transfer protocol.
 *
 * <p>Command handlers are responsible for executing specific commands and returning appropriate
 * responses. Implementations should:</p>
 * <ul>
 *   <li>Have a description defined by {@link jdk.jfr.Description}</li>
 *   <li>Validate input parameters</li>
 *   <li>Perform the requested operation</li>
 *   <li>Return a well-formed {@link Response}</li>
 *   <li>Handle any command-specific errors</li>
 * </ul>
 *
 *
 * @see CommandRegistry
 */
@FunctionalInterface
public interface CommandHandler {
    /**
     * Processes a command with the given parameters and returns a JSON response.
     *
     * @param params A map containing the command parameters (never null)
     * @return A {@link Response} containing the response data
     * @throws RuntimeException if command processing fails
     */
    Response handle(Map<String, Object> params);
}
