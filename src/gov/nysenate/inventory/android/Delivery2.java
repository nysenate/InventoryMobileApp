package gov.nysenate.inventory.android;

import java.util.ArrayList;
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
import android.text.Html;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class Delivery2 extends SenateActivity
{
    public TextView loc_details;
    String status = null;
    String URL = null;
    String res = null;
    public ArrayList<String> deliveryList = new ArrayList<String>();
    public ArrayList<PickupGroup> pickupGroups = new ArrayList<PickupGroup>();
    public String locCode = null;
    ListView listview;
    String location = "";
    Intent intent;
    static ProgressBar progBarDelivery2;
    static Button btnDelivery2Cancel;
    Activity currentActivity;
    String timeoutFrom = "delivery1";
    public final int DELIVERYLIST_TIMEOUT = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery2);
        registerBaseActivityReceiver();
        currentActivity = this;

        // define progressBar

        progBarDelivery2 = (ProgressBar) findViewById(R.id.progBarDelivery2);

        // define intent

        intent = new Intent(this, Delivery3.class);
        // 1. Get the intent from Delivery1 activity and display it

        StringBuilder sb = new StringBuilder();
        sb.append("Please select pickup for delivery to<br/><b>");
        sb.append(Delivery1.autoCompleteTextView1.getText().toString());
        sb.append("</b>");

        location = getIntent().getStringExtra("location");
        loc_details = (TextView) findViewById(R.id.textView1);
        loc_details.setText(Html.fromHtml(sb.toString()));
        listview = (ListView) findViewById(R.id.listView1);

        getDeliveryList();
        // listener for list click
        listview.setTextFilterEnabled(true);
        listview.setOnItemClickListener(new OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                if (checkServerResponse(true) == OK) {

                    progBarDelivery2.setVisibility(View.VISIBLE);
                    // this will go to the Delivery 3 activity with the data
                    PickupGroup selectedPickup = pickupGroups.get(position);
                    // String [] itemDetails=selectedPickup.split(":");
                    String nuxrpd = Integer.toString(selectedPickup.getNuxrpd());

                    intent.putExtra("location", location);
                    intent.putExtra("nuxrpd", nuxrpd);

                    startActivity(intent);
                    overridePendingTransition(R.anim.in_right, R.anim.out_left);
                }
            }

        });
        Delivery1.progBarDelivery1.setVisibility(View.VISIBLE);

    }

    public void noServerResponse() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle(Html.fromHtml("<font color='#000055'>NO SERVER RESPONSE</font>"));

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
        btnDelivery2Cancel = (Button) findViewById(R.id.btnDelivery2Cancel);
        if (this.progBarDelivery2 ==null) {
            this.progBarDelivery2 = (ProgressBar) this.findViewById(R.id.progBarDelivery2);
        }            
        btnDelivery2Cancel.getBackground().setAlpha(255);
        progBarDelivery2.getBackground().setAlpha(255);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_delivery2, menu);
        return true;
    }

    public void cancelButton(View view) {
        if (checkServerResponse(true) == OK) {
            Delivery2.btnDelivery2Cancel.getBackground().setAlpha(45);
            Intent intent = new Intent(this, Move.class);
            startActivity(intent);
            overridePendingTransition(R.anim.in_left, R.anim.out_right);
        }
    }

    public void startTimeout(int timeoutType) {
        Intent intentTimeout = new Intent(this, LoginActivity.class);
        intentTimeout.putExtra("TIMEOUTFROM", timeoutFrom);
        startActivityForResult(intentTimeout, timeoutType);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case DELIVERYLIST_TIMEOUT:
            if (resultCode == RESULT_OK) {
                getDeliveryList();
                break;
            }
        }
    }

    public void getDeliveryList() {
        // separate location code from the description
        String locDesc[] = location.split("-");
        locCode = locDesc[0];

        // 2. Display all the in transit moves for the current location
        // check network connection
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // fetch data
            status = "yes";

            // Get the URL from the properties
            URL = LoginActivity.properties.get("WEBAPP_BASE_URL").toString();

            AsyncTask<String, String, String> resr1 = new RequestTask()
                    .execute(URL + "/DeliveryList?loc_code=" + locCode);
            try {
                try {
                    res = null;
                    res = resr1.get().trim().toString();
                    if (res == null) {
                        this.noServerResponse();
                        return;
                    } else if (res.indexOf("Session timed out") > -1) {
                        startTimeout(this.DELIVERYLIST_TIMEOUT);
                        return;
                    }

                } catch (NullPointerException e) {
                    this.noServerResponse();
                    return;
                }

                // code for JSON

                try {
                    JSONArray jsonArray = new JSONArray(res);
                    JSONObject object;
                    PickupGroup currentPickupGroup;
                    int nuxrpd = -1;
                    String pickupDateTime = "N/A";
                    String pickupFrom = "N/A";
                    String pickupRelBy = "N/A";
                    String pickupLocat = "N/A";
                    String pickupAdstreet1 = "N/A";
                    String pickupAdcity = "N/A";
                    String pickupAdstate = "N/A";
                    String pickupAdzipcode = "N/A";
                    int pickupItemCount = -1;

                    for (int x = 0; x < jsonArray.length(); x++) {
                        nuxrpd = -1;
                        pickupDateTime = "N/A";
                        pickupFrom = "N/A";
                        pickupRelBy = "N/A";
                        pickupLocat = "N/A";
                        pickupAdstreet1 = "N/A";
                        pickupAdcity = "N/A";
                        pickupAdstate = "N/A";
                        pickupAdzipcode = "N/A";
                        pickupItemCount = -1;
                        object = jsonArray.getJSONObject(x);
                        try {
                            nuxrpd = object.getInt("nuxrpd");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            pickupDateTime = object.getString("pickupDateTime");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            pickupFrom = object.getString("pickupFrom");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            pickupRelBy = object.getString("pickupRelBy");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            pickupLocat = object.getString("pickupLocat");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            pickupItemCount = object.getInt("pickupItemCount");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            pickupAdstreet1 = object
                                    .getString("pickupAdstreet1");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            pickupAdcity = object.getString("pickupAdcity");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            pickupAdstate = object.getString("pickupAdstate");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            pickupAdzipcode = object
                                    .getString("pickupAdzipcode");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        // Log.i("JSON VALUES "+x, object.toString());
                        currentPickupGroup = new PickupGroup(nuxrpd,
                                pickupDateTime, pickupFrom, pickupRelBy,
                                pickupLocat, pickupAdstreet1, pickupAdcity,
                                pickupAdstate, pickupAdzipcode, pickupItemCount);
                        // System.out.println(nuxrpd+" ,  "+pickupDateTime+" ,"+
                        // pickupFrom+", "+pickupRelBy+" , "+pickupLocat+" , "+pickupItemCount);
                        pickupGroups.add(currentPickupGroup);
                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                // System.out.println
                // ("pickupGroups Count:"+pickupGroups.size());
                PickupGroupViewAdapter adapter = new PickupGroupViewAdapter(
                        this, R.layout.pickup_group_row, pickupGroups);
                // System.out.println ("Setup Listview with pickupGroups");

                listview.setAdapter(adapter);

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

    @Override
    public void commoditySelected(int rowSelected, Commodity commoditySelected) {
        // TODO Auto-generated method stub
        
    }

}
