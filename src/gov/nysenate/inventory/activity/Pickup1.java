package gov.nysenate.inventory.activity;

import gov.nysenate.inventory.android.ClearableAutoCompleteTextView;
import gov.nysenate.inventory.android.MsgAlert;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.android.RequestTask;
import gov.nysenate.inventory.android.R.anim;
import gov.nysenate.inventory.android.R.id;
import gov.nysenate.inventory.android.R.layout;
import gov.nysenate.inventory.android.R.menu;
import gov.nysenate.inventory.model.Location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
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

public class Pickup1 extends SenateActivity
{
    public final static String loc_code_intent = "gov.nysenate.inventory.android.loc_code_str";
    private String res = null;
    private String URL = "";
    private String originSummary = null;
    private String destinationSummary = null;
    private Location origin;
    private Location destination;
    private ArrayList<String> allLocations = new ArrayList<String>();
    private ClearableAutoCompleteTextView autoCompleteTextView1;
    private ClearableAutoCompleteTextView autoCompleteTextView2;
    private Button btnPickup1Cont;
    private Button btnPickup1Cancel;
    private TextView tvOffice1;
    private TextView tvDescript1;
    private TextView tvCount1;
    private TextView tvOffice2;
    private TextView tvDescript2;
    private TextView tvCount2;
    private boolean fromLocationBeingTyped = false;
    private boolean toLocationBeingTyped = false;
    public static ProgressBar progBarPickup1;
    String timeoutFrom = "pickup1";
    public final int LOCCODELIST_TIMEOUT = 101,
            FROMLOCATIONDETAILS_TIMEOUT = 102, TOLOCATIONDETAILS_TIMEOUT = 103;
    
    private int lastSize = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pickup1);
        registerBaseActivityReceiver();

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

        autoCompleteTextView1.addTextChangedListener(new TextWatcher(){

            @Override
            public void afterTextChanged(Editable arg0) {
                int currentSize = autoCompleteTextView1.getText().toString().length();
                if (currentSize==0||currentSize<lastSize) {
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
                
            } }
        );
        autoCompleteTextView2.addTextChangedListener(new TextWatcher(){

            @Override
            public void afterTextChanged(Editable arg0) {
                int currentSize = autoCompleteTextView2.getText().toString().length();
                
                if (currentSize==0||currentSize<lastSize) {
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
                
            } }
        );        
        
        try {
            // TODO: RequestDispatcher.getInstance() as parameter for tests DI.
            getAllLocations();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        } catch (ExecutionException e1) {
            e1.printStackTrace();
        } catch (JSONException e1) {
            e1.printStackTrace();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, allLocations);

        setupautoCompleteTextView1(adapter);
        setupautoCompleteTextView2(adapter);
    }

    public void noServerResponse() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(Html
                .fromHtml("<font color='#000055'>NO SERVER RESPONSE</font>"));
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

    private TextWatcher originTextWatcher = new TextWatcher()
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
            fromLocationBeingTyped = true;
            /*if (autoCompleteTextView1.getText().toString().length() >= 3) {
                getOriginLocationDetails();
            }*/
        }
    };

    private TextWatcher destinationTextWatcher = new TextWatcher()
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
            toLocationBeingTyped = true;
            /*if (autoCompleteTextView2.getText().toString().length() >= 3) {
                getDestinationLocationDetails();
            }*/
        }
    };

    public void continueButton(View view) {
        // For testing...
        // SessionManager.getSessionManager().checkServerResponse(true) == OK
        if (checkServerResponse(true) == OK) {
            int duration = Toast.LENGTH_SHORT;
            String currentFromLocation = this.autoCompleteTextView1.getText()
                    .toString();
            String currentToLocation = this.autoCompleteTextView2.getText()
                    .toString();

            if (currentFromLocation.trim().length() == 0) {
                Toast toast = Toast.makeText(this.getApplicationContext(),
                        "!!ERROR: You must first pick a from location.",
                        duration);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                boolean focusRequested = autoCompleteTextView1.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.SHOW_IMPLICIT);

            } else if (allLocations.indexOf(currentFromLocation) == -1) {
                Toast toast = Toast.makeText(this.getApplicationContext(),
                        "!!ERROR: From Location Code \"" + currentFromLocation
                                + "\" is invalid.", duration);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                boolean focusRequested = autoCompleteTextView1.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.SHOW_IMPLICIT);

            } else if (currentToLocation.trim().length() == 0) {
                Toast toast = Toast
                        .makeText(this.getApplicationContext(),
                                "!!ERROR: You must first pick a to location.",
                                duration);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                boolean focusRequested = autoCompleteTextView2.requestFocus();

            } else if (allLocations.indexOf(currentFromLocation) == -1) {
                Toast toast = Toast.makeText(this.getApplicationContext(),
                        "!!ERROR: To Location Code \"" + currentToLocation
                                + "\" is invalid.", duration);
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

            } else if (Integer.valueOf(tvCount1.getText().toString()) < 1) {
                Toast toast = Toast.makeText(this, "!!ERROR: Origin Location must have at least one item", duration);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();

            } else {
                btnPickup1Cont.getBackground().setAlpha(70);
                Intent intent = new Intent(this, Pickup2Activity.class);
                origin = new Location(originSummary);
                destination = new Location(destinationSummary);
                intent.putExtra("origin", origin);
                intent.putExtra("destination", destination);
                startActivity(intent);
                overridePendingTransition(R.anim.in_right, R.anim.out_left);
            }
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
                try {
                    // TODO: RequestDispatcher.getInstance() as parameter for
                    // testing.
                    getAllLocations();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
                    new Timer().schedule(new TimerTask()
                    {
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

    public ArrayList<String> getAllLocations() throws InterruptedException,
            ExecutionException, JSONException {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            if (LoginActivity.properties != null) {
                URL = LoginActivity.properties.get("WEBAPP_BASE_URL")
                        .toString();
            } else {
                MsgAlert msgAlert = new MsgAlert(
                        this,
                        "Properties cannot be loaded.",
                        "!!ERROR: Cannot load properties information. The app is no longer reliable. Please close the app and start again.");
            }
            AsyncTask<String, String, String> resr1 = new RequestTask()
                    .execute(URL + "/LocCodeList");
            res = resr1.get();
            if (res == null) {
                noServerResponse();
            } else if (res.indexOf("Session timed out") > -1) {
                startTimeout(LOCCODELIST_TIMEOUT);
            }

            JSONArray jsonArray = new JSONArray(res); // TODO: ?? catch
                                                      // exception/handle the
                                                      // case where res is
                                                      // invalid JSON ??
            for (int i = 0; i < jsonArray.length(); i++) {
                allLocations.add(jsonArray.getString(i));
            }

            Collections.sort(allLocations);
        }
        return allLocations;
    }

    public void getOriginLocationDetails() {
        originSummary = autoCompleteTextView1.getText().toString().trim();
        String locCode = originSummary.split("-")[0];

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            URL = LoginActivity.properties.get("WEBAPP_BASE_URL").toString();

            AsyncTask<String, String, String> resr1 = new RequestTask()
                    .execute(URL + "/LocationDetails?barcode_num=" + locCode);
            try {
                try {
                    res = null;
                    res = resr1.get().trim().toString();
                    if (res == null) {
                        noServerResponse();
                        return;
                    } else if (res.indexOf("Session timed out") > -1) {
                        startTimeout(FROMLOCATIONDETAILS_TIMEOUT);
                        return;
                    }
                } catch (NullPointerException e) {
                    noServerResponse();
                    return;
                }
                try {
                    JSONObject object = (JSONObject) new JSONTokener(res)
                            .nextValue();
                    tvOffice1.setText(object.getString("cdrespctrhd"));
                    // tvLocCd1.setText( object.getString("cdlocat"));
                    tvDescript1.setText(object.getString("adstreet1")
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
                    tvCount1.setText(object.getString("nucount"));

                } catch (JSONException e) {
                    tvOffice1.setText("!!ERROR: " + e.getMessage());
                    tvDescript1.setText("Please contact STS/BAC.");
                    tvCount1.setText("N/A");
                    e.printStackTrace();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public void getDestinationLocationDetails() {
        destinationSummary = autoCompleteTextView2.getText().toString().trim();
        String locCode = destinationSummary.split("-")[0];

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            AsyncTask<String, String, String> resr1 = new RequestTask()
                    .execute(URL + "/LocationDetails?barcode_num=" + locCode);
            try {
                try {
                    res = null;
                    res = resr1.get().trim().toString();
                    if (res == null) {
                        noServerResponse();
                        return;
                    } else if (res.indexOf("Session timed out") > -1) {
                        startTimeout(TOLOCATIONDETAILS_TIMEOUT);
                        return;
                    }
                } catch (NullPointerException e) {
                    noServerResponse();
                    return;
                }
                try {
                    JSONObject object = (JSONObject) new JSONTokener(res)
                            .nextValue();
                    tvOffice2.setText(object
                            .getString("cdrespctrhd"));
                    // tvLocCd2.setText( object.getString("cdlocat"));
                    tvDescript2.setText(object.getString("adstreet1")
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
                    tvCount2.setText(object.getString("nucount"));

                } catch (JSONException e) {
                    tvDescript2.setText("!!ERROR: " + e.getMessage());
                    tvOffice2.setText("Please contact STS/BAC.");
                    tvCount2.setText("N/A");
                    e.printStackTrace();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    private void setupautoCompleteTextView1(ArrayAdapter<String> adapter) {
        autoCompleteTextView1.setThreshold(1);
        autoCompleteTextView1.setAdapter(adapter);
        autoCompleteTextView1
                .setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {
                        Log.i("ItemClicked", "ITEM CLICKED");
                        if (autoCompleteTextView1.getText().toString().trim()
                                .length()>0) {
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
                .setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {
                        if (autoCompleteTextView2.getText().toString().trim()
                                .length()>0) {
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
