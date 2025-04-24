package org.reujdon.jtp.shared;

import org.json.JSONObject;

public class Response extends Message {
    public Response(String id){
        super(id, MessageType.RESPONSE);
    }

    public void addParams(JSONObject jsonParams) {
        if (jsonParams == null)
            return;

        for (String key : jsonParams.keySet())
            params.put(key, jsonParams.get(key));
    }
}
