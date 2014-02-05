package gov.nysenate.inventory.activity;

import gov.nysenate.inventory.android.ClearableAutoCompleteTextView;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.model.Transaction;
import gov.nysenate.inventory.util.AppProperties;
import gov.nysenate.inventory.util.TransactionParser;
import gov.nysenate.inventory.util.Toasty;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class EditPickup1Activity extends SenateActivity
{
    public enum SearchByParam {PICKUPLOC, DELIVERYLOC, NAPICKUPBY, DATE};
    private SearchByParam currentSearchParam = SearchByParam.PICKUPLOC;
    private Spinner searchParam;
    public static ClearableAutoCompleteTextView searchText;
    private List<Transaction> validPickups;
    private static Button btnEditPickup1Cont;
    private static Button btnEditPickup1Cancel;
    private TextView tableTitle;
    private TextView label1Title;
    private TextView label1Value;
    private TextView label2Title;
    private TextView label2Value;
    private static ProgressBar progressBar;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editpickup1);
        registerBaseActivityReceiver();

        tableTitle = (TextView) findViewById(R.id.tvTableTitle);
        label1Title = (TextView) findViewById(R.id.tvLabel1Title);
        label1Value = (TextView) findViewById(R.id.tvLabel1Value);
        label2Title = (TextView) findViewById(R.id.tvLabel2Title);
        label2Value = (TextView) findViewById(R.id.tvLabel2Value);
        progressBar = (ProgressBar) findViewById(R.id.progBarEditPickup1);
        searchText = (ClearableAutoCompleteTextView) findViewById(R.id.acSearchBy);
        searchParam = (Spinner) findViewById(R.id.spinSearchByList);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line);
        searchText.setThreshold(1);
        searchText.addTextChangedListener(filterTextWatcher);
        searchText.setAdapter(adapter);

        searchText.setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(
                                searchText.getWindowToken(), 0);
                        searchText.setSelection(0);
                        // locationBeingTyped = false;
                    }
                });

        currentSearchParam = SearchByParam.PICKUPLOC;
        if (checkServerResponse(true) == OK) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new GetAllPickups().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                new GetAllPickups().execute();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        btnEditPickup1Cont = (Button) findViewById(R.id.btnEditPickup1Cont);
        btnEditPickup1Cont.getBackground().setAlpha(255);
        btnEditPickup1Cancel = (Button) findViewById(R.id.btnEditPickup1Cancel);
        btnEditPickup1Cancel.getBackground().setAlpha(255);
    }


    @Override
    public void startTimeout(int timeoutType) {
        Intent intentTimeout = new Intent(this, LoginActivity.class);
        intentTimeout.putExtra("TIMEOUTFROM", timeoutFrom);
        startActivityForResult(intentTimeout, timeoutType);
    }

    private void updateGUI(String searchParam) {
        setLabelsToNA();
        if (searchParam.equals("Pickup Location")) {
            currentSearchParam = SearchByParam.PICKUPLOC;
            setupCdLocatFrom();
            setAdapterToPickupLocs();
        } else if (searchParam.equals("Delivery Location")) {
            currentSearchParam = SearchByParam.DELIVERYLOC;
            setupCdLocatTo();
            setAdapterToDeliveryLocs();
        } else if (searchParam.equals("Picked Up By")) {
            currentSearchParam = SearchByParam.NAPICKUPBY;
            setupNaPickupBy();
            setAdapterToPickupBy();
        } else if (searchParam.equals("Date")) {
            currentSearchParam = SearchByParam.DATE;
            setDtTxOrigin();
            setAdapterToDate();
        }
    }

    private void setLabelsToNA() {
        label1Value.setText("N/A");
        label2Value.setText("N/A");
    }

    private void setupCdLocatFrom() {
        tableTitle.setText("Pickup Location");
        searchText.setHint("Scan or enter location code.");
        label1Title.setText("Resp Center Hd:");
        label2Title.setText("Address:");
    }

    private void setupCdLocatTo() {
        tableTitle.setText("Delivery Location");
        searchText.setHint("Scan or enter location code.");
        label1Title.setText("Resp Center Hd:");
        label2Title.setText("Address:");
    }

    private void setupNaPickupBy() {
        tableTitle.setText("Picked Up By");
        searchText.setHint("Enter mobile app user name.");
        label1Title.setText("Senate Employee:");
        label2Title.setText("Resp Center Hd:");
    }

    private void setDtTxOrigin() {
        tableTitle.setText("Pickup Date");
        searchText.setHint("Enter Pickup Date.");
        label1Title.setText("Number of Pickups:");
        label2Title.setText("");
    }

    private TextWatcher filterTextWatcher = new TextWatcher()
    {

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            String loccode;
            String text = searchText.getText().toString();
            if (searchTextIsValidValue()) {
                switch (currentSearchParam) {

                case PICKUPLOC:
                    loccode = text.split("-")[0];
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        new LocationDetails(loccode).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        new LocationDetails(loccode).execute();
                    }

                    break;

                case DELIVERYLOC:
                    loccode = text.split("-")[0];
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        new LocationDetails(loccode).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        new LocationDetails(loccode).execute();
                    }

                    break;

                case NAPICKUPBY:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        new EmployeePickupInfo().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        new EmployeePickupInfo().execute();
                    }
                    break;

                case DATE:
                    int count = getCountForDate(searchText.getText().toString());
                    label1Value.setText(Integer.toString(count));
                    break;
                }
            } else {
                setLabelsToNA();
            }
        }
    };

    private boolean searchTextIsValidValue() {
        String selection = searchText.getText().toString();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (selection.equals(adapter.getItem(i).toString())) {
                return true;
            }
        }
        return false;
    }

    public void noServerResponse() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle(Html
                .fromHtml("<font color='#000055'>NO SERVER RESPONSE</font>"));

        // set dialog message
        alertDialogBuilder
                .setMessage(
                        Html.fromHtml("!!ERROR: There was <font color='RED'><b>NO SERVER RESPONSE</b></font>. <br/> Please contact STS/BAC."))
                .setCancelable(false)
                .setPositiveButton(Html.fromHtml("<b>Ok</b>"), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        Context context = getApplicationContext();

                        CharSequence text = "No action taken due to NO SERVER RESPONSE";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();

                        dialog.dismiss();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public void continueButton(View view) {
        if (checkServerResponse(true) != OK) {
            return;
        }

        Intent intent = new Intent(this, EditPickup2Activity.class);
        intent.putExtra("searchParam", searchParam.getSelectedItem().toString());
        intent.putExtra("searchText", searchText.getText().toString());
        ArrayList<String> json = new ArrayList<String>();
        for (Transaction tran: validPickups) {
            json.add(tran.toJson());
        }
        intent.putStringArrayListExtra("pickups", json);
        startActivity(intent);
        overridePendingTransition(R.anim.in_right, R.anim.out_left);
            
    }

    public void cancelButton(View view) {
        btnEditPickup1Cancel.getBackground().setAlpha(45);
        finish();
        overridePendingTransition(R.anim.in_left, R.anim.out_right);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_editpickup1, menu);
        return true;
    }

    private class GetAllPickups extends AsyncTask<Void, Void, Integer> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected Integer doInBackground(Void... arg0) {

            HttpClient httpClient = LoginActivity.getHttpClient();
            HttpResponse response = null;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            String url = AppProperties.getBaseUrl(EditPickup1Activity.this);
            url += "GetAllPickups?";
            url += "userFallback=" + LoginActivity.nauser;

            try {
                response = httpClient.execute(new HttpGet(url));
                response.getEntity().writeTo(out);
                validPickups = TransactionParser.parseMultiplePickups(out.toString());
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return response.getStatusLine().getStatusCode();
        }

        @Override
        protected void onPostExecute(Integer response) {
            progressBar.setVisibility(ProgressBar.INVISIBLE);
            updateGUI(searchParam.getSelectedItem().toString());
            searchParam.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    searchText.setText("");
                    updateGUI(searchParam.getSelectedItem().toString());
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            if (response == HttpStatus.SC_OK) {
                return;
            } else if (response == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                Toasty.displayCenteredMessage(EditPickup1Activity.this, "!!ERROR: Database Error while trying to get pickup info.", Toast.LENGTH_SHORT);
            } else {
                Toasty.displayCenteredMessage(EditPickup1Activity.this, "!!ERROR: Unknown Error occured pickup data may be inaccurate.", Toast.LENGTH_SHORT);
            }
        }
    }

    private void updateAdapter(Set<String> values) {
        adapter.clear();
        adapter.addAll(values);
    }

    private void setAdapterToPickupLocs() {
        Set<String> locSummarys = new HashSet<String>();
        for (Transaction pickup : validPickups) {
            locSummarys.add(pickup.getOriginSummaryString());
        }
        updateAdapter(locSummarys);
    }

    private void setAdapterToDeliveryLocs() {
        Set<String> locSummarys = new HashSet<String>();
        for (Transaction pickup : validPickups) {
            locSummarys.add(pickup.getDestinationSummaryString());
        }
        updateAdapter(locSummarys);
    }

    private void setAdapterToPickupBy() {
        Set<String> users = new HashSet<String>();
        for (Transaction pickup : validPickups) {
            users.add(pickup.getNapickupby());
        }
        updateAdapter(users);
    }

    private void setAdapterToDate() {
        SimpleDateFormat sdf = getSimpleDateFormat();
        Set<String> dates = new HashSet<String>(); // TODO: hashset<Date> ?
        for (Transaction pickup : validPickups) {
            dates.add(sdf.format(pickup.getPickupDate()));
        }
        updateAdapter(dates);
    }

    private class LocationDetails extends AsyncTask<Void, Map<TextView, String>, String> {

        private String loccode;

        public LocationDetails(String loccode) {
            this.loccode = loccode;
        }

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... params) {
            if (checkServerResponse(true) != OK) {
                return "";
            }

            HttpClient httpClient = LoginActivity.getHttpClient();
            HttpResponse response = null;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            String url = AppProperties.getBaseUrl(EditPickup1Activity.this);

            // barcode_num is actually the cdLoc
            url += "LocationDetails?barcode_num=" + loccode;
            url += "&userFallback=" + LoginActivity.nauser;

            try {
                response = httpClient.execute(new HttpGet(url));
                response.getEntity().writeTo(out);
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String respctrhd = "";
            String adstreet1 = "";
            String city = "";
            String zip = "";
            try {
                JSONObject json = (JSONObject) new JSONTokener(out.toString()).nextValue();
                // newLocation.setCdlocat(json.getString("cdlocat"));
                respctrhd = json.getString("cdrespctrhd");
                adstreet1 = json.getString("adstreet1");
                city = json.getString("adcity");
                zip = json.getString("adzipcode");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Map<TextView, String> textViews = new HashMap<TextView, String>();
            textViews.put(label1Value, respctrhd);
            textViews.put(label2Value, adstreet1 + " " + city + " " + zip);
            publishProgress(textViews);

            return out.toString();
        }

        @Override
        protected void onProgressUpdate(Map<TextView, String>... map) {
            for (TextView key : map[0].keySet()) {
                key.setText(map[0].get(key));
            }
        }

        @Override
        protected void onPostExecute(String response) {
            progressBar.setVisibility(ProgressBar.INVISIBLE);
        }
    }

    private class EmployeePickupInfo extends AsyncTask<Void, Map<TextView, String>, Integer> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            if (checkServerResponse(true) != OK) {
                return 0;
            }

            HttpClient httpClient = LoginActivity.getHttpClient();
            HttpResponse response = null;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            String url = AppProperties.getBaseUrl(EditPickup1Activity.this);

            url += "GetEmployee?nalast=" + searchText.getText();
            url += "&userFallback=" + LoginActivity.nauser;

            try {
                response = httpClient.execute(new HttpGet(url));
                response.getEntity().writeTo(out);
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String nafirst = "";
            String nalast = "";
            String respctrhd = "";
            try {
                JSONObject json = (JSONObject) new JSONTokener(out.toString()).nextValue();
                nafirst = json.getString("nafirst");
                nalast = json.getString("nalast");
                respctrhd = json.getString("cdrespctrhd");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Map<TextView, String> textViews = new HashMap<TextView, String>();
            textViews.put(label1Value, nafirst + " " + nalast);
            textViews.put(label2Value, respctrhd);
            publishProgress(textViews);

            return response.getStatusLine().getStatusCode();
        }

        @Override
        protected void onProgressUpdate(Map<TextView, String>... map) {
            for (TextView key : map[0].keySet()) {
                key.setText(map[0].get(key));
            }
        }

        @Override
        protected void onPostExecute(Integer response) {
            progressBar.setVisibility(ProgressBar.INVISIBLE);
            // TODO: test status code for errors.
        }
    }

    private int getCountForDate(String date) {
        SimpleDateFormat sdf = getSimpleDateFormat();
        int count = 0;
        for (Transaction pickup : validPickups) {
            if (sdf.format(pickup.getPickupDate()).equals(date)) {
                count++;
            }
        }
        return count;
    }

    private SimpleDateFormat getSimpleDateFormat() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy EEEE", Locale.US);
        return sdf;
    }
}
