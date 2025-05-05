package org.reujdon.jtp.client.commands;

import org.reujdon.jtp.shared.messaging.Request;

import java.util.Map;

public class AuthCommand extends Request {
    public AuthCommand(String key) {
        super("AUTH", 500);

        addParam("key", key);
    }

    @Override
    public void onSuccess(Map<String, Object> response) {
//        TODO:
//        Store token

//        System.out.println(response.get("token"));
    }

    @Override
    public void onTimeout() {
        System.out.println("AuthCommand.onTimeout");
    }
}
