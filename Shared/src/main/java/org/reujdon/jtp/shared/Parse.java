package org.reujdon.jtp.shared;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for parsing JSON data to and from Java objects.
 */
public class Parse {
    /**
     * Extracts parameters from a JSON object's "params" field into a Map.
     *
     * <p>This method safely handles cases where:</p>
     * <ul>
     *   <li>The input JSON is missing the "params" field (returns empty Map)</li>
     *   <li>The "params" field contains nested objects (preserved as JSONObject)</li>
     * </ul>
     *
     * @param json The JSON object containing parameters (cannot be null)
     * @return A Map containing all parameters from the "params" field, or empty Map if none exist
     * @throws JSONException if the "params" field exists but is not a JSON object
     */
    public static Map<String, Object> Params(JSONObject json) {
        Map<String, Object> params = new HashMap<>();
        if (json.has("params")) {
            JSONObject paramJson = json.getJSONObject("params");
            for (String key : paramJson.keySet())
                params.put(key, paramJson.get(key));
        }

        return params;
    }
}
