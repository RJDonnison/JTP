package org.reujdon.jtp.shared;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Parse {
    public static Map<String, Object> Params(JSONObject json) {
        Map<String, Object> params = new HashMap<>();
        if (json.has("params")) {
            JSONObject paramJson = json.getJSONObject("params");
            for (String key : paramJson.keySet())
                params.put(key, paramJson.get(key));
        }

        return params;
    }
}
