package gov.nysenate.inventory.activity.verification;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

import com.android.volley.Request;
import com.android.volley.Response;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import gov.nysenate.inventory.activity.LoginActivity;
import gov.nysenate.inventory.activity.MenuActivity;
import gov.nysenate.inventory.activity.SenateActivity;
import gov.nysenate.inventory.adapter.CommodityListViewAdapter;
import gov.nysenate.inventory.adapter.InvListViewAdapter;
import gov.nysenate.inventory.android.AppSingleton;
import gov.nysenate.inventory.android.ClearableEditText;
import gov.nysenate.inventory.android.CommentsDialog;
import gov.nysenate.inventory.android.InvApplication;
import gov.nysenate.inventory.android.KeywordDialog;
import gov.nysenate.inventory.android.MsgAlert;
import gov.nysenate.inventory.android.NewInvDialog;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.android.StringInvRequest;
import gov.nysenate.inventory.listener.CommentsDialogListener;
import gov.nysenate.inventory.listener.CommodityDialogListener;
import gov.nysenate.inventory.model.Commodity;
import gov.nysenate.inventory.model.InvItem;
import gov.nysenate.inventory.model.Item;
import gov.nysenate.inventory.model.ItemStatus;
import gov.nysenate.inventory.util.AppProperties;
import gov.nysenate.inventory.util.HttpUtils;
import gov.nysenate.inventory.util.Serializer;
import gov.nysenate.inventory.util.Toasty;

import static gov.nysenate.inventory.activity.verification.Verification.progBarVerify;

public class VerScanActivity extends SenateActivity implements
        CommodityDialogListener, CommentsDialogListener {

    public ClearableEditText barcode;
    public TextView tv_counts_new;
    public TextView tv_counts_existing;
    public TextView tv_counts_scanned;
    public TextView loc_details;
    public TextView tvCdlocat;
    public static ArrayList<InvItem> unscannedItems = null;
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
    ArrayList<verList> list = new ArrayList<verList>();
    public static ArrayList<InvItem> invList = new ArrayList<InvItem>();
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
    public static int numItems;
    protected boolean isAlive;

    public static ArrayList<InvItem> allScannedItems = new ArrayList<InvItem>();
    public static ArrayList<InvItem> newItems = new ArrayList<InvItem>();

    String lastClickedTo = "";
    int lastRowFound = -1;

    CommodityListViewAdapter commodityAdapter;
    KeywordDialog keywordDialog;

    android.app.FragmentManager fragmentManager = this.getFragmentManager();

    Response.Listener keepAliveRespListener = new Response.Listener<String>() {

        @Override
        public void onResponse(String response) {
            VerScanActivity.this.isAlive = true;
            InvApplication.timeoutType = -1;
            try {
                try {
                    if (response == null) {
                        VerScanActivity.this.isAlive = false;

                    } else if (response.toUpperCase().indexOf("SESSION TIMED OUT") > -1) {
                        VerScanActivity.this.isAlive = false;
                        VerScanActivity.this.startTimeout(KEEPALIVE_TIMEOUT);
                    }
                } catch (Exception e) {
                    VerScanActivity.this.isAlive = false;
                }
            } catch (Exception ex) {
                VerScanActivity.this.isAlive = false;
            }
        }
    };

    Response.Listener addItemRespListener = new Response.Listener<String>() {

        @Override
        public void onResponse(String response) {
            if (response == null) {
                try {
                    try {
                        playSound(R.raw.honk);
                    }
                    catch (Exception e0) {

                    }
                    new MsgAlert(VerScanActivity.this, "ERROR (A02): Add Item", "!!ERROR: Unable to add Item. Unable to get a server response");
                }
                catch (Exception  e1) {
                    new Toasty(VerScanActivity.this).showMessage("!!ERROR (A02): Unable to add Item. Unable to get a server response", Toast.LENGTH_LONG);
                }
                return;
            }

            try {
                InvApplication.timeoutType = -1;
                Item item = Serializer.deserialize(response, Item.class).get(0);

                if (item.getStatus() == ItemStatus.DOES_NOT_EXIST) {
                    nusenateDidNotExist(VerScanActivity.this.holdNusenate);
                    return;
                }

                String itemType = null;
                verList vl = new verList();
                vl.NUSENATE = item.getBarcode();
                String nusenateReturned = null;
                vl.CDCATEGORY = item.getCommodity().getCategory();
                vl.CDLOCAT = item.getLocation().getCdlocat();
                itemType = "EXISTING";

                try {
                    vl.CDLOCTYPE = item.getLocation().getCdloctype();
                } catch (Exception e) {
                    vl.CDLOCTYPE = "(N/A)";
                }
                nusenateReturned = item.getBarcode();

                if (nusenateReturned == null) {
                    vl.DECOMMODITYF = " ***NOT IN SFMS***  New Item";
                    vl.CONDITION = "NEW";
                    itemType = "NEW";
                    nusenateDidNotExist(VerScanActivity.this.holdNusenate);
                } else if (item.getStatus() == ItemStatus.INACTIVE) {
                    vl.DECOMMODITYF = item.getCommodity().getDescription();
                    inactiveInvItem = new InvItem(vl.NUSENATE, vl.CDCATEGORY,
                            vl.CONDITION, vl.DECOMMODITYF, vl.CDLOCAT);

                    inactiveInvItem.setType("INACTIVE");
                    itemType = "INACTIVE";

                    inactiveMessage(
                            VerScanActivity.this.holdNusenate,
                            "<b>**WARNING</b>: Senate Tag#:<b>" + VerScanActivity.this.holdNusenate
                                    + "</b> has been <b>INACTIVATED</b>.",
                            "Item Description: <b>"
                                    + vl.DECOMMODITYF
                                    + "</b><br/><br/>  <font color='RED'>Adding this item will </font><b>ONLY</b> <font color='RED'> save it as a Verification Exception Item. Further action is required by Management to bring it back into the Senate Tracking System via the <b>\"Inventory Record Adjustment E/U\"</b></font>. <br/><br/> Do you want to save this Item for further review?");

                } else {
                    vl.DECOMMODITYF = item.getCommodity().getDescription()
                            + " \n***Found in: " + vl.CDLOCAT + "-" + vl.CDLOCTYPE;
                    vl.CONDITION = "DIFFERENT LOCATION";
                    playSound(R.raw.warning);
                    itemType = "DIFFERENT LOCATION";
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

                if (invItem.getType() == null) {
                    invItem.setType(itemType);
                }

                invList.add(invItem);
                cntScanned++;

                allScannedItems.add(invItem);
                newItems.add(invItem); // to keep track of (number+details)

                // for summary
                updateChanges();
                currentState = NONE;
                // Clear Barcode after all actions are done
                barcode.setText("");
            }
            catch (Exception e) {
                try {
                    try {
                        playSound(R.raw.honk);
                    }
                    catch (Exception e0) {

                    }
                    new MsgAlert(VerScanActivity.this, "ERROR (A01): Add Item", "!!ERROR: Unable to add Item. " + e.getMessage());
                }
                catch (Exception  e1) {
                    new Toasty(VerScanActivity.this).showMessage("!!ERROR (A01): Unable to add Item. " + e.getMessage(), Toast.LENGTH_LONG);
                }
            }
        }
    };

    Response.Listener itemListRespListener = new Response.Listener<String>() {

        @Override
        public void onResponse(String response) {
            try {

                // this will populate the lists from the JSON array coming from
                // server

                invList = (ArrayList) Serializer.deserialize(response, InvItem.class);

                for (InvItem invItem : invList) {
                    invItem.setType("EXISTING");
                }

                count = invList.size();
                numItems = invList.size();

                adapter = new InvListViewAdapter(VerScanActivity.this, R.layout.invlist_item, invList);

                listView.setAdapter(adapter);
                updateChanges(true);

                progBarVerify.setVisibility(View.INVISIBLE);

                status = "yes1";
            } catch (Exception e) {
                try {
                    try {
                        playSound(R.raw.honk);
                    }
                    catch (Exception e0) {

                    }

                    new MsgAlert(VerScanActivity.this, "ERROR: Item List", "!!ERROR: Unable to build item list. "+ e.getMessage());
                }
                catch (Exception  e1) {
                    new Toasty(VerScanActivity.this).showMessage("!!ERROR: Unable to build item list. " + e.getMessage(), Toast.LENGTH_LONG);
                }
            }
        }
    };

    Response.Listener comKeywordListRespListener = new Response.Listener<String>() {

        @Override
        public void onResponse(String response) {
            try {

                // this will populate the lists from the JSON array coming from
                // server

                commodityList = (ArrayList) Serializer.deserialize(response, Commodity.class);

                commodityAdapter = new CommodityListViewAdapter(VerScanActivity.this,
                        R.layout.commoditylist_row, commodityList,
                        VerScanActivity.this.handleKeywords(NewInvDialog.tvKeywordsToBlock.getText()
                                .toString()));

                NewInvDialog.commodityList.setAdapter(commodityAdapter);

                newInvDialog.checkKeywordsFound();
                NewInvDialog.progBarNewInvItem.setVisibility(View.INVISIBLE);

            } catch (Exception e) {
                try {
                    try {
                        playSound(R.raw.honk);
                    }
                    catch (Exception e0) {

                    }

                    new MsgAlert(InvApplication.getAppContext(), "ERROR: Item List", "!!ERROR: Unable to build commodity list. "+e.getMessage());
                }
                catch (Exception  e1) {
                    new Toasty(VerScanActivity.this).showMessage("!!ERROR: Unable to build commodity list. " + e.getMessage(), Toast.LENGTH_LONG);
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verscan);
        registerBaseActivityReceiver();
        AppSingleton.getInstance(this).timeoutFrom = "VERIFICATIONLIST";
        currentActivity = this;
        VerScanActivity.allScannedItems = new ArrayList<InvItem>();
        VerScanActivity.unscannedItems = new ArrayList<InvItem>();
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

        tv_counts_new.setOnTouchListener(new View.OnTouchListener() {

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
                    checkAdapter();
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

        tv_counts_existing.setOnTouchListener(new View.OnTouchListener() {

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

                    checkAdapter();
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
    protected void onRestart() {
        super.onRestart();
        cntScanned = allScannedItems.size();
        updateChanges();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        invList = new ArrayList<>();
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

        if (progBarVerify != null) {
            progBarVerify.setVisibility(View.INVISIBLE);
        }

        barcode.requestFocus();
        barcode.postDelayed(new Runnable() {
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
                if (curInvItem.getType() != null) {
                    if (curInvItem.getType().equalsIgnoreCase(type)) {
                        count++;
                    }
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
                    HttpUtils.keepAlive();
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

    private final TextWatcher filterTextWatcher = new TextWatcher() {

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
            try {
                if (barcode.getText().toString().length() >= 6) {
                    String barcode_num = barcode.getText().toString().trim();
                    String barcode_number = barcode_num;

                    // to delete an element from the list

                    // If the item is already scanned then display a
                    // toaster "Already Scanned"
                    if (findBarcode(barcode_num, allScannedItems) > -1) {
                        // display toaster
                        Context context = getApplicationContext();
                        CharSequence text = "Already Scanned  ";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        barcode.setText("");

                        HttpUtils.keepAlive(keepAliveRespListener);

                        return;
                    }
                    handleItem();
                }
            }

            catch (Exception e0) {
                new Toasty(VerScanActivity.this).showMessage( "!!ERROR: Problem with interpreting barcode. "+e0.getMessage(), Toast.LENGTH_LONG);

                try {
                    playSound(R.raw.error);
                }
                catch (Exception e1) {

                }

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
                .setPositiveButton(Html.fromHtml("<b>Ok</b>"), new DialogInterface.OnClickListener() {
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
        new HttpUtils().playSound(R.raw.noconnect);

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
                adapter = null;
                NewInvDialog.commodityList.setAdapter(null);
            } else {
                dialogKeywords = NewInvDialog.tvKeywordsToBlock.getText()
                        .toString();
                keywordDialog = new KeywordDialog(this, newInvDialog,
                        "Modify Commodity Keywords",
                        "<h1>Add/Edit/Delete Keywords Below</h1>",
                        this.dialogKeywords);
                keywordDialog.setRetainInstance(true);
                keywordDialog.show(fragmentManager, "keyword_dialog");
                keywordDialog.addListener(newInvDialog);
            }

        } else {
            if (NewInvDialog.tvKeywordsToBlock.clearField) {
                adapter = null;
                NewInvDialog.commodityList.setAdapter(null);
                newInvDialog.setMode(newInvDialog.MODE_KEYWORD_SEARCH);
            } else {

            }
        }
        // keywordDialog.getDialog().setCanceledOnTouchOutside(false);
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
        Log.i(this.getClass().getName(), "nusenateDidNotExist: " + nusenate);

        playSound(R.raw.error);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle(Html.fromHtml("<font color='#000055'><b>**WARNING: Senate Tag#: " + nusenate + " DOES NOT EXIST</b> in the SFMS Tracking System. </font>"));

        // set dialog message
        alertDialogBuilder.setMessage(Html.fromHtml("The item <b>cannot</b> be tagged to current location at this time. You may Save the Tag# and item information as a Verification Exception.<br /><br /><b>Save New Tag# and item Information?</b>"))
                .setCancelable(false)
                .setPositiveButton(Html.fromHtml("<b>Yes</b>"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        /*
                         * if this button is clicked, open the dialog
                         * to allow entry for the new nusenate#
                         */
                        getNewNusenateInfo(nusenate);
                    }
                })
                .setNegativeButton(Html.fromHtml("<b>No</b>"), new DialogInterface.OnClickListener() {
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
        playSound(R.raw.error);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle(Html.fromHtml("<font color='#000055'>"
                + title + "</font>"));

        // set dialog message
        alertDialogBuilder.setMessage(Html.fromHtml(message))
                .setCancelable(false)
                .setPositiveButton(Html.fromHtml("<b>Ok</b>"), new DialogInterface.OnClickListener() {
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
            URL = AppProperties.getBaseUrl();

            StringInvRequest stringInvRequest = new StringInvRequest(Request.Method.GET,
                    URL + "ItemsList?loc_code=" + loc_code, null, itemListRespListener);

            InvApplication.timeoutType = this.ITEMLIST_TIMEOUT;

            /* Add your Requests to the RequestQueue to execute */
            AppSingleton.getInstance(InvApplication.getAppContext()).addToRequestQueue(stringInvRequest);
        }
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
                    Log.i("NULL CHECK", "invList.getInstance(" + i + ") IS NULL!!");

                }
                if (curInvItem.getNusenate() == null) {
                    Log.i("NULL CHECK", "invList.getInstance(" + i
                            + ").getNusenate() IS NULL!!");

                } else if (curInvItem.getNusenate() == null) {
                    Log.i("NULL CHECK", "invList.getInstance(" + i
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
                checkAdapter();
                adapter.removeBarCode(nusenate);

                HttpUtils.keepAlive();

                // display toster
                Context context = getApplicationContext();
                StringBuilder sb = new StringBuilder();
                sb.append("REMOVED: ");
                sb.append(curInvItem.getNusenate());
                sb.append(": ");
                sb.append(curInvItem.getDecommodityf());

                CharSequence text = sb.toString();
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();

                if (curInvItem.getType() == null || curInvItem.getType().length() == 0) {
                    curInvItem.setType("EXISTING");
                }

                Log.i(this.getClass().getName(), "(removeItem) ADD TO allScannedItems");

                allScannedItems.add(curInvItem);// to keep track of

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
                if (!isAlive) {
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

        Log.i(this.getClass().getName(), "(addNewItem) ADD TO allScannedItems and newItems");

        allScannedItems.add(newInvItem);
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

    public void addItem(String nusenate) {
        currentState = ADDITEM_STATE;

        // Don't Let getItemDetails handle the timeout, we want the timeout to
        // be shown as ITEMDETAILS_TIMEOUT
        // from the code in Add Item and to return false for Add Item before it
        // attempts to add an item with
        // the else condition.

        holdNusenate = nusenate;

        StringInvRequest stringInvRequest = new StringInvRequest(Request.Method.GET,
                URL + "Item?barcode=" + nusenate, null, addItemRespListener);

        InvApplication.timeoutType = this.ITEMDETAILS_TIMEOUT;

        /* Add your Requests to the RequestQueue to execute */
        AppSingleton.getInstance(InvApplication.getAppContext()).addToRequestQueue(stringInvRequest);
    }

    public void addKeyword(View view) {
        int rowAdded = keywordDialog.getAdapter().addRow();
        View view1 = new View(currentActivity);
    }

    @Override
    public void getDialogDataFromServer() {
        // check network connection

        commodityList = new ArrayList<Commodity>();

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // Get the URL from the properties
        URL = AppProperties.getBaseUrl();

        if (networkInfo != null && networkInfo.isConnected()) {

            StringInvRequest stringInvRequest = new StringInvRequest(Request.Method.GET,
                    URL + "CommodityList?keywords="
                            + NewInvDialog.tvKeywordsToBlock.getText().toString(), null, comKeywordListRespListener);

            NewInvDialog.progBarNewInvItem.setVisibility(View.VISIBLE);

            InvApplication.timeoutType = COMMODITYLIST;

            /* Add your Requests to the RequestQueue to execute */
            AppSingleton.getInstance(InvApplication.getAppContext()).addToRequestQueue(stringInvRequest);

        }
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

    public void updateChanges() {
        updateChanges(false);
    }

    public void updateChanges(boolean force) {
        checkAdapter(force);
        adapter.notifyDataSetChanged();
        count = adapter.getCount();
        int cntExisting = countOf(invList, "EXISTING");
        int cntNew = countOf(invList, "NEW");
        int cntInactive = countOf(invList, "INACTIVE");
        int cntDiffLoc = countOf(invList, "DIFFERENT LOCATION");
        Log.i(this.getClass().getName(), "(updateChanges) invList Size:" + invList.size() + " All Scanned Items Size:" + allScannedItems.size() + " (cntNew:" + cntNew + " + cntDiffLoc:" + cntDiffLoc + " + cntInactive:" + cntInactive + ") (cntExisting:" + cntExisting + ") ( cntScanned:" + cntScanned + ")");

        tv_counts_new.setText(Html.fromHtml("<b>New/Found</b><br/>"
                + (cntNew + cntDiffLoc + cntInactive)));
        tv_counts_existing.setText(Html.fromHtml("<b>Unscanned</b><br/>"
                + cntExisting));
        tv_counts_scanned.setText(Html.fromHtml("<b>Scanned</b><br />"
                + cntScanned));
    }

    public void checkAdapter() {
        checkAdapter(false);
    }

    public void checkAdapter(boolean force) {
        if (force || adapter == null) {
            adapter = new InvListViewAdapter(this, R.layout.invlist_item, invList);
            listView.setAdapter(adapter);
        }
    }

    public void handleItem() {
        handleItem(false); // Default is to not resume from a timeout
    }

    public void handleItem(boolean resumeFromTimeout) {
        // TODO resumeFromTimeout Code

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

        if (!resumeFromTimeout || (currentState == NONE || currentState == REMOVEITEM_STATE)) {
            invItemIndex = removeItem(nusenate, resumeFromTimeout);
        }

        if ((resumeFromTimeout && currentState == ADDITEM_STATE) || invItemIndex == -1) { // Item not found, so Add Item to list
            addItem(nusenate);  // addItemResults =
        }

        updateChanges();
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
        //if (checkServerResponse(true) == OK) {
        btnVerListCont.getBackground().setAlpha(45);

        // create lists for summary activity
        unscannedItems = new ArrayList<InvItem>();

        for (int i = 0; i < this.invList.size(); i++) {
            if ((invList.get(i).getType().equalsIgnoreCase("EXISTING")) == true) {
                unscannedItems.add(invList.get(i)); // if the
                // description
                // of dispList
                // is not new
                // item then add
                // it to missing
                // list
            }
        }

        Intent intent = new Intent(this, VerSummaryActivity.class);
        intent.putExtra("loc_code", loc_code);
        intent.putExtra("cdloctype", cdloctype);
        intent.putExtra("totalItemCount", numItems);
        startActivity(intent);
        overridePendingTransition(R.anim.in_right, R.anim.out_left);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
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
        finish();
        overridePendingTransition(R.anim.in_left, R.anim.out_right);
    }

    public void dispToster(String msg) {
        Context context = getApplicationContext();
        CharSequence text = msg;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    public class spinSortListComparator implements Comparator {

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

    public class SortChangedListener implements OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos,
                                   long id) {
            currentSortValue = parent.getItemAtPosition(pos).toString();
            Collections.sort(invList, new spinSortListComparator());
            checkAdapter();

            adapter.notifyDataSetChanged();

        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }

    }

    public class verList {
        String NUSENATE;
        String CDCATEGORY;
        String DECOMMODITYF;
        String CDLOCAT;
        String CDLOCTYPE;
        String CONDITION;
        String CDSTATUS;
    }

    @Override
    public void commoditySelected(int rowSelected, Commodity commoditySelected, String comments) {
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

            newInvItem.setCdcategory(commoditySelected.getCategory());
            newInvItem.setCdlocat(tvCdlocat.getText().toString());
            newInvItem.setType("NEW");
            newInvItem.setCdcommodity(commoditySelected.getCode());

            // BH 8/21/13 - Request from Sheila.. Show as
            // "*** NEW ITEM ** CC: {Commodity Code}: {Any Comments}" instead of
            // decommodityf..
            // as of 8/21/13 this is okay since decommodityf is only used as
            // display value. She

            newInvItem.setDecommodityf(Html.fromHtml(
                    commoditySelected.getDescription()).toString());
            newInvItem.setDecomments(comments);
            addNewItem(newInvItem);
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
        keywordDialog.getAdapter().returnToSelectedRow();
    }

    @Override
    public void onCommentOKButtonClicked(String decomments) {
        // if this button is clicked, just close
        // the dialog box and do nothing
        inactiveInvItem.setDecommodityf(inactiveInvItem.getDecommodityf());
        inactiveInvItem.setDecomments(decomments);
        invList.add(inactiveInvItem);
        cntScanned++;

        Log.i(this.getClass().getName(), "(onCommentOKButtonClicked) ADD TO allScannedItems and newItems");

        allScannedItems.add(inactiveInvItem);
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
