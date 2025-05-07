package org.reujdon.jtp.shared.messaging;

//TODO: javadoc
public class Auth extends Message {
    public Auth(String id){
        super(id, MessageType.AUTH);
    }

    public Auth(String id, String key){
        this(id);

        this.addParam("key", key);
    }
}
