package org.reujdon.jtp.client.commands;

import org.reujdon.jtp.shared.Request;
import java.util.Map;

public class TestCommand extends Request {
    public TestCommand() {
        super("test", 500);
    }

    @Override
    public void onSuccess(Map<String, Object> response) {
        System.out.println("Success!!!");
    }

    @Override
    public void onError(String error) {
        System.err.println(error);
    }

    @Override
    public void onTimeout() {
        System.err.println("Command timed out!!!");
    }
}
