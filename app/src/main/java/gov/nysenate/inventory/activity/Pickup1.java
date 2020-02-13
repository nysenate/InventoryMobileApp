package gov.nysenate.inventory.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import gov.nysenate.inventory.android.AppSingleton;
import gov.nysenate.inventory.android.ClearableAutoCompleteTextView;
import gov.nysenate.inventory.android.InvApplication;
import gov.nysenate.inventory.android.MsgAlert;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.android.StringInvRequest;
import gov.nysenate.inventory.model.Location;
import gov.nysenate.inventory.util.AppProperties;
import gov.nysenate.inventory.util.HttpUtils;
import gov.nysenate.inventory.util.Serializer;
import gov.nysenate.inventory.util.Toasty;

public class Pickup1 extends SenateActivity {
    public final static String loc_code_intent = "gov.nysenate.inventory.android.loc_code_str";
    private String res = null;
    private String URL = "";
    private String originSummary = null;
    private String destinationSummary = null;
    private Location origin;
    private Location destination;
    private ClearableAutoCompleteTextView autoCompleteTextView1;
    private ClearableAutoCompleteTextView autoCompleteTextView2;
    private Button btnPickup1Cont;
    private Button btnPickup1Cancel;
    protected TextView tvOffice1;
    protected TextView tvDescript1;
    protected TextView tvCount1;
    protected TextView tvOffice2;
    protected TextView tvDescript2;
    protected TextView tvCount2;
    private boolean fromLocationBeingTyped = false;
    private boolean toLocationBeingTyped = false;
    public static ProgressBar progBarPickup1;
    String timeoutFrom = "pickup1";
    public final int LOCCODELIST_TIMEOUT = 101,
            FROMLOCATIONDETAILS_TIMEOUT = 102, TOLOCATIONDETAILS_TIMEOUT = 103;

    private int lastSize = 0;
    private Map<String, Location> summaryToLocation;

    Response.Listener originLocresponseListener = new Response.Listener<String>() {

        @Override
        public void onResponse(String response) {
            try {
                try {
                    if (response == null) {
                        return;
                    } else if (response.indexOf("Session timed out") > -1) {
                        startTimeout(FROMLOCATIONDETAILS_TIMEOUT);
                        return;
                    }
                } catch (NullPointerException e) {
                    return;
                }
                try {
                    JSONObject object = (JSONObject) new JSONTokener(response)
                            .nextValue();
                    Pickup1.this.tvOffice1.setText(object.getString("cdrespctrhd"));
                    Pickup1.this.tvDescript1.setText(object.getString("adstreet1")
                            .replaceAll("&#34;", "\"")
                            + " ,"
                            + object.getString("adcity").replaceAll("&#34;",
                            "\"")
                            + ", "
                            + object.getString("adstate").replaceAll("&#34;",
                            "\"")
                            + " "
                            + object.getString("adzipcode").replaceAll("&#34;",
                            "\""));
                    Pickup1.this.tvCount1.setText(object.getString("nucount"));

                } catch (JSONException e) {
                    Pickup1.this.tvOffice1.setText("!!ERROR: " + e.getMessage());
                    Pickup1.this.tvDescript1.setText("Please contact STS/BAC.");
                    Pickup1.this.tvCount1.setText("N/A");
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    Response.Listener destLocresponseListener = new Response.Listener<String>() {

        @Override
        public void onResponse(String response) {
            try {
                try {
                    if (response == null) {
                        return;
                    } else if (response.indexOf("Session timed out") > -1) {
                        startTimeout(FROMLOCATIONDETAILS_TIMEOUT);
                        return;
                    }
                } catch (NullPointerException e) {
//                    noServerResponse();
                    return;
                }
                try {
                    JSONObject object = (JSONObject) new JSONTokener(response)
                            .nextValue();
                    Pickup1.this.tvOffice2.setText(object.getString("cdrespctrhd"));
                    Pickup1.this.tvDescript2.setText(object.getString("adstreet1")
                            .replaceAll("&#34;", "\"")
                            + " ,"
                            + object.getString("adcity").replaceAll("&#34;",
                            "\"")
                            + ", "
                            + object.getString("adstate").replaceAll("&#34;",
                            "\"")
                            + " "
                            + object.getString("adzipcode").replaceAll("&#34;",
                            "\""));
                    Pickup1.this.tvCount2.setText(object.getString("nucount"));

                } catch (JSONException e) {
                    Pickup1.this.tvOffice2.setText("!!ERROR: " + e.getMessage());
                    Pickup1.this.tvDescript2.setText("Please contact STS/BAC.");
                    Pickup1.this.tvCount2.setText("N/A");
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    Response.Listener locCodeListResponseListener = new Response.Listener<String>() {

        @Override
        public void onResponse(String response) {
            if (response.indexOf("Session timed out") > -1) {
                startTimeout(LOCCODELIST_TIMEOUT);
                return;
            }

            summaryToLocation = new HashMap<>();
            for (Location location : Serializer.deserialize(response, Location.class)) {
                summaryToLocation.put(location.getLocationSummaryString(), location);
            }
            List<String> locationSummaries = new ArrayList<>(summaryToLocation.keySet());
            Collections.sort(locationSummaries);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(Pickup1.this,
                    android.R.layout.simple_dropdown_item_1line, locationSummaries);

            setupautoCompleteTextView1(adapter);
            setupautoCompleteTextView2(adapter);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pickup1);
        registerBaseActivityReceiver();
        AppSingleton.getInstance(this).timeoutFrom = "pickup1";

        tvOffice1 = (TextView) this.findViewById(R.id.tvOffice1);
        tvDescript1 = (TextView) this.findViewById(R.id.tvDescript1);
        tvCount1 = (TextView) this.findViewById(R.id.tvCount1);
        tvOffice2 = (TextView) this.findViewById(R.id.tvOffice2);
        tvDescript2 = (TextView) this.findViewById(R.id.tvDescript2);
        tvCount2 = (TextView) this.findViewById(R.id.tvCount2);
        progBarPickup1 = (ProgressBar) this.findViewById(R.id.progBarPickup1);
        autoCompleteTextView1 = (ClearableAutoCompleteTextView) findViewById(R.id.autoCompleteTextView1);
        autoCompleteTextView2 = (ClearableAutoCompleteTextView) findViewById(R.id.autoCompleteTextView2);
        btnPickup1Cont = (Button) findViewById(R.id.btnPickup1Cont);
        btnPickup1Cancel = (Button) findViewById(R.id.btnPickup1Cancel);

        autoCompleteTextView1.addTextChangedListener(new TextWatcher() {

                                                         @Override
                                                         public void afterTextChanged(Editable arg0) {
                                                             int currentSize = autoCompleteTextView1.getText().toString().length();
                                                             if (currentSize == 0 || currentSize < lastSize) {
                                                                 tvOffice1.setText("N/A");
                                                                 tvDescript1.setText("N/A");
                                                                 tvCount1.setText("N/A");
                                                             }
                                                         }

                                                         @Override
                                                         public void beforeTextChanged(CharSequence arg0, int arg1,
                                                                                       int arg2, int arg3) {
                                                             lastSize = autoCompleteTextView1.getText().toString().length();
                                                         }

                                                         @Override
                                                         public void onTextChanged(CharSequence s, int start, int before,
                                                                                   int count) {
                                                             // TODO Auto-generated method stub

                                                         }
                                                     }
        );
        autoCompleteTextView2.addTextChangedListener(new TextWatcher() {

                                                         @Override
                                                         public void afterTextChanged(Editable arg0) {
                                                             int currentSize = autoCompleteTextView2.getText().toString().length();

                                                             if (currentSize == 0 || currentSize < lastSize) {
                                                                 tvOffice2.setText("N/A");
                                                                 tvDescript2.setText("N/A");
                                                                 tvCount2.setText("N/A");
                                                             }
                                                         }

                                                         @Override
                                                         public void beforeTextChanged(CharSequence arg0, int arg1,
                                                                                       int arg2, int arg3) {
                                                             lastSize = autoCompleteTextView2.getText().toString().length();
                                                         }

                                                         @Override
                                                         public void onTextChanged(CharSequence s, int start, int before,
                                                                                   int count) {
                                                             // TODO Auto-generated method stub

                                                         }
                                                     }
        );
        getAllLocations();

    }

    public void noServerResponse() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(Html
                .fromHtml("<font color='#000055'>NO SERVER RESPONSE</font>"));
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

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        btnPickup1Cont = (Button) findViewById(R.id.btnPickup1Cont);
        btnPickup1Cont.getBackground().setAlpha(255);
        btnPickup1Cancel = (Button) findViewById(R.id.btnPickup1Cancel);
        btnPickup1Cancel.getBackground().setAlpha(255);
        if (progBarPickup1 == null) {
            progBarPickup1 = (ProgressBar) this
                    .findViewById(R.id.progBarPickup1);
        }
        progBarPickup1.setVisibility(View.INVISIBLE);
    }

    private TextWatcher originTextWatcher = new TextWatcher() {
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
            fromLocationBeingTyped = true;
        }
    };

    private TextWatcher destinationTextWatcher = new TextWatcher() {
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
            toLocationBeingTyped = true;
        }
    };

    public void continueButton(View view) {
        int duration = Toast.LENGTH_SHORT;
        String currentFromLocation = this.autoCompleteTextView1.getText()
                .toString();
        String currentToLocation = this.autoCompleteTextView2.getText()
                .toString();

        if (currentFromLocation == null || currentFromLocation.trim().length() == 0) {
            Toast toast = Toast.makeText(this.getApplicationContext(),
                    "!!ERROR: You must first pick a from location.",
                    duration);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            boolean focusRequested = autoCompleteTextView1.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(0, InputMethodManager.SHOW_IMPLICIT);

        } else if (summaryToLocation == null || !summaryToLocation.containsKey(currentFromLocation)) {
            Toast toast = Toast.makeText(this.getApplicationContext(),
                    "!!ERROR: From Location Code \"" + currentFromLocation
                            + "\" is invalid.", duration);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            boolean focusRequested = autoCompleteTextView1.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(0, InputMethodManager.SHOW_IMPLICIT);

        } else if (currentToLocation == null || currentToLocation.trim().length() == 0) {
            Toast toast = Toast
                    .makeText(this.getApplicationContext(),
                            "!!ERROR: You must first pick a to location.",
                            duration);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            boolean focusRequested = autoCompleteTextView2.requestFocus();

        } else if (currentToLocation.equalsIgnoreCase(currentFromLocation)) {
            Toast toast = Toast
                    .makeText(
                            this.getApplicationContext(),
                            "!!ERROR: The Pickup Location \""
                                    + currentToLocation
                                    + "\" cannot be the same as the Delivery Location.",
                            duration);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            boolean focusRequested = autoCompleteTextView2.requestFocus();

        } else if (tvCount1.getText().equals("N/A") || Integer.valueOf(tvCount1.getText().toString()) < 1) {
            Toast toast = Toast.makeText(this, "!!ERROR: Origin Location must have at least one item", duration);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();

        } else {
            btnPickup1Cont.getBackground().setAlpha(70);
            Intent intent = new Intent(this, Pickup2.class);
            origin = summaryToLocation.get(currentFromLocation);
            destination = summaryToLocation.get(currentToLocation);
            intent.putExtra("origin", Serializer.serialize(origin));
            intent.putExtra("destination", Serializer.serialize(destination));
            startActivity(intent);
            overridePendingTransition(R.anim.in_right, R.anim.out_left);
        }
    }

    public void cancelButton(View view) {
        btnPickup1Cancel.getBackground().setAlpha(70);
        finish();
        overridePendingTransition(R.anim.in_left, R.anim.out_right);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_pickup1, menu);
        return true;
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
                    getAllLocations();
                    break;
                }
            case FROMLOCATIONDETAILS_TIMEOUT:
                if (resultCode == RESULT_OK) {
                    if (fromLocationBeingTyped) {
                        autoCompleteTextView1.setText(autoCompleteTextView1.getText());
                        autoCompleteTextView1.setSelection(autoCompleteTextView1.getText()
                                .length());
                    } else {
                        getOriginLocationDetails();
                        autoCompleteTextView2.requestFocus();
                    }
                    break;
                }
            case TOLOCATIONDETAILS_TIMEOUT:
                if (resultCode == RESULT_OK) {
                    if (toLocationBeingTyped) {
                        autoCompleteTextView2.setText(autoCompleteTextView2
                                .getText());
                        autoCompleteTextView2.setSelection(autoCompleteTextView2
                                .getText().length());
                    } else {
                        getDestinationLocationDetails();
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(
                                        autoCompleteTextView2.getWindowToken(), 0);
                            }
                        }, 50);
                    }
                    break;
                }

        }
    }

    public void getAllLocations() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            if (LoginActivity.properties != null) {
                URL = AppProperties.getBaseUrl();
            } else {
                MsgAlert msgAlert = new MsgAlert(
                        this,
                        "Properties cannot be loaded.",
                        "!!ERROR: Cannot load properties information. The app is no longer reliable. Please close the app and start again.");
            }

            InvApplication.timeoutType = LOCCODELIST_TIMEOUT;

            StringInvRequest stringInvRequest = new StringInvRequest(Request.Method.GET,
                    URL + "LocCodeList", null, locCodeListResponseListener);

            /* Add your Requests to the RequestQueue to execute */
            AppSingleton.getInstance(InvApplication.getAppContext()).addToRequestQueue(stringInvRequest);

        }
    }

    public void getOriginLocationDetails() {
        originSummary = autoCompleteTextView1.getText().toString().trim();
        Location selectedLocation = summaryToLocation.get(originSummary);

        if (selectedLocation == null) {
            Toasty.displayCenteredMessage(this, "Entered text is invalid.", Toast.LENGTH_SHORT);
            return;
        }
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {

            URL = LoginActivity.properties.get("WEBAPP_BASE_URL").toString();
            if (!URL.endsWith("/")) {
                URL += "/";
            }

            InvApplication.timeoutType = FROMLOCATIONDETAILS_TIMEOUT;

            StringInvRequest stringInvRequest = new StringInvRequest(Request.Method.GET,
                    URL + "LocationDetails?location_code=" + selectedLocation.getCdlocat()
                            + "&location_type=" + selectedLocation.getCdloctype(), null, originLocresponseListener);

            /* Add your Requests to the RequestQueue to execute */
            AppSingleton.getInstance(InvApplication.getAppContext()).addToRequestQueue(stringInvRequest);
        }
    }

    public void getDestinationLocationDetails() {
        destinationSummary = autoCompleteTextView2.getText().toString().trim();
        Location selectedLocation = summaryToLocation.get(destinationSummary);

        if (selectedLocation == null) {
            Toasty.displayCenteredMessage(this, "Entered text is invalid.", Toast.LENGTH_SHORT);
            return;
        }
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            URL = AppProperties.getBaseUrl();

            InvApplication.timeoutType = FROMLOCATIONDETAILS_TIMEOUT;

            StringInvRequest stringInvRequest = new StringInvRequest(Request.Method.GET,
                    URL + "LocationDetails?location_code=" + selectedLocation.getCdlocat()
                            + "&location_type=" + selectedLocation.getCdloctype(), null, destLocresponseListener);

            /* Add your Requests to the RequestQueue to execute */
            AppSingleton.getInstance(InvApplication.getAppContext()).addToRequestQueue(stringInvRequest);
        }
    }

    private void setupautoCompleteTextView1(ArrayAdapter<String> adapter) {
        autoCompleteTextView1.setThreshold(1);
        autoCompleteTextView1.setAdapter(adapter);
        autoCompleteTextView1
                .setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        if (autoCompleteTextView1.getText().toString().trim()
                                .length() > 0) {
                            getOriginLocationDetails();
                        }
                        if (autoCompleteTextView2.getText().toString().trim()
                                .length() > 0) {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(
                                    autoCompleteTextView1.getWindowToken(), 0);
                        } else {
                            boolean focusRequested = autoCompleteTextView2
                                    .requestFocus();
                        }
                        fromLocationBeingTyped = false;
                    }
                });

        autoCompleteTextView1.addTextChangedListener(originTextWatcher);
    }

    private void setupautoCompleteTextView2(ArrayAdapter<String> adapter) {
        autoCompleteTextView2.setThreshold(1);
        autoCompleteTextView2.setAdapter(adapter);
        autoCompleteTextView2
                .setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        if (autoCompleteTextView2.getText().toString().trim()
                                .length() > 0) {
                            getDestinationLocationDetails();
                        }

                        int duration = Toast.LENGTH_SHORT;
                        toLocationBeingTyped = false;
                        if (autoCompleteTextView1.getText().toString().trim()
                                .length() == 0) {
                            boolean focusRequested = autoCompleteTextView1
                                    .requestFocus();
                            Toast toast = Toast.makeText(
                                    getApplicationContext(),
                                    "Please pick a from location.", duration);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        } else {
                            if (autoCompleteTextView1.getText().toString().trim()
                                    .length() > 0) {
                                if (autoCompleteTextView1.getText().toString()
                                        .trim().length() > 0) {
                                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(
                                            autoCompleteTextView1.getWindowToken(),
                                            0);
                                } else {

                                }
                            } else {
                                boolean focusRequested = autoCompleteTextView1
                                        .requestFocus();
                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.toggleSoftInput(0,
                                        InputMethodManager.SHOW_IMPLICIT);
                            }

                        }
                    }
                });

        autoCompleteTextView2.addTextChangedListener(destinationTextWatcher);
    }

}
