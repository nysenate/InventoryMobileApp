package gov.nysenate.inventory.android;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.conn.ConnectTimeoutException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Properties;

import gov.nysenate.inventory.activity.LoginActivity;
import gov.nysenate.inventory.activity.SenateActivity;
import gov.nysenate.inventory.util.HttpUtils;
import gov.nysenate.inventory.util.Toasty;

public class RequestTask extends AsyncTask<String, String, String> {
    public final int GET = -2;
    public final int POST = -3;
    Hashtable<String, String> nameValuePairs;

    public static final int SC_SESSION_TIMEOUT = 599;

    Properties properties;

    public int currentMode = -2;

    boolean allowURLModification = true;

    public RequestTask() {
        this(true);
    }

    public RequestTask(boolean allowURLModification) {
        this(-2, allowURLModification);
    }

    public RequestTask(int mode) {
        this(-2, true);
    }

    public RequestTask(int mode, boolean allowURLModification) {
        this.currentMode = currentMode;
        this.allowURLModification = allowURLModification;
    }

    public RequestTask(Hashtable<String, String> nameValuePairs) {
        this(nameValuePairs, true);
    }

    public RequestTask(String name, String value, boolean allowURLModification) {
        this.currentMode = POST;
        this.nameValuePairs.put(name, value);
        this.allowURLModification = allowURLModification;
    }

    public RequestTask(Hashtable<String, String> nameValuePairs,
                       boolean allowURLModification) {
        this.currentMode = POST;
        this.nameValuePairs = nameValuePairs;
        this.allowURLModification = allowURLModification;
    }

    @Override
    public String doInBackground(String... uri) {
        String res = "";

        HttpUtils httpUtils = new HttpUtils();
        Log.i(this.getClass().getName(), "doInBackground before !httpUtils.isOnline()");

        if (!httpUtils.isOnline()) {
            Log.i("RequestTask", "App is offline for:" + LoginActivity.WEBAPP_BASE_URL);
            try {
                new Toasty(SenateActivity.stContext).showMessage("!!ERROR: App is Offline.");
            } catch (Exception e) {
                Log.i("RequestTask", "!!ERROR: Could not show toasty message that App is Offline!!");
            }

            httpUtils.playSound(R.raw.noconnect);
            Log.i(this.getClass().getName(), "doInBackground after returning blank");
            return "";
        }

        String responseString = null;
        StringBuilder url = new StringBuilder();
        String currentURI = uri[0].trim();
        new HttpUtils().updateOffline(uri[0]);  // testing only

        if (allowURLModification) {
            if (!currentURI.toLowerCase().startsWith("http")) {
                Log.i("RequestTask",
                        "(2)allowURLModification=true MISSING HTTP");
                String URL = null;
                try {
                    URL = LoginActivity.properties.get("WEBAPP_BASE_URL")
                            .toString().trim();
                    Log.i("RequestTask",
                            "(3)allowURLModification=true URL to add:" + URL);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                if (URL.endsWith("/")) {
                    if (currentURI.startsWith("/")) {
                        currentURI = currentURI.substring(1);
                        Log.i("RequestTask",
                                "(4)allowURLModification=true currentURI:" + currentURI);
                    }
                    url.append(URL);
                } else {
                    if (!currentURI.startsWith("/")) {
                        url.append(URL);
                        url.append("/");
                        Log.i("RequestTask",
                                "(5)allowURLModification=true url added:" + url.toString());
                    } else {
                        url.append(URL);
                    }

                }

            }
        }
        Log.i(this.getClass().getName(), "doInBackground after a bunch of stuff");

        url.append(currentURI);

        if (uri[0].indexOf("?") > -1) {
            if (!uri[0].trim().endsWith("?")) {
                url.append("&");
            }
        } else {
            url.append("?");
        }

        url.append("userFallback=");
        url.append(LoginActivity.nauser);
        Log.i(this.getClass().getName(), "doInBackground after more stuff");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            URL urlAddr = null;
            urlAddr = new URL(url.toString());

            HttpURLConnection urlConnection = (HttpURLConnection) urlAddr.openConnection();

            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setRequestProperty("Content-Language", "en-US");
            urlConnection.setRequestProperty("Cookie", LoginActivity.cookie);

            Log.i(this.getClass().getName(), "doInBackground BEFORE POST/GET");
            if (currentMode == POST) {
                Log.i(this.getClass().getName(), "Current Mode: POST: " + url.toString());
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                if (LoginActivity.cookie != null) {

                }
                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(HttpUtils.getPostParamString(nameValuePairs));
                writer.flush();
                writer.close();
                os.close();

                //HttpPost httpPost = new HttpPost(url.toString());
                //httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                //response = httpClient.execute(httpPost);
            } else {
                Log.i(this.getClass().getName(), "Current Mode: GET: " + url.toString());
                urlConnection.setRequestMethod("GET");
                //HttpGet httpGet = new HttpGet(url.toString());
                //response = httpClient.execute(httpGet);
            }
            int responseCode = urlConnection.getResponseCode();

            Log.i(this.getClass().getName(), "Response Code:" + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) { // connection ok
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                responseString = response.toString();
            } else {
                return "";
            }

            //  Log.i(this.getClass().getName(), "doInBackground AFTER POST/GET");

            //StatusLine statusLine = response.getStatusLine();
            //Log.i(this.getClass().getName(), "doInBackground AFTER POST/GET statusLine:"+statusLine);
            //response.getEntity().writeTo(out);
            //Log.i(this.getClass().getName(), "doInBackground AFTER  response.getEntity().writeTo");
            //responseString = out.toString();
            if (responseString == null && responseCode == SC_SESSION_TIMEOUT) {
                responseString = "Session timed out";
            }

            Log.w("HTTP1:", urlConnection.getContent() + ":" + responseCode);

            if (responseString == null && responseCode == SC_SESSION_TIMEOUT) {
                responseString = "Session timed out";
            }

        } catch (ClientProtocolException e) {
            // TODO Handle problems..
            Log.w("HTTP2:", e);
        } catch (ConnectTimeoutException e) {
            return "**WARNING: Server Connection timeout";
        } catch (SocketTimeoutException e) {
            return "**WARNING: Server Socket timeout";
        } catch (IOException e) {
            // TODO Handle problems..
            Log.w("HTTP3:", e);
        } catch (Exception e) {
            Log.w("HTTP4:", e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    Log.w("HTTP5:", e);
                }
            }
        }
        Log.i(this.getClass().getName(), "doInBackground ALMOST DONE:" + responseString);
        res = responseString;
        Log.i(this.getClass().getName(), "doInBackground ALMOST DONE");
        return responseString;

    }


}
