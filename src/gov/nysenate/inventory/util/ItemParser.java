package gov.nysenate.inventory.util;

import com.google.gson.*;
import gov.nysenate.inventory.model.Item;

import java.util.ArrayList;
import java.util.List;

public class ItemParser
{
    public static List<Item> parseItems(String json) {
        List<Item> items = new ArrayList<Item>();
        JsonParser parser = new JsonParser();
        JsonArray objs = parser.parse(json).getAsJsonArray();
        for (JsonElement e : objs) {
            items.add(parseItem(e.toString()));
        }
        return items;
    }

    public static Item parseItem(String json) {
        JsonParser parser = new JsonParser();
        JsonObject obj = parser.parse(json).getAsJsonObject();

        Gson gson = new Gson();
        return gson.fromJson(obj, Item.class);
    }
}