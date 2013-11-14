package gov.nysenate.inventory.util;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import gov.nysenate.inventory.model.Transaction;

public class TransactionParser {

    private static final Gson gson = new Gson();

    public static Transaction parseTransaction(String json) {
        Transaction pickup = new Transaction();
        JsonParser parser = new JsonParser();
        JsonObject obj = parser.parse(json).getAsJsonObject();
        pickup = gson.fromJson(obj, Transaction.class);
        return pickup;
    }

    // TODO: for when we call Gson.toJson(Collection<Transaction>) in server -> returns it all in one string... SHould use below!!!!!
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
}
