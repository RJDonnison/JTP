package org.reujdon.jtp.shared.messaging.messages;

import org.reujdon.jtp.shared.messaging.Message;
import org.reujdon.jtp.shared.messaging.MessageType;

/**
 * Message class for authentication operations.
 *
 * <p>This specialized message handles authentication flows including API keys,
 * tokens, and success/failure states. It automatically initializes with a
 * success state of false.</p>
 *
 * @see Message
 * @see MessageType#AUTH
 *
 * @author Reuben Donnison
 * @version 0.2
 */
public class Auth extends Message {
    /**
     * Creates a new authentication message with the specified API key.
     *
     * <p>The message is initialized with a global ID ("*") and AUTH type,
     * and sets the initial success state to false.</p>
     *
     * @param key The authentication key to use
     */
    public Auth(String key){
        super("*", MessageType.AUTH);

        this.addParam("key", key);
        this.addParam("success", false);
    }

    /**
     * Gets the authentication key associated with this message.
     *
     * @return the authentication key
     */
    public String getKey(){
        return (String)this.getParam("key");
    }

    /**
     * Sets the authentication token for this message.
     *
     * @param token the token to set
     */
    public void setToken(String token){
        this.addParam("token", token);
    }

    /**
     * Gets the authentication token if one exists.
     *
     * @return the authentication token, or null if not set
     */
    public String getToken(){
        return (String)this.getParam("token", null);
    }

    /**
     * Marks the authentication attempt as successful.
     *
     * <p>Updates the success parameter to true in the message parameters.</p>
     */
    public void success(){
        this.addParam("success", true);
    }

    /**
     * Gets the authentication success state.
     *
     * @return true if authentication was successful, false otherwise
     */
    public boolean getSuccess(){
        return (Boolean)this.getParam("success");
    }
}
