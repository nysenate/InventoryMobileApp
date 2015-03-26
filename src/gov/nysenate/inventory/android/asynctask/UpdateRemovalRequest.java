package gov.nysenate.inventory.android.asynctask;

import android.os.AsyncTask;
import android.widget.ProgressBar;
import gov.nysenate.inventory.activity.LoginActivity;
import gov.nysenate.inventory.model.RemovalRequest;
import gov.nysenate.inventory.util.Serializer;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class UpdateRemovalRequest extends AsyncTask<String, Void, RemovalRequest>
{
    public interface UpdateRemovalRequestI {
        public void onRemovalRequestUpdated(RemovalRequest rr);
    }

    private RemovalRequest rr;
    private UpdateRemovalRequestI handler;
    private ProgressBar bar;

    public UpdateRemovalRequest(RemovalRequest rr, UpdateRemovalRequestI handler) {
        this.rr = rr;
        this.handler = handler;
    }

    public void setProgressBar(ProgressBar bar) {
        this.bar = bar;
    }

    @Override
    protected void onPreExecute() {
        if (bar != null) {
            bar.setVisibility(ProgressBar.VISIBLE);
        }
    }

    @Override
    protected RemovalRequest doInBackground(String... urls) {
        HttpPost httpPost = new HttpPost(urls[0]);
        List<NameValuePair> values = new ArrayList<NameValuePair>();
        values.add(new BasicNameValuePair("RemovalRequest", Serializer.serialize(rr)));

        DefaultHttpClient httpClient = new DefaultHttpClient();
        CookieStore store = LoginActivity.getHttpClient().getCookieStore();
        HttpContext context = new BasicHttpContext();
        context.setAttribute(ClientContext.COOKIE_STORE, store);

        RemovalRequest removalRequest = null;
        HttpResponse response = null;
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(values));
            response = httpClient.execute(httpPost, context);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            response.getEntity().writeTo(out);
            removalRequest = Serializer.deserialize(out.toString(), RemovalRequest.class).get(0);

            response.getEntity().consumeContent();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return removalRequest;
    }

    @Override
    protected void onPostExecute(RemovalRequest rr) {
        if (bar != null) {
            bar.setVisibility(ProgressBar.GONE);
        }
        handler.onRemovalRequestUpdated(rr);
    }
}