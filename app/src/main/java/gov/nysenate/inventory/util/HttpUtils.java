package gov.nysenate.inventory.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.apache.http.HttpStatus;
import org.apache.http.entity.mime.MultipartEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.nysenate.inventory.activity.LoginActivity;
import gov.nysenate.inventory.activity.verification.VerScanActivity;
import gov.nysenate.inventory.android.AppSingleton;
import gov.nysenate.inventory.android.InvApplication;
import gov.nysenate.inventory.android.JsonInvObjectRequest;
import gov.nysenate.inventory.android.SoundAlert;
import gov.nysenate.inventory.android.StringInvRequest;
import gov.nysenate.inventory.android.VolleyCallback;

public class HttpUtils {

    public static final int SC_SESSION_TIMEOUT = 599, KEEPALIVE_TIMEOUT = 103;

    public final int OK = 100, SERVER_SESSION_TIMED_OUT = 1000,
            NO_SERVER_RESPONSE = 1001, EXCEPTION_IN_CODE = 1002;


    static Response.Listener keepAliveRespListener = new Response.Listener<String>() {

        @Override
        public void onResponse(String response) {
        }
    };


    // Displays appropriate messages to user after an attempt to update the database
    public static void displayResponseResults(Context context, Integer code) {
        if (code != null) {
            if (code == HttpStatus.SC_OK) {
                Toasty.displayCenteredMessage(context, "Successfully updated database", Toast.LENGTH_SHORT);
            } else if (code == HttpStatus.SC_BAD_REQUEST) {
                Toasty.displayCenteredMessage(context, "!!ERROR: Invalid parameter", Toast.LENGTH_SHORT);
            } else if (code == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                Toasty.displayCenteredMessage(context, "!!ERROR: Database Error, your update may not have been saved.", Toast.LENGTH_SHORT);
            } else {
                Toasty.displayCenteredMessage(context, "!!ERROR: Unknown Error, your update may not have been saved.", Toast.LENGTH_SHORT);
            }
        } else {
            Toasty.displayCenteredMessage(context, "!!ERROR: Exception when communicating with server, your update may not have been saved.", Toast.LENGTH_SHORT);
        }
    }

    public static void displayQueryResults(Context context, Integer code) {
        if (code == null || code != HttpStatus.SC_OK) {
            Toasty.displayCenteredMessage(context, "Error retrieving location list.", Toast.LENGTH_SHORT);
        }
    }


    public boolean isOnline() {
        /*
            Check to see if isOnline was called on the main ui thread, if it was, we need to call an AsyncTask so that isoonline check
            isn't on the main ui thread.  We call the getInstance simply to wait for the result. This was done because the code will return an
            exception when trying to connect to a URL on the main thread giving the impression that we are offline. Since we don't want
            false positives for being offline, we need to call from another thread.
         */

        if (Looper.myLooper() == Looper.getMainLooper()) {
            AsyncTaskCheckOnline asyncTaskCheckOnline = new AsyncTaskCheckOnline();
            asyncTaskCheckOnline.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            boolean result = false;

            try {
                result = asyncTaskCheckOnline.get();
            } catch (Exception e) {
            }

            return result;
        } else {
            /*
            Otherwise, if it wasn't called from the main ui thread, we can simply call the check on this thread.
             */
            return executeIsonline();
        }
    }

    public boolean executeIsonline() {
        String urlAddr = "";
        try {
            urlAddr = LoginActivity.WEBAPP_BASE_URL;

            if (!urlAddr.endsWith("/")) {
                urlAddr += "/";
            }

            urlAddr += "CheckAppVersion?appName=InventoryMobileApp.apk";

            URL myUrl = new URL(urlAddr);

            HttpURLConnection connection = (HttpURLConnection) myUrl.openConnection();
            connection.setConnectTimeout(1500);
            connection.connect();
//            connection.disconnect();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("HttpUtils", "Isonline failed error:" + e.getMessage() + " URL:" + urlAddr);
            return false;
        }
    }


    public boolean validateIpAddress(final String ipAddress) {
        final String IPADDRESS_PATTERN =
                "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
        Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
        Matcher matcher = pattern.matcher(ipAddress);
        return matcher.matches();
    }

    // Quick shortcut for playing a single sound.
    public void playSound(int sound) {
        int[] sounds = new int[1];
        sounds[0] = sound;
        playSounds(sounds);
    }

    public void playSounds(int[] sounds) {
        // void for now.. Might return error msgs, or completion result in
        // future.
        Object[] soundParams = new Object[sounds.length + 1];
        soundParams[0] = InvApplication.getAppContext();

        if (soundParams[0] == null) {
            new Toasty((Context) soundParams[0]).showMessage("Application Context is NULL!!!!!");
        }

        for (int x = 0; x < sounds.length; x++) {
            soundParams[x + 1] = new Integer(sounds[x]);
        }

        AsyncTask<Object, Integer, String> soundResults = new SoundAlert()
                .execute(soundParams);

    }

    public void updateOffline(String URL) {
        if (URL == null || URL.trim().length() == 0) {
            return;
        }

        Uri uri = Uri.parse(URL);
      /*  Log.i("HttpUtils", "update offline authority:" + uri.getAuthority());
        Log.i("HttpUtils", "update offline encoded authority:" + uri.getEncodedAuthority());
        Log.i("HttpUtils", "update offline encoded Fragment:" + uri.getEncodedFragment());
        Log.i("HttpUtils", "update offline encoded Path:" + uri.getEncodedPath());
        Log.i("HttpUtils", "update offline encoded Query:" + uri.getEncodedQuery());
        Log.i("HttpUtils", "update offline encoded Specific Part:" + uri.getEncodedSchemeSpecificPart());
        Log.i("HttpUtils", "update offline Path:" + uri.getPath());
        Log.i("HttpUtils", "update offline Last Path Segment:" + uri.getLastPathSegment());
        Log.i("HttpUtils", "update offline Scheme Specific Part:" + uri.getSchemeSpecificPart());*/
        Set<String> args = uri.getQueryParameterNames();

        String lastpathsegment = uri.getLastPathSegment();

        if (lastpathsegment.equalsIgnoreCase("LocationDetails")) {
            // location_code=A411F&location_type=W
        } else if (lastpathsegment.equalsIgnoreCase("ItemsList")) {
            // loc_code=A411F
        } else if (lastpathsegment.equalsIgnoreCase("Item")) {
            // barcode=089607&inventoried_date=true
            // barcode=087444
        } else if (lastpathsegment.equalsIgnoreCase("GetAllPickups")) {
            // userFallback=heitner&incompleteRemote=true
        } else if (lastpathsegment.equalsIgnoreCase("KeepSessionAlive")) {
        } else if (lastpathsegment.equalsIgnoreCase("SerialList")) {
        }

    }

    private class AsyncTaskCheckOnline extends AsyncTask<String, String, Boolean> {


        @Override
        protected Boolean doInBackground(String... params) {
            //LoginActivity.activeAsyncTask = this;
            return executeIsonline();
        }


        @Override
        protected void onPostExecute(Boolean result) {
            LoginActivity.activeAsyncTask = null;
            // execution of result of Long time consuming operation
        }


        @Override
        protected void onPreExecute() {
        }


        @Override
        protected void onProgressUpdate(String... text) {

        }
    }

    private static String multipost(String urlString, MultipartEntity reqEntity) {

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.addRequestProperty("Content-length", reqEntity.getContentLength() + "");
            conn.addRequestProperty(reqEntity.getContentType().getName(), reqEntity.getContentType().getValue());

            OutputStream os = conn.getOutputStream();
            reqEntity.writeTo(conn.getOutputStream());
            os.close();
            conn.connect();

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return readStream((InputStream) conn.getInputStream());
            }

        } catch (Exception e) {
            Log.e("MainActivity", "multipart post error " + e + "(" + urlString + ")");
        }
        return null;
    }

    private static String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return builder.toString();
    }

    public static String getPostParamString(Hashtable<String, String> params) {
        if (params.size() == 0)
            return "";

        StringBuffer buf = new StringBuffer();
        Enumeration<String> keys = params.keys();
        while (keys.hasMoreElements()) {
            buf.append(buf.length() == 0 ? "" : "&");
            String key = keys.nextElement();
            buf.append(key).append("=").append(params.get(key));
        }
        return buf.toString();
    }

    public static String GetCookieFromURL(String urlName) throws IOException {

        String result = null;
        HttpURLConnection con = null;

        try {
            URL url = new URL(urlName);
            con = (HttpURLConnection) (url.openConnection());
            con.setConnectTimeout(40000);
            con.setReadTimeout(40000);
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            con.setFollowRedirects(true);
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setUseCaches(false);
            con.setAllowUserInteraction(false);
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("Content-Language", "en-US");
            result = con.getHeaderField("Set-Cookie");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            con.disconnect();
            con = null;
        }
        return result;
    }

    public void getResponse(int method, String url, JSONObject jsonValue, final VolleyCallback callback) {

        RequestQueue queue = AppSingleton.getInstance(gov.nysenate.inventory.util.InvApplication.getAppContext()).getRequestQueue();

        StringRequest strreq = new StringRequest(Request.Method.GET, url, new Response.Listener < String > () {

            @Override
            public void onResponse(String Response) {
                callback.onSuccessResponse(Response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError e) {
                e.printStackTrace();
                Toast.makeText(InvApplication.getAppContext(), e + "error", Toast.LENGTH_LONG).show();
            }
        })
        {
            // set headers
            @Override
            public Map< String, String > getHeaders() throws com.android.volley.AuthFailureError {
                Map < String, String > params = new HashMap< String, String >();
//                params.put("Authorization: Basic", TOKEN);
                return params;
            }
        };

        AppSingleton.getInstance(InvApplication.getAppContext()).addToRequestQueue(strreq);
    }

    private void uploadImage(Bitmap bitmap, String upload_URL){

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject();
            String imgname = String.valueOf(Calendar.getInstance().getTimeInMillis());
            jsonObject.put("name", imgname);
            //  Log.e("Image name", etxtUpload.getText().toString().trim());
            jsonObject.put("image", encodedImage);
            // jsonObject.put("aa", "aa");
        } catch (JSONException e) {
            Log.e("JSONObject Here", e.toString());
        }
        JsonInvObjectRequest jsonInvObjectRequest = (JsonInvObjectRequest) new JsonObjectRequest(Request.Method.POST, upload_URL, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        Log.e("aaaaaaa", jsonObject.toString());
                        AppSingleton.getInstance(InvApplication.getAppContext()).getRequestQueue().getCache().clear();
                       // Toast.makeText(InvApplication.getAppContext(), "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e("aaaaaaa", volleyError.toString());

            }
        });

        AppSingleton.getInstance(InvApplication.getAppContext()).addToRequestQueue(jsonInvObjectRequest);
    }

    public static String ConvertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public static void keepAlive() {
        keepAlive(keepAliveRespListener);
    }

    public static void keepAlive(Response.Listener keepAliveRespListener) {
        // Simply contact the Web Server to keep the Session Alive,
        // to help minimize
        // issues with Session Timeouts

        try {
            String URL = AppProperties.getBaseUrl();

            StringInvRequest stringInvRequest = new StringInvRequest(Request.Method.GET,
                    URL + "/KeepSessionAlive", null, keepAliveRespListener);

            InvApplication.timeoutType = KEEPALIVE_TIMEOUT;

            /* Add your Requests to the RequestQueue to execute */
            AppSingleton.getInstance(InvApplication.getAppContext()).addToRequestQueue(stringInvRequest);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


}
