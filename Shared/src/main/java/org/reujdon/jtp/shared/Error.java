package org.reujdon.jtp.shared;

public class Error extends Message {
    public Error(String message){
        super(MessageType.ERROR);

        addParam("message", message);
    }
}
