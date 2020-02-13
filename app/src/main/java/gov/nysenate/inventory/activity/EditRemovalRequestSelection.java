package gov.nysenate.inventory.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;

import org.apache.http.HttpStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gov.nysenate.inventory.android.AppSingleton;
import gov.nysenate.inventory.android.CancelBtnFragment;
import gov.nysenate.inventory.android.InvApplication;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.android.RRListStatusFragment;
import gov.nysenate.inventory.android.StringInvRequest;
import gov.nysenate.inventory.android.asynctask.BaseAsyncTask;
import gov.nysenate.inventory.comparator.RemovalRequestComparer;
import gov.nysenate.inventory.model.RemovalRequest;
import gov.nysenate.inventory.util.AppProperties;
import gov.nysenate.inventory.util.Serializer;
import gov.nysenate.inventory.util.Toasty;

public class EditRemovalRequestSelection extends SenateActivity
        implements RRListStatusFragment.RRListStatusFragmentI, CancelBtnFragment.CancelBtnOnClick {
    private List<RemovalRequest> rrs = new ArrayList<RemovalRequest>();
    private RRListStatusFragment rrList;
    private ProgressBar bar;
    private RemovalRequestComparer removalRequestComparer = new RemovalRequestComparer();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_removal_request_selection);
        registerBaseActivityReceiver();

        rrList = (RRListStatusFragment) getFragmentManager().findFragmentById(R.id.removal_request_list_fragment);

        bar = (ProgressBar) findViewById(R.id.progress_bar);

        getRemovalRequests();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void cancelBtnOnClick(View v) {
        onBackPressed();
    }

    @Override
    public List<RemovalRequest> getListReference() {
        return rrs;
    }

    @Override
    public void nextActivity(int position) {
        RemovalRequest rr = rrs.get(position);
        String transactionNum = String.valueOf(rr.getTransactionNum());
        Intent intent = new Intent(this, EditRemovalRequest.class);
        intent.putExtra("transactionNum", transactionNum);
        startActivity(intent);
        overridePendingTransition(R.anim.in_right, R.anim.out_left);
    }

    public void getRemovalRequests() {
        if (bar != null) {
            bar.setVisibility(ProgressBar.VISIBLE);
        }

        StringInvRequest stringInvRequest = new StringInvRequest(Request.Method.GET,
                AppProperties.getBaseUrl(EditRemovalRequestSelection.this) + "RemovalRequest?status=PE&status=RJ", null, removalRequestResponseListener);

        /* Add your Requests to the RequestQueue to execute */
        AppSingleton.getInstance(InvApplication.getAppContext()).addToRequestQueue(stringInvRequest);
    }

    Response.Listener removalRequestResponseListener = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            List<RemovalRequest> rrs = null;
            rrs = Serializer.deserialize(response, RemovalRequest.class);
            bar.setVisibility(ProgressBar.INVISIBLE);
            if (rrs != null) {
                updateAdapter(rrs);
            } else {
                Toasty.displayCenteredMessage(EditRemovalRequestSelection.this,
                        "Error getting Removal Requests from server. Please try again.", Toast.LENGTH_SHORT);
            }
        }
    };

    private class GetRemovalRequests extends BaseAsyncTask<String, Void, List<RemovalRequest>> {
        @Override
        protected void onPreExecute() {
            bar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        public List<RemovalRequest> handleBackgroundResult(String out, int responseCode) {
            List<RemovalRequest> rrs = null;
            if (responseCode == HttpStatus.SC_OK) {
                rrs = Serializer.deserialize(out, RemovalRequest.class);
            }
            return rrs;
        }

        @Override
        protected void onPostExecute(List<RemovalRequest> rrs) {
            bar.setVisibility(ProgressBar.INVISIBLE);
            if (rrs != null) {
                updateAdapter(rrs);
            } else {
                Toasty.displayCenteredMessage(EditRemovalRequestSelection.this,
                        "Error getting Removal Requests from server. Please try again.", Toast.LENGTH_SHORT);
            }
        }
    }

    private void updateAdapter(List<RemovalRequest> rrs) {
        Collections.sort(rrs, Collections.reverseOrder());
        ;
        this.rrs.clear();
        this.rrs.addAll(rrs);
        rrList.refreshDisplay();
    }
}
