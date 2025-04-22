package org.reujdon.jtp.client.commands;

import org.reujdon.jtp.shared.Request;

public class TestCommand extends Request {
    public TestCommand() {
        super("Test", 500);
    }

    @Override
    public void onSuccess() {
        System.out.println("Test success");
    }

    @Override
    public void onError() {
        System.out.println("Test error");
    }
}
