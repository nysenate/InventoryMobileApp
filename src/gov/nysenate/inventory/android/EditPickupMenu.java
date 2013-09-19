package gov.nysenate.inventory.android;

import gov.nysenate.inventory.model.Pickup;
import gov.nysenate.inventory.util.JSONParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONException;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class EditPickupMenu extends SenateActivity implements OnItemClickListener
{
    private Pickup pickup;
    private ProgressBar progressBar;
    private List<RowItem> menuRowItems;
    private static final String[] titles = { "Cancel Pickup", " Change Destination", "Change Origin",
            "Remove Items", "Move Menu" };
    private static final Integer[] images = { R.drawable.ssearch, R.drawable.ssearch, R.drawable.ssearch,
            R.drawable.ssearch, R.drawable.slogout };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_pickup_menu);
        registerBaseActivityReceiver();
        progressBar = (ProgressBar) findViewById(R.id.edit_pickup_menu_progress_bar);
        ListView menuRowsListView = (ListView) findViewById(R.id.edit_pickup_menu_list);

        pickup = new Pickup();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            new GetPickup().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            new GetPickup().execute();
        }

        menuRowItems = new ArrayList<RowItem>();
        for (int i = 0; i < titles.length; i++) {
            menuRowItems.add(new RowItem(images[i], titles[i]));
        }

        CustomListViewAdapter adapter = new CustomListViewAdapter(this, R.layout.list_item, menuRowItems);
        menuRowsListView.setAdapter(adapter);
        menuRowsListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        RowItem selected = menuRowItems.get(position);
        if (checkServerResponse(true) == OK) {
            if (selected.getTitle().equalsIgnoreCase(titles[0])) {
                startActivity(CancelPickup.class);

            } else if (selected.getTitle().equalsIgnoreCase(titles[1])) {
                startActivity(ChangePickupDestination.class);

            } else if (selected.getTitle().equalsIgnoreCase(titles[2])) {
                startActivity(ChangePickupOrigin.class);

            } else if (selected.getTitle().equalsIgnoreCase(titles[3])) {
                startActivity(RemovePickupItems.class);

            } else if (selected.getTitle().equalsIgnoreCase(titles[4])) {
                startActivity(Move.class);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_menu, menu);
        return true;
    }

    private void startActivity(Class<?> activity) {
        Intent intent = new Intent(this, activity);
        intent.putExtra("pickup", pickup);
        startActivity(intent);
        overridePendingTransition(R.anim.in_right, R.anim.out_left);
    }

    private class GetPickup extends AsyncTask<Void, Void, Integer> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected Integer doInBackground(Void... arg0) {
            if (checkServerResponse(true) != OK) {
                return 0;
            }
            HttpClient httpClient = LoginActivity.getHttpClient();
            HttpResponse response = null;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            String url = (String) LoginActivity.properties.get("WEBAPP_BASE_URL");
            if (!url.endsWith("/")) {
                url += "/";
            }
            url += "/GetPickup?nuxrpd=" + getIntent().getStringExtra("nuxrpd");
            url += "&userFallback=" + LoginActivity.nauser;

            try {
                response = httpClient.execute(new HttpGet(url));
                response.getEntity().writeTo(out);
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                pickup = JSONParser.parsePickup(out.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return response.getStatusLine().getStatusCode();
        }

        @Override
        protected void onPostExecute(Integer response) {
            progressBar.setVisibility(ProgressBar.INVISIBLE);
            if (response == HttpStatus.SC_OK) {
                return;
            } else if (response == HttpStatus.SC_BAD_REQUEST) {
                displayShortToast("!!ERROR: Unable to get pickup info, invalid nuxrpd.");
            } else if (response == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                displayShortToast("!!ERROR: Database Error while trying to get pickup info.");
            } else {
                displayShortToast("!!ERROR: Unknown Error occured pickup data may be inaccurate.");
            }
        }
    }

    public void displayShortToast(CharSequence text) {
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
