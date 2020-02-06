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
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
                        Log.d("Response", response.toString());
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.getMessage()+" ["+error.getCause()+"]");
                    }
                }
        );

// add it to the RequestQueue

// Get a RequestQueue
/*        RequestQueue queue = AppSingleton.getInstance(InvApplication.getAppContext()).
                getRequestQueue();*/

        AppSingleton.getInstance(InvApplication.getAppContext()).addToRequestQueue(getRequest);

        //LoginActivity.activeAsyncTask = this;
/*        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CookieStore store = LoginActivity.getHttpClient().getCookieStore();
        HttpContext context = new BasicHttpContext();
        context.setAttribute(ClientContext.COOKIE_STORE, store);

        Result result = null;
        HttpResponse response = null;
        HttpUtils httpUtils = new HttpUtils();
        try {
            if (httpUtils.isOnline()) {

                httpUtils.updateOffline((String) urls[0]);  // Test only
                response = new DefaultHttpClient().execute(new HttpGet((String) urls[0]), context);
                response.getEntity().writeTo(out);
                result = handleBackgroundResult(out.toString(), response.getStatusLine().getStatusCode());
                response.getEntity().consumeContent();
                Log.i("BaseAsyncTask", "App is connected for:" + (String) urls[0]);
            } else {
                Log.i("BaseAsyncTask", "App is offline for:" + (String) urls[0]);
                httpUtils.updateOffline((String) urls[0]);

                try {
                    new Toasty(SenateActivity.stContext).showMessage("!!ERROR: App is Offline.");
                }
                catch (Exception e) {
                    Log.e(this.getClass().getName(), "!!ERROR: Could not show toasty message that App is Offline!!");
                }

                httpUtils.playSound(R.raw.noconnect);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Generic Exception Below Added on 9/18/19
        catch (Exception e) {
            Log.d("BaseAsyncTask", "!!GENERIC ERROR:" + e.getMessage());
            Log.d("BaseAsyncTask", "     RESPONSE:" + response.toString());
        }*/

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
