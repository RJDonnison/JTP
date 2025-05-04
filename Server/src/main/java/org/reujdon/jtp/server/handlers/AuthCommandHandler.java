package org.reujdon.jtp.server.handlers;

import jdk.jfr.Description;
import org.json.JSONObject;
import org.reujdon.jtp.server.CommandHandler;
import org.reujdon.jtp.shared.Permission;
import org.reujdon.jtp.shared.TokenUtil;

import java.util.HashMap;
import java.util.Map;

@Description("Checks API key and returns session token")
public class AuthCommandHandler implements CommandHandler {
    private static final Map<String, Permission> KEYS = new HashMap<>();

    static {
        KEYS.put("test", Permission.FULL);
    }

    @Override
    public JSONObject handle(Map<String, Object> params) {
        if (!params.containsKey("key"))
            throw new IllegalArgumentException("No key provided");

        String key = params.get("key").toString();

        if (!KEYS.containsKey(key))
            throw new IllegalStateException("Key invalid: " + key);

//        Store permissions
//        Generate and store token

        Map<String, String> result = new HashMap<>();
        result.put("token", TokenUtil.generateSessionToken());

        return new JSONObject(result);
    }
}
