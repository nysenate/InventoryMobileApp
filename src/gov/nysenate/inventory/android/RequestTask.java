package gov.nysenate.inventory.android;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.util.Log;

public class RequestTask extends AsyncTask<String, String, String>
{
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
        url.append(uri[0].trim());
        if (uri[0].indexOf("?") > -1) {
            if (!uri[0].trim().endsWith("?")) {
                url.append("&");
            }
        } else {
            url.append("?");
        }
        url.append("userFallback=");
        url.append(LoginActivity.nauser);

        try {
            response = httpClient.execute(new HttpGet(url.toString()));
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                responseString = out.toString();
            } else {
                // Closes the connection.
                Log.w("HTTP1:", statusLine.getReasonPhrase());
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (ClientProtocolException e) {
            // TODO Handle problems..
            Log.w("HTTP2:", e);
        } catch (IOException e) {
            // TODO Handle problems..
            Log.w("HTTP3:", e);
        } catch (Exception e) {
            Log.w("HTTP4:", e);
        }
        res = responseString;
        return responseString;

    }
}
