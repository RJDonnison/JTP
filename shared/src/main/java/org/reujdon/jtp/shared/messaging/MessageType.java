package org.reujdon.jtp.shared.messaging;

import org.reujdon.jtp.shared.messaging.messages.Auth;
import org.reujdon.jtp.shared.messaging.messages.Error;
import org.reujdon.jtp.shared.messaging.messages.Request;
import org.reujdon.jtp.shared.messaging.messages.Response;

/**
 * Represents the type of message used in communication.
 * <p>
 * Message types include:
 * <ul>
 *     <li>{@code REQUEST} – for initiating actions</li>
 *     <li>{@code RESPONSE} – for replying to requests</li>
 *     <li>{@code ERROR} – for reporting issues</li>
 *     <li>{@code AUTH} – for authentication-related communication</li>
 * </ul>
 * Used to classify and handle different communication intents.
 *
 * @see Request
 * @see Response
 * @see Error
 * @see Auth
 *
 * @author Reuben Donnison
 * @version 0.2
 */
public enum MessageType {
    /**
     * Client request message type.
     * <p>Contains a command and parameters.</p>
     */
    REQUEST,

    /**
     * Server response message type.
     * <p>Contains command execution results.</p>
     */
    RESPONSE,

    /**
     * Error notification message type.
     * <p>Contains error details.</p>
     */
    ERROR,

    /**
     * Authentication message type.
     * <p>Used for connection authentication.</p>
     */
    AUTH;

    /**
     * Parses a string to its corresponding {@link MessageType}.
     *
     * @param value the string representation of the message type
     * @return the corresponding {@code MessageType}
     * @throws IllegalArgumentException if the string does not match any known type
     */
    public static MessageType fromString(String value) {
        for (MessageType type : values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unknown message type: " + value);
    }
}
