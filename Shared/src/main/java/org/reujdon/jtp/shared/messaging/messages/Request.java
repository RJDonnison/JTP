package org.reujdon.jtp.shared.messaging.messages;

import org.reujdon.jtp.shared.messaging.Message;
import org.reujdon.jtp.shared.messaging.MessageType;

/**
 * Abstract base class representing a request message in the transfer protocol.
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
 * @see Message
 * @see MessageType#REQUEST
 */
public class Request extends Message {
    /**
     * Constructs a new Request message with command, authentication token, and timeout.
     *
     * <p>The request will automatically include these parameters:</p>
     * <ul>
     *   <li><b>command</b> - The command to execute (required)</li>
     *   <li><b>token</b> - The authentication token (optional)</li>
     * </ul>
     *
     * @param command The command string to execute
     * @throws IllegalArgumentException if:
     *         <ul>
     *           <li>command is null or blank</li>
     *         </ul>
     *
     * @see MessageType#REQUEST
     */
    public Request(String command) {
        super(MessageType.REQUEST);

        if (command == null || command.isBlank())
            throw new IllegalArgumentException("Command must not be null or blank");

        addParam("command", command);
    }

    public String getToken() {
        try {
            return getParam("token").toString();
        } catch (Exception e) {
            return null;
        }
    }

    public void setToken(String token) {
        if (token != null)
            addParam("token", token.trim());
        else
            addParam("token", null);
    }
}
