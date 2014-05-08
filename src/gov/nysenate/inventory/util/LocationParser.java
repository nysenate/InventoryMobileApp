package gov.nysenate.inventory.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import gov.nysenate.inventory.model.Location;

import java.util.ArrayList;
import java.util.List;

public class LocationParser {

    private static final Gson gson = new Gson();

    public static List<Location> parseMultipleLocations(String json) {
        List<Location> locations = new ArrayList<Location>();
        JsonParser parser = new JsonParser();
        JsonArray obj = parser.parse(json).getAsJsonArray();
        for (int i = 0; i < obj.size(); i++) {
            locations.add(parseLocation(obj.get(i).toString()));
        }
        return locations;
    }

    public static Location parseLocation(String json) {
        Location loc = new Location();
        JsonParser parser = new JsonParser();
        JsonObject obj = parser.parse(json).getAsJsonObject();
        loc = gson.fromJson(obj, Location.class);
        return loc;
    }
}
