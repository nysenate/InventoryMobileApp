package gov.nysenate.inventory.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import gov.nysenate.inventory.android.CancelBtnFragment;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.android.RemovalRequestListAdapter;
import gov.nysenate.inventory.android.asynctask.BaseAsyncTask;
import gov.nysenate.inventory.model.RemovalRequest;
import gov.nysenate.inventory.util.AppProperties;
import gov.nysenate.inventory.util.RemovalRequestParser;
import gov.nysenate.inventory.util.Toasty;
import org.apache.http.HttpStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EditRemovalRequestSelection extends SenateActivity implements CancelBtnFragment.CancelBtnOnClick
{
    private ListView list;
    private RemovalRequestListAdapter adapter;
    private List<RemovalRequest> rrs = new ArrayList<RemovalRequest>();
    private ProgressBar bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_removal_request_selection);
        registerBaseActivityReceiver();

        bar = (ProgressBar) findViewById(R.id.progress_bar);
        list = (ListView) findViewById(R.id.removal_request_list);

        adapter = new RemovalRequestListAdapter(this, R.layout.removal_request_list_adapter, R.id.column1, rrs);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (checkServerResponse() == OK) {
                    continueToNextActivity(position);
                }
            }
        });

        if (checkServerResponse(true) == OK) {
            GetRemovalRequests task = new GetRemovalRequests();
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                    AppProperties.getBaseUrl(EditRemovalRequestSelection.this) + "RemovalRequest?status=PE&user=" + LoginActivity.nauser);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkServerResponse(true);
    }

    private void continueToNextActivity(int position) {
        RemovalRequest rr = rrs.get(position);
        String transactionNum = String.valueOf(rr.getTransactionNum());
        Intent intent = new Intent(this, EditRemovalRequest.class);
        intent.putExtra("transactionNum", transactionNum);
        startActivity(intent);
        overridePendingTransition(R.anim.in_right, R.anim.out_left);
    }

    @Override
    public void cancelBtnOnClick(View v) {
        onBackPressed();
    }

    private class GetRemovalRequests extends BaseAsyncTask<String, Void, List<RemovalRequest>> {
        @Override
        protected void onPreExecute() {
            bar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        public List<RemovalRequest> handleBackgroundResult(String out, int responseCode) {
            List<RemovalRequest> rrs = null;
            if (responseCode == HttpStatus.SC_OK) {
                rrs = RemovalRequestParser.parseRemovalRequests(out);
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
        Collections.sort(rrs);
        this.rrs.clear();
        this.rrs.addAll(rrs);
        adapter.notifyDataSetChanged();
    }
}
