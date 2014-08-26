package gov.nysenate.inventory.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import gov.nysenate.inventory.adapter.NothingSelectedSpinnerAdapter;
import gov.nysenate.inventory.android.CancelBtnFragment;
import gov.nysenate.inventory.android.InvApplication;
import gov.nysenate.inventory.android.RemovalRequestItemsList;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.android.asynctask.BaseAsyncTask;
import gov.nysenate.inventory.android.asynctask.UpdateRemovalRequest;
import gov.nysenate.inventory.model.AdjustCode;
import gov.nysenate.inventory.model.Item;
import gov.nysenate.inventory.model.ItemStatus;
import gov.nysenate.inventory.model.RemovalRequest;
import gov.nysenate.inventory.util.*;
import org.apache.http.HttpStatus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EnterRemovalRequestActivity extends SenateActivity
        implements UpdateRemovalRequest.UpdateRemovalRequestI, CancelBtnFragment.CancelBtnOnClick
{
    private TextView date;
    private Spinner removalReasonCode;
    private EditText barcode;
    private RemovalRequestItemsList fragment;
    private TextView count;
    private ProgressBar progressBar;
    private Button saveBtn;


    private RemovalRequest removalRequest;
    private List<AdjustCode> adjustCodes;

    private void initalizeUI() {
        date = (TextView) findViewById(R.id.date);
        removalReasonCode = (Spinner) findViewById(R.id.removal_reason_code);
        barcode = (EditText) findViewById(R.id.barcode_text);
        count = (TextView) findViewById(R.id.count);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        saveBtn = (Button) findViewById(R.id.continue_btn);
        fragment = (RemovalRequestItemsList) getFragmentManager().findFragmentById(R.id.removal_item_list_fragment);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerBaseActivityReceiver();
        setContentView(R.layout.activity_enter_removal_request);

        initalizeUI();

        barcode.addTextChangedListener(barcodeWatcher);
        disableBarcode();
        removalRequest = new RemovalRequest(LoginActivity.nauser.toUpperCase(), new Date());
        removalRequest.setStatus("PE");
        SimpleDateFormat sdf = ((InvApplication) getApplication()).getDateTimeFormat();
        date.setText(sdf.format(removalRequest.getDate()));

        if (checkServerResponse(true) == OK) {
            QueryAdjustCodes queryAdjustCodes = new QueryAdjustCodes();
            queryAdjustCodes.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, AppProperties.getBaseUrl(this) + "AdjustCodeServlet");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkServerResponse(true);
    }

    private TextWatcher barcodeWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable text) {
            if (text.length() == 6) {
                if (checkServerResponse(true) == OK) {
                    GetItem task = new GetItem();
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                            AppProperties.getBaseUrl(EnterRemovalRequestActivity.this) + "Item?barcode=" + text.toString());
                }
            }
        }
    };

    public class GetItem extends BaseAsyncTask<String, Void, Item>
    {
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        public Item handleBackgroundResult(String out, int responseCode) {
            Item item = null;
            item = ItemParser.parseItem(out);
            return item;
        }

        @Override
        protected void onPostExecute(Item item) {
            progressBar.setVisibility(ProgressBar.GONE);
            if (item != null) {
                    checkItemStatus(item);
            } else {
                playSound(R.raw.error);
                Toasty.displayCenteredMessage(EnterRemovalRequestActivity.this, "A Server Error has occured, Please try again.", Toast.LENGTH_SHORT);
                barcode.setText("");
            }
        }

        private void checkItemStatus(Item item) {
            if (removalRequest.getItems().contains(item)) {
                Toasty.displayCenteredMessage(EnterRemovalRequestActivity.this, "You have already scanned that item.", Toast.LENGTH_SHORT);
                clearBarcode();
                playSound(R.raw.warning);
                return;
            }
            if (item.getStatus() == ItemStatus.ACTIVE) {
                add(item);
                playSound(R.raw.ok);
            } else {
                switch (item.getStatus()) {
                    case IN_TRANSIT:
                        Toasty.displayCenteredMessage(EnterRemovalRequestActivity.this, "Item is In Transit, It cannot be removed right now.", Toast.LENGTH_SHORT);
                        break;
                    case INACTIVE:
                        Toasty.displayCenteredMessage(EnterRemovalRequestActivity.this, "Item is Inactive.", Toast.LENGTH_SHORT);
                        break;
                    case PENDING_REMOVAL:
                        Toasty.displayCenteredMessage(EnterRemovalRequestActivity.this, "Item has already been added to the Inventory Removal Requests", Toast.LENGTH_SHORT);
                        break;
                    case DOES_NOT_EXIST:
                        Toasty.displayCenteredMessage(EnterRemovalRequestActivity.this, "Invalid Barcode", Toast.LENGTH_SHORT);
                }
                playSound(R.raw.warning);
                clearBarcode();
            }
        }
    }

    public void add(Item item) {
        removalRequest.addItem(item);
        fragment.addItem(item);
        clearBarcode();
        updateCount();
    }

    private void updateCount() {
        count.setText(String.valueOf(fragment.getItems().size()));
    }

    @Override
    public void cancelBtnOnClick(View v) {
        onBackPressed();
    }

    public void onSaveBtnClick(View view) {
        saveBtn.setEnabled(false);

        if (checkServerResponse(true) != OK) {
            saveBtn.setEnabled(true);
            return;
        }

        if (removalRequest.getItems().isEmpty()) {
            Toasty.displayCenteredMessage(this, "You have not scanned any items to remove from inventory.", Toast.LENGTH_SHORT);
            saveBtn.setEnabled(true);
            return;
        }
        setItemsAsPendingRemoval();
        displayConfirmationDialog();
    }

    private void setItemsAsPendingRemoval() {
        for (Item i: removalRequest.getItems()) {
            i.setStatus(ItemStatus.PENDING_REMOVAL);
        }
    }

    private void displayConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
        .setCancelable(false)
        .setTitle("Confirmation")
        .setMessage("Are you sure you want to save this Removal Request of " + count.getText().toString() + " items?")
        .setNegativeButton(Html.fromHtml("<b>Cancel</b>"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveBtn.setEnabled(true);
            }
        })

        .setPositiveButton(Html.fromHtml("<b>Ok</b>"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                UpdateRemovalRequest task = new UpdateRemovalRequest(removalRequest, EnterRemovalRequestActivity.this);
                task.setProgressBar(progressBar);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, AppProperties.getBaseUrl(EnterRemovalRequestActivity.this) + "RemovalRequest");
            }
        });
        builder.show();
    }

    @Override
    public void onRemovalRequestUpdated(RemovalRequest rr) {
        String message = "";
        if (rr != null) {
            message = "Removal Request successfully saved. <br><br>" +
                    "Request# " + rr.getTransactionNum() + ".<br>" +
                    "Date: " + ((InvApplication) getApplication()).getDateTimeFormat().format(rr.getDate()) + ". <br>" +
                    "Removal Reason: " + rr.getAdjustCode().getDescription() + ".";

        } else {
            message = "Error saving Removal Reqeust. Please contact STS/BAC";
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Info")
                .setMessage(Html.fromHtml(message))
                .setPositiveButton(Html.fromHtml("<b>Ok</b>"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(EnterRemovalRequestActivity.this, InventoryRemovalMenu.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                });
        builder.show();
    }

    private class QueryAdjustCodes extends BaseAsyncTask<String, Void, List<AdjustCode>> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        public List<AdjustCode> handleBackgroundResult(String out, int responseCode) {
            if (responseCode == HttpStatus.SC_OK) {
                adjustCodes = AdjustCodeParser.parseAdjustCodes(out.toString());
            }
            return adjustCodes;
        }

        @Override
        protected void onPostExecute(List<AdjustCode> codes) {
            if (adjustCodes != null) {
                initializeAdjustCodeSpinner();
            } else {
                Toasty.displayCenteredMessage(EnterRemovalRequestActivity.this, "A Server Error has occured, Please try again.", Toast.LENGTH_SHORT);
            }
        }
    }

    private void initializeAdjustCodeSpinner() {
        ArrayList<String> codeAndDescriptions = new ArrayList<String>();
        for (AdjustCode code : adjustCodes) {
            codeAndDescriptions.add(code.toString());
        }

        ArrayAdapter<CharSequence> spinAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, codeAndDescriptions.toArray(new String[codeAndDescriptions.size()]));
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        removalReasonCode.setAdapter(new NothingSelectedSpinnerAdapter(spinAdapter, R.layout.spinner_nothing_selected, this));

        removalReasonCode.setOnItemSelectedListener(new updateAdjustCodeDescription());
    }

    private class updateAdjustCodeDescription implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (removalReasonCode.getSelectedItem() != null) {
                AdjustCode code = getAdjustCode(removalReasonCode.getSelectedItem().toString());
                removalRequest.setAdjustCode(code);

                enableBarcode();
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

    private void clearBarcode() {
        barcode.setText("");
        barcode.requestFocus();
    }

    private void disableBarcode() {
        barcode.setFocusableInTouchMode(false); // Must select an adjustment code before entering items.

        barcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toasty.displayCenteredMessage(EnterRemovalRequestActivity.this, "Please select a Removal Reason.", Toast.LENGTH_SHORT);
            }
        });
    }

    private void enableBarcode() {
        barcode.setFocusableInTouchMode(true);
        barcode.setOnClickListener(null);
        barcode.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.SHOW_IMPLICIT);
    }

}
