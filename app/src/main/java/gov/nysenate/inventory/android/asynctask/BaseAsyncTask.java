package gov.nysenate.inventory.android.asynctask;

import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import gov.nysenate.inventory.activity.LoginActivity;
import gov.nysenate.inventory.android.AppSingleton;
import gov.nysenate.inventory.android.InvApplication;

public abstract class BaseAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
    @Override
    protected Result doInBackground(Params... urls) {

// prepare the Request
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, (String) urls[0], null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
                        Log.d("Response", response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.getMessage() + " [" + error.getCause() + "]");
                    }
                }
        );

// add it to the RequestQueue

// Get a RequestQueue

        AppSingleton.getInstance(InvApplication.getAppContext()).addToRequestQueue(getRequest);

        return null;//result;
    }

    /**
     * Here is where individual implementations convert the response (usually json) into the Result type.
     *
     * @param response
     * @param responseCode
     * @return
     */
    public abstract Result handleBackgroundResult(String response, int responseCode);

    protected void onPostExecute(Boolean result) {
        LoginActivity.activeAsyncTask = null;
        // execution of result of Long time consuming operation
    }

}
