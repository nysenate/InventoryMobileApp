package gov.nysenate.inventory.android;

import gov.nysenate.inventory.model.Location;
import gov.nysenate.inventory.model.Pickup;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

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
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class Pickup2Activity extends SenateActivity
{
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pickup2);
        registerBaseActivityReceiver();

        origin = getIntent().getParcelableExtra("origin");
        destination = getIntent().getParcelableExtra("destination");
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
        originSummary.setText(origin.getAddressLine1());
        destinationSummary = (TextView) findViewById(R.id.tv_destination_pickup2);
        destinationSummary.setText(destination.getAddressLine1());
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
        case R.id.menu_test_null:
            item.setChecked(!item.isChecked());
            testResNull = item.isChecked();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private final TextWatcher senateTagTextWatcher = new TextWatcher()
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
            if (senateTagTV.getText().toString().length() >= 6) {
                String barcode_num = senateTagTV.getText().toString().trim();
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
                    int returnedStatus = -1;
                    returnedStatus = getItemDetails();
                    if (returnedStatus == SERVER_SESSION_TIMED_OUT) {
                        return;
                    }
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
                                           // get initialized??
            senateTagTV = (ClearableEditText) findViewById(R.id.etNusenate);
            e.printStackTrace();
        }
    }

    public void barcodeDidNotExist(final String barcode_num) {
        Log.i("TESTING", "****Senate Tag# DidNotExist MESSAGE");
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
                .setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // 5/24/13 BH Coded below to use InvItem Objects to
                        // display
                        // the list.
                        /*
                         * VerList vl = new VerList(); vl.NUSENATE =
                         * barcode_num; vl.CDCATEGORY = ""; vl.DECOMMODITYF =
                         * " ***NOT IN SFMS***  New Item"; vl.CDINTRANSIT = "";
                         * 
                         * InvItem invItem = new InvItem(vl.NUSENATE,
                         * vl.CDCATEGORY, "NEW", vl.DECOMMODITYF, vl.CDLOCAT);
                         * invList.add(invItem);
                         * 
                         * list.add(vl); StringBuilder s_new = new
                         * StringBuilder(); // s_new.append(vl.NUSENATE); since
                         * the desc coming from // server already contains
                         * barcode number we wont add it // again //
                         * s_new.append(" "); s_new.append(vl.CDCATEGORY);
                         * s_new.append(" "); s_new.append(vl.DECOMMODITYF);
                         * 
                         * // display toster Context context =
                         * getApplicationContext(); CharSequence text = s_new;
                         * int duration = Toast.LENGTH_SHORT;
                         * 
                         * Toast toast = Toast.makeText(context, text,
                         * duration); toast.setGravity(Gravity.CENTER, 0, 0);
                         * toast.show();
                         * 
                         * // dispList.add(s_new); // this list will display the
                         * // contents // on screen scannedItems.add(invItem);
                         * allScannedItems.add(invItem);
                         * newItems.add(invItem);// to keep
                         * 
                         * list.add(vl); etNusenate.setText("");
                         * dialog.dismiss();
                         */
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
         * .setNegativeButton("No", new DialogInterface.OnClickListener() {
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

                        senateTagTV.setText("");

                        dialog.dismiss();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public void errorMessage(final String barcode_num, final String title,
            final String message) {
        Log.i("TESTING", "****errorMessgae MESSAGE");
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
            if (scannedItems.get(x).getNusenate().equals(barcode_num)) {
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
                .putString("savedOriginLoc", origin.getAddressLine1());
        savedInstanceState.putString("savedDestLoc",
                destination.getAddressLine1());
        savedInstanceState.putStringArrayList("savedScannedItems",
                getJSONArrayList(scannedItems));
    }

    // 3/15/13 Work in progress. Not fully implemented yet
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        origin.setAddressLine1(savedInstanceState.getString("savedOriginLoc"));
        scannedItems = getInvItemArrayList(savedInstanceState
                .getStringArrayList("savedScannedItems"));
        TextView TextView2 = (TextView) findViewById(R.id.textView2);
        TextView2.setText("Origin : " + origin.getAddressLine1() + "\n"
                + "Destination : " + destination.getAddressLine1());

    }

    public void continueButton(View view) {
        Log.i("continueButton", "BEFORE checkServerResponse(true)");

        if (scannedItems.size() < 1) {
            CharSequence text = "!!ERROR: You must first scan an item to pickup";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(this, text, duration);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }

        if (checkServerResponse(true) == OK) {
            progBarPickup2.setVisibility(View.VISIBLE);
            continueBtn.getBackground().setAlpha(70);
            Intent intent = new Intent(this, Pickup3.class);
            Pickup pickup = new Pickup(origin, destination);
            pickup.setPickupItems(scannedItems);
            intent.putExtra("pickup", pickup);
            startActivity(intent);
            overridePendingTransition(R.anim.in_right, R.anim.out_left);
        }
    }

    public void cancelButton(View view) {
        // send back to the Move Menu
        Log.i("cancelButton", "BEFORE checkServerResponse(true)");

        // Log.i("cancelButton",
        // "checkServerResponse(true):"+checkServerResponse(true) );
        if (checkServerResponse(true) == OK) {
            cancelBtn.getBackground().setAlpha(70);
            Intent intent = new Intent(this, Move.class);
            startActivity(intent);
            overridePendingTransition(R.anim.in_left, R.anim.out_right);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_pickup2, menu);
        return true;
    }

    public class VerList
    {
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
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // fetch data
            status = "yes";
            // int barcode= Integer.parseInt(barcode_num);
            // scannedItems.add(barcode);
            // Get the URL from the properties
            String URL = LoginActivity.properties.get("WEBAPP_BASE_URL")
                    .toString();

            AsyncTask<String, String, String> resr1 = new RequestTask()
                    .execute(URL + "/ItemDetails?barcode_num=" + barcode_num);
            System.out.println("URL CALL:" + URL + "/ItemDetails?barcode_num="
                    + barcode_num);
            try {
                res = null;
                res = resr1.get().trim().toString();
                if (testResNull) { // Testing Purposes Only
                    res = null;
                    resr1 = null;
                    Log.i("TEST RESNULL", "RES SET TO NULL");
                }
                System.out.println("URL RESULT:" + res);

                // add it to list and displist and scanned items
                JSONObject object = null;
                if (res == null) {
                    // Log.i("TESTING", "A CALL noServerResponse");
                    noServerResponse(barcode_num);
                    return NO_SERVER_RESPONSE;
                } else if (res.toUpperCase().contains(
                        "DOES NOT EXIST IN SYSTEM")) {
                    // Log.i("TESTING",
                    // "A CALL barcodeDidNotExist");
                    barcodeDidNotExist(barcode_num);
                    return SENTAG_NOT_FOUND;
                } else if (res.indexOf("Session timed out") > -1) {
                    startTimeout(ITEMDETAILS_TIMEOUT);
                    return SERVER_SESSION_TIMED_OUT;
                } else {
                    try {
                        object = (JSONObject) new JSONTokener(res).nextValue();
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    vl.NUSENATE = barcode_number;
                    vl.CDCATEGORY = object.getString("cdcategory");
                    vl.DECOMMODITYF = object.getString("decommodityf")
                            .replaceAll("&#34;", "\"");
                    vl.CDLOCATTO = object.getString("cdlocatto");
                    vl.CDLOCTYPETO = object.getString("cdloctypeto");
                    vl.ADSTREET1 = object.getString("adstreet1to").replaceAll(
                            "&#34;", "\"");
                    vl.DTISSUE = object.getString("dtissue");
                    vl.CDLOCAT = object.getString("cdlocatto");
                    vl.CDINTRANSIT = object.getString("cdintransit");
                    vl.CDSTATUS = object.getString("cdstatus");

                    if (vl.CDSTATUS.equalsIgnoreCase("I")) {
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
                        return INACTIVE_SENTAG;
                    }

                    if (vl.CDINTRANSIT != null
                            && vl.CDINTRANSIT.equalsIgnoreCase("Y")) {
                        barcodeIntransit(vl);
                        return SENTAG_IN_TRANSIT;
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
                }

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
        return OK;
    }

}
