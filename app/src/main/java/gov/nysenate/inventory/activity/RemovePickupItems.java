package gov.nysenate.inventory.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;

import org.apache.http.HttpStatus;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import gov.nysenate.inventory.adapter.InvSelListViewAdapter;
import gov.nysenate.inventory.android.AppSingleton;
import gov.nysenate.inventory.android.InvApplication;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.android.StringInvRequest;
import gov.nysenate.inventory.model.InvItem;
import gov.nysenate.inventory.model.Transaction;
import gov.nysenate.inventory.util.AppProperties;
import gov.nysenate.inventory.util.HttpUtils;

public class RemovePickupItems extends SenateActivity {

    Transaction pickup;
    ListView itemList;
    InvSelListViewAdapter adapter;
    ProgressBar progressBar;

    Response.Listener<String> cancelPickupResponseListener = new Response.Listener<String>() {

        @Override
        public void onResponse(String response) {
            progressBar.setVisibility(ProgressBar.INVISIBLE);
            Intent intent = new Intent(RemovePickupItems.this, Move.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            overridePendingTransition(R.anim.in_right, R.anim.out_left);
            HttpUtils.displayResponseResults(RemovePickupItems.this, HttpStatus.SC_OK);
        }
    };

    Response.Listener<String> removeItemResponseListener = new Response.Listener<String>() {

        @Override
        public void onResponse(String response) {
            progressBar.setVisibility(ProgressBar.INVISIBLE);
            Intent intent = new Intent(RemovePickupItems.this, EditPickupMenu.class);
            intent.putExtra("nuxrpd", Integer.toString(pickup.getNuxrpd()));
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            overridePendingTransition(R.anim.in_right, R.anim.out_left);
            HttpUtils.displayResponseResults(RemovePickupItems.this, HttpStatus.SC_OK);
        }
    };

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

        pickup = EditPickupMenu.pickup;

        if (pickup.isRemote()) {
            oldPickupLocation.setText(Html.fromHtml(pickup.getOrigin().getLocationSummaryStringRemoteAppended()));
            oldDeliveryLocation.setText(Html.fromHtml(pickup.getDestination().getLocationSummaryStringRemoteAppended()));
        } else {
            oldPickupLocation.setText(pickup.getOrigin().getLocationSummaryString());
            oldDeliveryLocation.setText(pickup.getDestination().getLocationSummaryString());
        }
        oldPickupBy.setText(pickup.getNapickupby());
        oldCount.setText(Integer.toString(pickup.getPickupItems().size()));
        SimpleDateFormat sdf = ((InvApplication) getApplicationContext()).getDateTimeFormat();
        oldDate.setText(sdf.format(pickup.getPickupDate()));

        adapter = new InvSelListViewAdapter(this, R.layout.invlist_sel_item, pickup.getPickupItems());
        adapter.setAllSelected(false);
        adapter.setNotifyOnChange(true);
        itemList.setAdapter(adapter);
        itemList.setItemsCanFocus(false);
    }

    public void backButton(View view) {
        super.onBackPressed();
    }

    public void continueButton(View view) {
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
                    cancelPickup();
//                    new CancelPickupTask().execute();
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
                    removeItems();
                    //new RemoveItemsTask().execute();
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

    private void cancelPickup() {
        String url = AppProperties.getBaseUrl();
        url += "CancelPickup?nuxrpd=" + pickup.getNuxrpd();
        url += "&userFallback=" + LoginActivity.nauser;

        StringInvRequest stringInvRequest = new StringInvRequest(Request.Method.GET, url, null, cancelPickupResponseListener);

        /* Add your Requests to the RequestQueue to execute */
        AppSingleton.getInstance(InvApplication.getAppContext()).addToRequestQueue(stringInvRequest);
    }

    private void removeItems() {
        String url = AppProperties.getBaseUrl();
        url += "RemovePickupItems";

        Map<String, String> params = new HashMap<String, String>();

        params.put("nuxrpd", Integer.toString(pickup.getNuxrpd()));
        try {
            StringBuilder values = new StringBuilder();

            boolean valueAdded = false;

            for (InvItem aValue : adapter.getSelectedItems(true)) {
                if (valueAdded) {
                    values.append(",");
                }

                values.append(aValue.getNusenate());
                valueAdded = true;
            }

            params.put("items", values.toString());


            StringInvRequest stringInvRequest = new StringInvRequest(Request.Method.POST,
                    url, params, removeItemResponseListener);

            /* Add your Requests to the RequestQueue to execute */
            AppSingleton.getInstance(InvApplication.getAppContext()).addToRequestQueue(stringInvRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
