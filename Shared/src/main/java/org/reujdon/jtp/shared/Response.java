package org.reujdon.jtp.shared;

public class Response extends Message {
    public Response(String id){
        super(id, MessageType.RESPONSE);
    }
}
