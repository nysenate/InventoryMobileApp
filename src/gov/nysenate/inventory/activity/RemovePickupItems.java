package gov.nysenate.inventory.activity;

import gov.nysenate.inventory.adapter.InvSelListViewAdapter;
import gov.nysenate.inventory.android.InvApplication;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.model.InvItem;
import gov.nysenate.inventory.model.Transaction;
import gov.nysenate.inventory.util.AppProperties;
import gov.nysenate.inventory.util.Formatter;
import gov.nysenate.inventory.util.HttpUtils;
import gov.nysenate.inventory.util.TransactionParser;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

public class RemovePickupItems extends SenateActivity {

    Transaction pickup;
    ListView itemList;
    InvSelListViewAdapter adapter;
    ProgressBar progressBar;

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
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        pickup = TransactionParser.parseTransaction(getIntent().getStringExtra("pickup"));

        String date = getIntent().getStringExtra("date");

        if (pickup.isRemote()) {
            oldPickupLocation.setText(Html.fromHtml(pickup.getOrigin().getLocationSummaryStringRemoteAppended()));
            oldDeliveryLocation.setText(Html.fromHtml(pickup.getDestination().getLocationSummaryStringRemoteAppended()));
        } else {
            oldPickupLocation.setText(pickup.getOrigin().getLocationSummaryString());
            oldDeliveryLocation.setText(pickup.getDestination().getLocationSummaryString());
        }
        oldPickupBy.setText(pickup.getNapickupby());
        oldCount.setText(Integer.toString(pickup.getPickupItems().size()));
        SimpleDateFormat sdf = ((InvApplication)getApplicationContext()).getDateTimeFormat();
        oldDate.setText(sdf.format(pickup.getPickupDate()));

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
            confirmDialog.setNeutralButton(Html.fromHtml("<b>Yes</b>"), new DialogInterface.OnClickListener() {

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
            confirmDialog.setNegativeButton(Html.fromHtml("<b>No</b>"), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    return;
                }
            });
            confirmDialog.setPositiveButton(Html.fromHtml("<b>Yes</b>"), new DialogInterface.OnClickListener() {

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
            confirmDialog.setNegativeButton(Html.fromHtml("<b>No</b>"), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    return;
                }
            });
            confirmDialog.setPositiveButton(Html.fromHtml("<b>Yes</b>"), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    new RemoveItemsTask().execute();
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

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected Integer doInBackground(Void... arg0) {
            HttpClient httpClient = LoginActivity.getHttpClient();
            HttpResponse response;
            String url = AppProperties.getBaseUrl(RemovePickupItems.this);
            url += "CancelPickup?nuxrpd=" + pickup.getNuxrpd();
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
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            overridePendingTransition(R.anim.in_right, R.anim.out_left);
            HttpUtils.displayResponseResults(RemovePickupItems.this, response);
        }
    }

    private class RemoveItemsTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected Integer doInBackground(Void... arg0) {
            String url = AppProperties.getBaseUrl(RemovePickupItems.this);
            url += "RemovePickupItems";

            HttpClient httpClient = LoginActivity.getHttpClient();
            HttpResponse response;
            HttpPost post = new HttpPost(url);

            try {
                List<NameValuePair> values = new ArrayList<NameValuePair>();
                values.add(new BasicNameValuePair("nuxrpd", Integer.toString(pickup.getNuxrpd())));

                for (InvItem aValue : adapter.getSelectedItems(true)) {
                    values.add(new BasicNameValuePair("items[]", aValue.getNusenate()));
                }

                post.setEntity(new UrlEncodedFormEntity(values));
                response = httpClient.execute(post);

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
            Intent intent = new Intent(RemovePickupItems.this, EditPickupMenu.class);
            intent.putExtra("nuxrpd", Integer.toString(pickup.getNuxrpd()));
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            overridePendingTransition(R.anim.in_right, R.anim.out_left);
            HttpUtils.displayResponseResults(RemovePickupItems.this, response);
        }
    }
}
