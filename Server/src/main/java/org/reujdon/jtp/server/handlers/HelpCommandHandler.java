package org.reujdon.jtp.server.handlers;

import jdk.jfr.Description;
import org.json.JSONObject;
import org.reujdon.jtp.server.CommandHandler;
import org.reujdon.jtp.server.CommandRegistry;

import java.util.Map;

@Description("Lists all commands and there descriptions.")
public class HelpCommandHandler implements CommandHandler {
    @Override
    public JSONObject handle(Map<String, Object> params) {
        return new JSONObject(CommandRegistry.getDescriptions());
    }
}
