package gov.nysenate.inventory.activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import gov.nysenate.inventory.android.*;
import gov.nysenate.inventory.model.Transaction;
import gov.nysenate.inventory.util.AppProperties;
import gov.nysenate.inventory.util.Toasty;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public abstract class SelectDelivery1 extends SenateActivity implements GetAllPickupsListener {

    public enum SearchByParam {PICKUPLOC, DELIVERYLOC, NAPICKUPBY, DATE}

    protected static final int PICKUP_LOCATION_INDEX = 0;
    protected static final int DELIVERY_LOCATION_INDEX = 1;

    protected Spinner searchParam;
    protected static ClearableAutoCompleteTextView searchText;
    protected List<Transaction> validPickups;
    protected static Button continueButton;
    protected static Button cancelButton;
    protected TextView tableTitle;
    protected TextView label1Title;
    protected TextView label1Value;
    protected TextView label2Title;
    protected TextView label2Value;
    protected static ProgressBar progressBar;
    protected ArrayAdapter<String> adapter;


    protected abstract String getPickupsUrl();

    protected abstract int getInitialSearchByParam();

    protected abstract Class getNextActivity();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_delivery_1);
        registerBaseActivityReceiver();

        tableTitle = (TextView) findViewById(R.id.tvTableTitle);
        label1Title = (TextView) findViewById(R.id.tvLabel1Title);
        label1Value = (TextView) findViewById(R.id.tvLabel1Value);
        label2Title = (TextView) findViewById(R.id.tvLabel2Title);
        label2Value = (TextView) findViewById(R.id.tvLabel2Value);
        cancelButton = (Button) findViewById(R.id.cancelBtn);
        continueButton = (Button) findViewById(R.id.continueBtn);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        searchText = (ClearableAutoCompleteTextView) findViewById(R.id.autoCompleteSearchBy);
        searchParam = (Spinner) findViewById(R.id.spinSearchByList);

        setupSearchTextAutoComplete();
        setupSearchParam();
    }

    private void setupSearchTextAutoComplete() {
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line);
        searchText.setThreshold(1);
        searchText.addTextChangedListener(searchTextWatcher);
        searchText.setAdapter(adapter);
        searchText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchText.getWindowToken(), 0);
                searchText.setSelection(0);
            }
        });
    }

    private void setupSearchParam() {
        searchParam.setSelection(getInitialSearchByParam());
        searchParam.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                searchText.setText("");
                updateGUI(currentSearchByParam());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        continueButton.setEnabled(true);
        validPickups = new ArrayList<Transaction>();

        GetAllPickupsTask task = new GetAllPickupsTask(this, getPickupsUrl(), validPickups);
        task.setProgressBar(progressBar);
        if (checkServerResponse(true) == OK) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                task.execute();
            }
        }
    }

    @Override
    public void onResponseExecute(Integer res) {
        progressBar.setVisibility(ProgressBar.INVISIBLE);
        updateGUI(currentSearchByParam());

        if (res == HttpStatus.SC_OK) {
            return;
        } else if (res == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            Toasty.displayCenteredMessage(SelectDelivery1.this, "!!ERROR: Database Error while trying to get pickup info.", Toast.LENGTH_SHORT);
        } else {
            Toasty.displayCenteredMessage(SelectDelivery1.this, "!!ERROR: Unknown Error occured pickup data may be inaccurate.", Toast.LENGTH_SHORT);
        }
    }

    @Override
    public void startTimeout(int timeoutType) {
        Intent intentTimeout = new Intent(this, LoginActivity.class);
        intentTimeout.putExtra("TIMEOUTFROM", timeoutFrom);
        startActivityForResult(intentTimeout, timeoutType);
    }

    private void updateGUI(SearchByParam searchParam) {
        setLabelsToNA();
        if (searchParam == SearchByParam.PICKUPLOC) {
            setupCdLocatFrom();
            setAdapterToPickupLocs();
        } else if (searchParam == SearchByParam.DELIVERYLOC) {
            setupCdLocatTo();
            setAdapterToDeliveryLocs();
        } else if (searchParam == SearchByParam.NAPICKUPBY) {
            setupNaPickupBy();
            setAdapterToPickupBy();
        } else if (searchParam == SearchByParam.DATE) {
            setDtTxOrigin();
            setAdapterToDate();
        }
    }

    private SearchByParam currentSearchByParam() {
        SearchByParam param = null;
        String search = searchParam.getSelectedItem().toString();
        if (search.equals("Pickup Location")) {
            param = SearchByParam.PICKUPLOC;
        } else if (search.equals("Delivery Location")) {
            param = SearchByParam.DELIVERYLOC;
        } else if (search.equals("Picked Up By")) {
            param = SearchByParam.NAPICKUPBY;
        } else if (search.equals("Date")) {
            param = SearchByParam.DATE;
        }

        return param;
    }

    private void setLabelsToNA() {
        label1Value.setText("N/A");
        if (currentSearchByParam() == SearchByParam.DATE) {
            label2Value.setText("");
        } else {
            label2Value.setText("N/A");
        }
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
        Set<String> dates = new HashSet<String>();
        for (Transaction pickup : validPickups) {
            dates.add(sdf.format(pickup.getPickupDate()));
        }
        updateAdapter(dates);
    }

    private void updateAdapter(Set<String> values) {
        adapter.clear();
        adapter.addAll(values);
    }

    private TextWatcher searchTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            displaySearchResultInfo();
        }
    };

    private void displaySearchResultInfo() {
        String loccode;
        String text = searchText.getText().toString();
        if (searchTextIsValidValue()) {
            switch (currentSearchByParam()) {

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
                        new EmployeeDetails().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        new EmployeeDetails().execute();
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

    private boolean searchTextIsValidValue() {
        String selection = searchText.getText().toString();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (selection.equals(adapter.getItem(i))) {
                return true;
            }
        }
        return false;
    }

    public void continueButton(View view) {
        if (checkServerResponse(true) != OK) {
            return;
        }
        
        if (!searchTextIsValidValue()) {
            Toasty.displayCenteredMessage(this, "Entered Text is invalid.", Toast.LENGTH_SHORT);
            return;
        }
        continueButton.setEnabled(false);

        Intent intent = new Intent(this, getNextActivity());
        intent.putExtra("searchParam", searchParam.getSelectedItem().toString());
        intent.putExtra("searchText", searchText.getText().toString());
        startActivity(intent);
        overridePendingTransition(R.anim.in_right, R.anim.out_left);
    }

    public void cancelButton(View view) {
        cancelButton.getBackground().setAlpha(45);
        finish();
        overridePendingTransition(R.anim.in_left, R.anim.out_right);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_editpickup1, menu);
        return true;
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
            String url = AppProperties.getBaseUrl(SelectDelivery1.this);

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

    private class EmployeeDetails extends AsyncTask<Void, Map<TextView, String>, Integer> {

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
            String url = AppProperties.getBaseUrl(SelectDelivery1.this);

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
        SimpleDateFormat sdf = ((InvApplication)getApplicationContext()).getLongDayFormat();
        return sdf;
    }


}
