package gov.nysenate.inventory.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.*;
import gov.nysenate.inventory.adapter.NothingSelectedSpinnerAdapter;
import gov.nysenate.inventory.adapter.RemovalRequestItemSelectionAdapter;
import gov.nysenate.inventory.android.InvApplication;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.android.asynctask.BaseAsyncTask;
import gov.nysenate.inventory.android.asynctask.UpdateRemovalRequest;
import gov.nysenate.inventory.model.AdjustCode;
import gov.nysenate.inventory.model.Item;
import gov.nysenate.inventory.model.ItemStatus;
import gov.nysenate.inventory.model.RemovalRequest;
import gov.nysenate.inventory.util.AdjustCodeParser;
import gov.nysenate.inventory.util.AppProperties;
import gov.nysenate.inventory.util.RemovalRequestParser;
import gov.nysenate.inventory.util.Toasty;
import org.apache.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

public class EditRemovalRequest extends SenateActivity implements UpdateRemovalRequest.UpdateRemovalRequestI
{
    private RemovalRequest removalRequest;
    private List<AdjustCode> adjustCodes;
    private RemovalRequestItemSelectionAdapter adapter;

    private AdjustCode originalAdjustCode;
    private String originalStatus;

    private TextView transactionNumView;
    private TextView date;
    private Spinner adjustCodeView;
    private ListView itemList;
    private ProgressBar progressBar;
    private CheckBox checkbox;

    private void initializeViewObjects() {
        transactionNumView = (TextView) findViewById(R.id.transaction_num);
        date = (TextView) findViewById(R.id.date);
        adjustCodeView = (Spinner) findViewById(R.id.adjust_code);
        itemList = (ListView) findViewById(R.id.removal_request_item_list);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        checkbox = (CheckBox) findViewById(R.id.submit_check_box);
        checkbox.setEnabled(false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_removal_request);
        registerBaseActivityReceiver();

        initializeViewObjects();

        if (checkServerResponse(true) == OK) {
            initializeAdjustCodes();
        }
    }

    private void initializeAdjustCodes() {
        QueryAdjustCodes queryAdjustCodes = new QueryAdjustCodes();
        queryAdjustCodes.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, AppProperties.getBaseUrl(this) + "AdjustCodeServlet");
    }

    public class QueryAdjustCodes extends BaseAsyncTask<String, Void, List<AdjustCode>> {
        @Override
        protected void onPreExecute() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        public List<AdjustCode> handleBackgroundResult(String out, int responseCode) {
            List<AdjustCode> codes = null;
            if (responseCode == HttpStatus.SC_OK) {
                codes = AdjustCodeParser.parseAdjustCodes(out.toString());
            }
            return codes;
        }

        @Override
        protected void onPostExecute(List<AdjustCode> codes) {
            progressBar.setVisibility(ProgressBar.INVISIBLE);

            adjustCodes = codes;
            if (adjustCodes != null) {
                initializeAdjustCodeSpinner();
                initializeRemovalRequest();
            } else {
                Toasty.displayCenteredMessage(EditRemovalRequest.this, "A Server Error has occured, Please try again.", Toast.LENGTH_SHORT);
            }
        }
    }

    private void initializeRemovalRequest() {
        GetRemovalRequest rrTask = new GetRemovalRequest();
        rrTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                AppProperties.getBaseUrl(this) + "RemovalRequest?id=" + Integer.valueOf(getIntent().getStringExtra("transactionNum")));
    }

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
                originalAdjustCode = rr.getAdjustCode();
                originalStatus = rr.getStatus();
                removalRequest = rr;
                initializeItemListAdapter();
                updateView();
                checkbox.setEnabled(true);
            } else {
                Toasty.displayCenteredMessage(EditRemovalRequest.this, "A Server Error has occured, Please try again.", Toast.LENGTH_SHORT);
            }
        }
    }

    private void initializeItemListAdapter() {
        adapter = new RemovalRequestItemSelectionAdapter(this, R.layout.removal_request_item_select_adapter, R.id.column1, removalRequest.getItems());
        itemList.setAdapter(adapter);
    }

    private void updateView() {
        transactionNumView.setText(String.valueOf(removalRequest.getTransactionNum()));
        date.setText(((InvApplication) getApplication()).getDateTimeFormat().format(removalRequest.getDate()));
        adjustCodeView.setSelection(removalRequestAdjustCodePosition());
        adapter.notifyDataSetChanged();
    }

    private int removalRequestAdjustCodePosition() {
        int size = adjustCodeView.getAdapter().getCount();
        for (int i = 0; i < size; i++) {
            if (removalRequest.getAdjustCode().toString().equals(adjustCodeView.getAdapter().getItem(i))) {
                return i;
            }
        }
        return 0;
    }

    public void submitToMgmtCheckBoxClicked(View view) {
        if (((CheckBox) view).isChecked()) {
            removalRequest.setStatus("SI");
        } else {
            removalRequest.setStatus("PE");
        }
    }

    public void onSaveBtnClick(View view) {
        if (noChangesMade()) {
            Toasty.displayCenteredMessage(this, "You have not made any changes", Toast.LENGTH_SHORT);
            return;
        } else if (allItemsDeleted()) {
            confirmDeleteAllItems();
        } else {
            confirmationDialog();
        }
    }

    private void confirmDeleteAllItems() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Confirmation")
                .setMessage(Html.fromHtml("You have selected all items to be deleted. This will delete the entire Inventory Removal Reqeust."))
                .setNegativeButton(Html.fromHtml("<b>Cancel</b>"), null)
                .setPositiveButton(Html.fromHtml("<b>Ok</b>"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        confirmationDialog();
                    }
                });
        builder.show();
    }

    private void confirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Confirmation")
                .setMessage(Html.fromHtml(getChangesMessage()))
                .setNegativeButton(Html.fromHtml("<b>Cancel</b>"), null)
                .setPositiveButton(Html.fromHtml("<b>Ok</b>"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updateRemovalRequest();
                    }
                });
        builder.show();
    }

    private boolean allItemsDeleted() {
        for (Item i: removalRequest.getItems()) {
            if (i.getStatus() != ItemStatus.INACTIVE) {
                return false;
            }
        }
        return true;
    }

    private boolean noChangesMade() {
        return getItemsDeleted().size() == 0 && originalAdjustCode.equals(removalRequest.getAdjustCode()) && originalStatus.equals(removalRequest.getStatus());
    }

    private String getChangesMessage() {
        String s = "Are you sure you want to save the following changes? <br>";
        if (!originalStatus.equals(removalRequest.getStatus())) {
            s += "Status: Submitted to Inventory Control.<br>";
        }
        if (!originalAdjustCode.equals(removalRequest.getAdjustCode())) {
            s += "Adjust Code: " + removalRequest.getAdjustCode().getCode() + ", " + removalRequest.getAdjustCode().getDescription() + ".<br>";
        }
        if (getItemsDeleted().size() > 0) {
            s += "Deleting " + getItemsDeleted().size() + " items.<br>";
        }

        return s;
    }

    private void updateRemovalRequest() {
        UpdateRemovalRequest task = new UpdateRemovalRequest(removalRequest, this);
        task.setProgressBar(progressBar);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, AppProperties.getBaseUrl(this) + "RemovalRequest");
    }

    @Override
    public void onRemovalRequestUpdated(RemovalRequest rr) {
        if (rr != null) {
            Toasty.displayCenteredMessage(this, "Successfully updated Removal Request.", Toast.LENGTH_SHORT);
        } else {
            Toasty.displayCenteredMessage(this, "Error updating Removal Request. Please contact STS/BAC", Toast.LENGTH_SHORT);
        }
        Intent intent = new Intent(this, Move.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.in_right, R.anim.out_left);
    }

    private List<Item> getItemsDeleted() {
        List<Item> deleted = new ArrayList<Item>();
        for (Item i : removalRequest.getItems()) {
            if (i.getStatus() == ItemStatus.INACTIVE) {
                deleted.add(i);
            }
        }
        return deleted;
    }

    public void onCancelBtnClick(View view) {
        if (checkServerResponse(true) == OK) {
            finish();
            overridePendingTransition(R.anim.in_left, R.anim.out_right);
        }
    }

    private void initializeAdjustCodeSpinner() {
        ArrayList<String> codes = new ArrayList<String>();
        for (AdjustCode ac : adjustCodes) {
            codes.add(ac.toString());
        }

        ArrayAdapter<CharSequence> spinAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, codes.toArray(new String[codes.size()]));
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adjustCodeView.setAdapter(new NothingSelectedSpinnerAdapter(spinAdapter, R.layout.spinner_nothing_selected, this));
        adjustCodeView.setOnItemSelectedListener(new updateAdjustCodeDescription());
    }

    private class updateAdjustCodeDescription implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (adjustCodeView.getSelectedItem() != null) {
                AdjustCode code = getAdjustCode(adjustCodeView.getSelectedItem().toString());
                removalRequest.setAdjustCode(code);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {  }
    }

    private AdjustCode getAdjustCode(String codeAndDescription) {
        for (AdjustCode code : adjustCodes) {
            if (code.toString().equals(codeAndDescription)) {
                return code;
            }
        }
        return null;
    }
}