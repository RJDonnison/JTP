package org.reujdon.jtp.shared;

public class Error extends Message {
    public Error(String id, String message) {
        super(id, MessageType.ERROR);

        addParam("message", message);
    }

    public Error(String message){
        super(MessageType.ERROR);

        addParam("message", message);
    }
}
