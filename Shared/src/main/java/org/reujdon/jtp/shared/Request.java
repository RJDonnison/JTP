package org.reujdon.jtp.shared;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

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
 * <p>Subclasses must implement the response handling methods:</p>
 * <ul>
 *   <li>{@link #onSuccess(Map)} - for successful responses</li>
 *   <li>{@link #onError(String)} - for error responses</li>
 *   <li>{@link #onTimeout()} - for timeout conditions</li>
 * </ul>
 *
 * @see Message
 * @see MessageType#REQUEST
 */
public abstract class Request extends Message {
    protected static final Logger logger = LoggerFactory.getLogger(Request.class);
    private final long timeout;

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
     * @param token The authentication token
     * @param timeout The timeout duration in milliseconds
     * @throws IllegalArgumentException if:
     *         <ul>
     *           <li>command is null or empty</li>
     *           <li>timeout is negative</li>
     *         </ul>
     *
     * @see MessageType#REQUEST
     */
    public Request(String command, String token, long timeout) {
        super(MessageType.REQUEST);

        if (timeout < 0)
            throw new IllegalArgumentException("Timeout must be a positive integer");

        if (command == null || command.trim().isEmpty())
            throw new IllegalArgumentException("Command must not be empty");

        if (token != null)
            addParam("token", token.trim());

        this.timeout = timeout;
        addParam("command", command);
    }

    /**
     * Constructs a new Request message with command and timeout (no authentication token).
     *
     * @param command The command string to execute
     * @param timeout The timeout duration in milliseconds
     * @throws IllegalArgumentException if:
     *         <ul>
     *           <li>command is null or empty</li>
     *           <li>timeout is negative</li>
     *         </ul>
     *
     * @see #Request(String, String, long)
     */
    public Request(String command, long timeout) {
        this(command, null, timeout);
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
            setParam("token", token.trim());
        else
            setParam("token", null);
    }

    public long getTimeout() {
        return timeout;
    }

    /**
     * Called when the request completes successfully.
     *
     * <p>Implementations should handle the successful response data contained in the
     * response map. The map structure will vary depending on the command executed.</p>
     *
     * @param response A map containing the successful response data
     *                with keys representing response fields and values containing
     *                the response data.
     *
     * @Example:
     * <pre>
     * {@code
     * public void onSuccess(Map<String, Object> response) {
     *     String username = (String) response.get("username");
     *     int accessLevel = (int) response.get("accessLevel");
     *     // ... process successful response
     * }
     * }
     * </pre>
     */
    public abstract void onSuccess(Map<String, Object> response);

    /**
     * Called when the request fails with an error.
     *
     * <p>Implementations should handle the error condition appropriately. The error
     * message will typically come from the server and describe what went wrong.</p>
     *
     * @param error A descriptive error message explaining the failure
     */
    public void onError(String error){
        logger.error("Error: {}", error);
    }

    /**
     * Called when the request times out before receiving a response.
     *
     * <p>Implementations should handle the timeout condition, typically by notifying
     * the user or retrying the request. This is called when no response is received
     * within the timeout period specified in the request.</p>
     *
     * @Example:
     * <pre>
     * {@code
     * // Handling a timeout
     * public void onTimeout() {
     *     if (retryCount < MAX_RETRIES) {
     *         retryCount++;
     *         sendRequest();
     *     } else {
     *         showTimeoutError();
     *     }
     * }
     * </pre>
     */
    public abstract void onTimeout();
}
