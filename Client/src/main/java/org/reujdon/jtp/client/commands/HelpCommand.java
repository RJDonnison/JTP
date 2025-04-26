package org.reujdon.jtp.client.commands;

import org.reujdon.jtp.shared.Request;

import java.util.Map;

public class HelpCommand extends Request {
    public HelpCommand() {
        super("Help", 500);
    }

    @Override
    public void onSuccess(Map<String, Object> response) {
        System.out.println("Commands available:\n");

        for (Map.Entry<String, Object> entry : response.entrySet())
            System.out.println(entry.getKey() + ": " + entry.getValue());
    }

    @Override
    public void onTimeout() {
        System.err.println("Commands available:\n");
    }
}
