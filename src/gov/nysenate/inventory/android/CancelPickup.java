package gov.nysenate.inventory.android;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import gov.nysenate.inventory.model.Pickup;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.widget.Adapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class CancelPickup extends SenateActivity {

    private Pickup pickup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancel_pickup);
        registerBaseActivityReceiver();

        TextView originLocation = (TextView) findViewById(R.id.tv_origin_pickup_cancel);
        TextView destinationLocation = (TextView) findViewById(R.id.tv_destination_pickup_cancel);
        TextView itemCount = (TextView) findViewById(R.id.tv_count_pickup_cancel);
        TextView comments = (TextView) findViewById(R.id.cancel_pickup_comments);
        TextView naPickupBy = (TextView) findViewById(R.id.cancel_pickup_sign);
        ListView items = (ListView) findViewById(R.id.cancel_pickup_listview);

        pickup = getIntent().getParcelableExtra("pickup");
        Adapter pickupListAdapter = new InvListViewAdapter(this, R.layout.invlist_item, pickup.getPickupItems());
        items.setAdapter((ListAdapter) pickupListAdapter);

        originLocation.setText(pickup.getOriginAddressLine1());
        destinationLocation.setText(pickup.getDestinationAddressLine1());
        itemCount.setText(Integer.toString(pickup.getPickupItems().size()));
        comments.setText(pickup.getComments());
        naPickupBy.setText(pickup.getNaPickupBy());
    }

    public void continueButton(View view) {
        if (checkServerResponse(true) == OK) {
            AlertDialog.Builder confirmDialog = new AlertDialog.Builder(this);
            confirmDialog.setTitle(Html.fromHtml("<font color='#000055'>Cancel Pickup</font>"));
            confirmDialog.setMessage(Html.fromHtml("You are about to <b>cancel</b> this pickup.<br><br>"
                    + "Are you sure you want to continue?"));
            confirmDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    return;
                }
            });
            confirmDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    new CancelPickupTask().execute();
                }
            });
            confirmDialog.show();
        }
    }

    public void backButton(View view) {
        if (checkServerResponse(true) == OK) {
            super.onBackPressed();
        }
    }
    
    public void displayShortToast(CharSequence text) {
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private class CancelPickupTask extends AsyncTask<Void, Void, Integer> {

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
            // TODO: move this -------------
            if (!url.endsWith("/")) {
                url += "/";
            }
            // -----------------------------
            url += "/CancelPickup?nuxrpd=" + pickup.getNuxrpd();
            url += "&userFallback=" + LoginActivity.nauser;

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
            Intent intent = new Intent(CancelPickup.this, Move.class);
            startActivity(intent);
            overridePendingTransition(R.anim.in_right, R.anim.out_left);
            if (response == HttpStatus.SC_OK) {
                displayShortToast("Successfully updated database");
            } else if (response == HttpStatus.SC_BAD_REQUEST) {
                displayShortToast("!!ERROR: Invalid nuxrpd");
            } else if (response == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                displayShortToast("!!ERROR: Database Error, your update may not have been saved.");
            } else {
                displayShortToast("!!ERROR: Unknown Error, your update may not have been saved.");
            }
        }
    }
}
