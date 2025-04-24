package org.reujdon.jtp.shared;

/**
 * Enumerates the types of messages supported in the transfer protocol.
 *
 * <p>Each message type serves a specific purpose in the communication protocol:</p>
 * <ul>
 *   <li><b>REQUEST</b> - Client-initiated messages requesting an operation</li>
 *   <li><b>RESPONSE</b> - Server responses to successful requests</li>
 *   <li><b>ERROR</b> - Error conditions or failed operations</li>
 *   <li><b>AUTH</b> - Authentication-related messages</li>
 * </ul>
 *
 * @see Request
 * @see Response
 * @see Error
 */
public enum MessageType {
    REQUEST,
    RESPONSE,
    ERROR,
    AUTH
}
