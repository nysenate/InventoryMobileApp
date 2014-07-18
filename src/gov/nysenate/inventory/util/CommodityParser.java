package gov.nysenate.inventory.util;

import com.google.gson.*;
import gov.nysenate.inventory.model.Commodity;

import java.util.ArrayList;
import java.util.List;

public class CommodityParser
{
    public static List<Commodity> parseCommodities(String json) {
        List<Commodity> commodities =  new ArrayList<Commodity>();
        JsonParser parser = new JsonParser();
        JsonArray obj = parser.parse(json).getAsJsonArray();
        for (JsonElement e : obj) {
            commodities.add(parseCommodity(e.toString()));
        }

        return commodities;
    }

    public static Commodity parseCommodity(String json) {
        JsonParser parser = new JsonParser();
        JsonObject obj = parser.parse(json).getAsJsonObject();

        Gson gson = new Gson();
        return gson.fromJson(obj, Commodity.class);
    }
}
