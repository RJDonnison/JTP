package org.reujdon.jtp.server.handlers;

import jdk.jfr.Description;
import org.reujdon.jtp.server.CommandHandler;
import org.reujdon.jtp.shared.Permission;
import org.reujdon.jtp.shared.TokenUtil;
import org.reujdon.jtp.shared.messaging.Response;

import java.util.HashMap;
import java.util.Map;

@Description("Checks API key and returns session token")
public class AuthCommandHandler implements CommandHandler {
    private static final Map<String, Permission> KEYS = new HashMap<>();

    static {
        KEYS.put("test", Permission.FULL);
    }

    @Override
    public Response handle(Map<String, Object> params) {
        if (!params.containsKey("key"))
            throw new IllegalArgumentException("No key provided");

        String key = params.get("key").toString();

        if (!KEYS.containsKey(key))
            throw new IllegalStateException("Key invalid: " + key);

//        TODO:
//        Store permissions
//        Generate and store token

        Response response = new Response();
        response.addParam("token", TokenUtil.generateSessionToken());

        return response;
    }
}
