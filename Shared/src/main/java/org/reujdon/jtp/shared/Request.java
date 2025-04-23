package org.reujdon.jtp.shared;

import java.util.Map;

public abstract class Request extends Message {
    private final long timeout;

    public Request(String command, long timeout) {
        super(MessageType.REQUEST);

        if (timeout < 0)
            throw new IllegalArgumentException("Timeout must be a positive integer");

        this.timeout = timeout;
        addParam("command", command);
    }

    public long getTimeout() {
        return timeout;
    }

    public abstract void onSuccess(Map<String, Object> response);
    public abstract void onError();
}
