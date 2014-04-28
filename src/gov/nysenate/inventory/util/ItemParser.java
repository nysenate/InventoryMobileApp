package gov.nysenate.inventory.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import gov.nysenate.inventory.model.InvItem;

public class ItemParser
{
    private static final Gson gson = new Gson();

    public static InvItem parseItem(String json) {
        JsonParser parser = new JsonParser();
        JsonObject obj = parser.parse(json).getAsJsonObject();
        return gson.fromJson(obj, InvItem.class);
    }
}

