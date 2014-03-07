package gov.nysenate.inventory.util;

import gov.nysenate.inventory.model.Location;
import gov.nysenate.inventory.model.Transaction;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class TransactionParser {

    private static final Gson gson = new Gson();

    public static Transaction parseTransaction(String json) {
        Transaction pickup = new Transaction();
        JsonParser parser = new JsonParser();
        JsonObject obj = parser.parse(json).getAsJsonObject();
        pickup = gson.fromJson(obj, Transaction.class);
        return pickup;
    }

    public static List<Transaction> parseMultiplePickups(String json) {
        List<Transaction> pickups = new ArrayList<Transaction>();
        JsonParser parser = new JsonParser();
        JsonArray obj = parser.parse(json).getAsJsonArray();
        for (int i = 0; i < obj.size(); i++) {
            pickups.add(parseTransaction(obj.get(i).toString()));
        }
        return pickups;
    }

    public static List<Transaction> parseMultiplePickups(ArrayList<String> json) {
        List<Transaction> pickups = new ArrayList<Transaction>();
        for (int i = 0; i < json.size(); i++) {
            pickups.add(parseTransaction(json.get(i)));
        }
        return pickups;
    }

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
