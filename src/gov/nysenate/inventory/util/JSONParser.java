package gov.nysenate.inventory.util;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import gov.nysenate.inventory.android.InvItem;
import gov.nysenate.inventory.model.Location;
import gov.nysenate.inventory.model.Pickup;

public class JSONParser {

    public static Pickup parsePickup(String json) throws JSONException {
        Pickup pickup = new Pickup();
        JSONObject obj = (JSONObject) new JSONTokener(json).nextValue();
        ArrayList<InvItem> pickupItems = parsePickupItems(obj.getJSONArray("pickupItems"));
        Location origin = parseLocation(obj.getJSONObject("origin"));
        Location destination = parseLocation(obj.getJSONObject("destination"));

        pickup.setNaPickupBy(obj.getString("napickupby"));
        pickup.setNaReleaseBy(obj.getString("nareleaseby"));
        pickup.setNuxrRelSign(obj.getString("nuxrrelsign"));
        pickup.setDate(obj.getString("date"));
        pickup.setNuxrpd(Integer.valueOf(obj.getString("nuxrpd")));
        pickup.setPickupItems(pickupItems);
        pickup.setOrigin(origin);
        pickup.setDestination(destination);

        return pickup;
    }

    private static ArrayList<InvItem> parsePickupItems(JSONArray json) throws JSONException {
        ArrayList<InvItem> items = new ArrayList<InvItem>();
        for (int i = 0; i < json.length(); i++) {
            InvItem aItem = new InvItem();
            JSONObject obj = json.getJSONObject(i);
            aItem.setDecommodityf(obj.getString("decommodityf"));
            aItem.setType(obj.getString("type"));
            aItem.setNusenate(obj.getString("nusenate"));
            aItem.setCdcategory(obj.getString("cdcategory"));
            aItem.setCdlocat(obj.getString("cdcategory"));
            aItem.setCdintransit(obj.getString("cdintransit"));
            aItem.setDecomments(obj.getString("decomments"));
            aItem.setCdcommodity(obj.getString("cdcommodity"));
            aItem.setSelected(Boolean.parseBoolean(obj.getString("selected")));
            items.add(aItem);
        }
        return items;
    }

    private static Location parseLocation(JSONObject json) throws JSONException {
        Location loc = new Location();
        loc.setCdlocat(json.getString("cdlocat"));
        loc.setCdloctype(json.getString("cdloctype"));
        loc.setAddressLine1(json.getString("adstreet1"));
        loc.setCity(json.getString("adcity"));
        loc.setZip(json.getString("adzipcode"));

        return loc;
    }
}
