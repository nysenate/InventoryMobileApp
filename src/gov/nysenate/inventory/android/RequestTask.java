package gov.nysenate.inventory.android;

import gov.nysenate.inventory.activity.LoginActivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.util.Log;

public class RequestTask extends AsyncTask<String, String, String>
{
    public final int GET = -2;
    public final int POST = -3;
    List<NameValuePair> nameValuePairs;

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

    public RequestTask(NameValuePair nameValuePair) {
        this(nameValuePair, true);
    }

    public RequestTask(NameValuePair nameValuePair, boolean allowURLModification) {
        this.currentMode = POST;
        this.nameValuePairs = new ArrayList<NameValuePair>();
        this.nameValuePairs.add(nameValuePair);
        this.allowURLModification = allowURLModification;
    }

    public RequestTask(List<NameValuePair> nameValuePairs) {
        this(nameValuePairs, true);
    }

    public RequestTask(List<NameValuePair> nameValuePairs,
            boolean allowURLModification) {
        this.currentMode = POST;
        this.nameValuePairs = nameValuePairs;
        this.allowURLModification = allowURLModification;
    }

    @Override
    public String doInBackground(String... uri) {
        String res = "";
        // HttpClient httpclient = new DefaultHttpClient();
        HttpClient httpClient = LoginActivity.httpClient;
        if (httpClient == null) {
            Log.i(RequestTask.class.getName(),
                    "MainActivity.httpClient was null so it is being reset");
            LoginActivity.httpClient = new DefaultHttpClient();
            httpClient = LoginActivity.httpClient;
        }
        HttpResponse response;
        String responseString = null;
        StringBuilder url = new StringBuilder();
        String currentURI = uri[0].trim();

        if (allowURLModification) {
            // Log.i("RequestTask", "(1)allowURLModification=true");
            if (!currentURI.toLowerCase().startsWith("http")) {
                // Log.i("RequestTask",
                // "(2)allowURLModification=true MISSING HTTP");
                String URL = null;
                try {
                    URL = LoginActivity.properties.get("WEBAPP_BASE_URL")
                            .toString().trim();
                    // Log.i("RequestTask",
                    // "(3)allowURLModification=true URL to add:"+URL);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                if (URL.endsWith("/")) {
                    if (currentURI.startsWith("/")) {
                        currentURI = currentURI.substring(1);
                        // Log.i("RequestTask",
                        // "(4)allowURLModification=true currentURI:"+currentURI);
                    }
                    url.append(URL);
                } else {
                    if (!currentURI.startsWith("/")) {
                        url.append(URL);
                        url.append("/");
                        // Log.i("RequestTask",
                        // "(5)allowURLModification=true url added:"+url.toString());
                    } else {
                        url.append(URL);
                    }

                }

            }
        }
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

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            if (currentMode == POST) {
                HttpPost httpPost = new HttpPost(url.toString());
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                
                response = httpClient.execute(httpPost);
            } else {
                HttpGet httpGet = new HttpGet(url.toString());
                response = httpClient.execute(httpGet);
            }

            StatusLine statusLine = response.getStatusLine();
            response.getEntity().writeTo(out);
            responseString = out.toString();
            if (responseString == null && statusLine.getStatusCode() == SC_SESSION_TIMEOUT) {
                responseString = "Session timed out";
            }

            Log.w("HTTP1:", statusLine.getReasonPhrase() + ":" + statusLine.getStatusCode());
        } catch (ClientProtocolException e) {
            // TODO Handle problems..
            Log.w("HTTP2:", e);
        } catch (ConnectTimeoutException e) {
            return "***WARNING: Server Connection timeout";
        } catch (SocketTimeoutException e) {
            return "***WARNING: Server Socket timeout";
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
        res = responseString;
        return responseString;

    }
}
