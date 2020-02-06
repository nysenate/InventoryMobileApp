package gov.nysenate.inventory.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;

import java.util.ArrayList;

import gov.nysenate.inventory.adapter.InvListViewAdapter;
import gov.nysenate.inventory.android.AppSingleton;
import gov.nysenate.inventory.android.ClearableEditText;
import gov.nysenate.inventory.android.InvApplication;
import gov.nysenate.inventory.android.MsgAlert;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.android.StringInvRequest;
import gov.nysenate.inventory.model.InvItem;
import gov.nysenate.inventory.model.Item;
import gov.nysenate.inventory.model.ItemStatus;
import gov.nysenate.inventory.model.Location;
import gov.nysenate.inventory.model.Transaction;
import gov.nysenate.inventory.util.AppProperties;
import gov.nysenate.inventory.util.HttpUtils;
import gov.nysenate.inventory.util.Serializer;
import gov.nysenate.inventory.util.Toasty;

public class Pickup2 extends SenateActivity {
    public ClearableEditText senateTagTV;
    private TextView pickupCountTV;
    private TextView originSummary;
    private TextView destinationSummary;
    private String res = null;
    String status = null;
    private ListView pickedUpItemsLV;
    private boolean testResNull = false;
    private ArrayList<InvItem> scannedItems = new ArrayList<InvItem>();
    private ArrayAdapter<InvItem> adapter;
    private int pickupCount;
    private Location origin;
    private Location destination;
    static Button continueBtn;
    static Button cancelBtn;
    static ProgressBar progBarPickup2;
    String timeoutFrom = "pickup2";
    public final int ITEMDETAILS_TIMEOUT = 101;
    public final int SENTAG_NOT_FOUND = 2001, INACTIVE_SENTAG = 2002,
            SENTAG_IN_TRANSIT = 2003;
    protected VerList vl = new VerList();
    protected String barcode_num;
    protected String URL;

    Response.Listener itemDetailResponseListener = new Response.Listener<String>() {

        @Override
        public void onResponse(String response) {
            try {
                 // add it to list and displist and scanned items
                if (response == null) {
                    noServerResponse(barcode_num);
                    return;
                }

                InvApplication.timeoutType = -1;

                Item item = Serializer.deserialize(response, Item.class).get(0);

                if (item.getStatus() == ItemStatus.DOES_NOT_EXIST) {
                    barcodeDidNotExist(barcode_num);
                    updateChanges();
                    return;
                }

                vl.NUSENATE = item.getBarcode();
                vl.CDCATEGORY = item.getCommodity().getCategory();
                vl.DECOMMODITYF = item.getCommodity().getDescription();
                vl.CDLOCATTO = item.getLocation().getCdlocat();
                vl.CDLOCTYPETO = item.getLocation().getCdloctype();
                vl.ADSTREET1 = item.getLocation().getAdstreet1();
                vl.CDLOCAT = item.getLocation().getCdlocat();

                if (item.getStatus() == ItemStatus.PENDING_REMOVAL) {
                    updateChanges();
                    errorMessage(barcode_num, "Item Pending Removal.",
                            "Senate Tag# " + barcode_num +
                                    " is pending removal and cannot be moved at this time.");
                    return;
                }

                if (item.getStatus() == ItemStatus.INACTIVE) {
                    updateChanges();
                    errorMessage(
                            barcode_num,
                            "Senate Tag#: " + barcode_num
                                    + " has been Inactivated.",
                            "!!ERROR: Senate Tag#: <b>"
                                    + barcode_num
                                    + "</b>"
                                    + " has been Inactivated.<br><br>"
                                    + " <b>This item will not be recorded as being picked up!</b><br><br>"
                                    + " The <b>\""
                                    + vl.DECOMMODITYF
                                    + "\"</b>  must be brought back into the Senate Tracking System by management via the"
                                    + " \"Inventory Record Adjustment E/U\". If you physically MOVE the item please report "
                                    + " Tag# and new location to Inventory Control Mgnt.");
                    return;
                }

                if (item.getStatus() == ItemStatus.IN_TRANSIT) {
                    barcodeIntransit(vl);
                    updateChanges();
                    return;
                }

                if (origin.getCdlocat().equalsIgnoreCase(vl.CDLOCAT)) {
                    vl.CONDITION = "EXISTING";
                    playSound(R.raw.ok);
                } else if (destination.getCdlocat().equalsIgnoreCase(
                        vl.CDLOCAT)) {
                    playSound(R.raw.honk);
                    vl.DECOMMODITYF = vl.DECOMMODITYF + "\n**Already in: "
                            + vl.CDLOCAT;
                } else {
                    playSound(R.raw.warning);
                    vl.CONDITION = "DIFFERENT LOCATION";
                    vl.DECOMMODITYF = vl.DECOMMODITYF + "\n**Found in: "
                            + vl.CDLOCAT;
                }

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            status = "yes1";

            String invStatus;

            if (vl.CDLOCATTO == null || vl.CDLOCATTO.trim().length() == 0) {
                invStatus = "NOT IN SFMS";
            }
            // This is what should be expected. Trying to move the
            else if (vl.CDLOCATTO.equalsIgnoreCase(origin.getCdlocat())) {
                invStatus = vl.CONDITION;
            } else if (vl.CDLOCATTO.equalsIgnoreCase(destination.getCdlocat())) { //
                invStatus = "AT DESTINATION";
            } else {
                invStatus = "Found in: " + vl.CDLOCATTO;
            }

            // 5/24/13 BH Coded below to use InvItem Objects to display
            // the list.
            InvItem invItem = new InvItem(vl.NUSENATE, vl.CDCATEGORY, invStatus,
                    vl.DECOMMODITYF, vl.CDLOCAT);
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

            scannedItems.add(invItem);
            updateChanges();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pickup2);
        registerBaseActivityReceiver();
        AppSingleton.getInstance(this).timeoutFrom = "pickup2";

        URL = AppProperties.getBaseUrl();

        origin = Serializer.deserialize(getIntent().getStringExtra("origin"), Location.class).get(0);
        destination = Serializer.deserialize(getIntent().getStringExtra("destination"), Location.class).get(0);
        pickedUpItemsLV = (ListView) findViewById(R.id.listView1);
        senateTagTV = (ClearableEditText) findViewById(R.id.etNusenate);
        senateTagTV.addTextChangedListener(senateTagTextWatcher);
        pickupCount = 0;
        adapter = new InvListViewAdapter(this, R.layout.invlist_item,
                scannedItems);
        pickedUpItemsLV.setAdapter(adapter);
        progBarPickup2 = (ProgressBar) findViewById(R.id.progBarPickup2);
        pickupCountTV = (TextView) findViewById(R.id.tv_count_pickup2);
        pickupCountTV.setText(Integer.toString(pickupCount));
        originSummary = (TextView) findViewById(R.id.tv_origin_pickup2);
        originSummary.setText(origin.getAdstreet1());
        destinationSummary = (TextView) findViewById(R.id.tv_destination_pickup2);
        destinationSummary.setText(destination.getAdstreet1());
        continueBtn = (Button) findViewById(R.id.btnPickup2Cont);
        continueBtn.getBackground().setAlpha(255);
        cancelBtn = (Button) findViewById(R.id.btnPickup2Cancel);
        cancelBtn.getBackground().setAlpha(255);

        try {
            Pickup1.progBarPickup1.setVisibility(View.INVISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        continueBtn = (Button) findViewById(R.id.btnPickup2Cont);
        continueBtn.getBackground().setAlpha(255);
        cancelBtn = (Button) findViewById(R.id.btnPickup2Cancel);
        cancelBtn.getBackground().setAlpha(255);
        if (progBarPickup2 == null) {
            progBarPickup2 = (ProgressBar) this
                    .findViewById(R.id.progBarPickup2);
        }
        progBarPickup2.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private final TextWatcher senateTagTextWatcher = new TextWatcher() {

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
            if (senateTagTV.getText().toString().length() >= 6) {
                barcode_num = senateTagTV.getText().toString().trim();
                int flag = 0;
                boolean barcodeFound = false;

                // If the item is already scanned then display a
                // toster"Already Scanned"
                if (findBarcode(barcode_num) > -1) { // TODO: findBarcode() call
                    // display toster
                    barcodeFound = true;
                    Context context = getApplicationContext();
                    CharSequence text = "Already Scanned  ";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }

                // if it is not already scanned and does not exist in the
                // list(location)
                // then add it to list and append new item to its description
                if ((flag == 0) && (barcodeFound == false)) {
                    getItemDetails();
                }

                // notify the adapter that the data in the list is changed and
                // refresh the view
                updateChanges();
            }
        }
    };

    public void updateChanges() {
        adapter.notifyDataSetChanged();
        pickupCount = scannedItems.size();
        pickupCountTV.setText(Integer.toString(pickupCount));
        pickedUpItemsLV.setAdapter(adapter);
        try {
            senateTagTV.setText("");
        } catch (NullPointerException e) { // TODO: when does senateTagTV not
            // getInstance initialized??
            senateTagTV = (ClearableEditText) findViewById(R.id.etNusenate);
            e.printStackTrace();
        }
    }

    public void barcodeDidNotExist(final String barcode_num) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle(Html
                .fromHtml("<font color='#000055'>Senate Tag#: " + barcode_num
                        + " DOES NOT EXIST IN SFMS</font>"));

        playSound(R.raw.error);
        // set dialog message
        alertDialogBuilder
                .setMessage(
                        Html.fromHtml("!!ERROR: Senate Tag#: <b>"
                                + barcode_num
                                + "</b> does not exist in SFMS. <b>This item WILL NOT be recorded as being PICKED UP!</b> "
                                + "If you physically MOVE the item please report the original location, intended new "
                                + "location and a detailed description of the item to Inventory Control Mgnt."))
                .setCancelable(false)
                .setPositiveButton(Html.fromHtml("<b>OK</b>"), new DialogInterface.OnClickListener() {
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

                        senateTagTV.setText("");

                        dialog.dismiss();

                    }
                })
        /*
         * .setNegativeButton(Html.fromHtml("<b>No</b>"), new DialogInterface.OnClickListener() {
         *
         * @Override public void onClick(DialogInterface dialog, int id) { // if
         * this button is clicked, just close // the dialog box and do nothing
         * Context context = getApplicationContext();
         *
         * CharSequence text = "Barcode#: " + barcode_num + " was NOT added";
         * int duration = Toast.LENGTH_SHORT;
         *
         * Toast toast = Toast.makeText(context, text, duration);
         * toast.setGravity(Gravity.CENTER, 0, 0); toast.show();
         *
         * etNusenate.setText("");
         *
         * dialog.dismiss(); } })
         */;

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public void barcodeIntransit(final VerList vl) {
        playSound(R.raw.error);
        new MsgAlert(
                this,
                "Senate Tag#: " + vl.NUSENATE + " IS ALREADY IN TRANSIT",
                "Senate Tag#: <b>"
                        + vl.NUSENATE
                        + "   "
                        + vl.DECOMMODITYF
                        + "</b> has  already been picked up and was marked as <font color='RED'><b>IN TRANSIT</b></font> and cannot be picked up.");
        senateTagTV.setText("");
    }

    public void noServerResponse(final String barcode_num) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle(Html
                .fromHtml("<font color='#000055'>Senate Tag#: " + barcode_num
                        + " DOES NOT EXIST IN SFMS</font>"));
        playSound(R.raw.error);
        // set dialog message
        alertDialogBuilder
                .setMessage(
                        Html.fromHtml("!!ERROR: There was <font color='RED'><b>NO SERVER RESPONSE</b></font>. Senate Tag#:<b>"
                                + barcode_num
                                + "</b> will be <b>IGNORED</b>.<br/> Please contact STS/BAC."))
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

                        senateTagTV.setText("");

                        dialog.dismiss();
                    }
                });
        new HttpUtils().playSound(R.raw.noconnect);

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
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

                        senateTagTV.setText("");

                        dialog.dismiss();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public int findBarcode(String barcode_num) {
        for (int x = 0; x < scannedItems.size(); x++) {

            if (scannedItems.get(x) != null && scannedItems.get(x).getNusenate() != null && scannedItems.get(x).getNusenate().equals(barcode_num)) {
                return x;
            }
        }
        return -1;
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

    public ArrayList<InvItem> getInvItemArrayList(ArrayList<String> invList) {
        ArrayList<InvItem> returnArray = new ArrayList<InvItem>();
        if (invList != null) {
            for (int x = 0; x < invList.size(); x++) {
                String curInvJson = invList.get(x);
                InvItem currentInvItem = new InvItem();
                currentInvItem.parseJSON(curInvJson);
                returnArray.add(currentInvItem);
            }
        }

        return returnArray;
    }

    // 3/15/13 Work in progress. Not fully implemented yet
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        savedInstanceState
                .putString("savedOriginLoc", origin.getAdstreet1());
        savedInstanceState.putString("savedDestLoc",
                destination.getAdstreet1());
        savedInstanceState.putStringArrayList("savedScannedItems",
                getJSONArrayList(scannedItems));
    }

    // 3/15/13 Work in progress. Not fully implemented yet
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        origin.setAdstreet1(savedInstanceState.getString("savedOriginLoc"));
        scannedItems = getInvItemArrayList(savedInstanceState
                .getStringArrayList("savedScannedItems"));
        TextView TextView2 = (TextView) findViewById(R.id.textView2);
        TextView2.setText("Origin : " + origin.getAdstreet1() + "\n"
                + "Destination : " + destination.getAdstreet1());

    }

    public void continueButton(View view) {
        if (scannedItems.size() < 1) {
            CharSequence text = "!!ERROR: You must first scan an item to pickup";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(this, text, duration);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }

        progBarPickup2.setVisibility(View.VISIBLE);
        continueBtn.getBackground().setAlpha(70);
        Intent intent = new Intent(this, Pickup3.class);
        Transaction trans = new Transaction();
        trans.setOrigin(origin);
        trans.setDestination(destination);
        trans.setPickupItems(scannedItems);
        intent.putExtra("pickup", Serializer.serialize(trans));
        startActivity(intent);
        overridePendingTransition(R.anim.in_right, R.anim.out_left);
    }

    public void cancelButton(View view) {
        //if (checkServerResponse(true) == OK) {
        cancelBtn.getBackground().setAlpha(70);
        finish();
        overridePendingTransition(R.anim.in_left, R.anim.out_right);
        //}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_pickup2, menu);
        return true;
    }

    public class VerList {
        String NUSENATE;
        String CDCATEGORY;
        String DECOMMODITYF;
        String CDLOCATTO;
        String CDLOCTYPETO;
        String ADSTREET1;
        String DTISSUE;
        String CDLOCAT;
        String CDINTRANSIT;
        String CONDITION;
        String CDSTATUS;
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
            case ITEMDETAILS_TIMEOUT:
                if (resultCode == RESULT_OK) {
                    if (getItemDetails() != SERVER_SESSION_TIMED_OUT) {
                        updateChanges();
                    }
                }
                break;
        }
    }

    public int getItemDetails() {
        String barcode_num = senateTagTV.getText().toString().trim();
        String barcode_number = barcode_num;
        VerList vl = new VerList();

        // check network connection
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        status = "yes";
        //  Volley recommends to use asynchronized execute but due to time constraints, used future in Volley.
        //  Future is a synchronized request that waits for the result. It was used here to minimize the coding changes.
        //  At a future date, this code should be changed to use Volley Asynchronized methods, (examples are in Search Activity and Verification)

        /// 11/20/19  --- HERE (TESTING)  -- Below freezes

        //  RequestFuture<String> future = RequestFuture.newFuture();
        //  StringInvRequest req = new StringInvRequest(Request.Method.GET, URL +  "Item?barcode=" + barcode_num, null, future, future);

        //  RequestFuture<JSONObject> future = RequestFuture.newFuture();
        //  JsonInvObjectRequest req = new JsonInvObjectRequest(URL + "Item?barcode=" + barcode_num, new JSONObject(), future, future);
        /* Add your Requests to the RequestQueue to execute */
        //  AppSingleton.getInstance(InvApplication.getAppContext()).addToRequestQueue(req);

        InvApplication.timeoutType =  ITEMDETAILS_TIMEOUT;

        StringInvRequest stringInvRequest = new StringInvRequest(Request.Method.GET, URL + "Item?barcode=" + barcode_num, null, itemDetailResponseListener);

        /* Add your Requests to the RequestQueue to execute */
        AppSingleton.getInstance(InvApplication.getAppContext()).addToRequestQueue(stringInvRequest);

        return 0;
    }

}
