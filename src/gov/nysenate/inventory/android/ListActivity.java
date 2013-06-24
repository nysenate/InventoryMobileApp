package gov.nysenate.inventory.android;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ListActivity extends SenateActivity
{
    public String res = null;
    public String status = null;
    public String barcode;
    public int barcode_number;
    public String loc_code = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        registerBaseActivityReceiver();

        ListView listview = (ListView) findViewById(R.id.preferenceList);
        ArrayList<verList> list = new ArrayList<verList>();
        ArrayList<StringBuilder> dispList = new ArrayList<StringBuilder>();
        int number;
        // check network connection
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // fetch data
            status = "yes";

            // Get the URL from the properties
            String URL = MainActivity.properties.get("WEBAPP_BASE_URL")
                    .toString();

            AsyncTask<String, String, String> resr1 = new RequestTask()
                    .execute(URL + "/ItemsList?loc_code=" + loc_code);
            try {

                String jsonString = resr1.get().trim().toString();
                JSONArray jsonArray = new JSONArray(jsonString);
                number = jsonArray.length();
                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jo = new JSONObject();
                    jo = jsonArray.getJSONObject(i);
                    verList vl = new verList();
                    vl.NUSENATE = jo.getString("NUSENATE");
                    vl.CDCATEGORY = jo.getString("CDCATEGORY");
                    vl.DECOMMODITYF = jo.getString("DECOMMODITYF");

                    list.add(vl);
                    StringBuilder s = new StringBuilder();
                    s.append(vl.NUSENATE);
                    s.append(" ");
                    s.append(vl.CDCATEGORY);
                    s.append(" ");
                    s.append(vl.DECOMMODITYF);

                    dispList.add(i, s);
                }

            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            status = "yes1";
        } else {
            // display error
            status = "no";
        }

        // to delete an element from the list

        ArrayAdapter<StringBuilder> adapter = new ArrayAdapter<StringBuilder>(
                this, android.R.layout.simple_list_item_1, dispList);

        listview.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_list, menu);
        return true;
    }

    /*
     * @Override public boolean onOptionsItemSelected(MenuItem item) {
     * 
     * switch (item.getItemId()) { case android.R.id.home: Toast toast =
     * Toast.makeText(getApplicationContext(), "Going Back",
     * Toast.LENGTH_SHORT); toast.setGravity(Gravity.CENTER, 0, 0);
     * toast.show(); NavUtils.navigateUpFromSameTask(this);
     * overridePendingTransition(R.anim.in_left, R.anim.out_right); return true;
     * default: return super.onOptionsItemSelected(item); } }
     */

    class RequestTask extends AsyncTask<String, String, String>
    {

        @Override
        protected String doInBackground(String... uri) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            String responseString = null;
            try {
                response = httpclient.execute(new HttpGet(uri[0]));
                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    responseString = out.toString();
                } else {
                    // Closes the connection.
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            } catch (ClientProtocolException e) {
                // TODO Handle problems..
            } catch (IOException e) {
                // TODO Handle problems..
            }
            res = responseString;
            return responseString;

        }
    }

    public class verList
    {
        String NUSENATE;
        String CDCATEGORY;
        String DECOMMODITYF;
    }

}
