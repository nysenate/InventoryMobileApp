package gov.nysenate.inventory.util;

import com.google.gson.*;
import gov.nysenate.inventory.model.RemovalRequest;

import java.util.ArrayList;
import java.util.List;

public class RemovalRequestParser
{
    public static List<RemovalRequest> parseRemovalRequests(String json) {
        List<RemovalRequest> requests = new ArrayList<RemovalRequest>();
        JsonParser parser = new JsonParser();
        JsonArray objs = parser.parse(json).getAsJsonArray();
        for (JsonElement e : objs) {
            requests.add(parseRemovalRequest(e.toString()));
        }
        return requests;
    }

    public static RemovalRequest parseRemovalRequest(String json) {
        JsonParser parser = new JsonParser();
        JsonObject obj = parser.parse(json).getAsJsonObject();

        Gson gson = new Gson();
        return gson.fromJson(obj, RemovalRequest.class);
    }
}
