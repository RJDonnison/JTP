package org.reujdon.jtp.client.commands;

import org.reujdon.jtp.shared.messaging.messages.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public abstract class Command extends Request {
    protected static final Logger logger = LoggerFactory.getLogger(Command.class);

    protected long timeout = 1000;

    public Command(String command) {
        super(command);
    }

    public Command(String command, long timeout) {
        super(command);
        this.timeout = timeout;
    }

    public long getTimeout() {
        return timeout;
    }

    public void onError(String error){
        logger.error("Error: {}", error);
    }

    public abstract void onSuccess(Map<String, Object> response);
    public abstract void onTimeout();
}
