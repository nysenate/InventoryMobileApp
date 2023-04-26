package gov.nysenate.inventory.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.RequestFuture;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import gov.nysenate.inventory.adapter.InvSerialAdapter;
import gov.nysenate.inventory.adapter.LocHistAdapter;
import gov.nysenate.inventory.android.AppSingleton;
import gov.nysenate.inventory.android.ClearableAutoCompleteTextView;
import gov.nysenate.inventory.android.ClearableEditText;
import gov.nysenate.inventory.android.InvApplication;
import gov.nysenate.inventory.android.JsonInvObjectRequest;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.android.StringInvRequest;
import gov.nysenate.inventory.dto.ItemInventoriedDetails;
import gov.nysenate.inventory.model.InvSerialNumber;
import gov.nysenate.inventory.model.Item;
import gov.nysenate.inventory.model.ItemStatus;
import gov.nysenate.inventory.model.Location;
import gov.nysenate.inventory.util.AppProperties;
import gov.nysenate.inventory.util.HttpUtils;
import gov.nysenate.inventory.util.Serializer;
import gov.nysenate.inventory.util.Toasty;

public class SearchActivity extends SenateActivity {
    ClearableEditText barcode;
    String res = null;
    public String status = null;
    TextView textView;
    TextView tvBarcode;
    TextView tvNuserial;
    TextView tvDescription;
    TextView tvLocation;
    TextView tvCategory;
    TextView tvDateInvntry;
    TextView tvCommodityCd;
    ListView lvLocationHistory;
    ClearableAutoCompleteTextView acNuserial;
    String URL = ""; // this will be initialized once in onCreate() and used for
    Spinner spinSearchBy;
    int serialLength = 0;
    boolean serialListNeeded = true;
    private ArrayList<InvSerialNumber> suggestions;
    LocHistAdapter locHistAdapter;

    TableRow rwNuserial;
    InputFilter invSerialFilter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence arg0, int arg1, int arg2, Spanned arg3, int arg4, int arg5) {
            for (int k = arg1; k < arg2; k++) {
                if (k > 0 && arg0.charAt(k) == ',' && acNuserial.getText().toString().endsWith(",")) {
                    return "";
                } else if (!Character.isLetterOrDigit(arg0.charAt(k)) && arg0.charAt(k) != '-' && arg0.charAt(k) != '/' && arg0.charAt(k) != '\\'/* && arg0.charAt(k) != '.' && arg0.charAt(k) != ','*/) {
                    return "";
                }
            }
            return null;
        }
    };

    public void searchBarcode(ItemInventoriedDetails itemInventoriedDetails) {
        if (itemInventoriedDetails == null || itemInventoriedDetails.getItem() == null) {
            updateChanges(null, true);
            noServerResponse();
            return;
        }

        if (itemInventoriedDetails.getItem().getStatus() == ItemStatus.DOES_NOT_EXIST) {
            tvBarcode.setText(barcode.getText().toString() + " - !!ERROR: DOES NOT EXIST.");
            tvBarcode.setTextColor(Integer.parseInt("bb0000", 16) + 0xFF000000);
            tvNuserial.setText("N/A");
            tvDescription.setText("N/A");
            tvCategory.setText("N/A");
            tvLocation.setText("N/A");
            tvDateInvntry.setText("N/A");
            tvCommodityCd.setText("N/A");
            rwNuserial.setVisibility(View.VISIBLE);
            updateChanges(null, true);
        } else {
            Item item = itemInventoriedDetails.getItem();
            updateChanges(item, true);
            tvBarcode.setTextColor(Integer.parseInt("000000", 16) + 0xFF000000);
            String message = item.getBarcode();
            if (item.getStatus() == ItemStatus.INACTIVE) {
                message += " <font color='RED'>(INACTIVE) ";
                message += item.getAdjustCode().getDescription();
            }
            tvBarcode.setText(Html.fromHtml(message));
            if (item.getSerialNumber() == null) {
                rwNuserial.setVisibility(View.GONE);
            } else {
                rwNuserial.setVisibility(View.VISIBLE);
                tvNuserial.setText(item.getSerialNumber());
            }
            tvDescription.setText(item.getCommodity().getDescription());
            tvCategory.setText(item.getCommodity().getCategory());
            tvLocation.setText(item.getLocation().getCdlocat()
                    + " (" + item.getLocation().getCdloctype() + " )"
                    + item.getLocation().getAdstreet1());
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
            tvDateInvntry.setText(sdf.format(itemInventoriedDetails.getLastInventoried()));
            tvCommodityCd.setText(item.getCommodity().getCode());
        }
        barcode.setText("");
    }

    static Button btnSrchBck;
    Activity currentActivity;

    protected ArrayList<InvSerialNumber> serialList = new ArrayList<InvSerialNumber>();
    //protected ArrayList<InvSerialNumber> serialListOrg;
    // all server calls.

    InvSerialAdapter serialListAdapter;

    String timeoutFrom = "search";
    public final int SEARCH_TIMEOUT = 101, SERIALLIST_TIMEOUT = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppSingleton.getInstance(this).timeoutFrom = "search";

        // Get the URL from the properties
        URL = AppProperties.getBaseUrl(this); //     LoginActivity.properties.get("WEBAPP_BASE_URL").toString();

        if (!URL.endsWith("/")) {
            URL += "/";
        }

        setContentView(R.layout.activity_search);
        registerBaseActivityReceiver();
        currentActivity = this;

        barcode = (ClearableEditText) findViewById(R.id.barcode);
        barcode.addTextChangedListener(filterTextWatcher);// Adding Listener
        // to
        // barcode field
        textView = (TextView) findViewById(R.id.textView1);
        // Setup Textviews used to display Data...
        tvBarcode = (TextView) findViewById(R.id.tvBarcode);
        tvNuserial = (TextView) findViewById(R.id.tvNuserial);
        tvDescription = (TextView) findViewById(R.id.tvDescription);
        tvLocation = (TextView) findViewById(R.id.tvLocation);
        tvDateInvntry = (TextView) findViewById(R.id.tvDateInvntry);
        tvCategory = (TextView) findViewById(R.id.tvCategory);
        tvCommodityCd = (TextView) findViewById(R.id.tvCommodityCd);
        rwNuserial = (TableRow) findViewById(R.id.rwNuserial);
        lvLocationHistory = (ListView) findViewById(R.id.lvLocationHistory);

        btnSrchBck = (Button) findViewById(R.id.btnSrchBck);
        btnSrchBck.getBackground().setAlpha(255);

        // Initialize Location History to null

        updateChanges(null, true);

        acNuserial = (ClearableAutoCompleteTextView) findViewById(R.id.acNuserial);
        acNuserial.setFilters(new InputFilter[]{invSerialFilter});
        acNuserial.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                InvSerialNumber selected = (InvSerialNumber) arg0.getAdapter().getItem(arg2);
                barcode.setText(selected.getNusenate());
            }
        });

        acNuserial.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                // call your adapter here
                String st = s.toString();
                int recordCount = 0;
                boolean serialListfromServer = false;
                if (serialListNeeded || serialLength > st.length()) {
                    if (serialLength > st.length()) {
                        serialListAdapter.setTextColor(false);
                    }
                    recordCount = getSerialList(st);
                    serialListfromServer = true;
                }

                serialListAdapter.getFilter().filter(st);
                if (serialListfromServer) {
                    //System.out.println(st+" FROM SERVER COUNT:"+recordCount);
                    serialListAdapter.setTextColor(false);
                    if (recordCount == 0) {
                        acNuserial.setTextColor(getResources().getColor(R.color.redlight));
                    } else {
                        acNuserial.setTextColor(getResources().getColor(R.color.black));
                    }
                } else {
                    serialListAdapter.setTextColor(true);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                serialLength = acNuserial.getText().toString().length();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        acNuserial.setThreshold(3);
        spinSearchBy = (Spinner) findViewById(R.id.spinSearchBy);
        String[] spinnerList = getResources().getStringArray(
                R.array.search_searchby);
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<String>(this,
                R.layout.spinner22_item, spinnerList);
        adapterSpinner
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinSearchBy.setAdapter(adapterSpinner);

        spinSearchBy.setOnItemSelectedListener(
                new OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> arg0, View arg1,
                                               int arg2, long arg3) {
                        String selectedValue = (String) spinSearchBy.getItemAtPosition(arg2);
                        if (selectedValue.equalsIgnoreCase("By Serial#")) {
                            barcode.setVisibility(View.GONE);
                            acNuserial.setVisibility(View.VISIBLE);
                        } else {
                            acNuserial.setVisibility(View.GONE);
                            barcode.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                        // TODO Auto-generated method stub

                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (btnSrchBck == null) {
            btnSrchBck = (Button) findViewById(R.id.btnSrchBck);
        }
        btnSrchBck.getBackground().setAlpha(255);
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

        Response.Listener responseListener = new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                ArrayList<Item> listdata = new ArrayList<Item>();

                if (response == null) {
                    return;
                }

                status = "yes";

                ItemInventoriedDetails itemInventoriedDetails = Serializer.deserialize(response, ItemInventoriedDetails.class).get(0);

                Log.i(this.getClass().getName(), "RESPONSE(A): " + response);

                SearchActivity.this.searchBarcode(itemInventoriedDetails);

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(
                        barcode.getWindowToken(), 0);

                if (!barcode.hasFocus()) {
                    barcode.requestFocus();
                }
            }
        };

        @Override
        public void afterTextChanged(Editable s) {
            if (barcode.getText().toString().length() >= 6) {
                StringInvRequest stringInvRequest = new StringInvRequest(Request.Method.GET,
                        URL + "Item?barcode=" + barcode.getText() + "&inventoried_date=true", null, responseListener);

                /* Add your Requests to the RequestQueue to execute */
                AppSingleton.getInstance(InvApplication.getAppContext()).addToRequestQueue(stringInvRequest);
            }
        }
    };

    public int parseSerialList(JSONObject response) {
        serialList = new ArrayList<InvSerialNumber>();
        int statusNum = 0;
        int recordCount = 0;

        try {

            // code for JSON
            try {
                if (response == null) {
                    noServerResponse();
                    return -2;
                } else if (res.indexOf("Session timed out") > -1) {
                    startTimeout(SERIALLIST_TIMEOUT);
                    return -1;
                }
            } catch (NullPointerException e) {
                noServerResponse();
                return -2;
            }

            JSONArray jsonArray = new JSONArray(response.toString());
            // this will populate the lists from the JSON array coming from
            // server
            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject jo;
                jo = jsonArray.getJSONObject(i);
                statusNum = jo.getInt("statusNum");
                //System.out.println("statusNum:"+statusNum);
                if (statusNum != 0) {
                    //System.out.println("Don't look at the rest");
                    serialListNeeded = true;
                    break;
                }

                Log.i(this.getClass().getName(), "Locations: " + jo.getString("locations"));

                serialListNeeded = false;
                InvSerialNumber invSerialNumber = new InvSerialNumber();
                invSerialNumber.setNuxrefsn(jo.getString("nuxrefsn"));
                invSerialNumber.setNuserial(jo.getString("nuserial"));
                invSerialNumber.setNusenate(jo.getString("nusenate"));
                invSerialNumber.setCdcommodity(jo.getString("cdcommodity"));
                invSerialNumber.setDecommodityf(jo.getString("decommodityf"));
                invSerialNumber.setLocations(jo.getJSONArray("locations"));

                serialList.add(invSerialNumber);
            }

            // code for JSON ends
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        status = "yes1";
        serialListAdapter = new InvSerialAdapter(getApplicationContext(), acNuserial, R.layout.row_serialitem, serialList);

        Toasty toasty = new Toasty(context);

        if (statusNum > 0) {
            //toasty.showMessage("Too many results ("+statusNum+") found, please keep typing.");
        } else if (statusNum < 0) {
            toasty.showMessage("Server returned an error number of " + statusNum + " when trying to filter Serial#s. Please contact STSBAC. .");
        }

        acNuserial.setAdapter(serialListAdapter);
        if (statusNum > 0) {
            recordCount = statusNum;
        } else {
            if (serialList == null) {
                recordCount = 0;
            } else {
                recordCount = serialList.size();
            }
        }

        return recordCount;
    }

    public void updateChanges(Item item, boolean force) {
        checkAdapter(item, force);
        locHistAdapter.notifyDataSetChanged();
    }


    public void checkAdapter(Item item, boolean force) {
        List<Location> locations = null;
        if (force || locHistAdapter == null) {
            if (item == null) {
                locations = new ArrayList<Location>();
            } else {
                locations = item.getLocations();
            }
            locHistAdapter = new LocHistAdapter(this, R.layout.row_location_history, locations);
            this.lvLocationHistory.setAdapter(locHistAdapter);
        }
    }

    public int getSerialList(String nuserialPartial) {
        status = "yes";

        // Get the URL from the properties
        URL = AppProperties.getBaseUrl(this);

        if (!URL.endsWith("/")) {
            URL += "/";
        }

        int recordCount = 0;

        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonInvObjectRequest req = new JsonInvObjectRequest(URL + "SerialList?nuserial=" + nuserialPartial + "&maxResults=50", new JSONObject(), future, future);
        /* Add your Requests to the RequestQueue to execute */
        AppSingleton.getInstance(InvApplication.getAppContext()).addToRequestQueue(req);

        try {

            JSONObject response = future.get(); // this will block
            recordCount = parseSerialList(response);

        } catch (InterruptedException e) {
            // exception handling
        } catch (ExecutionException e) {
            VolleyLog.e("Error: ", e.getMessage());
            Toasty.displayCenteredMessage(SearchActivity.this, "!!ERROR: Connection problem occured.", Toast.LENGTH_SHORT);
        }

        return recordCount;
    }

    public void toggleRowVisibility(TableRow row) {

        if (row.getVisibility() == View.VISIBLE) {
            row.setVisibility(View.GONE);
        } else {
            row.setVisibility(View.VISIBLE);
        }
    }

    public void noServerResponse() {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is
        // present.
        getMenuInflater().inflate(R.menu.activity_search, menu);
        return true;
    }

    /*
     * @Override public boolean onOptionsItemSelected(MenuItem item) {
     *
     * switch (item.getItemId()) { case android.R.id.home: Toast toast =
     * Toast.makeText(getApplicationContext(), "Going Back",
     * Toast.LENGTH_SHORT); toast.setGravity(Gravity.CENTER, 0, 0);
     * toast.show(); NavUtils.navigateUpFromSameTask(this);
     *
     * overridePendingTransition(R.anim.in_left, R.anim.out_right); return true;
     * default: return super.onOptionsItemSelected(item); } }
     */

    public void okButton(View view) {
        btnSrchBck.getBackground().setAlpha(45);
        finish();
        overridePendingTransition(R.anim.in_left, R.anim.out_right);
    }

    public void cancelButton(View view) {
        this.finish();
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);

    }


     @Override public void onBackPressed() {
        new Toasty(this).showMessage("Back Button PRESSED!!!");
        super.onBackPressed();
     //overridePendingTransition(R.anim.in_left, R.anim.out_right);
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
            case SEARCH_TIMEOUT:
                if (resultCode == RESULT_OK) {
                    //if (checkServerResponse(true) == OK) {

                    if (URL == null || URL.trim().length() == 0) {
                        URL = AppProperties.getBaseUrl(this);
                    }

                    JsonInvObjectRequest jsonObjReq = new JsonInvObjectRequest(Request.Method.GET,
                            URL + "SerialList?nuserial=" + acNuserial.getText().toString() + "&maxResults=50", null, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            ItemInventoriedDetails itemInventoriedDetails = new ItemInventoriedDetails();
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(SearchActivity.this, "Response ERROR!!!!!", Toast.LENGTH_SHORT).show();
                        }
                    });

                    /* Add your Requests to the RequestQueue to execute */
                    AppSingleton.getInstance(InvApplication.getAppContext()).addToRequestQueue(jsonObjReq);
                }
        }
    }
}