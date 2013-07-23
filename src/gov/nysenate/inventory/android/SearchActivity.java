package gov.nysenate.inventory.android;

import java.util.concurrent.ExecutionException;

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
import android.widget.Button;
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

    static Button btnSrchBck;
    Activity currentActivity;

    String timeoutFrom = "search";    
    public final int SEARCH_TIMEOUT = 101;    
    
    
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

        btnSrchBck = (Button) findViewById(R.id.btnSrchBck);
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
                String URL = LoginActivity.properties.get(
                        "WEBAPP_BASE_URL").toString();
                Log.i("Activity Search afterTextChanged ", "URL " + URL);
                AsyncTask<String, String, String> resr1 = new RequestTask()
                        .execute(URL + "/Search?barcode_num="
                                + barcode_num);
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

            } else {
                int color = Integer.parseInt("000000", 16) + 0xFF000000;
                tvBarcode.setTextColor(color);
                try {
                    JSONObject object = (JSONObject) new JSONTokener(
                            res).nextValue();
                    StringBuilder nusenateMsg = new StringBuilder();
                    nusenateMsg.append(object.getString("nusenate"));
                    String cdstatus = object.getString("cdstatus");
                    Log.i("TEST", "CDSTATUS:(" + cdstatus + ")");
                    if (cdstatus.equalsIgnoreCase("I")) {
                        nusenateMsg
                                .append(" <font color='RED'>(INACTIVE) ");
                        nusenateMsg
                                .append(object.getString("deadjust"));
                        Log.i("TEST", "INACTIVE CDSTATUS:(" + cdstatus
                                + ")");
                    }

                    Log.i("TEST", "Senate Tag#:" + nusenateMsg);
                    tvBarcode.setText(Html.fromHtml(nusenateMsg
                            .toString()));

                    tvDescription.setText(object.getString(
                            "decommodityf").replaceAll("&#34;", "\""));
                    tvCategory.setText(object.getString("cdcategory"));
                    tvLocation.setText(object.getString("cdlocatto")
                            + " ("
                            + object.getString("cdloctypeto")
                            + ") "
                            + object.getString("adstreet1to")
                                    .replaceAll("&#34;", "\""));
                    tvDateInvntry.setText(object
                            .getString("dtlstinvntry"));

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    tvDescription.setText("!!ERROR: " + e.getMessage());
                    tvCategory.setText("Please contact STS/BAC.");
                    tvLocation.setText("N/A");
                    tvDateInvntry.setText("N/A");

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
    
    public void startTimeout(int timeoutType) {
        Intent intentTimeout = new Intent(this, LoginActivity.class);
        intentTimeout.putExtra("TIMEOUTFROM", timeoutFrom);
        startActivityForResult(intentTimeout, timeoutType);
    }
    
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
