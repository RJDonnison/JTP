package org.reujdon.jtp.shared.messaging.messages;

import org.reujdon.jtp.shared.messaging.Message;
import org.reujdon.jtp.shared.messaging.MessageType;

public class Auth extends Message {
    public Auth(String key){
        super("*", MessageType.AUTH);

        this.addParam("key", key);
        this.addParam("success", false);
    }

    public String getKey(){
        return (String)this.getParam("key");
    }

    public void setToken(String token){
        this.addParam("token", token);
    }

    public String getToken(){
        return (String)this.getParam("token", null);
    }

    public void success(){
        this.addParam("success", true);
    }

    public boolean getSuccess(){
        return (Boolean)this.getParam("success");
    }
}
