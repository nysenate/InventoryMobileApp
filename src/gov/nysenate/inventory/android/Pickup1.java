package gov.nysenate.inventory.android;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
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
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class Pickup1 extends SenateActivity
{
    public final static String loc_code_intent = "gov.nysenate.inventory.android.loc_code_str";
    public EditText loc_code;
    public EditText locCodeDest;
    public TextView loc_details;
    public TextView locDetailsDest;
    public String res = null;
    public String status = null;
    public String loc_code_str = null;
    ClearableAutoCompleteTextView autoCompleteTextView1;// for origin location
                                                        // code
    ClearableAutoCompleteTextView autoCompleteTextView2;// for destination
                                                        // location code
    public String originLocation = null;
    public String destinationLocation = null;
    public ArrayList<String> locCodeList = new ArrayList<String>();
    String URL = "";
    Button btnPickup1Cont;
    Button btnPickup1Cancel;
    TextView tvOffice1;
    TextView tvDescript1;
    TextView tvCount1;
    TextView tvOffice2;
    TextView tvDescript2;
    TextView tvCount2;
    Activity currentActivity;
    boolean fromLocationBeingTyped = false;
    boolean toLocationBeingTyped = false;
    public static ProgressBar progBarPickup1;
    String timeoutFrom = "pickup1";    
    public final int LOCCODELIST_TIMEOUT = 101, FROMLOCATIONDETAILS_TIMEOUT = 102, TOLOCATIONDETAILS_TIMEOUT = 103;        
    ArrayAdapter<String> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pickup1);
        registerBaseActivityReceiver();
        currentActivity = this;

        // loc_code = (EditText) findViewById(R.id.editText1);

        // Setup Data Textviews
        tvOffice1 = (TextView) this.findViewById(R.id.tvOffice1);
        // tvLocCd1 = (TextView)this.findViewById(R.id.tvLocCd1);
        tvDescript1 = (TextView) this.findViewById(R.id.tvDescript1);
        tvCount1 = (TextView) this.findViewById(R.id.tvCount1);

        tvOffice2 = (TextView) this.findViewById(R.id.tvOffice2);
        // tvLocCd2 = (TextView)this.findViewById(R.id.tvLocCd2);
        tvDescript2 = (TextView) this.findViewById(R.id.tvDescript2);
        tvCount2 = (TextView) this.findViewById(R.id.tvCount2);

        progBarPickup1 = (ProgressBar) this.findViewById(R.id.progBarPickup1);

        getLocCodeList();
        autoCompleteTextView1.setThreshold(1);
        
        btnPickup1Cont = (Button) findViewById(R.id.btnPickup1Cont);
        btnPickup1Cancel = (Button) findViewById(R.id.btnPickup1Cancel);

        autoCompleteTextView1
                .setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> parent,
                            View view, int position, long id) {
                        Log.i("ItemClicked", "ITEM CLICKED");
                        if (autoCompleteTextView2.getText().toString()
                                .trim().length() > 0) {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(
                                    autoCompleteTextView1
                                            .getWindowToken(), 0);
                        } else {
                            boolean focusRequested = autoCompleteTextView2
                                    .requestFocus();
                        }
                        fromLocationBeingTyped = false;                        

                    }
                });


        // code for textwatcher
        // for origin location code
        // loc_code = (EditText) findViewById(R.id.editText1);
        // loc_code.addTextChangedListener(filterTextWatcher);
        autoCompleteTextView1.addTextChangedListener(filterTextWatcher);
        // autoCompleteTextView2.addTextChangedListener(filterTextWatcher2);
        loc_details = (TextView) findViewById(R.id.textView2);
        // loc_details.findFocus(); we can use this to find focus

        // for destination code
        autoCompleteTextView2.addTextChangedListener(filterTextWatcher2);
        locDetailsDest = (TextView) findViewById(R.id.textView3);

        Move.progBarMove.setVisibility(View.INVISIBLE);

    }

    public void noServerResponse() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle("NO SERVER RESPONSE");

        // set dialog message
        alertDialogBuilder
                .setMessage(
                        Html.fromHtml("!!ERROR: There was <font color='RED'><b>NO SERVER RESPONSE</b></font>. <br/> Please contact STS/BAC."))
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener()
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

    @Override
    protected void onResume() {
        super.onResume();
        btnPickup1Cont = (Button) findViewById(R.id.btnPickup1Cont);
        btnPickup1Cont.getBackground().setAlpha(255);
        btnPickup1Cancel = (Button) findViewById(R.id.btnPickup1Cancel);
        btnPickup1Cancel.getBackground().setAlpha(255);
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
            fromLocationBeingTyped = true;
            if (autoCompleteTextView1.getText().toString().length() >= 3) {
                  getFromLocationDetails();
            }
        }
    };

    // TextWatcher for destination
    private TextWatcher filterTextWatcher2 = new TextWatcher()
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
            if (autoCompleteTextView2.getText().toString().length() >= 3) {
                   getToLocationDetails();
            }
        }
    };

    public void continueButton(View view) {
        float alpha = 0.45f;
        AlphaAnimation alphaUp = new AlphaAnimation(alpha, alpha);
        alphaUp.setFillAfter(true);
        btnPickup1Cont.startAnimation(alphaUp);
        int duration = Toast.LENGTH_SHORT;

        String currentFromLocation = this.autoCompleteTextView1.getText()
                .toString();
        String currentToLocation = this.autoCompleteTextView2.getText()
                .toString();

        if (currentFromLocation.trim().length() == 0) {
            Toast toast = Toast.makeText(this.getApplicationContext(),
                    "!!ERROR: You must first pick a from location.", duration);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            boolean focusRequested = autoCompleteTextView1.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(0, InputMethodManager.SHOW_IMPLICIT);

        } else if (locCodeList.indexOf(currentFromLocation) == -1) {
            Toast toast = Toast.makeText(this.getApplicationContext(),
                    "!!ERROR: From Location Code \"" + currentFromLocation
                            + "\" is invalid.", duration);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            boolean focusRequested = autoCompleteTextView1.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(0, InputMethodManager.SHOW_IMPLICIT);

        } else if (currentToLocation.trim().length() == 0) {
            Toast toast = Toast.makeText(this.getApplicationContext(),
                    "!!ERROR: You must first pick a to location.", duration);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            boolean focusRequested = autoCompleteTextView2.requestFocus();

        } else if (locCodeList.indexOf(currentFromLocation) == -1) {
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
                            "!!ERROR: To Location Code \""
                                    + currentToLocation
                                    + "\" cannot be the same as the From Location Code.",
                            duration);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            boolean focusRequested = autoCompleteTextView2.requestFocus();

        } else {
            String cdlocatto = "";
            String cdloctypeto = "";
            String cdlocatfrm = "";
            String cdloctypefrm = "";

            int nuStop = 0;
            int nuStop2 = 0;
            if (originLocation != null) {
                nuStop = originLocation.indexOf("-");

                if (nuStop > -1) {
                    cdlocatfrm = originLocation.substring(0, nuStop);
                    nuStop2 = originLocation.indexOf(":", nuStop);
                    cdloctypefrm = originLocation
                            .substring(nuStop + 1, nuStop2);
                } else {
                    cdlocatfrm = originLocation;
                }
                nuStop = destinationLocation.indexOf("-");
                if (nuStop > -1) {
                    cdlocatto = destinationLocation.substring(0, nuStop);
                    nuStop2 = destinationLocation.indexOf(":", nuStop);
                    cdloctypeto = destinationLocation.substring(nuStop + 1,
                            nuStop2);
                } else {
                    cdlocatfrm = destinationLocation;
                }
            }

            progBarPickup1.setVisibility(View.VISIBLE);

            Intent intent = new Intent(this, Pickup2Activity.class);
            intent.putExtra("cdlocatfrm", cdlocatfrm);
            intent.putExtra("cdlocatto", cdlocatto);
            intent.putExtra("cdloctypefrm", cdloctypefrm);
            intent.putExtra("cdloctypeto", cdloctypeto);
            intent.putExtra("originLocation", originLocation); // for origin
                                                               // code
            intent.putExtra("destinationLocation", destinationLocation); // for
            // destination
            // code
            startActivity(intent);
            overridePendingTransition(R.anim.in_right, R.anim.out_left);
        }

        alphaUp = new AlphaAnimation(1f, 1f);
        alphaUp.setFillAfter(true);
        btnPickup1Cont.startAnimation(alphaUp);
    }

    public void cancelButton(View view) {

        float alpha = 0.45f;
        AlphaAnimation alphaUp = new AlphaAnimation(alpha, alpha);
        alphaUp.setFillAfter(true);
        btnPickup1Cancel.startAnimation(alphaUp);
        Intent intent = new Intent(this, Move.class);
        startActivity(intent);
        overridePendingTransition(R.anim.in_left, R.anim.out_right);
        alphaUp = new AlphaAnimation(1f, 1f);
        alphaUp.setFillAfter(true);
        btnPickup1Cancel.startAnimation(alphaUp);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_pickup1, menu);
        return true;
    }
    
    public void startTimeout(int timeoutType) {
        Intent intentTimeout = new Intent(this, LoginActivity.class);
        intentTimeout.putExtra("TIMEOUTFROM", timeoutFrom);
        startActivityForResult(intentTimeout, timeoutType);
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case LOCCODELIST_TIMEOUT:
            if (resultCode == RESULT_OK) {
                getLocCodeList();
                break;
            }
        case FROMLOCATIONDETAILS_TIMEOUT:
            if (resultCode == RESULT_OK) {
                if (fromLocationBeingTyped) {
                    autoCompleteTextView1.setText(autoCompleteTextView1.getText());
                    autoCompleteTextView1.setSelection(autoCompleteTextView1.getText().length());
                }
                else {
                    getFromLocationDetails();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(
                            autoCompleteTextView1.getWindowToken(), 0);                    
                }
                break;
            }
        case TOLOCATIONDETAILS_TIMEOUT:
            if (resultCode == RESULT_OK) {
                if (toLocationBeingTyped) {
                    autoCompleteTextView2.setText(autoCompleteTextView2.getText());
                    autoCompleteTextView2.setSelection(autoCompleteTextView2.getText().length());
                }
                else {
                    getToLocationDetails();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(
                            autoCompleteTextView2.getWindowToken(), 0);                    
                }
                break;
            }
            
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
            try {
                URL = LoginActivity.properties.get("WEBAPP_BASE_URL")
                        .toString();
            } catch (NullPointerException e) {
                MsgAlert msgAlert = new MsgAlert(
                        this,
                        "Properties cannot be loaded.",
                        "!!ERROR: Cannot load properties information. The app is no longer reliable. Please close the app and start again.");
                if (LoginActivity.properties == null) {
                    try {

                    } catch (Exception e2) {
                        e.printStackTrace();
                    }

                } else {

                }
            }

            AsyncTask<String, String, String> resr1 = new RequestTask()
                    .execute(URL + "/LocCodeList");
            try {
                try {
                    res = null;
                    res = resr1.get().trim().toString();
                    if (res == null) {
                        noServerResponse();
                        return;
                    } else if (res.indexOf("Session timed out") > -1) {
                        startTimeout(LOCCODELIST_TIMEOUT);
                        return;
                    }
                } catch (NullPointerException e) {
                    noServerResponse();
                    return;
                }
                // code for JSON

                String jsonString = resr1.get().trim().toString();
                JSONArray jsonArray = new JSONArray(jsonString);

                for (int i = 0; i < jsonArray.length(); i++) {
                    locCodeList.add(jsonArray.getString(i).toString());
                }
                Collections.sort(locCodeList);

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_dropdown_item_1line,
                        locCodeList);
                // for origin dest code
                autoCompleteTextView1 = (ClearableAutoCompleteTextView) findViewById(R.id.autoCompleteTextView1);
                autoCompleteTextView1.setThreshold(1);
                autoCompleteTextView1.setAdapter(adapter);       
                // for destination code

                autoCompleteTextView2 = (ClearableAutoCompleteTextView) findViewById(R.id.autoCompleteTextView2);
                autoCompleteTextView2.setThreshold(1);
                autoCompleteTextView2.setAdapter(adapter);
                autoCompleteTextView2
                        .setOnItemClickListener(new AdapterView.OnItemClickListener()
                        {
                            @Override
                            public void onItemClick(AdapterView<?> parent,
                                    View view, int position, long id) {
                                int duration = Toast.LENGTH_SHORT;
                                toLocationBeingTyped = false;
                                if (autoCompleteTextView1.getText().toString()
                                        .trim().length() == 0) {
                                    boolean focusRequested = autoCompleteTextView1
                                            .requestFocus();
                                    Toast toast = Toast.makeText(
                                            getApplicationContext(),
                                            "Please pick a from location.",
                                            duration);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.show();
                                } else {
                                    if (autoCompleteTextView1.getText()
                                            .toString().trim().length() > 0) {
                                        if (autoCompleteTextView1.getText()
                                                .toString().trim().length() > 0) {
                                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                            imm.hideSoftInputFromWindow(
                                                    autoCompleteTextView1
                                                            .getWindowToken(),
                                                    0);
                                        } else {

                                        }
                                    } else {
                                        boolean focusRequested = autoCompleteTextView1
                                                .requestFocus();
                                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.toggleSoftInput(
                                                0,
                                                InputMethodManager.SHOW_IMPLICIT);
                                    }

                                }
                            }
                        });
                MenuActivity.progBarMenu.setVisibility(View.INVISIBLE);

            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (ExecutionException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (JSONException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            status = "yes1";
        } else {
            // display error
            status = "no";
        }
                
    }
    
    public void getFromLocationDetails() {
        // loc_details.setText(loc_code.getText().toString());
        originLocation = autoCompleteTextView1.getText().toString()
                .trim();

        String barcodeNumberDetails[] = autoCompleteTextView1.getText()
                .toString().trim().split("-");
        String barcode_num = barcodeNumberDetails[0];// this will be
                                                     // passed to the
                                                     // server
        loc_code_str = barcodeNumberDetails[0];// this will be passed to
                                               // next activity with
                                               // intent

        // check network connection
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // fetch data
            status = "yes";

            // Get the URL from the properties
            URL = LoginActivity.properties.get("WEBAPP_BASE_URL")
                    .toString();

            AsyncTask<String, String, String> resr1 = new RequestTask()
                    .execute(URL + "/LocationDetails?barcode_num="
                            + barcode_num);
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
                    JSONObject object = (JSONObject) new JSONTokener(
                            res).nextValue();
                    tvOffice1.setText(object.getString("cdrespctrhd"));
                    // tvLocCd1.setText( object.getString("cdlocat"));
                    tvDescript1.setText(object.getString("adstreet1")
                            .replaceAll("&#34;", "\"")
                            + " ,"
                            + object.getString("adcity").replaceAll(
                                    "&#34;", "\"")
                            + ", "
                            + object.getString("adstate").replaceAll(
                                    "&#34;", "\"")
                            + " "
                            + object.getString("adzipcode").replaceAll(
                                    "&#34;", "\""));
                    tvCount1.setText(object.getString("nucount"));

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    // tvLocCd1.setText( "!!ERROR: "+e.getMessage());
                    tvOffice1.setText("!!ERROR: " + e.getMessage());
                    tvDescript1.setText("Please contact STS/BAC.");
                    tvCount1.setText("N/A");

                    e.printStackTrace();
                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            status = "yes1";
        } else {
            // display error
            status = "no";
        }        
    }
    
    public void getToLocationDetails() {
        // loc_details.setText(loc_code.getText().toString());
        destinationLocation = autoCompleteTextView2.getText()
                .toString().trim();
        String barcodeNumberDetails[] = autoCompleteTextView2.getText()
                .toString().trim().split("-");
        String barcode_num = barcodeNumberDetails[0];// this will be
                                                     // passed to the
                                                     // server
        loc_code_str = barcodeNumberDetails[0];// this will be passed to
                                               // next activity with
                                               // intent

        // check network connection
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // fetch data
            status = "yes";

            AsyncTask<String, String, String> resr1 = new RequestTask()
                    .execute(URL + "/LocationDetails?barcode_num="
                            + barcode_num);
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
                    JSONObject object = (JSONObject) new JSONTokener(
                            res).nextValue();
                    tvOffice2.setText(object.getString("cdrespctrhd"));
                    // tvLocCd2.setText( object.getString("cdlocat"));
                    tvDescript2.setText(object.getString("adstreet1")
                            .replaceAll("&#34;", "\"")
                            + " ,"
                            + object.getString("adcity").replaceAll(
                                    "&#34;", "\"")
                            + ", "
                            + object.getString("adstate").replaceAll(
                                    "&#34;", "\"")
                            + " "
                            + object.getString("adzipcode").replaceAll(
                                    "&#34;", "\""));
                    tvCount2.setText(object.getString("nucount"));

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    tvDescript2.setText("!!ERROR: " + e.getMessage());
                    tvOffice2.setText("Please contact STS/BAC.");
                    tvCount2.setText("N/A");

                    e.printStackTrace();
                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            status = "yes1";
        } else {
            // display error
            status = "no";
        }
        // locDetailsDest.setText(res);
        // locDetailsDest.append("\n"+loc_code.getText().toString());
        // autoCompleteTextView1.setText(barcode_num);
    }
    

}
