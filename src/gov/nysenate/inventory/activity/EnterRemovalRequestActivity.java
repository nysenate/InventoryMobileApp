package gov.nysenate.inventory.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import gov.nysenate.inventory.adapter.NothingSelectedSpinnerAdapter;
import gov.nysenate.inventory.android.RemovalListFragment;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.model.AdjustCode;
import gov.nysenate.inventory.model.InvItem;
import gov.nysenate.inventory.removalrequest.RemovalRequest;
import gov.nysenate.inventory.util.*;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EnterRemovalRequestActivity extends SenateActivity
{
    private TextView transactionNumber;
    private TextView date;
    private Spinner removalReasonCode;
    private TextView removalReasonDescription;
    private EditText barcode;
    private RemovalListFragment fragment;
    private ProgressBar progressBar;

    private RemovalRequest removalRequest;
    private List<AdjustCode> adjustCodes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_removal_request);
        registerBaseActivityReceiver();

        transactionNumber = (TextView) findViewById(R.id.transaction_number);
        date = (TextView) findViewById(R.id.date);
        removalReasonCode = (Spinner) findViewById(R.id.removal_reason_code);
        removalReasonDescription = (TextView) findViewById(R.id.removal_reason_description);
        barcode = (EditText) findViewById(R.id.barcode_text);
        Button cancelBtn = (Button) findViewById(R.id.cancel_btn); // TODO: setup buttons!
        Button continueBtn = (Button) findViewById(R.id.continue_btn);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        fragment = (RemovalListFragment) getFragmentManager().findFragmentById(R.id.removal_list_fragment);

        barcode.addTextChangedListener(barcodeWatcher);

        removalRequest = new RemovalRequest(LoginActivity.nauser);

        QueryAdjustCodes queryAdjustCodes = new QueryAdjustCodes();
        queryAdjustCodes.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, AppProperties.getBaseUrl(this) + "/AdjustCodeServlet");
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void add(InvItem item) {
        if (!allInfoEntered(barcode, removalReasonCode)) {
            return;
        }
        removalRequest.addItem(item);
        updateServer();
    }

    private void updateServer() {
//        UpdateRemovalRequest task = new UpdateRemovalRequest(request);
//        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, AppProperties.getBaseUrl(this) + "/URL...");
//
        clear();
        barcode.requestFocus();

        //TODO: Temp code below
        fragment.setItems(removalRequest.getItems());
    }

    public boolean allInfoEntered(EditText barcode, Spinner removalReason) {
        if (removalReason.getSelectedItem() == null) {
            Toasty.displayCenteredMessage(this, "Please select a Removal Code", Toast.LENGTH_SHORT);
            return false;
        } else if (removalReason.getSelectedItem() != removalRequest.getReason()) {
            // TODO: display confirmation dialog.
        }

        return true;
    }

    private void clear() {
        barcode.setText("");
    }

    TextWatcher barcodeWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable text) {
            if (text.length() == 6) {
                GetItem task = new GetItem();
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                        AppProperties.getBaseUrl(EnterRemovalRequestActivity.this) + "/ItemDetails?barcode_num=" + text.toString());
            }
        }
    };

    public class UpdateRemovalRequest extends AsyncTask<String, Void, Integer>
    // FIXME: change this to update Removal Request
    {
        RemovalRequest removal;

        public UpdateRemovalRequest(RemovalRequest removal) {
            this.removal = removal;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(String... url) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DefaultHttpClient httpClient = LoginActivity.getHttpClient();
            CookieStore store = httpClient.getCookieStore();
            HttpContext context = new BasicHttpContext();

            context.setAttribute(ClientContext.COOKIE_STORE, store);
            HttpResponse response = null;
            try {
                response = new DefaultHttpClient().execute(new HttpGet(url[0]), context);
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return response.getStatusLine().getStatusCode();
        }

        @Override
        protected void onPostExecute(Integer statusCode) {
            if (statusCode == HttpStatus.SC_OK) {
                // TODO:
                //parse RemovalRequest, update global removal reqeust
                // update transcaction number, date.
                // fragment.setItems(removalRequest.getItems());
            }
            else {
                // Error updating removal request
            }
        }
    }

    public class GetItem extends AsyncTask<String, Void, String>
    {
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected String doInBackground(String... url) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DefaultHttpClient httpClient = LoginActivity.getHttpClient();
            CookieStore store = httpClient.getCookieStore();
            HttpContext context = new BasicHttpContext();

            context.setAttribute(ClientContext.COOKIE_STORE, store);
            HttpResponse response = null;
            try {
                response = new DefaultHttpClient().execute(new HttpGet(url[0]), context);
                response.getEntity().writeTo(out);
                return out.toString();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            progressBar.setVisibility(ProgressBar.GONE);
            if (s == null || s.contains("Does not exist in system")) {
                Toasty.displayCenteredMessage(EnterRemovalRequestActivity.this, "Invalid Barcode", Toast.LENGTH_SHORT);
                barcode.setText("");
                // TODO: play sounds
            } else {
                InvItem item = ItemParser.parseItem(s);
                add(item);
            }

        }
    }

    public class QueryAdjustCodes extends AsyncTask<String, Void, Integer> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Integer doInBackground(String... url) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DefaultHttpClient httpClient = LoginActivity.getHttpClient();
            CookieStore store = httpClient.getCookieStore();
            HttpContext context = new BasicHttpContext();

            context.setAttribute(ClientContext.COOKIE_STORE, store);
            HttpResponse response = null;
            try {
                response = new DefaultHttpClient().execute(new HttpGet(url[0]), context);
                response.getEntity().writeTo(out);
                adjustCodes = AdjustCodeParser.parseAdjustCodes(out.toString());
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return response.getStatusLine().getStatusCode();
        }

        @Override
        protected void onPostExecute(Integer integer) {
            if (integer == HttpStatus.SC_OK) {
                initializeAdjustCodeSpinner();
            } else if (integer == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                Toasty.displayCenteredMessage(EnterRemovalRequestActivity.this, "A Server Error has occured, Please try again.", Toast.LENGTH_SHORT);
            }
        }
    }

    private void initializeAdjustCodeSpinner() {
        ArrayList<String> codes = new ArrayList<String>();
        for (AdjustCode ac : adjustCodes) {
            codes.add(ac.getCode());
        }

        ArrayAdapter<CharSequence> spinAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, codes.toArray(new String[codes.size()]));
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        removalReasonCode.setAdapter(new NothingSelectedSpinnerAdapter(spinAdapter, R.layout.spinner_nothing_selected, this));

        removalReasonCode.setOnItemSelectedListener(new updateAdjustCodeDescription());
    }

    private class updateAdjustCodeDescription implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (removalReasonCode.getSelectedItem() != null) {
                AdjustCode selected = getAdjustCode(removalReasonCode.getSelectedItem().toString());
                removalReasonDescription.setText(selected.getDescription());
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {  }
    }

    private AdjustCode getAdjustCode(String code) {
        for (AdjustCode ac : adjustCodes) {
            if (ac.getCode().equals(code)) {
                return ac;
            }
        }
        return null;
    }

}
