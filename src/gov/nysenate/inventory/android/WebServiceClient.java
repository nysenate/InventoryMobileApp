package gov.nysenate.inventory.android;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

public class WebServiceClient
{
    JSONObject json = new JSONObject();
    ArrayList<String> urlParameters = new ArrayList();
    ArrayList<String> urlParamValues = new ArrayList();
    String apiURL = null;
    String baseURL = null;

    public void setValue(String name, Object value) throws JSONException {
        json.put(name, value);
    }

    public Object getValue(String name) throws JSONException {
        return json.get(name);
    }

    public void addURLParameter(String name, String value) {
        urlParameters.add(name);
        urlParamValues.add(value);
    }

    public String getURLParameter(String name) {
        int foundAt = this.indexOf(name, urlParameters);

        if (foundAt == -1 || urlParamValues == null
                || urlParamValues.size() - 1 < foundAt) {
            return null;
        } else {
            return urlParamValues.get(foundAt);
        }
    }

    public void clearURLParameters() {
        urlParameters = new ArrayList();
        urlParamValues = new ArrayList();
    }

    public void clearDataAndURLParams() {
        clearURLParameters();
        clearData();
    }

    private int indexOf(String name, ArrayList<String> arrayList) {
        if (arrayList == null || arrayList.size() == 0) {
            return -1;
        }

        for (int x = 0; x < arrayList.size(); x++) {
            if (arrayList.get(x).equals(name)) {
                return x;
            }
        }
        return -1;

    }

    public void clearData() {
        json = new JSONObject();
    }

    public String get() {
        String response = null;
        return response;
    }

    public String put() {
        String response = null;
        return response;
    }

    public String post() {
        String response = null;
        return response;
    }

}
