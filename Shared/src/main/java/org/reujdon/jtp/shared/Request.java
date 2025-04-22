package org.reujdon.jtp.shared;

public abstract class Request extends Message {
    private long timeout;

    public Request(String command, long timeout) {
        super(MessageType.REQUEST);

        if (timeout < 0)
            throw new IllegalArgumentException("Timeout must be a positive integer");

        this.timeout = timeout;
        addParam("Command", command);
    }

    public long getTimeout() {
        return timeout;
    }

    public abstract void onSuccess();
    public abstract void onError();
}
