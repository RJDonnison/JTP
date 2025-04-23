package org.reujdon.jtp.server.handlers;

import org.json.JSONObject;
import org.reujdon.jtp.server.CommandHandler;

import java.util.Map;

public class Test implements CommandHandler {
    @Override
    public JSONObject handle(Map<String, Object> params) {
        JSONObject json = new JSONObject();
        json.put("command", "test");
        return json;
    }
}
