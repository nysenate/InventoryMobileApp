package gov.nysenate.inventory.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

public class Serializer {

    private static final Gson gson = new Gson();

    public static <T> String serialize(T type) {
        return gson.toJson(type);
    }

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
