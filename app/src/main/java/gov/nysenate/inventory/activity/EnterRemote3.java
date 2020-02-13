package gov.nysenate.inventory.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.nysenate.inventory.adapter.NothingSelectedSpinnerAdapter;
import gov.nysenate.inventory.android.AppSingleton;
import gov.nysenate.inventory.android.ClearableAutoCompleteTextView;
import gov.nysenate.inventory.android.ClearableEditText;
import gov.nysenate.inventory.android.InvApplication;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.android.StringInvRequest;
import gov.nysenate.inventory.listener.VerMethodListener;
import gov.nysenate.inventory.model.Employee;
import gov.nysenate.inventory.model.Transaction;
import gov.nysenate.inventory.util.AppProperties;
import gov.nysenate.inventory.util.HttpUtils;
import gov.nysenate.inventory.util.Serializer;
import gov.nysenate.inventory.util.Toasty;

public class EnterRemote3 extends SenateActivity {

    private Transaction pickup;
    private List<Employee> employeeNameList;

    private Spinner verMethod;
    private ClearableAutoCompleteTextView remoteSigner;
    private ClearableEditText remoteComment;
    private ClearableEditText remoteHelpReferenceNum;

    public ProgressBar progressBar;

    private TextView pickupLocation;
    private TextView deliveryLocation;
    private TextView pickupBy;
    private TextView itemCount;

    private Transaction original;

    //enterRemoteInfoResponseListener

    Response.Listener employeeListResponseListener = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            employeeNameList = Serializer.deserialize(response, Employee.class);
            progressBar.setVisibility(ProgressBar.INVISIBLE);
            initializeRemoteSignerTextBox();
        }
    };

    Response.Listener enterRemoteInfoResponseListener = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            EnterRemote3.this.progressBar.setVisibility(ProgressBar.INVISIBLE);
            HttpUtils.displayResponseResults(EnterRemote3.this, HttpStatus.SC_OK);
            returnToMainMenu();
        }
    };


    Response.Listener getOriginRemoteResponseListener = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            if (response != null && response.trim().length() > 0) {
                original = Serializer.deserialize(response, Transaction.class).get(0);
            }

            if (original != null) {
                setOriginalVerMethod();
                setOriginalSigner();
                setOriginalComments();
                setOriginalHelpNum();
            }
            LoginActivity.activeAsyncTask = null;
        }
    };

    Response.Listener getPickupResponseListener = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            try {
                pickup = Serializer.deserialize(response, Transaction.class).get(0);

                if (pickup.isRemote()) {
                    pickupLocation.setText(Html.fromHtml(pickup.getOrigin().getLocationSummaryStringRemoteAppended()));
                    deliveryLocation.setText(Html.fromHtml(pickup.getDestination().getLocationSummaryStringRemoteAppended()));
                } else {
                    pickupLocation.setText(pickup.getOrigin().getLocationSummaryString());
                    deliveryLocation.setText(pickup.getDestination().getLocationSummaryString());
                }
                pickupBy.setText(pickup.getNapickupby());
                itemCount.setText(Integer.toString(pickup.getPickupItems().size()));
                checkForPreviousEntry();
            } catch (Exception e) {
                e.printStackTrace();
                new Toasty(EnterRemote3.this).showMessage("!!ERROR: Problem with getting Pickup informatiom. " + e.getMessage() + " Please contact STSBAC.");
            }
            ArrayAdapter<CharSequence> spinAdapter = ArrayAdapter.createFromResource(EnterRemote3.this, R.array.remote_ver_method, android.R.layout.simple_spinner_item);
            spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            verMethod.setAdapter(new NothingSelectedSpinnerAdapter(spinAdapter, R.layout.spinner_nothing_selected, EnterRemote3.this));
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_remote_3);
        registerBaseActivityReceiver();

        pickupLocation = (TextView) findViewById(R.id.old_pickup_location);
        deliveryLocation = (TextView) findViewById(R.id.old_delivery_location);
        pickupBy = (TextView) findViewById(R.id.pickup_by);
        itemCount = (TextView) findViewById(R.id.pickup_count);
        verMethod = (Spinner) findViewById(R.id.remote_method);
        remoteSigner = (ClearableAutoCompleteTextView) findViewById(R.id.remote_employee_signer);
        remoteComment = (ClearableEditText) findViewById(R.id.remote_comments);
        remoteHelpReferenceNum = (ClearableEditText) findViewById(R.id.remote_helprefnum);
        remoteHelpReferenceNum.setVisibility(ClearableEditText.INVISIBLE);
        verMethod.setOnItemSelectedListener(new VerMethodListener(verMethod, remoteHelpReferenceNum, remoteSigner));
        progressBar = (ProgressBar) findViewById(R.id.enter_remote_3_progress_bar);

/*        if (checkServerResponse() != OK) {
            noServerResponse();
        }*/

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            new GetPickup().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            new GetEmployeeList().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            new GetPickup().execute();
            new GetEmployeeList().execute();
        }*/

        getPickup();
        getEmployeeList();
    }

    private void setOriginalVerMethod() {
        Adapter adapter = verMethod.getAdapter();
        for (int i = 1; i < adapter.getCount(); i++) {
            // Start at 1 since 0 is always null in this adapter.
            if (transVerMethodEqualsAdapterItem(adapter, i)) {
                verMethod.setSelection(i);
                return;
            }
        }
    }

    private boolean transVerMethodEqualsAdapterItem(Adapter adapter, int i) {
        return original.getVerificationMethod().equalsIgnoreCase(adapter.getItem(i).toString());
    }

    private void setOriginalSigner() {
        if (pickup.isRemotePickup()) {
            remoteSigner.setText(original.getNareleaseby());
        }
        if (pickup.isRemoteDelivery()) {
            remoteSigner.setText(original.getNaacceptby());
        }
    }

    private void setOriginalComments() {
        remoteComment.setText(original.getVerificationComments());
    }

    private void setOriginalHelpNum() {
        remoteHelpReferenceNum.setText(original.getHelpReferenceNum());
    }

    public void getPickup() {

        String url = AppProperties.getBaseUrl();
        url += "GetPickup?nuxrpd=" + getIntent().getStringExtra("nuxrpd");
        url += "&userFallback=" + LoginActivity.nauser;

        Log.i(this.getClass().getName(), url);

        StringInvRequest stringInvRequest = new StringInvRequest(Request.Method.GET, url, null, getPickupResponseListener);

        /* Add your Requests to the RequestQueue to execute */
        AppSingleton.getInstance(InvApplication.getAppContext()).addToRequestQueue(stringInvRequest);
    }

    public void getOriginalRemote() {

        String url = AppProperties.getBaseUrl();
        url += "PreviousRemoteInfo?nuxrpd=" + pickup.getNuxrpd();

        Log.i(this.getClass().getName(), url);

        StringInvRequest stringInvRequest = new StringInvRequest(Request.Method.GET, url, null, getOriginRemoteResponseListener);

        /* Add your Requests to the RequestQueue to execute */
        AppSingleton.getInstance(InvApplication.getAppContext()).addToRequestQueue(stringInvRequest);
    }

    private void checkForPreviousEntry() {
        getOriginalRemote();
    }

    public void getEmployeeList() {

        String url = AppProperties.getBaseUrl(EnterRemote3.this);
        url += "EmployeeList?";
        url += "userFallback=" + LoginActivity.nauser;

        Log.i(this.getClass().getName(), url);

        StringInvRequest stringInvRequest = new StringInvRequest(Request.Method.GET, url, null, employeeListResponseListener);

        /* Add your Requests to the RequestQueue to execute */
        AppSingleton.getInstance(InvApplication.getAppContext()).addToRequestQueue(stringInvRequest);
    }

    private void initializeRemoteSignerTextBox() {
        remoteSigner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, getAllEmployeeNames()));
        remoteSigner.setThreshold(1);
        remoteSigner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(remoteSigner.getWindowToken(), 0);
            }
        });
    }

    public List<String> getAllEmployeeNames() {
        List<String> allEmployeeNames = new ArrayList<String>();
        for (Employee emp : employeeNameList) {
            allEmployeeNames.add(emp.getFullName());
        }
        return allEmployeeNames;
    }

    public void cancelButton(View view) {
        // if (checkServerResponse(true) == OK) {
        super.onBackPressed();
        // }
    }

    public void continueButton(View view) {
        /*if (checkServerResponse(true) != OK) {
            return;
        }*/

        if (!completelyFilledOut()) {
            return;
        }

        displayConfirmationDialog();
    }

    private void displayConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("Confirmation");
        builder.setNegativeButton(Html.fromHtml("<b>Cancel</b>"), null);
        builder.setPositiveButton(Html.fromHtml("<b>Ok</b>"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateDeliveryInfo();
                updateDeliveryUsers(pickup, remoteSigner.getText().toString());
                enterRemoteInfo();

                /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    new EnterRemoteInfoTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    new EnterRemoteInfoTask().execute();
                }*/
            }
        });
        builder.show();
    }

    private boolean completelyFilledOut() {
        if (verMethod.getSelectedItem() != null) {
            String methodSelected = verMethod.getSelectedItem().toString();
            boolean isEmployeeEntered = findEmployee(remoteSigner.getText().toString(), employeeNameList) > -1;
            boolean isOsrNumEntered = !remoteHelpReferenceNum.getText().toString().isEmpty();

            if (methodSelected.equals("Paperwork")) {
                if (!isEmployeeEntered) {
                    Toasty.displayCenteredMessage(this, "Please enter an employee name", Toast.LENGTH_SHORT);
                    return false;
                } else {
                    return true;
                }
            } else if (methodSelected.equals("Phone Verified")) {
                if (!isEmployeeEntered) {
                    Toasty.displayCenteredMessage(this, "Please enter an employee name", Toast.LENGTH_SHORT);
                    return false;
                } else {
                    return true;
                }
            } else if (methodSelected.equals("OSR Verified")) {
                if (!isEmployeeEntered) {
                    Toasty.displayCenteredMessage(this, "Please enter an employee name", Toast.LENGTH_SHORT);
                    return false;
                }
                if (!isOsrNumEntered) {
                    Toasty.displayCenteredMessage(this, "Please enter an OSR Reference Number", Toast.LENGTH_SHORT);
                    return false;
                } else {
                    return true;
                }
            }
        }
        Toasty.displayCenteredMessage(this, "Please select a verification method.", Toast.LENGTH_SHORT);
        return false;
    }

    private void updateDeliveryInfo() {
        pickup.setVerificationMethod(verMethod.getSelectedItem().toString().split(" ")[0]);
        pickup.setVerificationComments(remoteComment.getText().toString());
        int nuxrefem = getEmployeeId(remoteSigner.getText().toString());
        pickup.setEmployeeId(nuxrefem);
        pickup.setHelpReferenceNum(remoteHelpReferenceNum.getText().toString());
    }

    public int getEmployeeId(String name) {
        for (Employee emp : employeeNameList) {
            if (emp.getFullName().equalsIgnoreCase(name)) {
                return emp.getNuxrefem();
            }
        }
        return 0;
    }

    private void updateDeliveryUsers(Transaction trans, String remoteUser) {
        if (trans.isRemoteDelivery()) {
            trans.setNaacceptby(remoteUser);
        } else {
            trans.setNareleaseby(remoteUser);
        }
    }

    public void enterRemoteInfo() {

        progressBar = (ProgressBar) findViewById(R.id.enter_remote_3_progress_bar);
        progressBar.setVisibility(ProgressBar.VISIBLE);


        String url = AppProperties.getBaseUrl();
        url += "GetPickup?nuxrpd=" + getIntent().getStringExtra("nuxrpd");
        url += "&userFallback=" + LoginActivity.nauser;

        Log.i(this.getClass().getName(), url);

        Map<String, String> params = new HashMap<String, String>();
        params.put("trans", Serializer.serialize(pickup));

        StringInvRequest stringInvRequest = new StringInvRequest(Request.Method.POST, url, params, enterRemoteInfoResponseListener);

        /* Add your Requests to the RequestQueue to execute */
        AppSingleton.getInstance(InvApplication.getAppContext()).addToRequestQueue(stringInvRequest);

    }

    private class EnterRemoteInfoTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected void onPreExecute() {
            progressBar = (ProgressBar) findViewById(R.id.enter_remote_3_progress_bar);
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            Integer responseCode = null;
            String json = null;

            String url = AppProperties.getBaseUrl(EnterRemote3.this);
            url += "EnterRemoteInfo";

            HttpClient httpClient = LoginActivity.getHttpClient();
            HttpPost httpPost = new HttpPost(url);
            try {
                List<NameValuePair> values = new ArrayList<NameValuePair>();
                values.add(new BasicNameValuePair("trans", Serializer.serialize(pickup)));
                httpPost.setEntity(new UrlEncodedFormEntity(values));

                HttpResponse response = httpClient.execute(httpPost);
                StatusLine statusLine = response.getStatusLine();
                responseCode = statusLine.getStatusCode();
                HttpEntity entity = response.getEntity();
                entity.consumeContent();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return responseCode;
        }

        @Override
        protected void onPostExecute(Integer resCode) {
            progressBar.setVisibility(ProgressBar.INVISIBLE);
            HttpUtils.displayResponseResults(EnterRemote3.this, resCode);
            returnToMainMenu();
        }
    }

    public void returnToMainMenu() {
        Intent intent = new Intent(this, MenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.in_right, R.anim.out_left);
    }
}
