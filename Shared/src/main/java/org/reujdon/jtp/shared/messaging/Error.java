package org.reujdon.jtp.shared.messaging;

/**
 * Represents an error message in the transfer protocol.
 *
 * <p>Error messages are used to communicate problems or exceptional conditions
 * that occurred during request processing. All error messages contain:</p>
 * <ul>
 *   <li>A message type of {@link MessageType#ERROR}</li>
 *   <li>An error description in the "message" parameter</li>
 *   <li>An automatically generated message ID (unless specified)</li>
 * </ul>
 *
 * <p>Example JSON representation:</p>
 * <pre>
 * {@code
 * {
 *   "type": "ERROR",
 *   "id": "123e4567-e89b-12d3-a456-426614174000",
 *   "params": {
 *     "message": "Invalid authentication token"
 *   }
 * }
 * }
 * </pre>
 *
 * @see Message
 * @see MessageType#ERROR
 */
public class Error extends Message {
    /**
     * Constructs an Error message with a specific ID and error message.
     *
     * @param id The unique identifier for this error message
     * @param message The descriptive error message
     * @throws IllegalArgumentException if either id or message is null/empty
     */
    public Error(String id, String message) {
        super(id, MessageType.ERROR);

        if (message == null || message.trim().isEmpty())
            throw new IllegalArgumentException("Message cannot be null or empty");

        addParam("message", message);
    }

    /**
     * Constructs an Error message with given message.
     *
     * @param message The descriptive error message
     * @throws IllegalArgumentException if message is null/empty
     */
    public Error(String message){
        super(MessageType.ERROR);

        if (message == null || message.trim().isEmpty())
            throw new IllegalArgumentException("Message cannot be null or empty");

        addParam("message", message);
    }
}
