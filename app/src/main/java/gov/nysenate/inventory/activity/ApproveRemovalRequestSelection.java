package gov.nysenate.inventory.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gov.nysenate.inventory.android.AppSingleton;
import gov.nysenate.inventory.android.CancelBtnFragment;
import gov.nysenate.inventory.android.InvApplication;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.android.RemovalRequestListFragment;
import gov.nysenate.inventory.android.StringInvRequest;
import gov.nysenate.inventory.model.RemovalRequest;
import gov.nysenate.inventory.util.AppProperties;
import gov.nysenate.inventory.util.Serializer;
import gov.nysenate.inventory.util.Toasty;

public class ApproveRemovalRequestSelection extends SenateActivity
        implements RemovalRequestListFragment.RemovalRequestListFragmentI, CancelBtnFragment.CancelBtnOnClick {
    private List<RemovalRequest> rrs = new ArrayList<RemovalRequest>();
    private RemovalRequestListFragment rrList;
    private ProgressBar bar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approve_request_selection);
        registerBaseActivityReceiver();

        rrList = (RemovalRequestListFragment) getFragmentManager().findFragmentById(R.id.removal_request_list_fragment);
        bar = (ProgressBar) findViewById(R.id.progress_bar);

        getRemovalRequests();
    }

    public void getRemovalRequests() {
        bar.setVisibility(ProgressBar.VISIBLE);

        StringInvRequest stringInvRequest = new StringInvRequest(Request.Method.GET,
                AppProperties.getBaseUrl(ApproveRemovalRequestSelection.this) + "RemovalRequest?status=SI", null, removalRequestResponseListener);

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
                populateListAdapter(rrs);
            } else {
                Toasty.displayCenteredMessage(ApproveRemovalRequestSelection.this,
                        "Error getting Removal Requests from server. Please try again.", Toast.LENGTH_SHORT);
            }
        }
    };


    private void populateListAdapter(List<RemovalRequest> rrs) {
        Collections.sort(rrs, Collections.reverseOrder());
        this.rrs.clear();
        this.rrs.addAll(rrs);
        rrList.refreshDisplay();
    }

    @Override
    public List<RemovalRequest> getListReference() {
        return rrs;
    }

    @Override
    public void nextActivity(int position) {
        // if (checkServerResponse(true) == OK) {
        RemovalRequest rr = rrs.get(position);
        String transactionNum = String.valueOf(rr.getTransactionNum());
        Intent intent = new Intent(this, ApproveRemovalRequest.class);
        intent.putExtra("transactionNum", transactionNum);
        startActivity(intent);
        overridePendingTransition(R.anim.in_right, R.anim.out_left);
        //  }
    }

    @Override
    public void cancelBtnOnClick(View v) {
        super.onBackPressed();
    }

    /* --- Background Tasks ---*/

}