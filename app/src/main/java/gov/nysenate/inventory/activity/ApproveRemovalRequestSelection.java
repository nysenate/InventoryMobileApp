package gov.nysenate.inventory.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import gov.nysenate.inventory.android.CancelBtnFragment;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.android.RemovalRequestListFragment;
import gov.nysenate.inventory.android.asynctask.BaseAsyncTask;
import gov.nysenate.inventory.model.RemovalRequest;
import gov.nysenate.inventory.util.AppProperties;
import gov.nysenate.inventory.util.Serializer;
import gov.nysenate.inventory.util.Toasty;
import org.apache.http.HttpStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ApproveRemovalRequestSelection extends SenateActivity
        implements RemovalRequestListFragment.RemovalRequestListFragmentI, CancelBtnFragment.CancelBtnOnClick
{
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

        queryRemovalRequests();
    }

    private void queryRemovalRequests() {
        if (checkServerResponse(true) == OK) {
            GetRemovalRequests task = new GetRemovalRequests();
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                    AppProperties.getBaseUrl(ApproveRemovalRequestSelection.this) + "RemovalRequest?status=SI");
        }
    }

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
        RemovalRequest rr = rrs.get(position);
        String transactionNum = String.valueOf(rr.getTransactionNum());
        Intent intent = new Intent(this, ApproveRemovalRequest.class);
        intent.putExtra("transactionNum", transactionNum);
        startActivity(intent);
        overridePendingTransition(R.anim.in_right, R.anim.out_left);
    }

    @Override
    public void cancelBtnOnClick(View v) {
        super.onBackPressed();
    }

    /* --- Background Tasks ---*/

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
                populateListAdapter(rrs);
            } else {
                Toasty.displayCenteredMessage(ApproveRemovalRequestSelection.this,
                        "Error getting Removal Requests from server. Please try again.", Toast.LENGTH_SHORT);
            }
        }
    }
}