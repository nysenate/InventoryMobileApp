package gov.nysenate.inventory.android;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
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

public class VerScanActivity extends SenateActivity implements
        CommodityDialogListener, CommentsDialogListener
{

    public ClearableEditText barcode;
    public TextView tv_counts_new;
    public TextView tv_counts_existing;
    public TextView tv_counts_scanned;
    public TextView loc_details;
    public TextView tvCdlocat;
    public static ArrayList<InvItem> missingItems = null;
    public String res = null;
    boolean testResNull = false; // flag used for Testing Purposes
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
    public static final int COMMODITYLIST_TIMEOUT = 2020, COMMODITYLIST = 3030,
            NEWITEMCOMMENTS = 3031;
    public String status = null;
    public ListView listView;
    public String loc_code = null; // populate this from the location code from
                                   // previous activity
    public String cdloctype = null;
    ArrayList commodityList = new ArrayList<Commodity>();
    public static ArrayList<InvItem> scannedItems = new ArrayList<InvItem>();
    ArrayList<verList> list = new ArrayList<verList>();
    ArrayList<InvItem> invList = new ArrayList<InvItem>();
    CommentsDialog commentsDialog = null;

    public final int ITEMLIST_TIMEOUT = 101, ITEMDETAILS_TIMEOUT = 102,
            KEEPALIVE_TIMEOUT = 103;
    public final int NONE = 200, REMOVEITEM_STATE = 201, ADDITEM_STATE = 202,
            ALIVE_STATE = 203;
    public final int SENTAG_NOT_FOUND = 1002, INACTIVE_SENTAG = 1004;
    String currentSortValue = "Description";
    public Spinner spinSortList;
    static Button btnVerListCont;
    static Button btnVerListCancel;
    static Button btnNoSenateTagAdd;
    int cntScanned = 0;
    Activity currentActivity;
    int currentState;
    InvItem inactiveInvItem;

    String holdNusenate = null;

    String URL = ""; // this will be initialized once in onCreate() and used for
                     // all server calls.
    String timeoutFrom = "VERIFICATIONLIST";
    InvListViewAdapter adapter;
    int count;
    int numItems;
    // These 3 ArrayLists will be used to transfer data to next activity and to
    // the server
    public static ArrayList<InvItem> AllScannedItems = new ArrayList<InvItem>();// for
                                                                                // saving
    // items which
    // are not
    // allocated
    // to
    // that
    // location

    public static ArrayList<InvItem> newItems = new ArrayList<InvItem>();// for
                                                                         // saving
                                                                         // items
    // which are not
    // allocated to that
    // location

    String lastClickedTo = "";
    int lastRowFound = -1;

    CommodityListViewAdapter commodityAdapter;
    CommodityDialogListener commodityDialogListener;
    KeywordDialog keywordDialog;

    android.app.FragmentManager fragmentManager = this.getFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verscan);
        registerBaseActivityReceiver();
        currentActivity = this;
        VerScanActivity.AllScannedItems = new ArrayList<InvItem>();
        VerScanActivity.missingItems = new ArrayList<InvItem>();
        VerScanActivity.scannedItems = new ArrayList<InvItem>();
        VerScanActivity.newItems = new ArrayList<InvItem>();

        // Get the location code from the previous activity
        Intent intent = getIntent();
        loc_code = intent.getStringExtra(Verification.loc_code_intent);
        cdloctype = intent.getStringExtra(Verification.cdloctype_intent);

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

        // Setup TextViews
        tvCdlocat = (TextView) findViewById(R.id.tvCdlocat);
        tvCdlocat.setText(loc_code);

        // display the count on screen
        tv_counts_new = (TextView) findViewById(R.id.tv_counts_new);
        tv_counts_existing = (TextView) findViewById(R.id.tv_counts_existing);
        tv_counts_scanned = (TextView) findViewById(R.id.tv_counts_scanned);

        getItemsList();

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
                    foundAt = adapter.findTypePosNOTEqualTo("EXISTING",
                            lastRowFound + 1);
                    lastRowFound = foundAt;
                    listView.setSelection(lastRowFound);
                    lastClickedTo = "v";

                    Log.v("index", "" + offset);
                }
                return true;
            }
        });

        // populate the listview

        // --code from other activity
        // ends-----------------------------------------------------------------

        // code for textwatcher

        barcode = (ClearableEditText) findViewById(R.id.preferencePWD);
        barcode.addTextChangedListener(filterTextWatcher);

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

        if (barcode == null) {
            barcode = (ClearableEditText) findViewById(R.id.preferencePWD);
            barcode.addTextChangedListener(filterTextWatcher);
        }

        barcode.requestFocus();
        barcode.postDelayed(new Runnable()
        {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
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

    @Override
    public void startTimeout(int timeoutType) {
        Intent intentTimeout = new Intent(this, LoginActivity.class);
        intentTimeout.putExtra("TIMEOUTFROM", timeoutFrom);
        startActivityForResult(intentTimeout, timeoutType);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        /**
         * Handle the results from the recognition activity.
         */
        case VOICE_RECOGNITION_REQUEST_CODE:
            if (resultCode == RESULT_OK) {
                // Fill the list view with the strings the recognizer thought it
                // could have heard
                ArrayList<String> matches = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                TextView t = (TextView) findViewById(R.id.textView2);
                t.setText(matches.get(0));
            }
            break;

        case ITEMDETAILS_TIMEOUT:
            if (resultCode == RESULT_OK) {
                handleItem(true);
            }
            break;
        case ITEMLIST_TIMEOUT:
            if (resultCode == RESULT_OK) {
                this.getItemsList();
            }
            break;
        case KEEPALIVE_TIMEOUT:
            if (resultCode == RESULT_OK) {
                keepAlive();
            }
            break;
        case COMMODITYLIST:
            if (resultCode == RESULT_OK) {
                // Fill the list view with the strings the recognizer thought it
                // could have heard
                ArrayList<String> matches = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                NewInvDialog.tvKeywordsToBlock.setText(matches.get(0)
                        .replaceAll(" ", ",").replaceAll(",,", ","));
                getDialogDataFromServer();
            }
            break;

        case NEWITEMCOMMENTS:
            if (resultCode == RESULT_OK) {
                ArrayList<String> matches = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                NewInvDialog.etNewItemComments.setText(matches.get(0));
            }
            break;
        case ITEMCOMMENTS:
            if (resultCode == RESULT_OK) {
                ArrayList<String> matches = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                CommentsDialog.etComments.setText(matches.get(0));
            }
            break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private final TextWatcher filterTextWatcher = new TextWatcher()
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
                /*
                 * Log.i("TESTING", " getItemDetails:" + barcode.getText() +
                 * " (" + barcode.getText().length() + ")");
                 */
                // loc_details.setText(loc_code.getText().toString());
                // listView.
                String barcode_num = barcode.getText().toString().trim();
                String barcode_number = barcode_num;

                boolean barcodeFound = false;

                // to delete an element from the list
                int flag = 0;

                // If the item is already scanned then display a
                // toaster "Already Scanned"
                if (findBarcode(barcode_num, AllScannedItems) > -1) {
                    // display toaster
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
                handleItem();
            }
        }
    };

    public boolean keepAlive() {
        // Simply contact the Web Server to keep the Session Alive,
        // to help minimize
        // issues with Session Timeouts
        AsyncTask<String, String, String> resr1 = new RequestTask().execute(URL
                + "/KeepSessionAlive");

        try {

            try {
                res = null;
                res = resr1.get().trim().toString();
                if (res == null) {
                    noServerResponse();
                    return false;
                } else if (res.toUpperCase().indexOf("SESSION TIMED OUT") > -1) {
                    this.startTimeout(KEEPALIVE_TIMEOUT);
                    return false;
                } else {
                    return true;
                }
            } catch (Exception e) {
                return false;
            }
        } catch (Exception ex) {
            return false;
        }

    }

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

    public void noServerResponse() {
        noServerResponse(null);

    }

    public void noServerResponse(final String barcode_num) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        StringBuilder title = new StringBuilder();
        title.append("<font size='5' color='#000055'>");
        if (barcode_num != null && barcode_num.trim().length() > 0) {
            title.append("Barcode#: ");
            title.append(barcode_num);
            title.append(" ");
        }
        title.append("NO SERVER RESPONSE</font>");

        StringBuilder msg = new StringBuilder();
        msg.append("!!ERROR: There was <font color='RED'><b>NO SERVER RESPONSE</b></font>.");
        if (barcode_num != null && barcode_num.trim().length() > 0) {
            msg.append(" Senate Tag#:<b>");
            msg.append(barcode_num);
            msg.append("</b> will be <b>IGNORED</b>.");
        }
        msg.append("<br/> Please contact STS/BAC.");

        // set title
        alertDialogBuilder.setTitle(Html.fromHtml(title.toString()));

        // set dialog message
        alertDialogBuilder.setMessage(Html.fromHtml(msg.toString()))
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        if (barcode_num != null
                                && barcode_num.trim().length() > 0) {
                            Context context = getApplicationContext();

                            CharSequence text = "Senate Tag#: " + barcode_num
                                    + " was NOT added";
                            int duration = Toast.LENGTH_SHORT;

                            Toast toast = Toast.makeText(context, text,
                                    duration);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        }

                        barcode.setText("");

                        dialog.dismiss();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public void editKeywordList(View view) {
        if (newInvDialog != null) {
            // newInvDialog.dismiss();
        }
        if (newInvDialog.currentMode == newInvDialog.MODE_KEYWORD_SEARCH) {
            if (NewInvDialog.tvKeywordsToBlock.clearField) {
                Log.i("editKeywordList", "MODE_KEYWORD_SEARCH clearField");
                adapter = null;
                NewInvDialog.commodityList.setAdapter(null);
            } else {
                Log.i("editKeywordList", "MODE_KEYWORD_SEARCH NOT clearField");
                dialogKeywords = NewInvDialog.tvKeywordsToBlock.getText()
                        .toString();
                // Log.i("editKeywordList",
                // "trying to display the Keywords Fragment Dialog 2 KEYWORDS:"
                // +dialogKeywords);
                keywordDialog = new KeywordDialog(this, newInvDialog,
                        "Modify Commodity Keywords",
                        "<h1>Add/Edit/Delete Keywords Below</h1>",
                        this.dialogKeywords);
                // Log.i("editKeywordList",
                // "trying to display the Keywords Fragment Dialog 3");
                keywordDialog.setRetainInstance(true);
                // Log.i("editKeywordList",
                // "trying to display the Keywords Fragment Dialog 4");
                keywordDialog.show(fragmentManager, "keyword_dialog");
                keywordDialog.addListener(newInvDialog);
            }

        } else {
            if (NewInvDialog.tvKeywordsToBlock.clearField) {
                Log.i("editKeywordList",
                        "(1) SET MODE_KEYWORD_SEARCH clearField");
                adapter = null;
                NewInvDialog.commodityList.setAdapter(null);
                newInvDialog.setMode(newInvDialog.MODE_KEYWORD_SEARCH);
                // newInvDialog.adapter.clearData();
            } else {
                Log.i("editKeywordList",
                        "(2)MODE_KEYWORD_SEARCH NOT clearField");

            }
        }
        // keywordDialog.getDialog().setCanceledOnTouchOutside(false);
        // Log.i("editKeywordList",
        // "trying to display the Keywords Fragment Dialog DONE");

    }

    public void noSenateTagAdd(View view) {
        senateTagNum = false;
        newInvDialog = new NewInvDialog(
                this,
                "<b>Enter New Senate Inventory Information</b>",
                "<b><h2>No Senate Tag#"  
                        + "</h2>Only use this option if you <font color='red'><b>ABSOLUTELY</b></font> do not have a Senate Tag# or replacement Senate Tag#.</b><br />", Gravity.CENTER_HORIZONTAL);
        newInvDialog.addListener(this);
        newInvDialog.setRetainInstance(true);
        newInvDialog.show(fragmentManager, "newInvDialog");
        /*
         * Dialog dialog = (AlertDialog) newInvDialog.getDialog(); Button
         * positiveButton = ((AlertDialog)
         * dialog).getButton(DialogInterface.BUTTON_POSITIVE); OKButtonListener
         * okButtonListener = new OKButtonListener(dialog);
         * positiveButton.setOnClickListener(okButtonListener);
         */

        // newInvDialog.getDialog().setCanceledOnTouchOutside(false);
    }

    public void nusenateDidNotExist(final String nusenate) {
        // Log.i("TESTING", "****Senate Tag# DidNotExist MESSAGE");
        playSound(R.raw.error);
        playSound(R.raw.error);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle(Html.fromHtml("<font color='#000055'><b>***WARNING: Senate Tag#: "+nusenate+" DOES NOT EXIST</b> in the SFMS Tracking System. </font>"));

        // set dialog message
        alertDialogBuilder.setMessage(Html.fromHtml("The item <b>cannot</b> be tagged to current location at this time. You may Save the Tag# and item information as a Verification Exception.<br /><br /><b>Save New Tag# and item Information?</b>"))
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        /*
                         * if this button is clicked, open the dialog
                         * to allow entry for the new nusenate#
                         */                        
                        getNewNusenateInfo(nusenate);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        /*
                         *  if this button is clicked, just close
                         *  the dialog box and do nothing
                         */
                        dialog.dismiss();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
        

        // newInvDialog.getDialog().setCanceledOnTouchOutside(false);
    }
    
    public void getNewNusenateInfo(final String nusenate) {
        senateTagNum = true;
        holdNusenate = nusenate;
        newInvDialog = new NewInvDialog(
                this,
                "<b>Enter New Senate Inventory Information</b>",
                "<b><h2>Senate Tag#: " + nusenate 
                        + "</h2><font color='red'>(Does not exist in SFMS Tracking System)</font></b><br />", Gravity.CENTER_HORIZONTAL);
        newInvDialog.addListener(this);
        newInvDialog.setRetainInstance(true);
        newInvDialog.show(fragmentManager, "fragment_name");
    }
    

    public void errorMessage(final String barcode_num, final String title,
            final String message) {
        // Log.i("TESTING", "****errorMessgae MESSAGE");
        playSound(R.raw.error);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle(Html.fromHtml("<font color='#000055'>"
                + title + "</font>"));

        // set dialog message
        alertDialogBuilder.setMessage(Html.fromHtml(message))
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        Context context = getApplicationContext();

                        CharSequence text = "Senate Tag#: " + barcode_num
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

    public void inactiveMessage(final String barcode_num, final String title,
            final String message) {
        playSound(R.raw.error);

        senateTagNum = true;
        holdNusenate = barcode_num;
        commentsDialog = new CommentsDialog(this, title, message);
        commentsDialog.addListener(this);
        commentsDialog.setRetainInstance(true);
        commentsDialog.show(fragmentManager, "comments_dialog");
    }

    public void getItemsList() {
        // check network connection

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // fetch data
            status = "yes";

            // Get the URL from the properties
            URL = LoginActivity.properties.get("WEBAPP_BASE_URL").toString();

            AsyncTask<String, String, String> resr1 = new RequestTask()
                    .execute(URL + "/ItemsList?loc_code=" + loc_code);

            try {

                // code for JSON
                try {
                    res = null;
                    res = resr1.get().trim().toString();
                    if (res == null) {
                        noServerResponse();
                        return;
                    } else if (res.indexOf("Session timed out") > -1) {
                        startTimeout(ITEMLIST_TIMEOUT);
                        return;
                    }
                } catch (NullPointerException e) {
                    noServerResponse();
                    return;
                }
                if (this.testResNull) { // Testing Purposes Only
                    resr1 = null;
                }
                String jsonString = resr1.get().trim().toString();
                if (this.testResNull) { // Testing Purposes Only
                    res = null;
                    resr1 = null;
                    // Log.i("TEST RESNULL", "RES SET TO NULL");
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
                    try {
                        vl.CDLOCTYPE = jo.getString("CDLOCTYPETO");
                    }
                    catch (Exception e) {
                        vl.CDLOCTYPE = "(N/A)";
                    }

                    list.add(vl);
                    StringBuilder s = new StringBuilder();
                    s.append(vl.NUSENATE);
                    s.append(" ");
                    s.append(vl.CDCATEGORY);
                    s.append(" ");
                    s.append(vl.DECOMMODITYF);

                    // 3/15/13 BH Coded below to use InvItem Objects to display
                    // the list.
                    InvItem invItem = new InvItem(vl.NUSENATE, vl.CDCATEGORY,
                            "EXISTING", vl.DECOMMODITYF, vl.CDLOCAT);
                    invList.add(invItem);
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

        /*
         * adapter = new InvListAdapter<StringBuilder>(this,
         * android.R.layout.simple_list_item_1, dispList);
         */

        /*
         * tv_counts_new.setText(Html.fromHtml("<b>New</b><br/>" +
         * countOf(invList, "NEW")));
         * 
         * int cntExisting = countOf(invList, "EXISTING"); int cntNew =
         * countOf(invList, "NEW");
         * tv_counts_existing.setText(Html.fromHtml("<b>Unscanned</b><br/>" +
         * cntExisting));
         * 
         * tv_counts_scanned.setText(Html.fromHtml("<b>Scanned</b><br />" +
         * cntScanned));
         */
        listView.setAdapter(adapter);
        updateChanges();

        Verification.progBarVerify.setVisibility(View.INVISIBLE);
    }

    public int removeItem(String nusenate, boolean resumeFromTimeout) {
        currentState = REMOVEITEM_STATE;
        int foundAt = -1;
        for (int i = invList.size() - 1; i > -1; i--) {
            // this if will not remove the "New items" and previously
            // scanned items
            InvItem curInvItem = invList.get(i);

            try {
                if (curInvItem == null) {
                    Log.i("NULL CHECK", "invList.get(" + i + ") IS NULL!!");

                }
                if (curInvItem.getNusenate() == null) {
                    Log.i("NULL CHECK", "invList.get(" + i
                            + ").getNusenate() IS NULL!!");

                } else if (curInvItem.getNusenate() == null) {
                    Log.i("NULL CHECK", "invList.get(" + i
                            + ").getNusenate() IS NULL!!");
                }
                if (nusenate == null) {
                    Log.i("NULL CHECK",
                            "Senate tag number IS NULL!! AS OF CHECKING INDEX#:"
                                    + i);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            if ((curInvItem.getNusenate().equals(nusenate))) {
                /*
                 * Log.i("TEST", nusenate +
                 * " BEFORE REMOVE BARCODE INVLIST SIZE:" + invList.size());
                 */
                adapter.removeBarCode(nusenate);
                /*
                 * Log.i("TEST", nusenate +
                 * " AFTER REMOVE BARCODE INVLIST SIZE:" + invList.size());
                 */

                // display toster
                Context context = getApplicationContext();
                StringBuilder sb = new StringBuilder();
                sb.append("REMOVED: ");
                sb.append(curInvItem.getNusenate());
                //sb.append(" ");
                //sb.append(curInvItem.getType());
                sb.append(": ");
                sb.append(curInvItem.getDecommodityf());

                CharSequence text = sb.toString();
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                AllScannedItems.add(curInvItem);// to keep track of
                                                // (number+details)
                                                // for summary
                /*
                 * Log.i("TEST", nusenate + " BEFORE REMOVE(" + i +
                 * ") INVLIST SIZE:" + invList.size() + "");
                 */
                // invList.remove(i);
                scannedItems.add(curInvItem);// to keep track of all
                                             // scanned items
                                             // numbers for
                                             // oracle table
                cntScanned++;
                playSound(R.raw.ok);
                // Simply contact the Web Server to keep the Session
                // Alive, to help minimize
                // issues with Session Timeouts
                try {
                    StringBuffer values = new StringBuffer();
                    values.append(curInvItem.getNusenate());
                    values.append("|");
                    values.append(curInvItem.getType());
                    values.append("|");
                    values.append(curInvItem.getCdcategory());
                    values.append("|");
                    values.append(curInvItem.getCdintransit());
                    values.append("|");
                    values.append("-1");
                    values.append("|");
                    values.append(curInvItem.getDecommodityf());
                    values.append("|");
                    values.append(curInvItem.getCdlocat());
                    values.append("|now|");
                    values.append(LoginActivity.nauser);
                    values.append("|now|");
                    values.append(LoginActivity.nauser);

                    long rowsInserted = MenuActivity.db
                            .insert("ad12verinv",
                                    "nusenate|cdcond|cdcategory|cdintransit|nuxrpickup|decommodityf|cdlocatfrm|dttxnorigin|natxnorguser|dttxnupdate|natxnupduser",
                                    values.toString());

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                foundAt = i;
                if (!keepAlive()) {
                    currentState = NONE;
                    return -2;
                }
            }
        }
        currentState = NONE;
        return foundAt;
    }

    //
    // Dialog will call this if the user picks a Commodity Code
    //

    public void addNewItem(InvItem newInvItem) {
        invList.add(newInvItem);
        cntScanned++;

        scannedItems.add(newInvItem);
        AllScannedItems.add(newInvItem);
        newItems.add(newInvItem); // to keep track of (number+details)
                                  // for summary

        StringBuilder s_new = new StringBuilder();
        // s_new.append(vl.NUSENATE); since the desc coming from
        // server already contains barcode number we wont add it
        // again
        // s_new.append(" ");
        s_new.append(newInvItem.getCdcategory());
        s_new.append(" ");
        s_new.append(newInvItem.getDecommodityf());
        s_new.append(" New Item");

        adapter = new InvListViewAdapter(this, R.layout.invlist_item, invList);

        listView.setAdapter(adapter);
        updateChanges();

        // display toster
        Context context = getApplicationContext();
        CharSequence text = s_new;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

        currentState = NONE;
        // Clear Barcode after all actions are done
        barcode.setText("");
    }

    public int addItem(String nusenate) {
        currentState = ADDITEM_STATE;

        // Don't Let getItemDetails handle the timeout, we want the timeout to
        // be shown as ITEMDETAILS_TIMEOUT
        // from the code in Add Item and to return false for Add Item before it
        // attempts to add an item with
        // the else condition.

        String serverResponse = getItemDetails(nusenate, false);

        /*
         * Log.i("AddItem", "nusenate:" + nusenate + " Server RESPONSE:" +
         * serverResponse);
         */

        if (serverResponse == null) {
            noServerResponse();
            return NO_SERVER_RESPONSE;
        } else if (res.indexOf("Session timed out") > -1) {
            startTimeout(ITEMDETAILS_TIMEOUT);
            return SERVER_SESSION_TIMED_OUT;
        } else if (res.contains("Does not exist in system")) {
            // Log.i("TESTING", "A CALL Senate Tag# DidNotExist");
            nusenateDidNotExist(nusenate);
            return SENTAG_NOT_FOUND;
        } else {
            JSONObject jo;
            try {
                jo = new JSONObject(serverResponse);
                verList vl = new verList();
                vl.NUSENATE = nusenate;
                String nusenateReturned = null;
                vl.CDCATEGORY = jo.getString("cdcategory");
                vl.CDLOCAT = jo.getString("cdlocatto");
                vl.CDSTATUS = jo.getString("cdstatus");
                try {
                    vl.CDLOCTYPE = jo.getString("cdloctypeto");
                }
                catch (Exception e) {
                    vl.CDLOCTYPE = "(N/A)";
                }
                nusenateReturned = jo.getString("nusenate");

                if (nusenateReturned == null) {
                    vl.DECOMMODITYF = " ***NOT IN SFMS***  New Item";
                    vl.CONDITION = "NEW";
                    nusenateDidNotExist(nusenate);
                    return SENTAG_NOT_FOUND;
                } else if (vl.CDSTATUS.equalsIgnoreCase("I")) {
                    vl.DECOMMODITYF = jo.getString("decommodityf");
                    inactiveInvItem = new InvItem(vl.NUSENATE, vl.CDCATEGORY,
                            vl.CONDITION, vl.DECOMMODITYF, vl.CDLOCAT);

                    inactiveInvItem.setType("INACTIVE");

                    inactiveMessage(
                            nusenate,
                            "<b>***WARNING</b>: Senate Tag#:<b>" + nusenate
                                    + "</b> has been <b>INACTIVATED</b>.",
                            "Item Description: <b>"
                                    + vl.DECOMMODITYF
                                    + "</b><br/><br/>  <font color='RED'>Adding this item will </font><b>ONLY</b> <font color='RED'> save it as a Verification Exception Item. Further action is required by Management to bring it back into the Senate Tracking System via the <b>\"Inventory Record Adjustment E/U\"</b></font>. <br/><br/> Do you want to save this Item for further review?");
                    return -4;

                } else {
                    // Log.i("TESTING",
                    // "nusenateReturned was not null LENGTH:"+nusenateReturned.length());
                    vl.DECOMMODITYF = jo.getString("decommodityf")
                            + " \n***Found in: " + vl.CDLOCAT + "-" + vl.CDLOCTYPE;
                    vl.CONDITION = "DIFFERENT LOCATION";
                    playSound(R.raw.warning);
                }
                StringBuilder s_new = new StringBuilder();
                // s_new.append(vl.NUSENATE); since the desc coming from
                // server already contains barcode number we wont add it
                // again
                // s_new.append(" ");
                s_new.append("ADDED: ");
                s_new.append(vl.NUSENATE);
                s_new.append(": ");
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
                currentState = NONE;

                return OK;
            } catch (JSONException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                return EXCEPTION_IN_CODE;
            }
        }
    }

    public void addKeyword(View view) {
        // Log.i("addKeyword", "addKeyword adding row");
        int rowAdded = keywordDialog.adapter.addRow();
        View view1 = new View(currentActivity);
        // Log.i("addKeyword",
        // "ROW#:"+rowAdded+", ACTUAL FIELD COUNT:"+keywordDialog.adapter.etKeywordFields.size());

        // keywordDialog.adapter.etKeywordFields.get(rowAdded).requestFocus();

        // Log.i("addKeyword", "addKeyword adding row done");
    }

    @Override
    public void getDialogDataFromServer() {
        // check network connection

        long startTime = System.currentTimeMillis();
        long endTime = System.currentTimeMillis();

        commodityList = new ArrayList<Commodity>();

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // Get the URL from the properties
        URL = LoginActivity.properties.get("WEBAPP_BASE_URL").toString();

        if (networkInfo != null && networkInfo.isConnected()) {

            // Get the URL from the properties
            URL = LoginActivity.properties.get("WEBAPP_BASE_URL").toString();

            // Log.i("getCommodityList", URL + "/getCommodityList?keywords=" +
            // NewInvDialog.tvKeywordsToBlock.getText().toString());
            AsyncTask<String, String, String> resr1 = new RequestTask()
            {
                @Override
                public void onPreExecute() {
                    // Log.i("onPreExecute", "Progress Bar Visible");
                    NewInvDialog.progBarNewInvItem.setVisibility(View.VISIBLE);
                }

                public void onPostExecute() {
                    // Log.i("onPostExecute","Progress Bar INVisible");
                    NewInvDialog.progBarNewInvItem
                            .setVisibility(View.INVISIBLE);
                }

            };

            resr1.execute(URL + "/CommodityList?keywords="
                    + NewInvDialog.tvKeywordsToBlock.getText().toString());

            try {

                // code for JSON
                try {
                    res = null;
                    res = resr1.get().trim().toString();
                    endTime = System.currentTimeMillis();
                    // Log.i("Time Test",
                    // "GetCommodityList "+((endTime-startTime)/1000.0));
                    startTime = System.currentTimeMillis();

                    if (res == null) {
                        noServerResponse();
                        return;
                    } else if (res.indexOf("Session timed out") > -1) {
                        startTimeout(COMMODITYLIST_TIMEOUT);
                        return;
                    }
                } catch (NullPointerException e) {
                    noServerResponse();
                    return;
                }
                String jsonString = resr1.get().trim().toString();
                // Log.i("getCommodityList","SERVER RESPONSE:"+jsonString);

                JSONArray jsonArray = new JSONArray(jsonString);
                // this will populate the lists from the JSON array coming from
                // server

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jo = new JSONObject();
                    jo = jsonArray.getJSONObject(i);
                    Commodity curCommodity = new Commodity();
                    curCommodity.setCdcategory(jo.getString("cdcategory"));
                    curCommodity.setCdcommodity(jo.getString("cdcommodity"));
                    curCommodity.setCDissunit(jo.getString("cdissunit"));
                    curCommodity.setCdtype(jo.getString("cdtype"));
                    curCommodity.setDecomments(jo.getString("decomments"));
                    curCommodity.setDecommodityf(jo.getString("decommodityf"));
                    curCommodity.setNucnt(jo.getString("nucnt"));
                    curCommodity.setNuxrefco(jo.getString("nuxrefco"));

                    commodityList.add(curCommodity);

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
        } else {
            // display error
        }
        endTime = System.currentTimeMillis();
        // Log.i("Time Test",
        // "add Commodity List "+((endTime-startTime)/1000.0));
        startTime = System.currentTimeMillis();

        commodityAdapter = new CommodityListViewAdapter(this,
                R.layout.commoditylist_row, commodityList,
                handleKeywords(NewInvDialog.tvKeywordsToBlock.getText()
                        .toString()));

        /*
         * if (commodityAdapter==null) { Log.i("getCommodityList",
         * "****commodityAdapter is null"); } else { Log.i("getCommodityList",
         * "commodityAdapter is NOT null"); }
         * 
         * if (newInvDialog==null) { Log.i("getCommodityList",
         * "****newInvDialog is null"); } else { Log.i("getCommodityList",
         * "newInvDialog is NOT null"); }
         * 
         * if (newInvDialog.commodityList==null) { Log.i("getCommodityList",
         * "****newInvDialog.commodityList is null"); } else {
         * Log.i("getCommodityList", "newInvDialog.commodityList is NOT null");
         * }
         */

        NewInvDialog.commodityList.setAdapter(commodityAdapter);
        endTime = System.currentTimeMillis();
        // Log.i("Time Test",
        // "Commodity List adapter "+((endTime-startTime)/1000.0));
        startTime = System.currentTimeMillis();

        newInvDialog.checkKeywordsFound();
        NewInvDialog.progBarNewInvItem.setVisibility(View.INVISIBLE);
        endTime = System.currentTimeMillis();
        // Log.i("Time Test",
        // "Commodity List checkKeywordsFound "+((endTime-startTime)/1000.0));
        startTime = System.currentTimeMillis();
    }

    public String[] handleKeywords(String keywords) {
        String[] keywordList = null;

        if (keywords == null) { // Return early so we don't hit a null pointer
                                // exception
            return keywordList;
        }

        String onlyKeywords = Html.fromHtml(keywords).toString();
        String[] tempKeywordList = onlyKeywords.split(",");
        ArrayList<String> holdKeywords = new ArrayList<String>();
        for (int x = 0; x < tempKeywordList.length; x++) {
            String currentKeyword = tempKeywordList[x].trim();
            if (currentKeyword.length() > 0) {
                holdKeywords.add(currentKeyword);
            }
        }
        keywordList = new String[holdKeywords.size()];
        for (int x = 0; x < holdKeywords.size(); x++) {
            keywordList[x] = holdKeywords.get(x);
        }
        return keywordList;
    }

    public String getItemDetails(String nusenate) {
        return getItemDetails(nusenate, true); // By default, check for a
                                               // timeout
    }

    public String getItemDetails(String nusenate, boolean checkTimeout) {
        // check network connection
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // fetch data
            status = "yes";
            // int barcode= Integer.parseInt(barcode_num);
            // scannedItems.add(barcode);

            AsyncTask<String, String, String> resr1 = new RequestTask()
                    .execute(URL + "/ItemDetails?barcode_num=" + nusenate);
            try {
                if (testResNull) { // Testing Purposes Only
                    resr1 = null;
                    Log.i("TEST RESNULL", "RES SET TO NULL");
                }
                res = null;
                res = resr1.get().trim().toString();
                if (res == null) {
                    noServerResponse();
                    return res;
                } else if (checkTimeout
                        && res.indexOf("Session timed out") > -1) {
                    startTimeout(ITEMDETAILS_TIMEOUT);
                    return res;
                }

            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NullPointerException e) {
                noServerResponse(nusenate);
                return res;
            }

            status = "yes1";
        } else {
            // display error
            status = "no";
        }

        return res;
    }

    public void updateChanges() {
        adapter.notifyDataSetChanged();
        count = adapter.getCount();
        int cntExisting = countOf(invList, "EXISTING");
        int cntNew = countOf(invList, "NEW");
        int cntInactive = countOf(invList, "INACTIVE");
        int cntDiffLoc = countOf(invList, "DIFFERENT LOCATION");
        tv_counts_new.setText(Html.fromHtml("<b>New/Found</b><br/>"
                + (cntNew + cntDiffLoc + cntInactive)));
        tv_counts_existing.setText(Html.fromHtml("<b>Unscanned</b><br/>"
                + cntExisting));
        tv_counts_scanned.setText(Html.fromHtml("<b>Scanned</b><br />"
                + cntScanned));
        Log.i("check", "listview updated");
    }

    public void handleItem() {
        handleItem(false); // Default is to not resume from a timeout
    }

    public void handleItem(boolean resumeFromTimeout) {
        // TODO resumeFromTimeout Code

        /*
         * Log.i("TESTING", " handleItem:(resumeFromTimeout:" +
         * resumeFromTimeout + ")  " + barcode.getText() + " (" +
         * barcode.getText().length() + ")");
         */

        String nusenate = barcode.getText().toString().trim();

        if (resumeFromTimeout) {
            if (currentState == NONE) {
                Log.i("TESTING", "STATE WAS NONE PRIOR TO TIMEOUT");
            } else if (currentState == REMOVEITEM_STATE) {
                Log.i("TESTING", "STATE WAS REMOVEITEM_STATE PRIOR TO TIMEOUT");
            } else if (currentState == ADDITEM_STATE) {
                Log.i("TESTING", "STATE WAS ADDITEM_STATE PRIOR TO TIMEOUT");
            }
        }

        boolean barcodeFound = false;
        // Try to remove an item from the list....
        int invItemIndex = -1;
        if (!resumeFromTimeout
                || (currentState == NONE || currentState == REMOVEITEM_STATE)) {
            Log.i("TESTING", "Removing item " + nusenate);
            invItemIndex = removeItem(nusenate, resumeFromTimeout);
        }

        if ((resumeFromTimeout && currentState == ADDITEM_STATE)
                || invItemIndex == -1) { // Item not found, so Add Item to list
            // Log.i("TESTING", "Adding item " + nusenate);
            int addItemResults = -1;
            addItemResults = addItem(nusenate);
            if (addItemResults == SERVER_SESSION_TIMED_OUT) {
                return;
            }
        }

        updateChanges();
        // Log.i("TESTING", "State set back to NONE");
        currentState = NONE;
        // Clear Barcode after all actions are done
        barcode.setText("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_ver_scan, menu);
        return true;
    }

    public void continueButton(View view) {
        if (checkServerResponse(true) == OK) {
            Log.i("VerScanActivity", "continueButton 1");
            btnVerListCont.getBackground().setAlpha(45);

            // create lists for summary activity
            missingItems = new ArrayList<InvItem>();// for
                                                    // saving
                                                    // items
                                                    // which
                                                    // are
                                                    // not
                                                    // allocated
                                                    // to
                                                    // that
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

            String summary;
            summary = "{\"nutotitems\":\"" + numItems + "\",\"nuscanitems\":\""
                    + AllScannedItems.size() + "\",\"numissitems\":\""
                    + missingItems.size() + "\",\"nunewitems\":\""
                    + newItems.size() + "\"}";

            Intent intent = new Intent(this, VerSummaryActivity.class);
            intent.putExtra("loc_code", loc_code);
            intent.putExtra("cdloctype", cdloctype);
            intent.putExtra("summary", summary);
            /*
             * intent.putStringArrayListExtra("scannedBarcodeNumbers",
             * getJSONArrayList(scannedItems));
             */
            /*
             * intent.putStringArrayListExtra("scannedList",
             * getJSONArrayList(AllScannedItems));// scanned
             */
            // items
            // list
            /*
             * intent.putStringArrayListExtra("missingList",
             * getJSONArrayList(missingItems));// missing
             */
            // items
            // list
            intent.putStringArrayListExtra("newItems",
                    getJSONArrayList(newItems));// new
                                                // items
                                                // list
            startActivity(intent);
            overridePendingTransition(R.anim.in_right, R.anim.out_left);
        }
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
        if (checkServerResponse(true) == OK) {
            btnVerListCancel.getBackground().setAlpha(45);
            finish();
            overridePendingTransition(R.anim.in_left, R.anim.out_right);
        }
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
        String CDLOCTYPE;
        String CONDITION;
        String CDSTATUS;
    }

    @Override
    public void commoditySelected(int rowSelected, Commodity commoditySelected) {
        try {
            InvItem newInvItem = new InvItem();
            if (senateTagNum) {
                newInvItem.setNusenate(holdNusenate);
            } else {
                int newBarcodenum = countOf(invList, "NEW") + 1;
                DecimalFormat numberFormat = new DecimalFormat("000");
                String formattedNumber = numberFormat.format(newBarcodenum);
                newInvItem.setNusenate("NEW" + formattedNumber);
            }

            newInvItem.setCdcategory(commoditySelected.getCdcategory());
            newInvItem.setCdlocat(tvCdlocat.getText().toString());
            newInvItem.setType("NEW");
            newInvItem.setCdcommodity(commoditySelected.getCdcommodty());

            // BH 8/21/13 - Request from Sheila.. Show as
            // "*** NEW ITEM ** CC: {Commodity Code}: {Any Comments}" instead of
            // decommodityf..
            // as of 8/21/13 this is okay since decommodityf is only used as
            // display value. She

            String decomments = null;
            try {
                decomments = commoditySelected.getDecomments().trim();
            } catch (Exception e) {
                e.printStackTrace();
            }
            newInvItem.setDecommodityf(Html.fromHtml(
                    commoditySelected.getDecommodityf()).toString());
            newInvItem.setDecomments(commoditySelected.getDecomments());
            addNewItem(newInvItem);
            // Log.i("commoditySelected",
            // "NEW INV ITEM COMMENTS:"+newInvItem.getDecomments());

            if (newInvDialog != null) {
                newInvDialog.dismiss();
            }
            holdNusenate = null;
        } catch (NullPointerException e) {
            new MsgAlert(this, "!!ERROR Occurred in adding new Senate Tag#.",
                    "!!ERROR: An error occured in adding a new Senate Tag Number. "
                            + e.getMessage() + " Please contact STS/BAC.");
            this.reOpenNewInvDialog();
        }
    }

    public void returnToList(View view) {
        if (keywordDialog == null) {
            return;
        }
        keywordDialog.adapter.returnToSelectedRow();
    }

    @Override
    public void onCommentOKButtonClicked(String decomments) {
        // if this button is clicked, just close
        // the dialog box and do nothing
        inactiveInvItem.setDecommodityf(inactiveInvItem.getDecommodityf());
        inactiveInvItem.setDecomments(decomments);
        invList.add(inactiveInvItem);
        cntScanned++;

        scannedItems.add(inactiveInvItem);
        AllScannedItems.add(inactiveInvItem);
        newItems.add(inactiveInvItem); // to keep track of (number+details)
        // for summary
        adapter = new InvListViewAdapter(VerScanActivity.this,
                R.layout.invlist_item, invList);

        listView.setAdapter(adapter);
        updateChanges();
        currentState = NONE;

        Context context = getApplicationContext();

        CharSequence text = "Senate Tag#: " + inactiveInvItem.getNusenate()
                + " was added";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

        barcode.setText("");
    }
}
