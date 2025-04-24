package org.reujdon.jtp.shared;

import org.json.JSONObject;

public class Response extends Message {
    public Response(String id){
        super(id, MessageType.RESPONSE);
    }

    public Response(String id, JSONObject data){
        this(id);

        this.addParams(data);
    }

    public void addParams(JSONObject data) {
        if (data == null)
            return;

        for (String key : data.keySet())
            params.put(key, data.get(key));
    }
}
