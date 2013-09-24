package gov.nysenate.inventory.android;

import android.widget.Filter;

import gov.nysenate.inventory.model.InvSerialNumber;

import java.util.ArrayList;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class SearchActivity extends SenateActivity
{
    ClearableEditText barcode;
    String res = null;
    public String status = null;
    TextView textView;
    TextView tvBarcode;
    TextView tvDescription;
    TextView tvLocation;
    TextView tvCategory;
    TextView tvDateInvntry;
    TextView tvCommodityCd;
    ClearableAutoCompleteTextView acNuserial;
    Spinner spinSearchBy;

    static Button btnSrchBck;
    Activity currentActivity;
    
    ArrayList<InvSerialNumber> serialList = new ArrayList<InvSerialNumber>();

    String URL = ""; // this will be initialized once in onCreate() and used for
    // all server calls.    
    
    ArrayAdapter serialListAdapter;
    
    String timeoutFrom = "search";
    public final int SEARCH_TIMEOUT = 101, SERIALLIST_TIMEOUT = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        tvDescription = (TextView) findViewById(R.id.tvDescription);
        tvLocation = (TextView) findViewById(R.id.tvLocation);
        tvDateInvntry = (TextView) findViewById(R.id.tvDateInvntry);
        tvCategory = (TextView) findViewById(R.id.tvCategory);
        tvCommodityCd = (TextView) findViewById(R.id.tvCommodityCd);

        btnSrchBck = (Button) findViewById(R.id.btnSrchBck);
        btnSrchBck.getBackground().setAlpha(255);
        
        acNuserial = (ClearableAutoCompleteTextView) findViewById(R.id.acNuserial);
        acNuserial.setOnItemClickListener (new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                InvSerialNumber selected = (InvSerialNumber) arg0.getAdapter().getItem(arg2);
                Toast.makeText(SearchActivity.this,
                        "Clicked " + arg2 + " name: " + selected.getNusenate(),
                        Toast.LENGTH_SHORT).show();
            }
        });  
        
        getSerialList();
        acNuserial.setThreshold(2);
        spinSearchBy = (Spinner) findViewById(R.id.spinSearchBy);
        
        spinSearchBy.setOnItemSelectedListener(
                new OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> arg0, View arg1,
                            int arg2, long arg3) {
                        String selectedValue = (String) spinSearchBy.getItemAtPosition(arg2);
                        Log.i("Search By Change", "VALUE:"+selectedValue);
                        if (selectedValue.equalsIgnoreCase("By Serial#")) {
                            Log.i("Search By Change", "Changed to Serial");
                            barcode.setVisibility(View.GONE);
                            acNuserial.setVisibility(View.VISIBLE);
                        }
                        else {
                            Log.i("Search By Change", "Changed to Senate Tag#");
                            acNuserial.setVisibility(View.GONE);
                            barcode.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                        // TODO Auto-generated method stub
                        
                    }
                });

        // Suppress the Menu ProgressBar
        MenuActivity.progBarMenu.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (btnSrchBck == null) {
            btnSrchBck = (Button) findViewById(R.id.btnSrchBck);
        }
        btnSrchBck.getBackground().setAlpha(255);
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
                getSearchDetails();

            }
        }
    };
    
    
    public void getSerialList() {
        status = "yes";

        // Get the URL from the properties
        URL = LoginActivity.properties.get("WEBAPP_BASE_URL").toString();

        AsyncTask<String, String, String> resr1 = new RequestTask()
                .execute(URL + "/SerialList");

        try {

            // code for JSON
            try {
                res = null;
                res = resr1.get().trim().toString();
                if (res == null) {
                    noServerResponse();
                    return;
                } else if (res.indexOf("Session timed out") > -1) {
                    startTimeout(SERIALLIST_TIMEOUT);
                    return;
                }
            } catch (NullPointerException e) {
                noServerResponse();
                return;
            }
            String jsonString = resr1.get().trim().toString();

            JSONArray jsonArray = new JSONArray(jsonString);
            // this will populate the lists from the JSON array coming from
            // server
            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject jo = new JSONObject();
                jo = jsonArray.getJSONObject(i);
                InvSerialNumber invSerialNumber = new InvSerialNumber();
                invSerialNumber.setNuxrefsn( jo.getString("nuxrefsn"));
                invSerialNumber.setNuserial(jo.getString("nuserial"));
                invSerialNumber.setNusenate(jo.getString("nusenate"));
                invSerialNumber.setCdcommodity(jo.getString("cdcommodity"));
                invSerialNumber.setDecommodityf(jo.getString("decommodityf"));
                if (invSerialNumber.getNuserial()==null) {
                    Log.i("ADD SERIAL", "ADDING NUSERIAL IS NULL");
                }
                serialList.add(invSerialNumber);
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
        serialListAdapter = new ArrayAdapter(this,R.layout.row_serialitem, serialList) {
            private  ArrayList<InvSerialNumber> suggestions;            
            class ViewHolder
            {
                RelativeLayout rlSerialRow;
                // TextView commodityListNucnt;
                TextView tvNuserial;
                TextView tvNusenate;
                TextView tvDecommodityf;
            }            
            @Override
            public View getView(final int position, View convertView, final ViewGroup parent) {
                ViewHolder holder = null;
                InvSerialNumber rowItem = null;
                if (convertView == null) {
                    final LayoutInflater mInflater = (LayoutInflater) context
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                    convertView = mInflater.inflate(R.layout.row_serialitem, null);
                    holder = new ViewHolder();
                    holder.rlSerialRow = (RelativeLayout) convertView
                            .findViewById(R.id.rlSerialRow);
                    /*
                     * holder.commodityListNucnt = (TextView) convertView
                     * .findViewById(R.id.commodityListNucnt);
                     */
                    holder.tvNuserial = (TextView) convertView
                            .findViewById(R.id.tvNuserial);
                    holder.tvNusenate = (TextView) convertView
                            .findViewById(R.id.tvNusenate);
                    holder.tvDecommodityf = (TextView) convertView
                            .findViewById(R.id.tvDecommodityf);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
                
                
                if (position > -1 && serialList != null && position < serialList.size()) {
                    rowItem = serialList.get(position);
                    // holder.commodityListNucnt.setText(rowItem.getNucnt());
                   /* if (holder.tvNuserial==null) {
                        Log.i("holder.tvNuserial", "IS NULL!!!");
                    }
                    else {
                        Log.i("holder.tvNuserial", "IS NOT NULL");
                    }
                    
                    if (rowItem==null) {
                        Log.i("rowItem", "IS NULL!!!");
                    }
                    else {
                        Log.i("rowItem", "IS NOT NULL");
                    }
                    if (rowItem.getNuserial()==null) {
                        Log.i("rowItem.getNuSerial()", "IS NULL!!!");
                    }
                    else {
                        Log.i("rowItem.getNuSerial()", "IS NOT NULL");
                    }
                    if (rowItem.getNusenate() ==null) {
                        Log.i("rowItem.getNusenate()", "IS NULL!!!");
                    }
                    else {
                        Log.i("rowItem.getNusenate()", "IS NOT NULL");
                    }
                    Log.i("rowitem NUSENATE",rowItem.getNusenate() );*/
                    holder.tvNusenate.setText(Html.fromHtml("<b>T: "+rowItem.getNusenate()+"</b>"));
                    holder.tvDecommodityf.setText(Html.fromHtml(rowItem.getDecommodityf()));
                    holder.tvNuserial.setText(Html.fromHtml("<b>S: "+rowItem.getNuserial()+"</b>"));
                    holder.tvNusenate.setTextColor(context.getResources()
                            .getColor(R.color.black));
                    holder.tvDecommodityf.setTextColor(context.getResources()
                            .getColor(R.color.black));
                    holder.tvNuserial.setTextColor(context.getResources()
                            .getColor(R.color.black));
                    
                } else {
                    // holder.commodityListNucnt.setText("");
                    holder.tvNuserial.setText("");
                    holder.tvNusenate.setText("");
                    holder.tvDecommodityf.setText("");                }

                if (position % 2 > 0) {
                    holder.rlSerialRow.setBackgroundColor(context.getResources()
                            .getColor(R.color.white));
                } else {
                    holder.rlSerialRow.setBackgroundColor(context.getResources()
                            .getColor(R.color.blueveryverylight));
                }

                return convertView;
            }
            
            @Override
            public Filter  getFilter() {
                return  nameFilter;
            }

            Filter nameFilter = new Filter() {
                public String convertResultToString(Object resultValue) {
                    String str = ((InvSerialNumber)(resultValue)).getNusenate(); 
                    return str;
                }
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    if(constraint != null) {
                        if (suggestions==null) {
                            suggestions = new ArrayList<InvSerialNumber>();
                        }
                        else {
                            suggestions.clear();
                        }
                        for (InvSerialNumber invSerialNumber : serialList) {
                            if(invSerialNumber.getNuserial().startsWith(constraint.toString().toLowerCase())){
                                suggestions.add(invSerialNumber);
                            }
                        }
                        FilterResults filterResults = new FilterResults();
                        filterResults.values = suggestions;
                        filterResults.count = suggestions.size();
                        return filterResults;
                    } else {
                        return new FilterResults();
                    }
                }
                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    ArrayList<InvSerialNumber> filteredList = (ArrayList<InvSerialNumber>) results.values;
                    if(results != null && results.count > 0) {
                        clear();
                        for (InvSerialNumber invSN : filteredList) {
                            add(invSN);
                        }
                        notifyDataSetChanged();
                    }
                }

            };            
        };
        acNuserial.setAdapter(serialListAdapter);

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

    public void getSearchDetails() {
        try {
            String barcode_num = barcode.getText().toString().trim();
            Log.i("Activity Search afterTextChanged ", "Senate Tag#"
                    + barcode_num);
            // check network connection
            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                // fetch data
                status = "yes";
                // Get the URL from the properties
                String URL = LoginActivity.properties.get("WEBAPP_BASE_URL")
                        .toString();
                Log.i("Activity Search afterTextChanged ", "URL " + URL);
                AsyncTask<String, String, String> resr1 = new RequestTask()
                        .execute(URL + "/Search?barcode_num=" + barcode_num);
                try {
                    res = null;
                    res = resr1.get().trim().toString();
                    Log.i("Search res ", "res:" + res);
                    if (res == null) {
                        noServerResponse();
                        return;
                    } else if (res.indexOf("Session timed out") > -1) {
                        startTimeout(SEARCH_TIMEOUT);
                        return;
                    }

                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    noServerResponse();
                    return;
                }
                status = "yes1";
            } else {
                // display error
                status = "no";
            }
            if (res.toUpperCase().contains("DOES NOT EXIST IN SYSTEM")) {
                tvBarcode.setText(barcode.getText().toString()
                        + " - !!ERROR: DOES NOT EXIST.");
                int color = Integer.parseInt("bb0000", 16) + 0xFF000000;
                tvBarcode.setTextColor(color);
                tvDescription.setText("N/A");
                tvCategory.setText("N/A");
                tvLocation.setText("N/A");
                tvDateInvntry.setText("N/A");
                tvCommodityCd.setText("N/A");

            } else {
                int color = Integer.parseInt("000000", 16) + 0xFF000000;
                tvBarcode.setTextColor(color);
                try {
                    JSONObject object = (JSONObject) new JSONTokener(res)
                            .nextValue();
                    StringBuilder nusenateMsg = new StringBuilder();
                    nusenateMsg.append(object.getString("nusenate"));
                    String cdstatus = object.getString("cdstatus");
                    Log.i("TEST", "CDSTATUS:(" + cdstatus + ")");
                    if (cdstatus.equalsIgnoreCase("I")) {
                        nusenateMsg.append(" <font color='RED'>(INACTIVE) ");
                        nusenateMsg.append(object.getString("deadjust"));
                        Log.i("TEST", "INACTIVE CDSTATUS:(" + cdstatus + ")");
                    }

                    Log.i("TEST", "Senate Tag#:" + nusenateMsg);
                    tvBarcode.setText(Html.fromHtml(nusenateMsg.toString()));

                    tvDescription.setText(object.getString("decommodityf")
                            .replaceAll("&#34;", "\""));
                    tvCategory.setText(object.getString("cdcategory"));
                    tvLocation.setText(object.getString("cdlocatto")
                            + " ("
                            + object.getString("cdloctypeto")
                            + ") "
                            + object.getString("adstreet1to").replaceAll(
                                    "&#34;", "\""));
                    tvDateInvntry.setText(object.getString("dtlstinvntry"));
                    tvCommodityCd.setText(object.getString("commodityCd"));

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    tvDescription.setText("!!ERROR: " + e.getMessage());
                    tvCategory.setText("Please contact STS/BAC.");
                    tvLocation.setText("N/A");
                    tvDateInvntry.setText("N/A");
                    tvCommodityCd.setText("N/A");

                    e.printStackTrace();
                }
            }
            // textView.setText("\n" + res);
            barcode.setText("");
        } catch (Exception e) {
            tvDescription.setText("!!ERROR: " + e.getMessage());
            tvCategory.setText("Please contact STS/BAC.");
            tvLocation.setText("N/A");
            tvDateInvntry.setText("N/A");
            tvCommodityCd.setText("N/A");
        }
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
        if (view.getId() == R.id.btnSrchBck) {
            btnSrchBck.getBackground().setAlpha(45);
            // this.finish();// close the current activity
            Intent intent = new Intent(this, MenuActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.in_left, R.anim.out_right);
        }

    }

    public void cancelButton(View view) {
        this.finish();
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);

    }

    /*
     * @Override public void onBackPressed() { super.onBackPressed();
     * overridePendingTransition(R.anim.in_left, R.anim.out_right); }
     */

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
                getSearchDetails();
                break;
            }
        }
    }

}
