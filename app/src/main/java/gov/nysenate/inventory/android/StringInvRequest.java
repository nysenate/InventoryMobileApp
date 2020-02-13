package gov.nysenate.inventory.android;

import android.content.SharedPreferences;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class StringInvRequest extends StringRequest {
    protected String session_id = "";
    private String TAG = this.getClass().getName();
    private Map<String, String> params;
    private Map<String, String> arguments = null;
    private boolean fireAfterResponse = false;
    private boolean fireAfterError = false;

    /**
     * @param method
     * @param url
     * @param params        A {@link HashMap} to post with the request. Null is allowed
     *                      and indicates no parameters will be posted along with request.
     * @param listener
     * @param errorListener
     */
    public StringInvRequest(int method, String url, Map<String, String> params, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method, url, params, listener, errorListener);
        this.params = params;
    }

    public StringInvRequest(int method, String url, Map<String, String> params, Response.Listener<String> listener) {
        super(method, url, params, listener, InvApplication.getInstance().errorListener);
        this.params = params;
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
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

    @Override
    protected Map<String, String> getParams() {
        return params;
    }

    ;


}
