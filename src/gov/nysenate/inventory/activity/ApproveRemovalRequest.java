package gov.nysenate.inventory.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.*;
import gov.nysenate.inventory.android.InvApplication;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.android.asynctask.BaseAsyncTask;
import gov.nysenate.inventory.model.RemovalRequest;
import gov.nysenate.inventory.util.AppProperties;
import gov.nysenate.inventory.util.RemovalRequestParser;
import gov.nysenate.inventory.util.Toasty;
import org.apache.http.HttpStatus;

public class ApproveRemovalRequest extends SenateActivity
{
    private RemovalRequest removalRequest;

    private EditText barcode;
    private ListView list;
    private TextView transactionNum;
    private TextView requestedBy;
    private TextView adjustCode;
    private TextView date;
    private ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approve_removal_request);
        registerBaseActivityReceiver();

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        transactionNum = (TextView) findViewById(R.id.transaction_num);
        requestedBy = (TextView) findViewById(R.id.requested_by);
        adjustCode = (TextView) findViewById(R.id.adjust_code);
        date = (TextView) findViewById(R.id.date);
        barcode = (EditText) findViewById(R.id.barcode);
        list = (ListView) findViewById(R.id.removal_request_item_list);

        queryRemovalRequest();
    }

    private void queryRemovalRequest() {
        GetRemovalRequest rrTask = new GetRemovalRequest();
        rrTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                AppProperties.getBaseUrl(this) + "RemovalRequest?id=" + Integer.valueOf(getIntent().getStringExtra("transactionNum")));
    }


    private void initializeOriginalInfo(RemovalRequest rr) {
        transactionNum.setText(String.valueOf(rr.getTransactionNum()));
        requestedBy.setText(rr.getEmployee());
        adjustCode.setText(rr.getAdjustCode().getDescription());
        date.setText(((InvApplication) getApplication()).getDateTimeFormat().format(removalRequest.getDate()));
    }

    private void initializeListAdapter() {
//        adapter = new RemovalRequestItemSelectionAdapter(this, R.layout.removal_request_item_select_adapter, R.id.column1, removalRequest.getItems());
//        list.setAdapter(adapter);
    }

    // TODO:
    // Buttons
    // barcode textWatcher
    // list adapter.

    /* --- Background --- */

    private class GetRemovalRequest extends BaseAsyncTask<String, Void, RemovalRequest> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        public RemovalRequest handleBackgroundResult(String out, int responseCode) {
            RemovalRequest rr = null;
            if (responseCode == HttpStatus.SC_OK) {
                rr = RemovalRequestParser.parseRemovalRequest(out);
            }
            return rr;
        }

        @Override
        protected void onPostExecute(RemovalRequest rr) {
            progressBar.setVisibility(ProgressBar.INVISIBLE);
            if (rr != null) {
                removalRequest = rr;
                initializeOriginalInfo(rr);
                initializeListAdapter();
            } else {
                Toasty.displayCenteredMessage(ApproveRemovalRequest.this, "A Server Error has occured, Please try again.", Toast.LENGTH_SHORT);
            }
        }
    }

}