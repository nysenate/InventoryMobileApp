package gov.nysenate.inventory.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gov.nysenate.inventory.android.AppSingleton;
import gov.nysenate.inventory.android.ClearableAutoCompleteTextView;
import gov.nysenate.inventory.android.GetAllPickupsListener;
import gov.nysenate.inventory.android.InvApplication;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.android.StringInvRequest;
import gov.nysenate.inventory.model.Transaction;
import gov.nysenate.inventory.util.AppProperties;
import gov.nysenate.inventory.util.Serializer;
import gov.nysenate.inventory.util.Toasty;

public abstract class SelectDelivery1 extends SenateActivity implements GetAllPickupsListener {

    public enum SearchByParam {PICKUPLOC, DELIVERYLOC, NAPICKUPBY, DATE}

    protected static final int PICKUP_LOCATION_INDEX = 0;
    protected static final int DELIVERY_LOCATION_INDEX = 1;

    protected Spinner searchParam;
    protected static ClearableAutoCompleteTextView searchText;
    final protected List<Transaction> validPickups = new ArrayList<Transaction>();
    protected static Button continueButton;
    protected static Button cancelButton;
    protected TextView tableTitle;
    protected TextView label1Title;
    protected TextView label1Value;
    protected TextView label2Title;
    protected TextView label2Value;
    protected static ProgressBar progressBar;
    protected ArrayAdapter<String> adapter;
    public final int LOCCODELIST_TIMEOUT = 101,
            FROMLOCATIONDETAILS_TIMEOUT = 102, TOLOCATIONDETAILS_TIMEOUT = 103;

    Response.Listener locationResponseListener = new Response.Listener<String>() {

        @Override
        public void onResponse(String response) {
            if (response.indexOf("Session timed out") != -1) {
                startTimeout(LOCCODELIST_TIMEOUT);
                return;
            }
            InvApplication.timeoutType = -1;

            try {
                String respctrhd = "";
                String adstreet1 = "";
                String city = "";
                String zip = "";
                try {
                    JSONObject json = (JSONObject) new JSONTokener(response).nextValue();
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

                for (TextView key : textViews.keySet()) {
                    key.setText(textViews.get(key));
                }
                progressBar.setVisibility(ProgressBar.INVISIBLE);

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    };

    protected abstract String getPickupsUrl();

    protected abstract String getPickupsAPIUrl();

    protected abstract String getPickupsParams();

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
        this.getAllPickupsTask();

    }

    protected void populatePickups(ArrayList<Transaction> allPickups) {
        this.validPickups.addAll(allPickups);
    }

    public void getAllPickupsTask() {
        // Pass second argument as "null" for GET requests

        /*
                            } else if (response.indexOf("Session timed out") > -1) {
                        startTimeout(FROMLOCATIONDETAILS_TIMEOUT);
                        return;
                    }
                } catch (NullPointerException e) {
                    return;
                }


         */

        StringInvRequest req = new StringInvRequest(Request.Method.GET, getPickupsAPIUrl(), null,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            if (response.indexOf("Session timed out") > -1) {
                                startTimeout(FROMLOCATIONDETAILS_TIMEOUT);
                                return;
                            }
                        } catch (NullPointerException e) {
                            return;
                        }

                        InvApplication.timeoutType = -1;
                        SelectDelivery1.this.populatePickups((ArrayList<Transaction>) Serializer.deserialize(response, Transaction.class));
                        progressBar.setVisibility(ProgressBar.INVISIBLE);
                        updateGUI(currentSearchByParam());
                    }
                });
/*
, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
                Toasty.displayCenteredMessage(SelectDelivery1.this, "!!ERROR: Database Error while trying to getInstance pickup info.", Toast.LENGTH_SHORT);
            }
        }
 */
        InvApplication.timeoutType = this.LOCCODELIST_TIMEOUT;

        /* Add your Requests to the RequestQueue to execute */
        AppSingleton.getInstance(InvApplication.getAppContext()).addToRequestQueue(req);
    }

    @Override
    public void onResponseExecute(Integer res) {
        progressBar.setVisibility(ProgressBar.INVISIBLE);
        updateGUI(currentSearchByParam());

        if (res == HttpStatus.SC_OK) {
            return;
        } else if (res == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            Toasty.displayCenteredMessage(SelectDelivery1.this, "!!ERROR: Database Error while trying to getInstance pickup info.", Toast.LENGTH_SHORT);
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
        if (searchTextIsValidValue()) {

            InvApplication.timeoutType = -1;

            switch (currentSearchByParam()) {

                case PICKUPLOC:
                    InvApplication.timeoutType = this.FROMLOCATIONDETAILS_TIMEOUT;
                case DELIVERYLOC:
                    String locationCode = null;
                    String locationType = null;
                    for (Transaction pickup : validPickups) {
                        if (pickup.getOriginSummaryString().equals(searchText.getText().toString())) {
                            locationCode = pickup.getOriginCdLoc();
                            locationType = pickup.getOriginCdLocType();
                        } else if (pickup.getDestinationSummaryString().equals(searchText.getText().toString())) {
                            locationCode = pickup.getDestinationCdLoc();
                            locationType = pickup.getDestinationCdLocType();
                        }
                    }

                    if (locationCode == null || locationType == null) {
                        setLabelsToNA();
                        Toasty.displayCenteredMessage(this, "Entered Text is invalid.", Toast.LENGTH_SHORT);
                        return;
                    }

                    StringInvRequest stringInvRequest = new StringInvRequest(Request.Method.POST,
                            AppProperties.getBaseUrl() + "LocationDetails?location_code=" + locationCode
                                    + "&location_type=" + locationType, null, locationResponseListener);

                    /* Add your Requests to the RequestQueue to execute */

                    if (InvApplication.timeoutType == -1) {
                        InvApplication.timeoutType = this.TOLOCATIONDETAILS_TIMEOUT;
                    }

                    AppSingleton.getInstance(InvApplication.getAppContext()).addToRequestQueue(stringInvRequest);

                    break;

                case NAPICKUPBY:
                    getEmployeeDetails();
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

    public void getEmployeeDetails() {
        // Pass second argument as "null" for GET requests

        String url = AppProperties.getBaseUrl(SelectDelivery1.this);

        url += "GetEmployee?nalast=" + searchText.getText();
        url += "&userFallback=" + LoginActivity.nauser;

        StringInvRequest req = new StringInvRequest(Request.Method.GET, url, null,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        InvApplication.timeoutType = -1;

                        String nafirst = "";
                        String nalast = "";
                        String respctrhd = "";

                        try {
                            JSONObject json = (JSONObject) new JSONTokener(response).nextValue();
                            nafirst = json.getString("nafirst");
                            nalast = json.getString("nalast");
                            respctrhd = json.getString("cdrespctrhd");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Map<TextView, String> textViews = new HashMap<TextView, String>();
                        textViews.put(label1Value, nafirst + " " + nalast);
                        textViews.put(label2Value, respctrhd);

                        for (TextView key : textViews.keySet()) {
                            key.setText(textViews.get(key));
                        }

                        progressBar.setVisibility(ProgressBar.INVISIBLE);

                    }
                });

        InvApplication.timeoutType = -1;


        /* Add your Requests to the RequestQueue to execute */
        AppSingleton.getInstance(InvApplication.getAppContext()).addToRequestQueue(req);
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
        SimpleDateFormat sdf = ((InvApplication) getApplicationContext()).getLongDayFormat();
        return sdf;
    }

}
