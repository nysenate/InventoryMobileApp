package gov.nysenate.inventory.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Adapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;
import java.text.SimpleDateFormat;

import gov.nysenate.inventory.adapter.InvListViewAdapter;
import gov.nysenate.inventory.android.AppSingleton;
import gov.nysenate.inventory.android.InvApplication;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.android.StringInvRequest;
import gov.nysenate.inventory.model.Transaction;
import gov.nysenate.inventory.util.AppProperties;
import gov.nysenate.inventory.util.HttpUtils;


public class CancelPickup extends SenateActivity {

    private Transaction pickup;
    protected ProgressBar progressBar = null;

    Response.Listener<String> cancelPickupresponseListener = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            progressBar.setVisibility(ProgressBar.INVISIBLE);
            Intent intent = new Intent(CancelPickup.this, Move.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            overridePendingTransition(R.anim.in_right, R.anim.out_left);
            HttpUtils.displayResponseResults(CancelPickup.this, HttpStatus.SC_OK);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancel_pickup);
        registerBaseActivityReceiver();

        TextView oldPickupLocation = (TextView) findViewById(R.id.old_pickup_location);
        TextView oldDeliveryLocation = (TextView) findViewById(R.id.old_delivery_location);
        TextView oldPickupBy = (TextView) findViewById(R.id.pickup_by);
        TextView oldCount = (TextView) findViewById(R.id.pickup_count);
        TextView oldDate = (TextView) findViewById(R.id.pickup_date);
        TextView comments = (TextView) findViewById(R.id.cancel_pickup_comments);
        TextView commentsTitle = (TextView) findViewById(R.id.cancel_pickup_comments_title);
        ListView items = (ListView) findViewById(R.id.cancel_pickup_listview);

        pickup = EditPickupMenu.pickup;

        Adapter pickupListAdapter = new InvListViewAdapter(this, R.layout.invlist_item, pickup.getPickupItems());
        items.setAdapter((ListAdapter) pickupListAdapter);

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

        // Only show comments if there are some.
        if (pickup.getPickupComments().length() > 0) {
            commentsTitle.setText("Comments:");
            comments.setText(pickup.getPickupComments());
        }
    }

    public void continueButton(View view) {
       // if (checkServerResponse(true) == OK) {
            AlertDialog.Builder confirmDialog = new AlertDialog.Builder(this);
            confirmDialog.setCancelable(false);
            confirmDialog.setTitle(Html.fromHtml("<font color='#000055'>Cancel Pickup</font>"));
            confirmDialog.setMessage(Html.fromHtml("You are about to <b>cancel</b> this pickup.<br><br>"
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
                    progressBar = (ProgressBar) findViewById(R.id.pickup_cancel_progress_bar);
                    progressBar.setVisibility(ProgressBar.VISIBLE);

                    dialog.dismiss();
                    String url = AppProperties.getBaseUrl(CancelPickup.this);
                    url += "CancelPickup?nuxrpd=" + pickup.getNuxrpd();
                    url += "&userFallback=" + LoginActivity.nauser;

                    StringInvRequest stringInvRequest = new StringInvRequest(Request.Method.GET, url, null, cancelPickupresponseListener);

                    /* Add your Requests to the RequestQueue to execute */
                    AppSingleton.getInstance(InvApplication.getAppContext()).addToRequestQueue(stringInvRequest);

                    //new CancelPickupTask().execute();
                }
            });
            confirmDialog.show();
       // }
    }

    public void backButton(View view) {
      //  if (checkServerResponse(true) == OK) {
            super.onBackPressed();
       // }
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
            //LoginActivity.activeAsyncTask = this;
            HttpClient httpClient = LoginActivity.getHttpClient();
            HttpResponse response;
            String url = AppProperties.getBaseUrl(CancelPickup.this);
            url += "CancelPickup?nuxrpd=" + pickup.getNuxrpd();
            url += "&userFallback=" + LoginActivity.nauser;

           // if (checkServerResponse(true) == OK) {
                try {
                    response = httpClient.execute(new HttpGet(url));
                    return response.getStatusLine().getStatusCode();
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
           // }

            return null;
        }

        @Override
        protected void onPostExecute(Integer response) {
            progressBar.setVisibility(ProgressBar.INVISIBLE);
            Intent intent = new Intent(CancelPickup.this, Move.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            overridePendingTransition(R.anim.in_right, R.anim.out_left);
            HttpUtils.displayResponseResults(CancelPickup.this, HttpStatus.SC_OK);
            LoginActivity.activeAsyncTask = null;
        }
    }
}
