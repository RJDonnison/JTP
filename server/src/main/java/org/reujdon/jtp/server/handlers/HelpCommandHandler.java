package org.reujdon.jtp.server.handlers;

import jdk.jfr.Description;
import org.reujdon.jtp.server.CommandHandler;
import org.reujdon.jtp.server.CommandRegistry;
import org.reujdon.jtp.shared.Permission;
import org.reujdon.jtp.shared.messaging.Message;
import org.reujdon.jtp.shared.messaging.messages.Response;

/**
 * Command handler that lists all available commands and their descriptions.
 *
 * <p>This handler provides a help function that returns metadata about
 * all registered commands in the system.</p>
 *
 * @author Reuben Donnison
 * @version 0.2
 * @see CommandHandler
 * @see CommandRegistry#getDescriptions()
 */
@Description("Lists all commands and there descriptions.")
public class HelpCommandHandler implements CommandHandler {
    /**
     * Specifies that no permissions are required to use the help command.
     *
     * @return Permission.NONE
     * @see Permission
     */
    @Override
    public Permission requiredPermission() {
        return Permission.NONE;
    }

    /**
     * Handles help command request by returning all registered commands.
     *
     * @param message the incoming message (unused)
     * @return response containing command descriptions
     * @see CommandRegistry#getDescriptions()
     */
    @Override
    public Response handle(Message message) {
        Response response = new Response();
        response.addParams(CommandRegistry.getDescriptions());
        return response;
    }
}
