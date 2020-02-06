package gov.nysenate.inventory.android.asynctask;

import android.os.AsyncTask;
import gov.nysenate.inventory.activity.LoginActivity;
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

public abstract class BaseAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result>
{
    @Override
    protected Result doInBackground(Params... urls) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CookieStore store = LoginActivity.getHttpClient().getCookieStore();
        HttpContext context = new BasicHttpContext();
        context.setAttribute(ClientContext.COOKIE_STORE, store);

        Result result = null;
        HttpResponse response = null;
        try {
            response = new DefaultHttpClient().execute(new HttpGet((String) urls[0]), context);
            response.getEntity().writeTo(out);
            result = handleBackgroundResult(out.toString(), response.getStatusLine().getStatusCode());
            response.getEntity().consumeContent();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Here is where individual implementations convert the response (usually json) into the Result type.
     * @param response
     * @param responseCode
     * @return
     */
    public abstract Result handleBackgroundResult(String response, int responseCode);
}
