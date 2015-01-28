package gov.nysenate.inventory.activity;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import gov.nysenate.inventory.comparator.RemovalRequestComparer;
import gov.nysenate.inventory.android.*;
import gov.nysenate.inventory.android.asynctask.BaseAsyncTask;
import gov.nysenate.inventory.android.asynctask.UpdateRemovalRequest;
import gov.nysenate.inventory.model.Item;
import gov.nysenate.inventory.model.RemovalRequest;
import gov.nysenate.inventory.util.AppProperties;
import gov.nysenate.inventory.util.RemovalRequestParser;
import gov.nysenate.inventory.util.Toasty;
import org.apache.http.HttpStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ApproveRemovalRequest extends SenateActivity
        implements RemovalRequestListSelectionFragment.RRListSelectionFragmentI,
        CancelBtnFragment.CancelBtnOnClick, UpdateRemovalRequest.UpdateRemovalRequestI,
        RRRejectConfirmationDialog.RRRejectConfirmationDialogI
{
    private RemovalRequest removalRequest;
    private List<Item> items = new ArrayList<Item>();

    private EditText barcode;
    private RemovalRequestListSelectionFragment list;
    private TextView transactionNum;
    private TextView requestedBy;
    private TextView adjustCode;
    private TextView date;
    private TextView itemsRemaining;
    private ProgressBar progressBar;
    private Button rejectBtn;
    private Button approveBtn;
    RemovalRequestComparer removalRequestComparer;    

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
        itemsRemaining = (TextView) findViewById(R.id.item_count);
        barcode = (EditText) findViewById(R.id.barcode);
        rejectBtn = (Button) findViewById(R.id.reject_btn);
        approveBtn = (Button) findViewById(R.id.approve_all_btn);
        list = (RemovalRequestListSelectionFragment) getFragmentManager().findFragmentById(R.id.list_fragment);

        barcode.addTextChangedListener(barcodeTextWatcher);

        queryRemovalRequest();
    }

    private TextWatcher barcodeTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == 6) {
                if (checkServerResponse() == OK && items != null) {
                    Item item = getItemByBarcode(s.toString());
                    if (item != null) {
                        list.approveItem(item);
                        updateRemainingCount();
                        playSound(R.raw.ok);
                    } else {
                        playSound(R.raw.warning);
                        Toasty.displayCenteredMessage(ApproveRemovalRequest.this,
                                "Scanned Item is not part of the Removal Request", Toast.LENGTH_SHORT);
                    }
                    barcode.setText("");
                } else {
                    playSound(R.raw.warning);
                }
            }
        }
    };

    private void updateRemainingCount() {
        itemsRemaining.setText(String.valueOf(remainingItemCount()));
    }

    private Item getItemByBarcode(String barcode) {
        for (Item i : items) {
            if (i.getBarcode().equals(barcode)) {
                return i;
            }
        }
        return null;
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

    private void initializeItems() {
        items.clear();
        removalRequestComparer = new RemovalRequestComparer();
        items.addAll(removalRequest.getItems()); 
        Collections.sort(items, removalRequestComparer);
        list.itemsInitialized();
        list.refresh();
    }

    @Override
    public List<Item> getItemsReference() {
        return items;
    }

    public void onApproveAll(View view) {
        String approveText = "Approve All";
        String disapproveText = "Disapprove All";


        if (approveBtn.getText().toString().equals(approveText)) {
            list.approveAll();
            approveBtn.setText(disapproveText);
        } else {
            list.disapproveAll();
            approveBtn.setText(approveText);
        }
        updateRemainingCount();
    }

    @Override
    public void cancelBtnOnClick(View v) {
        super.onBackPressed();
    }

    public void onRejectBtnClick(View v) {
        rejectBtn.setEnabled(false);

        if (checkServerResponse() != OK) {
            rejectBtn.setEnabled(true);
            return;
        }

        DialogFragment dialog = RRRejectConfirmationDialog.newInstance(removalRequest, this);
        dialog.setCancelable(false);
        dialog.show(getFragmentManager(), "reject_dialog");
    }

    @Override
    public void onRejectBtnPositiveClick() {
        saveChanges();
    }

    @Override
    public void onRejectBtnNegativeClick() {
        rejectBtn.setEnabled(true);
    }

    public void onApproveBtnSubmit(View v) {
        final Button approvalBtn = (Button) findViewById(R.id.approve_submit_btn);
        approvalBtn.setEnabled(false);

        if (checkServerResponse() != OK) {
            approvalBtn.setEnabled(true);
            return;
        }

        if (allItemsApproved()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle("Confirmation")
                    .setMessage(Html.fromHtml("Are you sure you want to <b>Approve</b> this Removal Request?"))
                    .setNegativeButton(Html.fromHtml("<b>Cancel</b>"), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            approvalBtn.setEnabled(true);
                        }
                    })

                    .setPositiveButton(Html.fromHtml("<b>Ok</b>"), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (checkServerResponse(true) == OK) {
                                removalRequest.setStatus("SM");
                                saveChanges();
                            }
                        }

                    });
            builder.show();
        } else {
            Toasty.displayCenteredMessage(this, "All items must be approved.", Toast.LENGTH_SHORT);
            approvalBtn.setEnabled(true);
        }
    }

    private boolean allItemsApproved() {
        return remainingItemCount() == 0 ? true : false;
    }

    private void saveChanges() {
        UpdateRemovalRequest task = new UpdateRemovalRequest(removalRequest, this);
        task.setProgressBar(progressBar);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, AppProperties.getBaseUrl(this) + "RemovalRequest");
        returnToInventoryRemovalMenu();
    }

    private void returnToInventoryRemovalMenu() {
        Intent intent = new Intent(this, InventoryRemovalMenu.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void onRemovalRequestUpdated(RemovalRequest rr) {
        if (rr != null) {
            Toasty.displayCenteredMessage(this,"Removal Request successfully updated.", Toast.LENGTH_SHORT);
        } else {
            Toasty.displayCenteredMessage(this,"Error updating Removal Request. Please contact STS/BAC", Toast.LENGTH_SHORT);
        }
    }

    private int remainingItemCount() {
        return removalRequest.getItems().size() - list.getApprovedItems().size();
    }

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
                initializeItems();
                updateRemainingCount();
            } else {
                Toasty.displayCenteredMessage(ApproveRemovalRequest.this, "A Server Error has occured, Please try again.", Toast.LENGTH_SHORT);
            }
        }
    }
    
}