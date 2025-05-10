package org.reujdon.jtp.shared.messaging.messages;

import org.reujdon.jtp.shared.messaging.Message;
import org.reujdon.jtp.shared.messaging.MessageType;

public class Auth extends Message {
    public Auth(String id){
        super(id, MessageType.AUTH);
    }

    public Auth(String id, String key){
        this(id);

        this.addParam("key", key);
    }
}
