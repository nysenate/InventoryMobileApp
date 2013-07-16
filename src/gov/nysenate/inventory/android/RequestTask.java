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
        Log.i(RequestTask.class.getName(), "RequestTask RUNNING");
        Log.i(RequestTask.class.getName(), "RequestTask RUNNING FOR:"+uri[0]);
        //HttpClient httpclient = new DefaultHttpClient();
        HttpClient httpclient = LoginActivity.httpClient;
        Log.i(RequestTask.class.getName(), "RequestTask RUNNING 2");
        if (httpclient==null) {
            Log.i(RequestTask.class.getName(), "MainActivity.httpClient was null so it is being reset");
            LoginActivity.httpClient = new DefaultHttpClient();
            httpclient = LoginActivity.httpClient;
        }
        Log.i(RequestTask.class.getName(), "RequestTask RUNNING 3");
        HttpResponse response;
        String responseString = null;
        StringBuilder url = new StringBuilder();
        Log.i(RequestTask.class.getName(), "RequestTask RUNNING 7");
        url.append(uri[0].trim());
        if (uri[0].indexOf("?")>-1) {
            if (!uri[0].trim().endsWith("?")) {
                url.append("&");
            }
        }
        else {
            url.append("?");
        }
        Log.i(RequestTask.class.getName(), "RequestTask RUNNING 15");
        url.append("userFallback=");
        url.append(LoginActivity.nauser);
        
        Log.i(RequestTask.class.getName(), "RequestTask RUNNING 17");
        try {
            Log.i(RequestTask.class.getName(), "RequestTask RUNNING 18");
            Log.i(RequestTask.class.getName(), "RequestTask RUNNING 18 FOR:"+url.toString());
            
            response = httpclient.execute(new HttpGet(url.toString()));
            Log.i(RequestTask.class.getName(), "RequestTask RUNNING 19");
            StatusLine statusLine = response.getStatusLine();
            Log.i(RequestTask.class.getName(), "RequestTask RUNNING 20");
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                Log.i(RequestTask.class.getName(), "RequestTask RUNNING 21");
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                Log.i(RequestTask.class.getName(), "RequestTask RUNNING 22");
                response.getEntity().writeTo(out);
                Log.i(RequestTask.class.getName(), "RequestTask RUNNING 23");
                out.close();
                Log.i(RequestTask.class.getName(), "RequestTask RUNNING 24");
                responseString = out.toString();
                Log.i(RequestTask.class.getName(), "RequestTask RUNNING 25");
            } else {
                // Closes the connection.
                Log.w("HTTP1:",statusLine.getReasonPhrase());
                response.getEntity().getContent().close();
                Log.i(RequestTask.class.getName(), "RequestTask RUNNING 26");
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (ClientProtocolException e) {
            // TODO Handle problems..
            Log.w("HTTP2:",e );
        } catch (IOException e) {
            // TODO Handle problems..
            Log.w("HTTP3:",e );
        } catch (Exception e) {
            Log.w("HTTP4:",e );
        }        
        Log.i(RequestTask.class.getName(), "RequestTask RUNNING 40");
        res = responseString;
        Log.i(RequestTask.class.getName(), "RequestTask RUNNING 41 DONE:"+responseString);
        return responseString;

    }
}
