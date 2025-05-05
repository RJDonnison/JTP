package org.reujdon.jtp.server.handlers;

import jdk.jfr.Description;
import org.reujdon.jtp.server.CommandHandler;
import org.reujdon.jtp.server.CommandRegistry;
import org.reujdon.jtp.shared.messaging.Response;

import java.util.Map;

@Description("Lists all commands and there descriptions.")
public class HelpCommandHandler implements CommandHandler {
    @Override
    public Response handle(Map<String, Object> params) {
        return new Response(CommandRegistry.getDescriptions());
    }
}
