package gov.nysenate.inventory.android;

import gov.nysenate.inventory.android.Pickup2Activity.VerList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.Html;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ListtestActivity extends SenateActivity
{

    public ClearableEditText barcode;
    public TextView tv_counts_new;
    public TextView tv_counts_existing;
    public TextView tv_counts_scanned;
    public TextView loc_details;
    public String res = null;
    boolean testResNull = false; // flag used for Testing Purposes
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
    public String status = null;
    public ListView listView;
    public String loc_code = null; // populate this from the location code from
                                   // previous activity
    ArrayList<InvItem> scannedItems = new ArrayList<InvItem>();
    ArrayList<verList> list = new ArrayList<verList>();
    ArrayList<InvItem> invList = new ArrayList<InvItem>();
    String currentSortValue = "Description";
    public Spinner spinSortList;
    static Button btnVerListCont;
    static Button btnVerListCancel;
    int cntScanned = 0;

    String URL = ""; // this will be initialized once in onCreate() and used for
                     // all server calls.
    InvListViewAdapter adapter;
    int count;
    int numItems;
    // These 3 ArrayLists will be used to transfer data to next activity and to
    // the server
    ArrayList<InvItem> AllScannedItems = new ArrayList<InvItem>();// for saving
                                                                  // items which
                                                                  // are not
                                                                  // allocated
                                                                  // to
                                                                  // that
                                                                  // location

    ArrayList<InvItem> newItems = new ArrayList<InvItem>();// for saving items
                                                           // which are not
                                                           // allocated to that
                                                           // location

    String lastClickedTo = "";
    int lastRowFound = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listtest);
        registerBaseActivityReceiver();

        // Get the location code from the previous activity
        Intent intent = getIntent();
        loc_code = intent.getStringExtra(Verification.loc_code_intent);

        // ----------code from other activity starts
        listView = (ListView) findViewById(R.id.preferenceList);
        spinSortList = (Spinner) findViewById(R.id.spinSortList);
        spinSortList.setOnItemSelectedListener(new SortChangedListener());
        String[] spinnerList = getResources().getStringArray(
                R.array.verify_sort);
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<String>(this,
                R.layout.spinner_item, spinnerList);
        adapterSpinner
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinSortList.setAdapter(adapterSpinner);

        // Setup Buttons
        btnVerListCont = (Button) findViewById(R.id.btnVerListCont);
        btnVerListCont.getBackground().setAlpha(255);
        btnVerListCancel = (Button) findViewById(R.id.btnVerListCancel);
        btnVerListCancel.getBackground().setAlpha(255);

        // check network connection

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // fetch data
            status = "yes";

            // Get the URL from the properties
            URL = MainActivity.properties.get("WEBAPP_BASE_URL").toString();

            AsyncTask<String, String, String> resr1 = new RequestTask()
                    .execute(URL + "/ItemsList?loc_code=" + loc_code);

            try {

                // code for JSON
                res = resr1.get().trim().toString();
                if (this.testResNull) {  // Testing Purposes Only
                    resr1 = null;
                }
                String jsonString = resr1.get().trim().toString();
                if (this.testResNull) {  // Testing Purposes Only
                    res = null;
                    resr1 = null;
                    Log.i("TEST RESNULL", "RES SET TO NULL");
                }

                JSONArray jsonArray = new JSONArray(jsonString);
                count = jsonArray.length();
                numItems = jsonArray.length();
                // this will populate the lists from the JSON array coming from
                // server
                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jo = new JSONObject();
                    jo = jsonArray.getJSONObject(i);
                    verList vl = new verList();
                    vl.NUSENATE = jo.getString("NUSENATE");
                    vl.CDCATEGORY = jo.getString("CDCATEGORY");
                    vl.DECOMMODITYF = jo.getString("DECOMMODITYF");
                    vl.CDLOCAT = jo.getString("CDLOCATTO");

                    list.add(vl);
                    StringBuilder s = new StringBuilder();
                    s.append(vl.NUSENATE);
                    s.append(" ");
                    s.append(vl.CDCATEGORY);
                    s.append(" ");
                    s.append(vl.DECOMMODITYF);
                    // dispList.add(s);

                    // 3/15/13 BH Coded below to use InvItem Objects to display
                    // the list.
                    InvItem invItem = new InvItem(vl.NUSENATE, vl.CDCATEGORY,
                            "EXISTING", vl.DECOMMODITYF, vl.CDLOCAT);
                    invList.add(invItem);
                    // invItem = null;

                    // String s =
                    // vl.NUSENATE+" "+vl.CDCATEGORY+" "+vl.DECOMMODITYF;
                    // dispList.add(s);
                    // number= dispList.size();
                    // list.add((verList) jsonArray.getJSONObject(i));
                }
                // code for JSON ends
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            status = "yes1";
        } else {
            // display error
            status = "no";
        }

        adapter = new InvListViewAdapter(this, R.layout.invlist_item, invList);

        // ArrayAdapter<StringBuilder>
        /*
         * adapter = new InvListAdapter<StringBuilder>(this,
         * android.R.layout.simple_list_item_1, dispList);
         */

        // display the count on screen
        tv_counts_new = (TextView) findViewById(R.id.tv_counts_new);
        tv_counts_existing = (TextView) findViewById(R.id.tv_counts_existing);
        tv_counts_scanned = (TextView) findViewById(R.id.tv_counts_scanned);

        tv_counts_new.setText(Html.fromHtml("<b>New</b><br/>"
                + countOf(invList, "NEW")));
        tv_counts_new.setOnTouchListener(new View.OnTouchListener()
        {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Layout layout = ((TextView) v).getLayout();
                int x = (int) event.getX();
                int y = (int) event.getY();
                // int plusPos =
                // tv_counts_new.getText().toString().indexOf("+");
                int foundAt = -1;
                if (layout != null) {
                    int line = layout.getLineForVertical(y);
                    int offset = layout.getOffsetForHorizontal(line, x);
                    /*
                     * if (offset<plusPos) { if (!lastClickedTo.equals("NEW")) {
                     * lastRowFound = -1; } lastClickedTo = "NEW"; foundAt =
                     * adapter.findTypePos("NEW", lastRowFound+1); } else if
                     * (offset>plusPos) { if (lastClickedTo.equals("NEW")) {
                     * lastRowFound = -1; } lastClickedTo = "EXISTING"; foundAt
                     * = adapter.findTypePos("EXISTING", lastRowFound+1); }
                     */
                    if (lastClickedTo.equals("EXISTING")) {
                        lastRowFound = -1;
                    }
                    foundAt = adapter.findTypePos("NEW", lastRowFound + 1);
                    lastRowFound = foundAt;
                    listView.setSelection(lastRowFound);
                    lastClickedTo = "v";

                    Log.v("index", "" + offset);
                }
                return true;
            }
        });
        int cntExisting = countOf(invList, "EXISTING");
        int cntNew = countOf(invList, "NEW");
        tv_counts_existing.setText(Html.fromHtml("<b>Existing</b><br/>"
                + cntExisting));
        tv_counts_existing.setOnTouchListener(new View.OnTouchListener()
        {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Layout layout = ((TextView) v).getLayout();
                int x = (int) event.getX();
                int y = (int) event.getY();
                // int plusPos =
                // tv_counts_new.getText().toString().indexOf("+");
                int foundAt = -1;
                if (layout != null) {
                    int line = layout.getLineForVertical(y);
                    int offset = layout.getOffsetForHorizontal(line, x);
                    /*
                     * if (offset<plusPos) { if (!lastClickedTo.equals("NEW")) {
                     * lastRowFound = -1; } lastClickedTo = "NEW"; foundAt =
                     * adapter.findTypePos("NEW", lastRowFound+1); } else if
                     * (offset>plusPos) { if (lastClickedTo.equals("NEW")) {
                     * lastRowFound = -1; } lastClickedTo = "EXISTING"; foundAt
                     * = adapter.findTypePos("EXISTING", lastRowFound+1); }
                     */
                    if (lastClickedTo.equals("NEW")) {
                        lastRowFound = -1;
                    }
                    foundAt = adapter.findTypePos("EXISTING", lastRowFound + 1);
                    lastRowFound = foundAt;
                    listView.setSelection(lastRowFound);
                    lastClickedTo = "EXISTING";

                    Log.v("index", "" + offset);
                }
                return true;
            }
        });
        tv_counts_scanned.setText(Html.fromHtml("<b>Scanned</b><br />"
                + cntScanned));

        // populate the listview
        listView.setAdapter(adapter);

        // --code from other activity
        // ends-----------------------------------------------------------------

        // code for textwatcher

        barcode = (ClearableEditText) findViewById(R.id.preferencePWD);
        barcode.addTextChangedListener(filterTextWatcher);

        // listView = (ListView) findViewById(R.id.listView1 );

        Verification.progBarVerify.setVisibility(View.INVISIBLE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Setup Buttons
        btnVerListCont = (Button) findViewById(R.id.btnVerListCont);
        btnVerListCont.getBackground().setAlpha(255);
        btnVerListCancel = (Button) findViewById(R.id.btnVerListCancel);
        btnVerListCancel.getBackground().setAlpha(255);
        
        
        // Force Keyboard to Popup
        
        if (barcode==null) {
            barcode = (ClearableEditText) findViewById(R.id.preferencePWD);
            barcode.addTextChangedListener(filterTextWatcher);            
        }
        
        barcode.requestFocus();
        barcode.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(barcode, InputMethodManager.SHOW_FORCED);
            }   
        }, 100);

    }

    public int countOf(ArrayList<InvItem> invList, String type) {
        int count = 0;
        for (int x = 0; x < invList.size(); x++) {
            InvItem curInvItem = invList.get(x);
            if (curInvItem != null) {
                if (curInvItem.getType().equalsIgnoreCase(type)) {
                    count++;
                }
            }

        }
        return count;

    }

    private void startVoiceRecognitionActivity() {
        Intent intentSpeech = new Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intentSpeech.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intentSpeech.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Speech recognition demo");
        startActivityForResult(intentSpeech, VOICE_RECOGNITION_REQUEST_CODE);
    }

    /**
     * Handle the results from the recognition activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE
                && resultCode == RESULT_OK) {
            // Fill the list view with the strings the recognizer thought it
            // could have heard
            ArrayList<String> matches = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            TextView t = (TextView) findViewById(R.id.textView2);
            t.setText(matches.get(0));
        }

        super.onActivityResult(requestCode, resultCode, data);
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
            if (barcode.getText().toString().length() >= 6) {
                Log.i("TESTING", " afterTextChanged:" + barcode.getText()
                        + " (" + barcode.getText().length() + ")");
                // loc_details.setText(loc_code.getText().toString());
                // listView.
                String barcode_num = barcode.getText().toString().trim();
                String barcode_number = barcode_num;

                boolean barcodeFound = false;

                // to delete an element from the list
                int flag = 0;

                // If the item is already scanned then display a
                // toster"Already Scanned"
                if (findBarcode(barcode_num, AllScannedItems) > -1) {
                    // display toster
                    barcodeFound = true;
                    Context context = getApplicationContext();
                    CharSequence text = "Already Scanned  ";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    barcode.setText("");
                    return;
                }

                for (int i = invList.size() - 1; i > -1; i--) {
                    // this if will not remove the "New items" and previously
                    // scanned items
                    InvItem curInvItem = invList.get(i);

                    try {
                        if (curInvItem == null) {
                            Log.i("NULL CHECK", "invList.get(" + i
                                    + ") IS NULL!!");

                        }
                        if (curInvItem.getNusenate() == null) {
                            Log.i("NULL CHECK", "invList.get(" + i
                                    + ").getNusenate() IS NULL!!");

                        } else if (curInvItem.getNusenate() == null) {
                            Log.i("NULL CHECK", "invList.get(" + i
                                    + ").getNusenate() IS NULL!!");
                        }
                        if (barcode_number == null) {
                            Log.i("NULL CHECK",
                                    "barcode_number IS NULL!! AS OF CHECKING INDEX#:"
                                            + i);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if ((curInvItem.getNusenate().equals(barcode_number))
                            && (barcodeFound == false)) {
                        Log.i("TEST", barcode_number
                                + " BEFORE REMOVE BARCODE INVLIST SIZE:"
                                + invList.size());
                        adapter.removeBarCode(barcode_number);
                        Log.i("TEST", barcode_number
                                + " AFTER REMOVE BARCODE INVLIST SIZE:"
                                + invList.size());

                        // display toster
                        Context context = getApplicationContext();
                        StringBuilder sb = new StringBuilder();
                        sb.append("Removed: ");
                        sb.append(curInvItem.getNusenate());
                        sb.append(" ");
                        sb.append(curInvItem.getType());
                        sb.append(" ");
                        sb.append(curInvItem.getDecommodityf());

                        CharSequence text = sb.toString();
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        AllScannedItems.add(curInvItem);// to keep track of
                                                        // (number+details)
                                                        // for summary
                        Log.i("TEST", barcode_number + " BEFORE REMOVE(" + i
                                + ") INVLIST SIZE:" + invList.size() + "");
                        //invList.remove(i);
                        scannedItems.add(curInvItem);// to keep track of all
                                                     // scanned items
                                                     // numbers for
                                                     // oracle table
                        cntScanned++;
                        playSound(R.raw.ok);        

                        flag = 1;
                    }

                }

                // if it is not already scanned and does not exist in the
                // list(location)
                // then add it to list and append new item to its description
                if ((flag == 0)
                        && (scannedItems.contains(barcode_number) == false)) {

                    // check network connection
                    ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                    if (networkInfo != null && networkInfo.isConnected()) {
                        // fetch data
                        status = "yes";
                        // int barcode= Integer.parseInt(barcode_num);
                        // scannedItems.add(barcode);

                        AsyncTask<String, String, String> resr1 = new RequestTask()
                                .execute(URL + "/ItemDetails?barcode_num="
                                        + barcode_num);
                        try {
                            if (testResNull) {  // Testing Purposes Only
                                res = null;
                                resr1 = null;
                                Log.i("TEST RESNULL", "RES SET TO NULL");
                            } 
                            res = resr1.get().trim().toString();

                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (NullPointerException e) {
                            
                        }
                        
                        status = "yes1";
                    } else {
                        // display error
                        status = "no";
                    }

                    // add it to list and displist and scanned items
                    verList vl = new verList();
                    // vl.NUSENATE = barcode_number;
                    // vl.CDCATEGORY = res;
                    // vl.DECOMMODITYF = " New Item";
                    try {
                        Log.i("SERVER RESULTS", "RES:" + res);
                        if (res == null) {
                            Log.i("TESTING", "A CALL noServerResponse");
                            noServerResponse(barcode_num);
                            return;

                        } else if (res.contains("Does not exist in system")) {

                            // vl.NUSENATE = barcode_num;
                            // vl.CDCATEGORY = "";
                            // vl.DECOMMODITYF = " ***NOT IN SFMS***  New Item";
                            Log.i("TESTING", "A CALL barcodeDidNotExist");
                            barcodeDidNotExist(barcode_num);
                            return;

                        } else {
                            JSONObject jo = new JSONObject(res);
                            vl.NUSENATE = barcode_num;
                            vl.CDCATEGORY = jo.getString("cdcategory");
                            vl.CDLOCAT = jo.getString("cdlocatto");
                            String nusenateReturned = jo.getString("nusenate");
                            

                            if (nusenateReturned == null) {
                                vl.DECOMMODITYF = " ***NOT IN SFMS***  New Item";
                                vl.CONDITION = "NEW";
                                barcodeDidNotExist(barcode_num);
                                return;
                            } else {
                                // Log.i("TESTING",
                                // "nusenateReturned was not null LENGTH:"+nusenateReturned.length());
                                vl.DECOMMODITYF = jo.getString("decommodityf")
                                        + " \n***Found in: "+vl.CDLOCAT;
                                vl.CONDITION = "DIFFERENT LOCATION";
                                playSound(R.raw.warning);        
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    list.add(vl);
                    StringBuilder s_new = new StringBuilder();
                    // s_new.append(vl.NUSENATE); since the desc coming from
                    // server already contains barcode number we wont add it
                    // again
                    // s_new.append(" ");
                    s_new.append(vl.CDCATEGORY);
                    s_new.append(" ");
                    s_new.append(vl.DECOMMODITYF);

                    // display toster
                    Context context = getApplicationContext();
                    CharSequence text = s_new;
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    String invStatus;

                    // 3/15/13 BH Coded below to use InvItem Objects to display
                    // the list.
                    InvItem invItem = new InvItem(vl.NUSENATE, vl.CDCATEGORY,
                            vl.CONDITION, vl.DECOMMODITYF, vl.CDLOCAT);
                    invList.add(invItem);
                    cntScanned++;

                    scannedItems.add(invItem);
                    AllScannedItems.add(invItem);
                    newItems.add(invItem); // to keep track of (number+details)
                                           // for summary
                }

                // notify the adapter that the data in the list is changed and
                // refresh the view

                // adapter = new InvListViewAdapter(getApplicationContext(),
                // R.layout.invlist_item, invList);
                // adapter.clear();
                adapter.notifyDataSetChanged();
                count = adapter.getCount();
                int cntExisting =  countOf(invList, "EXISTING");
                int cntNew = countOf(invList, "NEW");
                tv_counts_new.setText(Html.fromHtml("<b>New</b><br/>"
                        + cntNew));
                tv_counts_existing.setText(Html.fromHtml("<b>Existing</b><br/>"
                        +cntExisting));
                tv_counts_scanned.setText(Html.fromHtml("<b>Scanned</b><br />"
                        +cntScanned));
                // listView.setAdapter(adapter);
                Log.i("check", "listview updated");

                barcode.setText("");
            }
        }
    };

    public int findBarcode(String barcode_num) {
        return findBarcode(barcode_num, invList);
    }

    public int findBarcode(String barcode_num, ArrayList<InvItem> invList) {
        for (int x = 0; x < invList.size(); x++) {
            if (invList.get(x).getNusenate().equals(barcode_num)) {
                return x;
            }
        }
        return -1;
    }

    public void noServerResponse(final String barcode_num) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle("Barcode#: " + barcode_num
                + " NO SERVER RESPONSE");

        // set dialog message
        alertDialogBuilder
                .setMessage(
                        Html.fromHtml("!!ERROR: There was <font color='RED'><b>NO SERVER RESPONSE</b></font>. Barcode#:<b>"
                                + barcode_num
                                + "</b> will be <b>IGNORED</b>.<br/> Please contact STS/BAC."))
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        Context context = getApplicationContext();

                        CharSequence text = "Barcode#: " + barcode_num
                                + " was NOT added";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();

                        barcode.setText("");

                        dialog.dismiss();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public void barcodeDidNotExist(final String barcode_num) {
        Log.i("TESTING", "****barcodeDidNotExist MESSAGE");
        playSound(R.raw.error);        
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle("Barcode#: " + barcode_num
                + " DOES NOT EXIST IN SFMS");

        // set dialog message
        alertDialogBuilder
                .setMessage(
                        Html.fromHtml("***WARNING: Barcode#: <b>"
                                + barcode_num
                                + "</b> does not exist in SFMS. This should not occur with a Senate Tag#.<br/><br/> Do you want to add this barcode?"))
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        verList vl = new verList();
                        vl.NUSENATE = barcode_num;
                        vl.CDCATEGORY = "";
                        vl.DECOMMODITYF = " ***NOT IN SFMS***  New Item";
                        vl.CDLOCAT = "";
                        list.add(vl);
                        StringBuilder s_new = new StringBuilder();
                        // s_new.append(vl.NUSENATE); since the desc coming from
                        // server already contains barcode number we wont add it
                        // again
                        // s_new.append(" ");
                        s_new.append("DIALOG_YES: ");
                        s_new.append(vl.CDCATEGORY);
                        s_new.append(" ");
                        s_new.append(vl.DECOMMODITYF);

                        InvItem invItem = new InvItem(vl.NUSENATE,
                                vl.CDCATEGORY, "NEW", vl.DECOMMODITYF, vl.CDLOCAT);
                        invList.add(invItem);

                        scannedItems.add(invItem);
                        AllScannedItems.add(invItem);
                        newItems.add(invItem); // to keep track of
                                               // (number+details)

                        Context context = getApplicationContext();
                        CharSequence text = s_new;
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();

                        adapter.notifyDataSetChanged();
                        count = adapter.getCount();
                        cntScanned++;
                        int cntExisting =  countOf(invList, "EXISTING");
                        int cntNew = countOf(invList, "NEW");                      
                        tv_counts_new.setText(Html.fromHtml("New<br/><b>"
                                + cntNew + "</b>"));
                        tv_counts_existing.setText(Html
                                .fromHtml("Existing<br/><b>"
                                        + cntExisting + "</b>"));
                        tv_counts_scanned.setText(Html.fromHtml("Scanned<br /><b>"
                                + cntScanned + "</b>"));
                        barcode.setText("");
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        Context context = getApplicationContext();

                        CharSequence text = "Barcode#: " + barcode_num
                                + " was NOT added";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();

                        barcode.setText("");

                        dialog.dismiss();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_listtest, menu);
        return true;
    }

    /*
     * @Override public void onBackPressed() { super.onBackPressed();
     * overridePendingTransition(R.anim.in_left, R.anim.out_right); }
     */

    public void continueButton(View view) {
        btnVerListCont.getBackground().setAlpha(45);

        // create lists for summary activity
        ArrayList<InvItem> missingItems = new ArrayList<InvItem>();// for saving
                                                                   // items
                                                                   // which are
                                                                   // not
                                                                   // allocated
                                                                   // to that
                                                                   // location
        for (int i = 0; i < this.invList.size(); i++) {
            if ((invList.get(i).getType().equalsIgnoreCase("EXISTING")) == true) {
                missingItems.add(invList.get(i)); // if the
                                                  // description
                                                  // of dispList
                                                  // is not new
                                                  // item then add
                                                  // it to missing
                                                  // list
            }
        }
        Log.i("MISSING ITEMS COUNT", "MISSING ITEMS:"+missingItems.size());
        
        String summary;
        summary = "{\"nutotitems\":\"" + numItems + "\",\"nuscanitems\":\""
                + AllScannedItems.size() + "\",\"numissitems\":\""
                + missingItems.size() + "\",\"nunewitems\":\""
                + newItems.size() + "\"}";

        Intent intent = new Intent(this, VerSummaryActivity.class);
        intent.putExtra("loc_code", loc_code);
        intent.putExtra("summary", summary);
        intent.putStringArrayListExtra("scannedBarcodeNumbers",
                getJSONArrayList(scannedItems));
        intent.putStringArrayListExtra("scannedList",
                getJSONArrayList(AllScannedItems));// scanned
        // items
        // list
        intent.putStringArrayListExtra("missingList",
                getJSONArrayList(missingItems));// missing
        // items
        // list
        intent.putStringArrayListExtra("newItems", getJSONArrayList(newItems));// new
                                                                               // items
                                                                               // list
        /*
         * if (1==1) { // Testing return; }
         */
        startActivity(intent);
        overridePendingTransition(R.anim.in_right, R.anim.out_left);

    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.menu_test_null:
            item.setChecked(!item.isChecked());
            testResNull = item.isChecked();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }    

    public ArrayList<String> getJSONArrayList(ArrayList<InvItem> invList) {
        ArrayList<String> returnArray = new ArrayList<String>();
        if (invList != null) {
            for (int x = 0; x < invList.size(); x++) {
                returnArray.add(invList.get(x).toJSON());
            }
        }

        return returnArray;
    }

    public void cancelButton(View view) {
        btnVerListCancel.getBackground().setAlpha(45);
        Intent intent = new Intent(this, Verification.class);
        startActivity(intent);
        overridePendingTransition(R.anim.in_left, R.anim.out_right);
    }

    public void dispToster(String msg) {
        Context context = getApplicationContext();
        CharSequence text = msg;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
    

    public class spinSortListComparator implements Comparator
    {

        @Override
        public int compare(Object lObject, Object rObject) {
            InvItem lInvItem = (InvItem) lObject;
            InvItem rInvItem = (InvItem) rObject;

            if (currentSortValue.equalsIgnoreCase("Description")) {
                return lInvItem.getDecommodityf().compareTo(
                        rInvItem.getDecommodityf());
            } else if (currentSortValue.equalsIgnoreCase("Senate Tag#")) {
                Integer lNusenate = new Integer(lInvItem.getNusenate());
                Integer rNusenate = new Integer(rInvItem.getNusenate());
                return lNusenate.compareTo(rNusenate);
            } else if (currentSortValue.equalsIgnoreCase("Last Inventory Date")) {
                // Need to Pull Inventory Date C
                return lInvItem.getDecommodityf().compareTo(
                        rInvItem.getDecommodityf());
            } else if (currentSortValue
                    .equalsIgnoreCase("Last Inventory Date Descending")) {
                // Need to Pull Inventory Date C
                return rInvItem.getDecommodityf().compareTo(
                        lInvItem.getDecommodityf());
            } else {
                return lInvItem.getDecommodityf().compareTo(
                        rInvItem.getDecommodityf());
            }
        }

    }

    public class SortChangedListener implements OnItemSelectedListener
    {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos,
                long id) {
            currentSortValue = parent.getItemAtPosition(pos).toString();
            Collections.sort(invList, new spinSortListComparator());
            adapter.notifyDataSetChanged();
            // listView.setAdapter(adapter);

        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }

    }

    public class verList
    {
        String NUSENATE;
        String CDCATEGORY;
        String DECOMMODITYF;
        String CDLOCAT;
        String CONDITION;
    }
}
