package gov.nysenate.inventory.android;

import android.os.AsyncTask;
import android.widget.ProgressBar;
import gov.nysenate.inventory.activity.LoginActivity;
import gov.nysenate.inventory.model.Transaction;
import gov.nysenate.inventory.util.Serializer;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class GetAllPickupsTask extends AsyncTask<Void, Void, Integer> {

    private GetAllPickupsListener listener;
    private String url;
    private ProgressBar bar;
    private List<Transaction> allPickups;

    public GetAllPickupsTask(GetAllPickupsListener listener, String url, List<Transaction> allPickups) {
        this.listener = listener;
        this.url = url;
        this.allPickups = allPickups;
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
    protected Integer doInBackground(Void... arg0) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // We cant use the same HttpClient (LoginActivity.httpClient) in 2 requests at the same time.
        // Therefore we copy the cookies into a new HttpClient.
        DefaultHttpClient httpClient = LoginActivity.getHttpClient();
        CookieStore store = httpClient.getCookieStore();
        HttpContext context = new BasicHttpContext();
        context.setAttribute(ClientContext.COOKIE_STORE, store);
        HttpResponse response = null;
        try {
            response = new DefaultHttpClient().execute(new HttpGet(url), context);
            response.getEntity().writeTo(out);
            List<Transaction> pickups = Serializer.deserialize(out.toString(), Transaction.class);
            allPickups.addAll(pickups);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response.getStatusLine().getStatusCode();
    }

    @Override
    protected void onPostExecute(Integer response) {
        if (bar != null) {
            bar.setVisibility(ProgressBar.INVISIBLE);
        }
        listener.onResponseExecute(response);
    }
}
