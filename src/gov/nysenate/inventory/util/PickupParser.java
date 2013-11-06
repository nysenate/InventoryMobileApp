package gov.nysenate.inventory.util;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import android.util.Log;

import gov.nysenate.inventory.model.InvItem;
import gov.nysenate.inventory.model.Location;
import gov.nysenate.inventory.model.Pickup;

public class PickupParser {

    private static final Gson gson = new Gson();

    public static Pickup parsePickup(String json) throws JSONException {
        Pickup pickup = new Pickup();
        JsonParser parser = new JsonParser();
        JsonObject obj = parser.parse(json).getAsJsonObject();
        pickup = gson.fromJson(obj, Pickup.class);
        return pickup;
    }

    public static List<Pickup> parseMultiplePickups(String json) throws JSONException {
        List<Pickup> pickups = new ArrayList<Pickup>();
        JsonParser parser = new JsonParser();
        JsonArray obj = parser.parse(json).getAsJsonArray();
        for (int i = 0; i < obj.size(); i++) {
            pickups.add(parsePickup(obj.get(i).toString()));
        }
        return pickups;
    }
}
