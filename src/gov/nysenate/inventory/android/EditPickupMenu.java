package gov.nysenate.inventory.android;

import gov.nysenate.inventory.model.Pickup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;

public class EditPickupMenu extends SenateActivity implements OnItemClickListener
{
    private Pickup pickup;
    private static ProgressBar progressBar;
    private List<RowItem> menuRowItems;
    private static final String[] titles = { "Cancel Pickup", " Change Destination", "Change Origin",
            "Remove Items", "Move Menu" };
    private static final Integer[] images = { R.drawable.ssearch, R.drawable.ssearch, R.drawable.ssearch,
            R.drawable.ssearch, R.drawable.slogout };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_pickup_menu);
        progressBar = (ProgressBar) findViewById(R.id.edit_pickup_menu_progress_bar);
        ListView menuRowsListView = (ListView) findViewById(R.id.edit_pickup_menu_list);

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
                progressBar.setVisibility(View.VISIBLE);
                startActivity(CancelPickup.class);

            } else if (selected.getTitle().equalsIgnoreCase(titles[1])) {
                progressBar.setVisibility(View.VISIBLE);
                startActivity(ChangePickupDestination.class);

            } else if (selected.getTitle().equalsIgnoreCase(titles[2])) {
                progressBar.setVisibility(View.VISIBLE);
                startActivity(ChangePickupOrigin.class);

            } else if (selected.getTitle().equalsIgnoreCase(titles[3])) {
                progressBar.setVisibility(View.VISIBLE);
                startActivity(RemovePickupItems.class);

            } else if (selected.getTitle().equalsIgnoreCase(titles[4])) {
                progressBar.setVisibility(View.VISIBLE);
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
        startActivity(intent);
        overridePendingTransition(R.anim.in_right, R.anim.out_left);
    }

    private class GetPickup extends AsyncTask<Void, Void, Integer> {

        ProgressBar progressBar;

        @Override
        protected void onPreExecute() {
            progressBar = (ProgressBar) findViewById(R.id.pickup_cancel_progress_bar);
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected Integer doInBackground(Void... arg0) {
            HttpClient httpClient = LoginActivity.getHttpClient();
            HttpResponse response;
            String url = (String) LoginActivity.properties.get("WEBAPP_BASE_URL");
            if (!url.endsWith("/")) {
                url += "/";
            } // TODO set servlet info etc..
              // url += "/CancelPickup?nuxrpd=" + pickup.getNuxrpd();
              // url += "&userFallback=" + LoginActivity.nauser;

            try {
                response = httpClient.execute(new HttpGet(url));
                return response.getStatusLine().getStatusCode();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer response) {
            progressBar.setVisibility(ProgressBar.INVISIBLE);
        }
    }
}
