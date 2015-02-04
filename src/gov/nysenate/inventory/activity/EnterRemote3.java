package gov.nysenate.inventory.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import gov.nysenate.inventory.adapter.NothingSelectedSpinnerAdapter;
import gov.nysenate.inventory.android.ClearableAutoCompleteTextView;
import gov.nysenate.inventory.android.ClearableEditText;
import gov.nysenate.inventory.android.OrigRemoteTask;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.listener.VerMethodListener;
import gov.nysenate.inventory.model.Employee;
import gov.nysenate.inventory.model.Transaction;
import gov.nysenate.inventory.util.*;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EnterRemote3 extends SenateActivity {

    private Transaction pickup;
    private List<Employee> employeeNameList;

    private Spinner verMethod;
    private ClearableAutoCompleteTextView remoteSigner;
    private ClearableEditText remoteComment;
    private ClearableEditText remoteHelpReferenceNum;

    private ProgressBar progressBar;

    private TextView pickupLocation;
    private TextView deliveryLocation;
    private TextView pickupBy;
    private TextView itemCount;

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

        if (checkServerResponse() != OK) {
            noServerResponse();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            new GetPickup().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            new GetEmployeeList().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            new GetPickup().execute();
            new GetEmployeeList().execute();
        }

        ArrayAdapter<CharSequence> spinAdapter = ArrayAdapter.createFromResource(this, R.array.remote_ver_method, android.R.layout.simple_spinner_item);
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        verMethod.setAdapter(new NothingSelectedSpinnerAdapter(spinAdapter, R.layout.spinner_nothing_selected, this));
    }

    private class GetPickup extends AsyncTask<Void, Void, Integer> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Integer doInBackground(Void... arg0) {
            if (checkServerResponse(true) != OK) {
                return 0;
            }
            HttpClient httpClient = LoginActivity.getHttpClient();
            HttpResponse response = null;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            String url = AppProperties.getBaseUrl(EnterRemote3.this);
            url += "GetPickup?nuxrpd=" + getIntent().getStringExtra("nuxrpd");
            url += "&userFallback=" + LoginActivity.nauser;

            try {
                response = httpClient.execute(new HttpGet(url));
                response.getEntity().writeTo(out);
                pickup = TransactionParser.parseTransaction(out.toString());
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return response.getStatusLine().getStatusCode();
        }

        @Override
        protected void onPostExecute(Integer response) {
            if (response == HttpStatus.SC_OK) {
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
            }
        }
    }

    private void checkForPreviousEntry() {
        OrigRemoteTask task = new OrigRemoteTask(this, pickup, verMethod, remoteSigner, remoteComment, remoteHelpReferenceNum);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            task.execute();
        }
    }

    private class GetEmployeeList extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            progressBar = (ProgressBar) findViewById(R.id.enter_remote_3_progress_bar);
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... params) {
            String responseJson = "";

            try {
                String url = AppProperties.getBaseUrl(EnterRemote3.this);
                url += "EmployeeList?";
                url += "userFallback=" + LoginActivity.nauser;

                // We cant use the same HttpClient (LoginActivity.httpClient) in 2 requests at the same time.
                // Therefore we copy the cookies into a new HttpClient.
                DefaultHttpClient httpClient = LoginActivity.getHttpClient();
                CookieStore store = httpClient.getCookieStore();
                HttpContext context = new BasicHttpContext();
                context.setAttribute(ClientContext.COOKIE_STORE, store);

                HttpResponse response = new DefaultHttpClient().execute(new HttpGet(url), context);

                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    responseJson = out.toString();
                    employeeNameList = EmployeeParser.parseMultipleEmployees(responseJson);
                } else {
                    // Closes the connection.
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return responseJson;
        }

        @Override
        protected void onPostExecute(String response) {
            progressBar.setVisibility(ProgressBar.INVISIBLE);
            initializeRemoteSignerTextBox();
        }

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
        for (Employee emp: employeeNameList) {
            allEmployeeNames.add(emp.getFullName());
        }
        return allEmployeeNames;
    }

    public void cancelButton(View view) {
        if (checkServerResponse(true) == OK) {
            super.onBackPressed();
        }
    }

    public void continueButton(View view) {
        if (checkServerResponse(true) != OK) {
            return;
        }

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

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    new EnterRemoteInfoTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    new EnterRemoteInfoTask().execute();
                }
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
        for (Employee emp: employeeNameList) {
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
                values.add(new BasicNameValuePair("trans", pickup.toJson()));
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

    private void returnToMainMenu() {
        Intent intent = new Intent(this, MenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.in_right, R.anim.out_left);
    }
}
