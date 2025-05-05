package org.reujdon.jtp.shared.messaging;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.reujdon.jtp.shared.Parse;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParseTest {
    @Test
    public void testParamsWithEmptyJson() {
        JSONObject json = new JSONObject();
        Map<String, Object> params = Parse.Params(json);
        assertTrue(params.isEmpty());
    }

    @Test
    public void testParamsWithNoParamsField() {
        JSONObject json = new JSONObject();
        json.put("otherField", "value");
        Map<String, Object> params = Parse.Params(json);
        assertTrue(params.isEmpty());
    }

    @Test
    public void testParamsWithEmptyParams() {
        JSONObject json = new JSONObject();
        json.put("params", new JSONObject());
        Map<String, Object> params = Parse.Params(json);
        assertTrue(params.isEmpty());
    }

    @Test
    public void testParamsWithSimpleValues() {
        JSONObject json = new JSONObject();
        JSONObject paramsJson = new JSONObject();
        paramsJson.put("string", "value");
        paramsJson.put("int", 42);
        paramsJson.put("boolean", true);
        paramsJson.put("double", 3.14);
        json.put("params", paramsJson);

        Map<String, Object> params = Parse.Params(json);
        assertEquals(4, params.size());
        assertEquals("value", params.get("string"));
        assertEquals(42, params.get("int"));
        assertEquals(true, params.get("boolean"));
        assertEquals(3.14, params.get("double"));
    }

}