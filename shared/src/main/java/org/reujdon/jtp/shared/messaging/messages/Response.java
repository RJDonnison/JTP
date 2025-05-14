package org.reujdon.jtp.shared.messaging.messages;

import org.reujdon.jtp.shared.messaging.Message;
import org.reujdon.jtp.shared.messaging.MessageType;

/**
 * Represents a response message in JTP.
 *
 * <p>Response messages are used to return data from successful command executions.
 * They contain:</p>
 * <ul>
 *   <li>A message type of {@link MessageType#RESPONSE}</li>
 *   <li>Response data as key-value pairs</li>
 *   <li>An identifier matching the original request</li>
 * </ul>
 *
 * @author Reuben Donnison
 * @version 0.2
 * @see Message
 * @see MessageType#RESPONSE
 */
public class Response extends Message {
    /**
     * Constructs an empty Response message
     */
    public Response(){
        super(MessageType.RESPONSE);
    }

    /**
     * Constructs a Response message with the specified ID.
     *
     * @param id The response identifier (should match the original request ID)
     * @throws IllegalArgumentException if id is null or empty
     */
    public Response(String id){
        super(id, MessageType.RESPONSE);
    }


}
