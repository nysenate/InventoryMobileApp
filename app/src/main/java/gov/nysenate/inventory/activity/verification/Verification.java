package gov.nysenate.inventory.activity.verification;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import gov.nysenate.inventory.activity.LoginActivity;
import gov.nysenate.inventory.activity.SenateActivity;
import gov.nysenate.inventory.android.AppSingleton;
import gov.nysenate.inventory.android.ClearableAutoCompleteTextView;
import gov.nysenate.inventory.android.InvApplication;
import gov.nysenate.inventory.android.JsonInvObjectRequest;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.android.StringInvRequest;
import gov.nysenate.inventory.model.Location;
import gov.nysenate.inventory.util.AppProperties;
import gov.nysenate.inventory.util.HttpUtils;
import gov.nysenate.inventory.util.Serializer;
import gov.nysenate.inventory.util.Toasty;

public class Verification extends SenateActivity {
    public final static String loc_code_intent = "gov.nysenate.inventory.android.loc_code_str";
    public final static String cdloctype_intent = "gov.nysenate.inventory.android.cdloctype";
    public final static String timeout_intent = "gov.nysenate.inventory.android.timeoutFrom";
    public EditText loc_code;
    public TextView loc_details;
    public String res = null;
    public String status = null;
    public String loc_code_str = null;
    public String cdloctype = null;
    static ClearableAutoCompleteTextView autoCompleteTextView1;
    static Button btnVerify1Cont;
    static Button btnVerify1Cancel;
    TextView tvDescript;
    TextView tvCount;
    TextView tvOffice;
    Activity currentActivity;
    String timeoutFrom = "verification";
    boolean locationBeingTyped = false;
    int lastSize = 0;
    private Map<String, Location> summaryToLocation;

    Response.Listener responseListener = new Response.Listener<JSONObject>() {

        @Override
        public void onResponse(JSONObject response) {
            try {
                InvApplication.timeoutType = -1;

                String cdrespcrthd = response.getString("cdrespctrhd");
                if (cdrespcrthd == null || cdrespcrthd.trim().length() == 0
                        || cdrespcrthd.equals("~")) {
                    tvOffice.setText("N/A");
                } else {
                    tvOffice.setText(response.getString("cdrespctrhd"));
                }
                String descript = response.getString("adstreet1").replaceAll(
                        "&#34;", "\"")
                        + " ,"
                        + response.getString("adcity").replaceAll("&#34;",
                        "\"")
                        + ", "
                        + response.getString("adstate").replaceAll("&#34;",
                        "\"")
                        + " "
                        + response.getString("adzipcode").replaceAll("&#34;",
                        "\"");
                if (descript == null || descript.trim().length() == 0
                        || descript.equals("~")
                        || descript.trim().equals(",,")) {
                    tvDescript.setText("N/A");
                } else {
                    tvDescript.setText(descript);
                }
                String nucount = response.getString("nucount");
                if (nucount == null || nucount.trim().length() == 0
                        || nucount.equals("~")) {
                    tvCount.setText("N/A");
                } else {
                    tvCount.setText(nucount);
                }

            } catch (JSONException e) {
                tvOffice.setText("!!ERROR: " + e.getMessage());
                tvDescript.setText("Please contact STS/BAC.");
                tvCount.setText("N/A");

                e.printStackTrace();
            } catch (
                    Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            status = "yes1";
        }
    };

    Response.Listener locListRespListener = new Response.Listener<String>() {

        @Override
        public void onResponse(String response) {
            try {
                try {
                    if (response == null) {
                        noServerResponse();
                        return;
                    } else if (response.indexOf("Session timed out") > -1) {
                        startTimeout(LOCCODELIST_TIMEOUT);
                        return;
                    }

                } catch (NullPointerException e) {
                    noServerResponse();
                    return;
                }

                InvApplication.timeoutType = -1;

                summaryToLocation = new HashMap<>();
                List<Location> locations = Serializer.deserialize(response, Location.class);
                for (Location loc : locations) {
                    summaryToLocation.put(loc.getLocationSummaryString(), loc);
                }

                List<String> sortedSummaries = new ArrayList<>(summaryToLocation.keySet());
                Collections.sort(sortedSummaries);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(Verification.this,
                        android.R.layout.simple_dropdown_item_1line, sortedSummaries);

                autoCompleteTextView1.setThreshold(1);
                autoCompleteTextView1.setAdapter(adapter);

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    };

    public final int LOCCODELIST_TIMEOUT = 101, LOCATIONDETAILS_TIMEOUT = 102;

    String URL = "";

    public static ProgressBar progBarVerify;

    private TextWatcher filterTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            lastSize = autoCompleteTextView1.getText().toString().trim().length();
        }

        @Override
        public void afterTextChanged(Editable s) {
            locationBeingTyped = true;
            int currentSize = autoCompleteTextView1.getText().toString().trim().length();

            if (currentSize == 0 || currentSize < lastSize) {
                clearLocationDetails();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);
        registerBaseActivityReceiver();
        currentActivity = this;
        AppSingleton.getInstance(this).timeoutFrom = "verification";

        autoCompleteTextView1 = (ClearableAutoCompleteTextView) findViewById(R.id.autoCompleteTextView1);

        //loc_code = (EditText) findViewById(R.id.preferencePWD);
        // code for the autocomplete arraylist of location

        // Button Setup
        btnVerify1Cont = (Button) findViewById(R.id.btnVerify1Cont);
        btnVerify1Cont.getBackground().setAlpha(255);
        btnVerify1Cancel = (Button) findViewById(R.id.btnVerify1Cancel);
        btnVerify1Cancel.getBackground().setAlpha(255);

        // Data TextView Setup
        // tvLocCd= (TextView) findViewById(R.id.tvLocCd);
        tvDescript = (TextView) findViewById(R.id.tvDescript);
        tvCount = (TextView) findViewById(R.id.tvCount);
        tvOffice = (TextView) findViewById(R.id.tvOffice);

        // ProgressBar Setup
        progBarVerify = (ProgressBar) this.findViewById(R.id.progBarVerify);

        getLocCodeList();
        // code for textwatcher

        //loc_code = (EditText) findViewById(R.id.preferencePWD);

        if (filterTextWatcher == null) {
            Log.i(this.getClass().getName(), "filterTextWatcher IS NULL");
        } else {
            autoCompleteTextView1.addTextChangedListener(filterTextWatcher);
        }
        autoCompleteTextView1
                .setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(
                                autoCompleteTextView1.getWindowToken(), 0);
                        Log.i("autocomplete clicked",
                                "Before getLocationDetails");
                        if (autoCompleteTextView1.getText().toString().trim()
                                .length() > 0) {
                            getLocationDetails();
                        }
                        locationBeingTyped = false;
                    }
                });

        //loc_details = (TextView) findViewById(R.id.textView2);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Verification.btnVerify1Cancel == null) {
            Verification.btnVerify1Cancel = (Button) this
                    .findViewById(R.id.btnVerify1Cancel);
        }
        if (Verification.btnVerify1Cont == null) {
            Verification.btnVerify1Cont = (Button) this
                    .findViewById(R.id.btnVerify1Cont);
        }
        Verification.btnVerify1Cancel.getBackground().setAlpha(255);
        Verification.btnVerify1Cont.getBackground().setAlpha(255);
    }

    public void noServerResponse() {
        Log.i(this.getClass().getName(), "NOSERVERRESPONSE (VERIFICATION)");
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle(Html
                .fromHtml("<font color='#000055'>NO SERVER RESPONSE</font>"));

        // set dialog message
        alertDialogBuilder
                .setMessage(
                        Html.fromHtml("!!ERROR: There was <font color='RED'><b>NO SERVER RESPONSE</b></font>. <br/> Please contact STS/BAC."))
                .setCancelable(false)
                .setPositiveButton(Html.fromHtml("<b>Ok</b>"), new DialogInterface.OnClickListener() {
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
        new HttpUtils().playSound(R.raw.noconnect);

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public void locationDetailTimeout() {
        Intent intentTimeout = new Intent(this, LoginActivity.class);
        intentTimeout.putExtra("TIMEOUTFROM", timeoutFrom);
        startActivityForResult(intentTimeout, LOCATIONDETAILS_TIMEOUT);
    }

    @Override
    public void startTimeout(int timeoutType) {
        Intent intentTimeout = new Intent(this, LoginActivity.class);
        intentTimeout.putExtra("TIMEOUTFROM", timeoutFrom);
        startActivityForResult(intentTimeout, timeoutType);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case LOCCODELIST_TIMEOUT:
                if (resultCode == RESULT_OK) {
                    getLocCodeList();
                    break;
                }
            case LOCATIONDETAILS_TIMEOUT:
                if (resultCode == RESULT_OK) {
                    if (locationBeingTyped) {
                        autoCompleteTextView1.setText(autoCompleteTextView1
                                .getText());
                        autoCompleteTextView1.setSelection(autoCompleteTextView1
                                .getText().length());
                    } else {
                        getLocationDetails();
                        autoCompleteTextView1.requestFocus();
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(
                                        autoCompleteTextView1.getWindowToken(), 0);
                            }
                        }, 50);

                    }
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_verification, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                Toast toast = Toast.makeText(getApplicationContext(), "Going Back",
                        Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                NavUtils.navigateUpFromSameTask(this);

                overridePendingTransition(R.anim.in_left, R.anim.out_right);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void clearLocationDetails() {
        tvOffice.setText("N/A");
        tvDescript.setText("N/A");
        tvCount.setText("N/A");

    }

    public void getLocationDetails() {
        if (autoCompleteTextView1.getAdapter().getCount() == 0) {
            return;
        }
        String barcodeNumberDetails[] = autoCompleteTextView1.getText()
                .toString().trim().split("-");
        String barcode_num = barcodeNumberDetails[0];// this will be
        // passed to the
        // server
        loc_code_str = barcodeNumberDetails[0];// this will be passed to
        // next activity with
        // intent
        String[] nextSplit = null;
        if (barcodeNumberDetails.length > 1) {
            nextSplit = barcodeNumberDetails[1].split(":");
            if (nextSplit != null && nextSplit.length > 0)
                cdloctype = nextSplit[0];
            else {
                Log.w("Verification",
                        "**WARNING: Could not extract cdloctype from chosen location (a1).");
                cdloctype = null;
            }
        } else {
            Log.w("Verification",
                    "**WARNING: Could not extract cdloctype from chosen location (a1b).");
            cdloctype = null;

        }

        // check network connection
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // fetch data
            status = "yes";

            Location selectedLocation = summaryToLocation.get(autoCompleteTextView1.getText().toString());
            if (selectedLocation == null) {
                Toasty.displayCenteredMessage(this, "Entered text is invalid.", Toast.LENGTH_SHORT);
                return;
            }

            JsonInvObjectRequest jsonInvObjectRequest = new JsonInvObjectRequest(Request.Method.GET,
                    URL + "LocationDetails?location_code=" + selectedLocation.getCdlocat()
                            + "&location_type=" + selectedLocation.getCdloctype(), null, responseListener);

            InvApplication.timeoutType = this.LOCATIONDETAILS_TIMEOUT;

            /* Add your Requests to the RequestQueue to execute */
            AppSingleton.getInstance(InvApplication.getAppContext()).addToRequestQueue(jsonInvObjectRequest);

            status = "yes1";
        } else {
            // display error
            status = "no";
        }
    }

    public void getLocCodeList() {
        // check network connection
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // fetch data
            status = "yes";
            // Get the URL from the properties

            URL = AppProperties.getBaseUrl();

            StringInvRequest stringInvRequest = new StringInvRequest(Request.Method.GET,
                    URL + "LocCodeList", null, locListRespListener);

            InvApplication.timeoutType = LOCCODELIST_TIMEOUT;

            /* Add your Requests to the RequestQueue to execute */
            AppSingleton.getInstance(InvApplication.getAppContext()).addToRequestQueue(stringInvRequest);

            status = "yes1";
        } else {
            // display error
            status = "no";
        }
    }

    public void continueButton(View view) {
        String currentLocation = Verification.autoCompleteTextView1
                .getText().toString();
        int duration = Toast.LENGTH_SHORT;
        if (currentLocation == null || currentLocation.trim().length() == 0) {
            new Toasty(this).showMessage("!!ERROR: You must first pick a location.", duration);
            boolean focusRequested = autoCompleteTextView1.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(0, InputMethodManager.SHOW_IMPLICIT);

        } else if (!summaryToLocation.containsKey(currentLocation)) {
            new Toasty(this).showMessage("!!ERROR: Location Code \"" + currentLocation
                    + "\" is invalid.", duration);
            boolean focusRequested = autoCompleteTextView1.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(0, InputMethodManager.SHOW_IMPLICIT);

        } else {
            progBarVerify.setVisibility(View.VISIBLE);
            btnVerify1Cont.getBackground().setAlpha(45);
            Intent intent = new Intent(this, VerScanActivity.class);
            intent.putExtra(loc_code_intent, loc_code_str);
            intent.putExtra(cdloctype_intent, cdloctype);
            startActivity(intent);
            overridePendingTransition(R.anim.in_right, R.anim.out_left);
        }

    }

    public void cancelButton(View view) {
        btnVerify1Cancel.getBackground().setAlpha(45);
        finish();
        overridePendingTransition(R.anim.in_left, R.anim.out_right);
    }

    /*
     * @Override public void onBackPressed() { super.onBackPressed();
     * overridePendingTransition(R.anim.in_left, R.anim.out_right); }
     */

}
