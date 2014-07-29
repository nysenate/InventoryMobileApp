package gov.nysenate.inventory.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import com.google.gson.Gson;
import gov.nysenate.inventory.adapter.NothingSelectedSpinnerAdapter;
import gov.nysenate.inventory.android.RemovalListFragment;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.android.asynctask.BaseAsyncTask;
import gov.nysenate.inventory.model.AdjustCode;
import gov.nysenate.inventory.model.Item;
import gov.nysenate.inventory.model.ItemStatus;
import gov.nysenate.inventory.model.RemovalRequest;
import gov.nysenate.inventory.util.*;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
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

    private void initalizeUI() {
        transactionNumber = (TextView) findViewById(R.id.transaction_number);
        date = (TextView) findViewById(R.id.date);
        removalReasonCode = (Spinner) findViewById(R.id.removal_reason_code);
        removalReasonDescription = (TextView) findViewById(R.id.removal_reason_description);
        barcode = (EditText) findViewById(R.id.barcode_text);
        Button cancelBtn = (Button) findViewById(R.id.cancel_btn); // TODO: setup buttons!, change continue to submit.
        Button continueBtn = (Button) findViewById(R.id.continue_btn);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        fragment = (RemovalListFragment) getFragmentManager().findFragmentById(R.id.removal_list_fragment);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_removal_request);
        registerBaseActivityReceiver();

        initalizeUI();

        barcode.addTextChangedListener(barcodeWatcher);
        barcode.setEnabled(false); // Must select an adjustment code before entering items.
        removalRequest = new RemovalRequest(LoginActivity.nauser.toUpperCase(), new Date());
        removalRequest.setStatus("PE");
        date.setText(removalRequest.getDate().toString());

        QueryAdjustCodes queryAdjustCodes = new QueryAdjustCodes();
        queryAdjustCodes.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, AppProperties.getBaseUrl(this) + "/AdjustCodeServlet");
    }

    private TextWatcher barcodeWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable text) {
            if (text.length() == 6) {
                GetItem task = new GetItem();
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                        AppProperties.getBaseUrl(EnterRemovalRequestActivity.this) + "/Item?barcode=" + text.toString());
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
            if (responseCode == HttpStatus.SC_OK) {
                item = ItemParser.parseItem(out);
            }
            return item;
        }

        @Override
        protected void onPostExecute(Item item) {
            progressBar.setVisibility(ProgressBar.GONE);
            if (item != null) {
                if (item.getStatus() == ItemStatus.ACTIVE) {
                    add(item);
                } else {
                    switch(item.getStatus()) {
                        case IN_TRANSIT:
                            Toasty.displayCenteredMessage(EnterRemovalRequestActivity.this, "Item is In Transit, It cannot be removed right now.", Toast.LENGTH_SHORT);
                            break;
                        case INACTIVE:
                            Toasty.displayCenteredMessage(EnterRemovalRequestActivity.this, "Item is already Inactive.", Toast.LENGTH_SHORT);
                            break;
                        case PENDING_REMOVAL:
                            Toasty.displayCenteredMessage(EnterRemovalRequestActivity.this, "Item has already been added to the Inventory Removal Requests", Toast.LENGTH_SHORT);
                            break;
                    }
                    clearBarcode();
                }
            } else {
                Toasty.displayCenteredMessage(EnterRemovalRequestActivity.this, "Invalid Barcode", Toast.LENGTH_SHORT);
                barcode.setText("");
                // TODO: play sounds
            }
        }
    }

    public void add(Item item) {
        item.setStatus(ItemStatus.PENDING_REMOVAL);
        removalRequest.addItem(item);
        fragment.addItem(item);
        clearBarcode();

        UpdateRemovalRequest task = new UpdateRemovalRequest();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, AppProperties.getBaseUrl(this) + "/RemovalRequest");
    }

    public class UpdateRemovalRequest extends AsyncTask<String, Void, Integer>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(String... urls) {
            HttpPost httpPost = new HttpPost(urls[0]);
            List<NameValuePair> values = new ArrayList<NameValuePair>();
            values.add(new BasicNameValuePair("RemovalRequest", new Gson().toJson(removalRequest)));

            DefaultHttpClient httpClient = new DefaultHttpClient();
            CookieStore store = LoginActivity.getHttpClient().getCookieStore();
            HttpContext context = new BasicHttpContext();
            context.setAttribute(ClientContext.COOKIE_STORE, store);

            int statusCode = 0;
            HttpResponse response = null;
            try {
                httpPost.setEntity(new UrlEncodedFormEntity(values));
                response = httpClient.execute(httpPost, context);
                statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == HttpStatus.SC_OK && transactionNumber.getText().length() < 1) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    removalRequest = RemovalRequestParser.parseRemovalRequest(out.toString());
                }
                response.getEntity().consumeContent();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return statusCode;
        }

        @Override
        protected void onPostExecute(Integer statusCode) {
            if (statusCode == HttpStatus.SC_OK) {
                transactionNumber.setText(String.valueOf(removalRequest.getTransactionNum()));
            } else {
                // TODO: handle this error better.
                Toasty.displayCenteredMessage(EnterRemovalRequestActivity.this, "Error updating RR", Toast.LENGTH_SHORT);
            }
        }
    }

    public class QueryAdjustCodes extends BaseAsyncTask<String, Void, List<AdjustCode>> {
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
                AdjustCode code = getAdjustCode(removalReasonCode.getSelectedItem().toString());
                removalReasonDescription.setText(code.getDescription());
                removalRequest.setAdjustCode(code);

                // Also allow entering of barcodes // TODO; do we want this here?
                barcode.setEnabled(true);
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

    private void clearBarcode() {
        barcode.setText("");
        barcode.requestFocus();
    }

}
