package gov.nysenate.inventory.util;

import com.google.gson.*;
import gov.nysenate.inventory.model.AdjustCode;

import java.util.ArrayList;
import java.util.List;

public class AdjustCodeParser
{
    public static List<AdjustCode> parseAdjustCodes(String json) {
        List<AdjustCode> adjustCodes =  new ArrayList<AdjustCode>();
        JsonParser parser = new JsonParser();
        JsonArray obj = parser.parse(json).getAsJsonArray();
        for (JsonElement e : obj) {
            adjustCodes.add(parseAdjustCode(e.toString()));
        }

        return adjustCodes;
    }

    public static AdjustCode parseAdjustCode(String json) {
        JsonParser parser = new JsonParser();
        JsonObject obj = parser.parse(json).getAsJsonObject();

        Gson gson = new Gson();
        return gson.fromJson(obj, AdjustCode.class);
    }
}
