package org.reujdon.jtp.client.commands;

import org.reujdon.jtp.shared.Request;
import java.util.Map;

public class TestCommand extends Request {
    public TestCommand() {
        super("Test", 500);
    }

    @Override
    public void onSuccess(Map<String, Object> response) {
        System.out.println(response.get("command"));
    }

    @Override
    public void onError() {
        System.out.println("Test error");
    }
}
