package gov.nysenate.inventory.android;

import gov.nysenate.inventory.activity.LoginActivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import android.os.AsyncTask;

public class EmployeeListRequest extends AsyncTask<String, String, String> {

    @Override
    protected String doInBackground(String... uri) {
        HttpClient httpclient = LoginActivity.httpClient;
        HttpResponse response;
        String responseString = null;
        try {
            StringBuilder urls = new StringBuilder();
            urls.append(uri[0].trim());
            if (uri[0].indexOf("?") > -1) {
                if (!uri[0].trim().endsWith("?")) {
                    urls.append("&");
                }
            }
            else {
                urls.append("?");
            }
            urls.append("userFallback=");
            urls.append(LoginActivity.nauser);
            response = httpclient.execute(new HttpGet(urls.toString()));
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                responseString = out.toString();
            }
            else {
                // Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        }
        catch (ClientProtocolException e) {
            // TODO Handle problems..
        }
        catch (IOException e) {
            // TODO Handle problems..
        }
        return responseString;
    }
}
