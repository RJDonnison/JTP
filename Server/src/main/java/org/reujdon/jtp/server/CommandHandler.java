package org.reujdon.jtp.server;

import org.json.JSONObject;

import java.util.Map;

public interface CommandHandler {
    JSONObject handle(Map<String, Object> params);
}
