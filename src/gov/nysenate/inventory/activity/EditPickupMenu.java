package gov.nysenate.inventory.activity;

import gov.nysenate.inventory.adapter.CustomListViewAdapter;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.android.R.anim;
import gov.nysenate.inventory.android.R.drawable;
import gov.nysenate.inventory.android.R.id;
import gov.nysenate.inventory.android.R.layout;
import gov.nysenate.inventory.android.R.menu;
import gov.nysenate.inventory.model.Pickup;
import gov.nysenate.inventory.model.RowItem;
import gov.nysenate.inventory.util.AppProperties;
import gov.nysenate.inventory.util.JSONParser;
import gov.nysenate.inventory.util.Toasty;

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
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class EditPickupMenu extends SenateActivity implements OnItemClickListener
{
    private Pickup pickup;
    private ProgressBar progressBar;
    private List<RowItem> menuRowItems;
    private static final String[] titles = { "Cancel Pickup", "Change Delivery Location", "Change Pickup Location",
            "Remove Items", "Move Menu" };
    private static final Integer[] images = { R.drawable.cancelpickup, R.drawable.editlocdelivery, R.drawable.editlocpickup,
            R.drawable.removeitems, R.drawable.mainmenu };
    private TextView oldPickupLocation;
    private TextView oldDeliveryLocation;
    private TextView oldPickupBy;
    private TextView oldCount;
    private TextView oldDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_pickup_menu);
        registerBaseActivityReceiver();
        progressBar = (ProgressBar) findViewById(R.id.edit_pickup_menu_progress_bar);
        ListView menuRowsListView = (ListView) findViewById(R.id.edit_pickup_menu_list);
        oldPickupLocation = (TextView) findViewById(R.id.old_pickup_location);
        oldDeliveryLocation = (TextView) findViewById(R.id.old_delivery_location);
        oldPickupBy = (TextView) findViewById(R.id.pickup_by);
        oldCount = (TextView) findViewById(R.id.pickup_count);
        oldDate = (TextView) findViewById(R.id.pickup_date);

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

        CustomListViewAdapter adapter = new CustomListViewAdapter(this, R.layout.edit_pickup_menu, menuRowItems);
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
                startActivity(Move.class, R.anim.in_left);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_menu, menu);
        return true;
    }

    private void startActivity(Class<?> activity) {
        startActivity(activity, R.anim.in_right);
    }

    private void startActivity(Class<?> activity, int inTransition) {
        int outTransition = -1;
        switch (inTransition) {
        case R.anim.in_right:
            outTransition = R.anim.out_left;
            break;
        case R.anim.in_left:
            outTransition = R.anim.out_right;
            break;
        case R.anim.in_down:
            outTransition = R.anim.out_down;
            break;
        case R.anim.in_up:
            outTransition = R.anim.out_up;
            break;
        default: 
            inTransition = R.anim.in_right;
            outTransition = R.anim.in_left;
        }
        Intent intent = new Intent(this, activity);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("pickup", pickup);
        intent.putExtra("date", oldDate.getText());
        startActivity(intent);
        overridePendingTransition(inTransition, outTransition);
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
            String url = AppProperties.getBaseUrl(EditPickupMenu.this);
            url += "GetPickup?nuxrpd=" + getIntent().getStringExtra("nuxrpd");
            url += "&userFallback=" + LoginActivity.nauser;

            try {
                response = httpClient.execute(new HttpGet(url));
                response.getEntity().writeTo(out);
                Log.i("EditPickupMenu", "Server Pickup Response:"+out.toString());
                pickup = JSONParser.parsePickup(out.toString());
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return response.getStatusLine().getStatusCode();
        }

        @Override
        protected void onPostExecute(Integer response) {
            progressBar.setVisibility(ProgressBar.INVISIBLE);
            if (response == HttpStatus.SC_OK) {
                oldPickupLocation.setText(pickup.getOriginSummaryString());
                oldDeliveryLocation.setText(pickup.getDestinationSummaryString());
                oldPickupBy.setText(pickup.getNapickupby());
                oldCount.setText(Integer.toString(pickup.getPickupItems().size()));
                oldDate.setText(getIntent().getStringExtra("date"));
            } else if (response == HttpStatus.SC_BAD_REQUEST) {
                Toasty.displayCenteredMessage(EditPickupMenu.this, "!!ERROR: Unable to get pickup info, invalid nuxrpd.", Toast.LENGTH_SHORT);
            } else if (response == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                Toasty.displayCenteredMessage(EditPickupMenu.this, "!!ERROR: Database Error while trying to get pickup info.", Toast.LENGTH_SHORT);
            } else {
                Toasty.displayCenteredMessage(EditPickupMenu.this, "!!ERROR: Unknown Error occured pickup data may be inaccurate.", Toast.LENGTH_SHORT);
            }
        }
    }
}
