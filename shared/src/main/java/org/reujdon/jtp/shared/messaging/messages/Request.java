package org.reujdon.jtp.shared.messaging.messages;

import org.reujdon.jtp.shared.messaging.Message;
import org.reujdon.jtp.shared.messaging.MessageType;

/**
 * Abstract base class representing a request message in JTP.
 *
 * <p>Request messages are used to execute commands on the server and handle their responses.
 * They support:</p>
 * <ul>
 *   <li>Command execution with parameters</li>
 *   <li>Optional authentication via tokens</li>
 *   <li>Timeout configuration</li>
 *   <li>Response handling through callbacks</li>
 * </ul>
 *
 * @author Reuben Donnison
 * @version 0.2
 * @see Message
 * @see MessageType#REQUEST
 */
public class Request extends Message {
    /**
     * Constructs a new Request message with command.
     *
     * @param command The command string to execute
     * @throws IllegalArgumentException if command is null or blank
     */
    public Request(String command) {
        super(MessageType.REQUEST);

        if (command == null || command.isBlank())
            throw new IllegalArgumentException("Command must not be null or blank");

        addParam("command", command);
    }

    /**
     * Gets the authentication token for this request.
     *
     * @return the authentication token, or null if not set
     */
    public String getToken() {
        try {
            return getParam("token").toString();
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * Sets the authentication token for this request.
     *
     * @param token The authentication token to set (can be null)
     */
    public void setToken(String token) {
        if (token != null)
            addParam("token", token.trim());
        else
            addParam("token", null);
    }
}
