package gov.nysenate.inventory.android;

import gov.nysenate.inventory.model.InvSerialAdapter;
import gov.nysenate.inventory.model.InvSerialNumber;
import gov.nysenate.inventory.model.Toasty;
import gov.nysenate.inventory.util.ClearableAutoCompleteTextView;
import gov.nysenate.inventory.util.ClearableEditText;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import javax.xml.transform.Result;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class SearchActivity extends SenateActivity
{
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
    ClearableAutoCompleteTextView acNuserial;
    Cursor serialCursor;
    Spinner spinSearchBy;
    int serialLength = 0;
    boolean serialListNeeded = true;
    private  ArrayList<InvSerialNumber> suggestions;      
    TableRow rwNuserial;
    boolean nuserialLoadDone = false;
    InputFilter invSerialFilter = new InputFilter() {   
        @Override  
        public CharSequence filter(CharSequence arg0, int arg1, int arg2, Spanned arg3, int arg4, int arg5)  
            {  
                 for (int k = arg1; k < arg2; k++) {
                     if (k>0  && arg0.charAt(k) == ',' && acNuserial.getText().toString().endsWith(",") ) {
                         return "";   
                     }
                    /* else if (Character.isSpaceChar(arg0.charAt(k))) {
                         return ",";   
                     }*/
                     else if (!Character.isLetterOrDigit(arg0.charAt(k)) && arg0.charAt(k) != '-' && arg0.charAt(k) != '/' && arg0.charAt(k) != '\\'/* && arg0.charAt(k) != '.' && arg0.charAt(k) != ','*/) {   
                         return "";   
                     }   
                 }   
             return null;   
            }   
    };     
    
    AsyncTask<String, String, String> nuserialResponse = new RequestTask(){
        @Override
        public void onPreExecute() {
            
        }
        public void onPostExecute(Result result) {
            
        }
        
    };
    
    //SimpleCursorAdapter serialAdapter;

    static Button btnSrchBck;
    Activity currentActivity;
    
   
    protected ArrayList<InvSerialNumber> serialList = new ArrayList<InvSerialNumber>();
    //protected ArrayList<InvSerialNumber> serialListOrg;
    String URL = ""; // this will be initialized once in onCreate() and used for
    // all server calls.    
    
    InvSerialAdapter serialListAdapter;
    
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
        tvNuserial = (TextView) findViewById(R.id.tvNuserial);
        tvDescription = (TextView) findViewById(R.id.tvDescription);
        tvLocation = (TextView) findViewById(R.id.tvLocation);
        tvDateInvntry = (TextView) findViewById(R.id.tvDateInvntry);
        tvCategory = (TextView) findViewById(R.id.tvCategory);
        tvCommodityCd = (TextView) findViewById(R.id.tvCommodityCd);
        rwNuserial = (TableRow) findViewById(R.id.rwNuserial);

        btnSrchBck = (Button) findViewById(R.id.btnSrchBck);
        btnSrchBck.getBackground().setAlpha(255);
        
        acNuserial = (ClearableAutoCompleteTextView) findViewById(R.id.acNuserial);
        acNuserial.setFilters(new InputFilter[]{ invSerialFilter});
        acNuserial.setOnItemClickListener (new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                InvSerialNumber selected = (InvSerialNumber) arg0.getAdapter().getItem(arg2);
                barcode.setText(selected.getNusenate());
                
                //Log.i("NUSERIAL CLICK", "Clicked on Item "+selected.getNuserial());
            }
        });  
         
        acNuserial.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                // call your adapter here
                String st = s.toString();
                //Log.i("SERIALTEXTCHANGE", "!!!!Get Serial List:"+st);
                int recordCount = 0;
                boolean serialListfromServer = false;
                if (serialListNeeded || serialLength>st.length()) {
                    if (serialLength>st.length()) {
                        serialListAdapter.setTextColor(false);
                    }
                    recordCount = getSerialList(st);
                    //Log.i("SERIALTEXTCHANGE", "!!!!Serial Server RecordCount:"+recordCount);
                    serialListfromServer = true;
                }
                //Log.i("SERIALTEXTCHANGE", "!!!!FILTER ON:"+st);
                /*if (acNuserial.getText().toString().length()<serialLength) {
                    serialList = (ArrayList<InvSerialNumber>) serialListOrg.clone();
                    Log.i("TextChanged", "!!!!Restore original list size to "+serialList.size());
                }*/
                serialListAdapter.getFilter().filter(st);
                if (serialListfromServer) {
                    //System.out.println(st+" FROM SERVER COUNT:"+recordCount);
                    serialListAdapter.setTextColor(false);
                    if (recordCount==0) {
                        acNuserial.setTextColor(getResources().getColor(R.color.redlight));
                    }
                    else {
                        acNuserial.setTextColor(getResources().getColor(R.color.black));
                    }
                }
                else {
                    serialListAdapter.setTextColor(true);
                    /*if (serialListAdapter.suggestions==null) {
                        acNuserial.setTextColor(getResources().getColor(R.color.redlight));
                        System.out.println(st+" FROM ADAPTER COUNT: NULL(0)");
                    }
                    else { if (serialListAdapter.getFilteredCount(st)==0) {            
                        int cnt = serialListAdapter.getFilteredCount(st);
                        if (cnt==0) {
                            System.out.println(st+" FROM ADAPTER FILTER COUNT: "+cnt);
                            acNuserial.setTextColor(getResources().getColor(R.color.redlight));
                        }
                        else {
                            System.out.println(st+" FROM ADAPTER FILTER COUNT: "+cnt);
                            acNuserial.setTextColor(getResources().getColor(R.color.black));
                            }
                    }

                        
                    }*/
                }

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,int after) {
                serialLength = acNuserial.getText().toString().length();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
            });
        
        //getSerialList();
        acNuserial.setThreshold(3);
/*        acNuserial.setOnItemSelectedListener(
                new OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> arg0, View arg1,
                            int arg2, long arg3) {
                        String selectedValue = (String) acNuserial.get
                        Log.i("Serial Number", "VALUE:"+selectedValue);
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
                });*/
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
                        //Log.i("Search By Change", "VALUE:"+selectedValue);
                        if (selectedValue.equalsIgnoreCase("By Serial#")) {
                            //Log.i("Search By Change", "Changed to Serial");
                            barcode.setVisibility(View.GONE);
                            acNuserial.setVisibility(View.VISIBLE);
                        }
                        else {
                            //Log.i("Search By Change", "Changed to Senate Tag#");
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
    
    
    public int getSerialList(String nuserialPartial) {
        status = "yes";

        // Get the URL from the properties
        URL = LoginActivity.properties.get("WEBAPP_BASE_URL").toString();
        //System.out.println(URL + "/SerialList?nuserial="+nuserialPartial+"&maxResults=50");
        AsyncTask<String, String, String> resr1 = new RequestTask()
                .execute(URL + "/SerialList?nuserial="+nuserialPartial+"&maxResults=50");
        
        serialList = new ArrayList<InvSerialNumber>();
        int statusNum  = 0;
        int recordCount = 0;

        try {

            // code for JSON
            try {
                res = null;
                res = resr1.get().trim().toString();
                if (res == null) {
                    noServerResponse();
                    return -2;
                } else if (res.indexOf("Session timed out") > -1) {
                    startTimeout(SERIALLIST_TIMEOUT);
                    return -1;
                }
            } catch (NullPointerException e) {
                noServerResponse();
                return  -2;
            }
            String jsonString = resr1.get().trim().toString();
            //System.out.println("Serial# jsonString:"+jsonString);

            JSONArray jsonArray = new JSONArray(jsonString);
            // this will populate the lists from the JSON array coming from
            // server
            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject jo = new JSONObject();
                jo = jsonArray.getJSONObject(i);
                statusNum = jo.getInt("statusNum");
                //System.out.println("statusNum:"+statusNum);
                if (statusNum!=0) {
                    //System.out.println("Don't look at the rest");
                    serialListNeeded = true;
                    break;
                }
                serialListNeeded = false;
                InvSerialNumber invSerialNumber = new InvSerialNumber();
                invSerialNumber.setNuxrefsn( jo.getString("nuxrefsn"));
                invSerialNumber.setNuserial(jo.getString("nuserial"));
                invSerialNumber.setNusenate(jo.getString("nusenate"));
                invSerialNumber.setCdcommodity(jo.getString("cdcommodity"));
                invSerialNumber.setDecommodityf(jo.getString("decommodityf"));
                
          /*      try {
                    StringBuffer values = new StringBuffer();
                    values.append(invSerialNumber.getNuserial());
                    values.append("|");
                    values.append(invSerialNumber.getNusenate());
                    values.append("|");
                    values.append(invSerialNumber.getDecommodityf());
                    values.append("|now|");
                    values.append(LoginActivity.nauser);
                    values.append("|now|");
                    values.append(LoginActivity.nauser);
                    System.out.println ("INSERTING SERIAL#:"+invSerialNumber.getNuserial());
                    long rowsInserted = MenuActivity.db
                            .insert("ad12serial",
                                    "nuserial|nusenate|decommodityf|dttxnorigin|natxnorguser|dttxnupdate|natxnupduser",
                                    values.toString());

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }               */     
                
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
        serialListAdapter = new InvSerialAdapter(getApplicationContext(), acNuserial, R.layout.row_serialitem, serialList);
                
         Toasty toasty = new Toasty(context);
         
         if (statusNum>0) {
             //toasty.showMessage("Too many results ("+statusNum+") found, please keep typing.");
         }
         else if (statusNum<0) {
             toasty.showMessage("Server returned an error number of "+statusNum+" when trying to filter Serial#s. Please contact STSBAC. .");
         }
        
                /*new ArrayAdapter(this,R.layout.row_serialitem, serialList) {
            
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
                    //
                    // holder.commodityListNucnt = (TextView) convertView
                    // .findViewById(R.id.commodityListNucnt);
                    //
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
            public Filter getFilter() {
                return  nameFilter;
            }

            Filter nameFilter = new Filter() {
                
                public String convertResultToString(Object resultValue) {
                    String str = ((InvSerialNumber)(resultValue)).getNusenate(); 
                    return str;
                }
                
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    Log.i("performFiltering", "constraint:"+constraint+" serialListOrg SIZE:"+serialListOrg.size());
                    
                    if(constraint != null) {
                        if (suggestions==null) {
                            suggestions = new ArrayList<InvSerialNumber>();
                        }
                        suggestions.clear();
                        for (InvSerialNumber invSerialNumber : serialListOrg) {
                            Log.i("performFiltering", "invSerialNumber:"+invSerialNumber.toString());
                            if(invSerialNumber.getNuserial().startsWith(constraint.toString().toLowerCase())){
                                //Log.i("performFiltering", "     ADDED invSerialNumber:"+invSerialNumber.toString());
                                suggestions.add(invSerialNumber);
                            }
                        }
                        FilterResults filterResults = new FilterResults();
                        filterResults.values = suggestions;
                        filterResults.count = suggestions.size();
                        Log.i("performFiltering", "new filter count:"+filterResults.count);
                        return filterResults;
                    } else {
                            // No filter implemented we return all the list
                            FilterResults filterResults = new FilterResults();
                            filterResults.values = serialListOrg;
                            filterResults.count = serialListOrg.size();
                            return filterResults;                            
    
                    }
                }
                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    Log.i("publishResults", "start");
                    ArrayList<InvSerialNumber> filteredList = (ArrayList<InvSerialNumber>) results.values;
                    Log.i("publishResults", "got result values");
                    if(results != null && results.count > 0) {
                        Log.i("publishResults", "clearing");
                        clear();
                        Log.i("publishResults", "loop filtered list");
                        for (InvSerialNumber invSN : filteredList) {
                            Log.i("publishResults", "adding:"+invSN.toString());
                            add(invSN);
                        }
                        Log.i("publishResults", "notifyDataSetChanged");
                        notifyDataSetChanged();
                        Log.i("publishResults", "done");
                    }
                }

            };   
           
          
        };*/
        acNuserial.setAdapter(serialListAdapter);
        if (statusNum>0) {
            recordCount = statusNum;
        }
        else {
            if (serialList==null) {
               recordCount = 0;
            }
            else {
                recordCount = serialList.size();
            }
        }
        return recordCount;

    }
    
    public void toggleRowVisibility(TableRow row){

        if(row.getVisibility() == View.VISIBLE)
        {
            row.setVisibility(View.GONE);
        }
        else
        {
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

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }
    
    // initialize the cursor adapter
 /*   private void initCursorAdapter()
       {
        String[] searchBy = new String[1];
        searchBy[0] = this.acNuserial.getText().toString()+"%";
         
           serialCursor =  MenuActivity.db.rawQuery("SELECT nuserial, nusenate, decommodityf FROM ad12serial WHERE nuserial like ?", searchBy);     
//           startManagingCursor(mItemCursor);
           
           serialAdapter = new SimpleCursorAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, serialCursor, searchBy, null);
                   
       }
       
       // initialize AutocompleteTextView
    private void initItemFilter()
       {
          String[] searchBy = new String[1];
          searchBy[0] = this.acNuserial.getText().toString()+"%";
           this.acNuserial.setAdapter(serialAdapter);
           acNuserial.setThreshold(1);
       }     */

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
                    //Log.i("Search res ", "res:" + res);
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
            if (res.toUpperCase(Locale.ENGLISH).contains("DOES NOT EXIST IN SYSTEM")) {
                tvBarcode.setText(barcode.getText().toString()
                        + " - !!ERROR: DOES NOT EXIST.");
                int color = Integer.parseInt("bb0000", 16) + 0xFF000000;
                tvBarcode.setTextColor(color);
                tvNuserial.setText("N/A");
                tvDescription.setText("N/A");
                tvCategory.setText("N/A");
                tvLocation.setText("N/A");
                tvDateInvntry.setText("N/A");
                tvCommodityCd.setText("N/A");
                rwNuserial.setVisibility(View.VISIBLE);

            } else {
                int color = Integer.parseInt("000000", 16) + 0xFF000000;
                tvBarcode.setTextColor(color);
                try {
                    JSONObject object = (JSONObject) new JSONTokener(res)
                            .nextValue();
                    StringBuilder nusenateMsg = new StringBuilder();
                    nusenateMsg.append(object.getString("nusenate"));
                    String cdstatus = object.getString("cdstatus");
                    //Log.i("TEST", "CDSTATUS:(" + cdstatus + ")");
                    if (cdstatus.equalsIgnoreCase("I")) {
                        nusenateMsg.append(" <font color='RED'>(INACTIVE) ");
                        nusenateMsg.append(object.getString("deadjust"));
                        //Log.i("TEST", "INACTIVE CDSTATUS:(" + cdstatus + ")");
                    }

                    //Log.i("TEST", "Senate Tag#:" + nusenateMsg);
                    tvBarcode.setText(Html.fromHtml(nusenateMsg.toString()));
                    try {
                        tvNuserial.setText(object.getString("nuserial"));
                    }
                    catch (JSONException e1) {
                        tvNuserial.setText("N/A");
                    }

                    if (tvNuserial.getText().toString().trim().length()>0) {
                        rwNuserial.setVisibility(View.VISIBLE);
                    }
                    else {
                        rwNuserial.setVisibility(View.GONE);
                    }
                    
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
                    tvNuserial.setText("N/A");
                    tvDateInvntry.setText("N/A");
                    tvCommodityCd.setText("N/A");
                    rwNuserial.setVisibility(View.VISIBLE);

                    e.printStackTrace();
                }
            }
            // textView.setText("\n" + res);
            barcode.setText("");
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
              @Override
              public void run() {
                  acNuserial.setText("");
              }
            }, 100);
        } catch (Exception e) {
            tvDescription.setText("!!ERROR: " + e.getMessage());
            tvCategory.setText("Please contact STS/BAC.");
            tvLocation.setText("N/A");
            tvNuserial.setText("N/A");
            tvDateInvntry.setText("N/A");
            tvCommodityCd.setText("N/A");
            rwNuserial.setVisibility(View.VISIBLE);
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
        btnSrchBck.getBackground().setAlpha(45);
        finish();
        overridePendingTransition(R.anim.in_left, R.anim.out_right);
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

/*    class SerialCursorAdapter extends SimpleCursorAdapter {

        private Context mContext;
        private Context appContext;
        private int layout;
        private Cursor cr;
        private final LayoutInflater inflater;
        
        class ViewHolder
        {
            RelativeLayout rlSerialRow;
            // TextView commodityListNucnt;
            TextView tvNuserial;
            TextView tvNusenate;
            TextView tvDecommodityf;
        }          

        @SuppressWarnings("deprecation")
        public SerialCursorAdapter(Context context,int layout, Cursor c,String[] from,int[] to) {
            super(context,layout,c,from,to);
            this.layout=layout;
            this.mContext = context;
            this.inflater=LayoutInflater.from(context);
            this.cr=c;
        }

        @Override
        public View newView (Context context, Cursor cursor, ViewGroup parent) {
                return inflater.inflate(layout, null);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            super.bindView(view, context, cursor);
                ViewHolder holder = null;
                InvSerialNumber rowItem = null;
                if (view == null) {
                    final LayoutInflater mInflater = (LayoutInflater) context
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                    view = mInflater.inflate(R.layout.row_serialitem, null);
                    holder = new ViewHolder();
                    holder.rlSerialRow = (RelativeLayout) view
                            .findViewById(R.id.rlSerialRow);
                    //
                    // holder.commodityListNucnt = (TextView) convertView
                    // .findViewById(R.id.commodityListNucnt);
                    //
                    holder.tvNuserial = (TextView) view
                            .findViewById(R.id.tvNuserial);
                    holder.tvNusenate = (TextView) view
                            .findViewById(R.id.tvNusenate);
                    holder.tvDecommodityf = (TextView) view
                            .findViewById(R.id.tvDecommodityf);
                    view.setTag(holder);
                } else {
                    holder = (ViewHolder) view.getTag();
                }
                
                
                //if (position > -1 && serialList != null && position < serialList.size()) {
                    // holder.commodityListNucnt.setText(rowItem.getNucnt());
                    holder.tvNuserial.setText(Html.fromHtml("<b>S: "+cursor.getString(1)+"</b>"));
                    holder.tvNusenate.setText(Html.fromHtml("<b>T: "+cursor.getString(2)+"</b>"));
                    holder.tvDecommodityf.setText(Html.fromHtml(cursor.getString(3)));
                    holder.tvNusenate.setTextColor(context.getResources()
                            .getColor(R.color.black));
                    holder.tvDecommodityf.setTextColor(context.getResources()
                            .getColor(R.color.black));
                    holder.tvNuserial.setTextColor(context.getResources()
                            .getColor(R.color.black));
                    
    
        }

} */  
        
    
}
