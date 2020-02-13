package gov.nysenate.inventory.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.nysenate.inventory.adapter.NothingSelectedSpinnerAdapter;
import gov.nysenate.inventory.adapter.RemovalRequestItemSelectionAdapter;
import gov.nysenate.inventory.android.AppSingleton;
import gov.nysenate.inventory.android.CancelBtnFragment;
import gov.nysenate.inventory.android.InvApplication;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.android.StringInvRequest;
import gov.nysenate.inventory.android.asynctask.UpdateRemovalRequest;
import gov.nysenate.inventory.comparator.RemovalRequestComparer;
import gov.nysenate.inventory.model.AdjustCode;
import gov.nysenate.inventory.model.Item;
import gov.nysenate.inventory.model.ItemStatus;
import gov.nysenate.inventory.model.RemovalRequest;
import gov.nysenate.inventory.util.AppProperties;
import gov.nysenate.inventory.util.HttpUtils;
import gov.nysenate.inventory.util.Serializer;
import gov.nysenate.inventory.util.Toasty;

public class EditRemovalRequest extends SenateActivity
        implements UpdateRemovalRequest.UpdateRemovalRequestI, CancelBtnFragment.CancelBtnOnClick,
        RemovalRequestItemSelectionAdapter.RemovalRequestItemSelectionAdapterI {
    private RemovalRequest removalRequest;
    private List<AdjustCode> adjustCodes;
    private RemovalRequestItemSelectionAdapter adapter;

    private AdjustCode originalAdjustCode;
    private String originalStatus;

    private TextView transactionNumView;
    private TextView requestedBy;
    private TextView status;
    private TextView date;
    private TextView itemCount;
    private Spinner adjustCodeView;
    private ListView itemList;
    private ProgressBar progressBar;
    private CheckBox checkbox;
    private Button saveBtn;
    private Button rejectComments;

    private void initializeViewObjects() {
        transactionNumView = (TextView) findViewById(R.id.transaction_num);
        requestedBy = (TextView) findViewById(R.id.requested_by);
        status = (TextView) findViewById(R.id.status);
        date = (TextView) findViewById(R.id.date);
        itemCount = (TextView) findViewById(R.id.item_count);
        adjustCodeView = (Spinner) findViewById(R.id.adjust_code);
        itemList = (ListView) findViewById(R.id.removal_request_item_list);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        saveBtn = (Button) findViewById(R.id.continue_btn);
        rejectComments = (Button) findViewById(R.id.inventory_control_comments);
        checkbox = (CheckBox) findViewById(R.id.submit_check_box);
        checkbox.setEnabled(false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_removal_request);
        registerBaseActivityReceiver();

        initializeViewObjects();

        initializeAdjustCodes();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initializeAdjustCodes() {
        queryAdjustCodes();
    }

    public void queryAdjustCodes() {
        progressBar.setVisibility(ProgressBar.VISIBLE);

        StringInvRequest stringInvRequest = new StringInvRequest(Request.Method.GET,
                AppProperties.getBaseUrl(this) + "AdjustCodeServlet", null, adjustCodeListResponseListener);

        /* Add your Requests to the RequestQueue to execute */
        AppSingleton.getInstance(InvApplication.getAppContext()).addToRequestQueue(stringInvRequest);
    }

    Response.Listener adjustCodeListResponseListener = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            try {
                progressBar.setVisibility(ProgressBar.INVISIBLE);

                adjustCodes = Serializer.deserialize(response, AdjustCode.class);

                if (adjustCodes != null) {
                    initializeAdjustCodeSpinner();
                    initializeRemovalRequest();
                } else {
                    Toasty.displayCenteredMessage(EditRemovalRequest.this, "A Server Error has occured, Please try again.", Toast.LENGTH_SHORT);
                }

            } catch (Exception e) {
                e.printStackTrace();
                new Toasty(EditRemovalRequest.this).showMessage("!!ERROR: Problem with getting Pickup informatiom. " + e.getMessage() + " Please contact STSBAC.");
            }
        }
    };

    private void initializeRemovalRequest() {
        getRemovalRequests();
    }

    public void getRemovalRequests() {
        progressBar.setVisibility(ProgressBar.VISIBLE);

        StringInvRequest stringInvRequest = new StringInvRequest(Request.Method.GET,
                AppProperties.getBaseUrl(EditRemovalRequest.this) + "RemovalRequest?id=" + Integer.valueOf(getIntent().getStringExtra("transactionNum")), null, removalRequestResponseListener);

        Log.i(this.getClass().getName(), AppProperties.getBaseUrl(EditRemovalRequest.this) + "RemovalRequest?id=" + Integer.valueOf(getIntent().getStringExtra("transactionNum")));

        /* Add your Requests to the RequestQueue to execute */
        AppSingleton.getInstance(InvApplication.getAppContext()).addToRequestQueue(stringInvRequest);
    }

    Response.Listener removalRequestResponseListener = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            RemovalRequest rr = null;
            rr = Serializer.deserialize(response, RemovalRequest.class).get(0);
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
    };

    private void initializeItemListAdapter() {
        List<Item> itemsSorted = removalRequest.getItems();
        RemovalRequestComparer removalRequestComparer = new RemovalRequestComparer();
        Collections.sort(itemsSorted, removalRequestComparer);
        adapter = new RemovalRequestItemSelectionAdapter(this, R.layout.two_column_and_checkbox_adapter, R.id.column1, itemsSorted, this);
        itemList.setAdapter(adapter);
    }

    private void updateView() {
        transactionNumView.setText(String.valueOf(removalRequest.getTransactionNum()));
        requestedBy.setText(removalRequest.getEmployee());
        populateStatus();
        itemCheckBoxPressed();
        date.setText(((InvApplication) getApplication()).getDateTimeFormat().format(removalRequest.getDate()));
        adjustCodeView.setSelection(removalRequestAdjustCodePosition());
        adapter.notifyDataSetChanged();
        setupRejectCommentsBtn();
    }

    private void populateStatus() {
        if (removalRequest.getStatus().equals("RJ")) {
            status.setText(removalRequest.getStatus() + " - Rejected");
        } else if (removalRequest.getStatus().equals("PE")) {
            status.setText(removalRequest.getStatus() + " - Pending");
        }
    }

    private void setupRejectCommentsBtn() {

        if (rejectCommentsExist()) {
            rejectComments.setVisibility(View.VISIBLE);
            rejectComments.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(EditRemovalRequest.this)
                            .setCancelable(true)
                            .setTitle("Reject Comments")
                            .setMessage(removalRequest.getInventoryControlComments())
                            .setNeutralButton("Done", null);

                    builder.show();
                }
            });
        }
    }

    private boolean rejectCommentsExist() {
        return removalRequest.getInventoryControlComments() != null
                && removalRequest.getInventoryControlComments().length() > 0;
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
            removalRequest.setStatus(originalStatus);
        }
    }

    public void onSaveBtnClick(View view) {
        saveBtn.setEnabled(false);

        if (noChangesMade()) {
            Toasty.displayCenteredMessage(this, "You have not made any changes", Toast.LENGTH_SHORT);
            saveBtn.setEnabled(true);
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
                .setNegativeButton(Html.fromHtml("<b>Cancel</b>"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveBtn.setEnabled(true);
                    }
                })
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
                .setNegativeButton(Html.fromHtml("<b>Cancel</b>"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveBtn.setEnabled(true);
                    }
                })
                .setPositiveButton(Html.fromHtml("<b>Ok</b>"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updateRemovalRequest();
                    }
                });
        builder.show();
    }

    private boolean allItemsDeleted() {
        for (Item i : removalRequest.getItems()) {
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
        return getChangeMessageBody(s);
    }

    private String getChangeMessageBody(String s) {
        if (!originalStatus.equals(removalRequest.getStatus())) {
            s += "Status: Submitted to Inventory Control.<br>";
        }
        if (!originalAdjustCode.equals(removalRequest.getAdjustCode())) {
            s += "Adjust Code: " + removalRequest.getAdjustCode().getCode() + ", " + removalRequest.getAdjustCode().getDescription() + ".<br>";
        }
        if (getItemsDeleted().size() > 0) {
            s += "Delete: " + getItemsDeleted().size() + " items.<br>";
        }
        return s;
    }

    public void updateRemovalRequest() {
        Map<String, String> params = new HashMap<>();
        Log.i(this.getClass().getName(), "removalRequest: " + Serializer.serialize(removalRequest));
        params.put("RemovalRequest", Serializer.serialize(removalRequest));

        StringInvRequest stringInvRequest = new StringInvRequest(Request.Method.POST,
                AppProperties.getBaseUrl(EditRemovalRequest.this) + "RemovalRequest", params, updateRemovalResponseListener) {

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";  // ; charset=UTF-8
            }
        };


//  Old code for testing purposes
        List<NameValuePair> values = new ArrayList<NameValuePair>();
        values.add(new BasicNameValuePair("RemovalRequest", Serializer.serialize(removalRequest)));
        UrlEncodedFormEntity urlEncodedFormEntity = null;

        try {
            urlEncodedFormEntity = new UrlEncodedFormEntity(values);
        } catch (Exception e) {
            e.printStackTrace();
        }
//  Old code for testing purposes  END
        String oldRequest = null;
        try {
            String newRequest = new String(stringInvRequest.getBody());
            oldRequest = HttpUtils.ConvertStreamToString(urlEncodedFormEntity.getContent());

//            new MsgAlert(this).showMessage("NEW REQUEST", newRequest);
//            new MsgAlert(this).showMessage("OLD REQUEST", oldRequest);

            Log.i(this.getClass().getName(), "OLD HEADER: " + urlEncodedFormEntity.getContentType().getValue());
            Log.i(this.getClass().getName(), "NEW HEADER: " + stringInvRequest.getBodyContentType());

            Log.i(this.getClass().getName(), "OLD: " + oldRequest);
            Log.i(this.getClass().getName(), "   ");
            Log.i(this.getClass().getName(), "NEW: " + newRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }

        /* Add your Requests to the RequestQueue to execute */
        AppSingleton.getInstance(InvApplication.getAppContext()).addToRequestQueue(stringInvRequest);
    }

    Response.Listener updateRemovalResponseListener = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {

            removalRequest = Serializer.deserialize(response, RemovalRequest.class).get(0);

            removalRequest.setStatusCode(HttpStatus.SC_OK);

            EditRemovalRequest.this.onRemovalRequestUpdated(removalRequest);
        }
    };

    @Override
    public void onRemovalRequestUpdated(RemovalRequest rr) {
        String title;
        String message = "Removal Request successfully updated. <br><br>";

        if (rr != null && rr.getStatusCode() == HttpStatus.SC_OK) {
            title = "Info";
            message = "Removal Request successfully updated. <br><br>";
            message = getChangeMessageBody(message);
        } else {
            title = "Error";
            switch (rr.getStatusCode()) {
                case HttpStatus.SC_BAD_REQUEST:
                    message = "!!ERROR: Invalid parameter, your update may not have been saved. Please contact STS/BAC.";
                    break;
                case HttpStatus.SC_INTERNAL_SERVER_ERROR:
                    message = "!!ERROR: Database Error, your update may not have been saved. Please contact STS/BAC.";
                    break;
                default:
                    title = "Error";
                    message = "!!ERROR: Unknown Error, your update may not have been saved. Please contact STS/BAC.";
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(title)
                .setMessage(Html.fromHtml(message))
                .setPositiveButton(Html.fromHtml("<b>Ok</b>"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(EditRemovalRequest.this, InventoryRemovalMenu.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                });
        builder.show();
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

    @Override
    public void cancelBtnOnClick(View v) {
        onBackPressed();
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
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    private AdjustCode getAdjustCode(String codeAndDescription) {
        for (AdjustCode code : adjustCodes) {
            if (code.toString().equals(codeAndDescription)) {
                return code;
            }
        }
        return null;
    }

    /**
     * Indicates a checkbox in the list view has been pressed
     */
    @Override
    public void itemCheckBoxPressed() {
        itemCount.setText(String.valueOf(numberOfActiveItems()));
    }

    private int numberOfActiveItems() {
        int count = 0;
        for (Item item : removalRequest.getItems()) {
            if (item.getStatus().equals(ItemStatus.PENDING_REMOVAL)) {
                count++;
            }
        }
        return count;
    }

}