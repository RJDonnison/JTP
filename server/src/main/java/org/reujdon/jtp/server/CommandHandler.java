package org.reujdon.jtp.server;

import org.reujdon.jtp.shared.Permission;
import org.reujdon.jtp.shared.messaging.Message;
import org.reujdon.jtp.shared.messaging.messages.Response;

/**
 * An interface for processing commands in JTP.
 *
 * <p>Command handlers execute specific commands and return responses. Implementations must:</p>
 * <ul>
 *   <li>Define descriptions with {@link jdk.jfr.Description}</li>
 *   <li>Validate input parameters</li>
 *   <li>Perform requested operations</li>
 *   <li>Return well-formed {@link Response} objects</li>
 * </ul>
 *
 * @author Reuben Donnison
 * @version 0.2
 * @see CommandRegistry
 */
public interface CommandHandler {
    /**
     * Gets the permission required to execute this command.
     *
     * @return the required permission level
     */
    Permission requiredPermission();

    /**
     * Processes a command message and generates a response.
     *
     * @param message the incoming command message
     * @return the command response
     * @throws RuntimeException if command processing fails
     */
    Response handle(Message message);
}
