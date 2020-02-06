package gov.nysenate.inventory.util;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import gov.nysenate.inventory.dto.ItemInventoriedDetails;

public class Serializer {

    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();

    /**
     * Serialize an object into a json string
     */
    public static <T> String serialize(T type) {
        return gson.toJson(type);
    }

    /**
     * Constructs a list of objects from a json string.
     *
     * @param json  The json string.
     * @param clazz The type of objects.
     * @param <T>
     * @return
     */
    public static <T> List<T> deserialize(String json, Class<T> clazz) {
        List<T> results = new ArrayList<>();
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(json);
        if (element.isJsonObject()) {
            results.add(gson.fromJson(json, clazz));
        } else {
            JsonArray array = element.getAsJsonArray();
            for (int i = 0; i < array.size(); i++) {
                results.add(gson.fromJson(array.get(i).toString(), clazz));
            }
        }
        return results;
    }
}
