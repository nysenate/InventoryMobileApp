package gov.nysenate.inventory.android;

import android.content.SharedPreferences;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class JsonInvObjectRequest extends JsonObjectRequest {
    protected String session_id = "";
    private String TAG = this.getClass().getName();
    private Map<String, String> arguments = null;

    public JsonInvObjectRequest(int method, String url, JSONObject jsonRequest,
                                Response.Listener listener, Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
    }

    public JsonInvObjectRequest(int method, String url, JSONObject jsonRequest,
                                Response.Listener listener) {
        super(method, url, jsonRequest, listener, InvApplication.getInstance().errorListener);
    }

    public JsonInvObjectRequest(String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener) {
        super(url, jsonRequest, listener, InvApplication.getInstance().errorListener);
    }

    public JsonInvObjectRequest(String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(url, jsonRequest, listener, errorListener);
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        // since we don't know which of the two underlying network vehicles
        // will Volley use, we have to handle and store session cookies manually

        InvApplication.getInstance().checkSessionCookie(response.headers);

        return super.parseNetworkResponse(response);
    }

    /* (non-Javadoc)
     * @see com.android.volley.Request#getHeaders()
     */
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {


        Map<String, String> headers = super.getHeaders();

        SharedPreferences settings = InvApplication.getInstance().getSharedPreferences();

        String auth_token_string = settings.getString("token", "");

        if (headers == null
                || headers.equals(Collections.emptyMap())) {
            headers = new HashMap<String, String>();
            headers.put("Content-Type", "application/json; charset=UTF-8");
            headers.put("Authorization", "Basic " + auth_token_string);
        }

        InvApplication.getInstance().addSessionCookie(headers);

        return headers;
    }

    public void setArguments(Map<String, String> arguments) {
        this.arguments = arguments;
    }

    public Map<String, String> getArguments() {
        return arguments;
    }

}