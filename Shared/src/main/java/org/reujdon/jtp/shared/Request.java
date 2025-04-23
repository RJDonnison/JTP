package org.reujdon.jtp.shared;

import java.util.Map;

public abstract class Request extends Message {
    private final long timeout;

    public Request(String command, String token, long timeout) {
        super(MessageType.REQUEST);

        if (token != null)
            addParam("token", token.trim());

        if (timeout < 0)
            throw new IllegalArgumentException("Timeout must be a positive integer");

        this.timeout = timeout;
        addParam("command", command);
    }

    public Request(String command, long timeout) {
        this(command, null, timeout);
    }

    public String getToken() {
        try {
            return getParam("token").toString();
        } catch (Exception e) {
            return null;
        }
    }

    public void setToken(String token) {
        if (token != null)
            setParam("token", token.trim());
        else
            setParam("token", null);
    }

    public long getTimeout() {
        return timeout;
    }

    public abstract void onSuccess(Map<String, Object> response);
    public abstract void onError();
}
