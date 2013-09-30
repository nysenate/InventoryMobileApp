package gov.nysenate.inventory.android;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import gov.nysenate.inventory.model.Pickup;
import gov.nysenate.inventory.util.Formatter;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class RemovePickupItems extends SenateActivity {

    Pickup pickup;
    ListView itemList;
    InvSelListViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_pickup_items);
        registerBaseActivityReceiver();

        TextView oldPickupLocation = (TextView) findViewById(R.id.old_pickup_location);
        TextView oldDeliveryLocation = (TextView) findViewById(R.id.old_delivery_location);
        TextView oldPickupBy = (TextView) findViewById(R.id.pickup_by);
        TextView oldCount = (TextView) findViewById(R.id.pickup_count);
        TextView oldDate = (TextView) findViewById(R.id.pickup_date);
        itemList = (ListView) findViewById(R.id.remove_list);

        pickup = getIntent().getParcelableExtra("pickup");
        String date = getIntent().getStringExtra("date");

        oldPickupLocation.setText(pickup.getOriginAddressLine1());
        oldDeliveryLocation.setText(pickup.getDestinationAddressLine1());
        oldPickupBy.setText(pickup.getNaPickupBy());
        oldCount.setText(Integer.toString(pickup.getPickupItems().size()));
        oldDate.setText(date);

        adapter = new InvSelListViewAdapter(this, R.layout.invlist_sel_item, pickup.getPickupItems());
        adapter.setAllSelected(false);
        adapter.setNotifyOnChange(true);
        itemList.setAdapter(adapter);
        itemList.setItemsCanFocus(false);
    }

    public void backButton(View view) {
        if (checkServerResponse(true) == OK) {
            super.onBackPressed();
        }
    }

    public void continueButton(View view) {
        if (checkServerResponse(true) != OK) {
            return;
        }

        if (adapter.getSelectedItems(true).size() < 1) {
            AlertDialog.Builder confirmDialog = new AlertDialog.Builder(this);
            confirmDialog.setCancelable(false);
            confirmDialog.setTitle(Html.fromHtml("<font color='#000055'>Remove Delivery Items</font>"));
            confirmDialog.setMessage(Html.fromHtml("You have not selected any items to be removed"));
            confirmDialog.setNeutralButton("Yes", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            confirmDialog.show();
            
        } else if (allItemsSelected()) {
            AlertDialog.Builder confirmDialog = new AlertDialog.Builder(this);
            confirmDialog.setCancelable(false);
            confirmDialog.setTitle(Html.fromHtml("<font color='#000055'>Remove Delivery Items</font>"));
            confirmDialog.setMessage(Html.fromHtml("You have selected <b>All</b> items from this delivery."
                    + " <b>Continuing will cancel the entire delivery.</b><br><br>"
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

        } else {
            AlertDialog.Builder confirmDialog = new AlertDialog.Builder(this);
            confirmDialog.setCancelable(false);
            confirmDialog.setTitle(Html.fromHtml("<font color='#000055'>Remove Delivery Items</font>"));
            confirmDialog.setMessage(Html.fromHtml("You are about to remove <b>" + adapter.getSelectedItems(true).size()
                    + "</b> items from this delivery.<br><br>"
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
                    // // new CancelPickupTask().execute();
                }
            });
            confirmDialog.show();
        }
    }

    private boolean allItemsSelected() {
        if (adapter.getSelectedItems(true).size() == adapter.getAllItems().size()) {
            return true;
        }
        return false;
    }

    private class CancelPickupTask extends AsyncTask<Void, Void, Integer> {

        ProgressBar progressBar;

        @Override
        protected void onPreExecute() {
            progressBar = (ProgressBar) findViewById(R.id.progress_bar);
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
            Intent intent = new Intent(RemovePickupItems.this, Move.class);
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

    private class RemoveItemsTask extends AsyncTask<Void, Void, Integer> {

        ProgressBar progressBar;

        @Override
        protected void onPreExecute() {
            progressBar = (ProgressBar) findViewById(R.id.progress_bar);
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
            url += "/RemovePickupItems?nuxrpd=" + pickup.getNuxrpd();
            url += "&" + Formatter.generateGetArray("items", adapter.getSelectedItems(true));
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
            Intent intent = new Intent(RemovePickupItems.this, Move.class);
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

    public void displayShortToast(CharSequence text) {
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

}
