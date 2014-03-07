package gov.nysenate.inventory.activity;

import gov.nysenate.inventory.android.ClearableAutoCompleteTextView;
import gov.nysenate.inventory.android.InvApplication;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.model.Location;
import gov.nysenate.inventory.model.Transaction;
import gov.nysenate.inventory.util.AppProperties;
import gov.nysenate.inventory.util.HttpUtils;
import gov.nysenate.inventory.util.Toasty;
import gov.nysenate.inventory.util.TransactionParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ChangePickupOrigin extends SenateActivity {

    Transaction pickup;
    ProgressBar progressBar;
    List<String> locations;
    ClearableAutoCompleteTextView newPickupLocation;
    TextView newLocRespCenterHd;
    TextView newLocAddress;
    Location newLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_origin);
        registerBaseActivityReceiver();

        locations = new ArrayList<String>();
        newLocation = new Location();

        TextView oldPickupLocation = (TextView) findViewById(R.id.old_pickup_location);
        TextView oldDeliveryLocation = (TextView) findViewById(R.id.old_delivery_location);
        TextView oldPickupBy = (TextView) findViewById(R.id.pickup_by);
        TextView oldCount = (TextView) findViewById(R.id.pickup_count);
        TextView oldDate = (TextView) findViewById(R.id.pickup_date);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        newPickupLocation = (ClearableAutoCompleteTextView) findViewById(R.id.autoCompleteTextView1);
        newLocRespCenterHd = (TextView) findViewById(R.id.tvOfficeD);
        newLocAddress = (TextView) findViewById(R.id.tvDescriptD);

        pickup = TransactionParser.parseTransaction(getIntent().getStringExtra("pickup"));


        String date = getIntent().getStringExtra("date");

        if (pickup.isRemote()) {
            oldPickupLocation.setText(Html.fromHtml(pickup.getOrigin().getLocationSummaryStringRemoteAppended()));
            oldDeliveryLocation.setText(Html.fromHtml(pickup.getDestination().getLocationSummaryStringRemoteAppended()));
        } else {
            oldPickupLocation.setText(pickup.getOrigin().getLocationSummaryString());
            oldDeliveryLocation.setText(pickup.getDestination().getLocationSummaryString());
        }
        oldPickupBy.setText(pickup.getNapickupby());
        oldCount.setText(Integer.toString(pickup.getPickupItems().size()));
        SimpleDateFormat sdf = ((InvApplication)getApplicationContext()).getDateTimeFormat();
        oldDate.setText(sdf.format(pickup.getPickupDate()));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            new GetLocations().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            new GetLocations().execute();
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, locations);
        newPickupLocation.setThreshold(1);
        newPickupLocation.setAdapter(adapter);
        newPickupLocation.addTextChangedListener(originTextWatcher);
        newPickupLocation.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    new LocationDetails().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    new LocationDetails().execute();
                }
            }
        });
    }

    private class GetLocations extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        // TODO: need to investigate/document the /LocCodeList servlet,
        // e.g. what does it send back to indicate an error?
        @Override
        protected String doInBackground(Void... params) {
            if (checkServerResponse(true) != OK) {
                return "";
            }
            HttpClient httpClient = LoginActivity.getHttpClient();
            HttpResponse response = null;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            String url = AppProperties.getBaseUrl(ChangePickupOrigin.this);
            url += "LocCodeList?";
            url += "&userFallback=" + LoginActivity.nauser;

            try {
                response = httpClient.execute(new HttpGet(url));
                response.getEntity().writeTo(out);
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                JSONArray json = new JSONArray(out.toString());
                for (int i = 0; i < json.length(); i++) {
                    locations.add(json.getString(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Collections.sort(locations);
            return out.toString();
        }

        @Override
        protected void onPostExecute(String response) {
            progressBar.setVisibility(ProgressBar.INVISIBLE);
        }
    }

    private class LocationDetails extends AsyncTask<Void, Map<TextView, String>, String> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }
        
        @Override
        protected String doInBackground(Void... params) {
            if (checkServerResponse(true) != OK) {
                return "";
            }

            HttpClient httpClient = LoginActivity.getHttpClient();
            HttpResponse response = null;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            String url = AppProperties.getBaseUrl(ChangePickupOrigin.this);
            url += "LocationDetails?barcode_num=" + parseCdLoc(newPickupLocation.getText().toString());
            url += "&userFallback=" + LoginActivity.nauser;

            try {
                response = httpClient.execute(new HttpGet(url));
                response.getEntity().writeTo(out);
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String respctrhd = "";
            String adstreet1 = "";
            try {
                // TODO: Fully implement with Location object.
                JSONObject json = (JSONObject) new JSONTokener(out.toString()).nextValue();
                newLocation.setCdlocat(json.getString("cdlocat"));
                respctrhd = json.getString("cdrespctrhd");
                adstreet1 = json.getString("adstreet1");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Map<TextView, String> textViews = new HashMap<TextView, String>();
            textViews.put(newLocRespCenterHd, respctrhd);
            textViews.put(newLocAddress, adstreet1);
            publishProgress(textViews);

            return out.toString();
        }

        @Override
        protected void onProgressUpdate(Map<TextView, String>... map) {
            for (TextView key : map[0].keySet()) {
                key.setText(map[0].get(key));
            }
        }

        @Override
        protected void onPostExecute(String response) {
            progressBar.setVisibility(ProgressBar.INVISIBLE);
        }
    }

    private TextWatcher originTextWatcher = new TextWatcher()
    {
        private int textLength;

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                int count) {
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {
            textLength = newPickupLocation.getText().length();
        }
        @Override
        public void afterTextChanged(Editable s) {
            // Backspace pressed
            if (textLength > newPickupLocation.getText().length()) {
                newLocRespCenterHd.setText("N/A");
                newLocAddress.setText("N/A");
            }
        }
    };

    public void backButton(View view) {
        if (checkServerResponse(true) == OK) {
            super.onBackPressed();
        }
    }

    public void continueButton(View view) {
        if (checkServerResponse(true) != OK) {
            return;
        }

        if (locations.indexOf(newPickupLocation.getText().toString()) == -1) {
            if (newPickupLocation.getText().length() > 0) {
                Toasty.displayCenteredMessage(this, "You must select a valid Pickup Location.", Toast.LENGTH_SHORT);
            } else {
                Toasty.displayCenteredMessage(this, "You must select a new Pickup Location.", Toast.LENGTH_SHORT);
            }
            return;
        } else {
            AlertDialog.Builder confirmDialog = new AlertDialog.Builder(this);
            confirmDialog.setCancelable(false);
            confirmDialog.setTitle(Html.fromHtml("<font color='#000055'>Change Pickup Location</font>"));
            confirmDialog.setMessage(Html.fromHtml("Are you sure you want to change the pickup location to " + newLocation.getCdlocat() + "?"));
            confirmDialog.setNegativeButton(Html.fromHtml("<b>No</b>"), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    return;
                }
            });
            confirmDialog.setPositiveButton(Html.fromHtml("<b>Yes</b>"), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        new ChangePickupLocation().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        new ChangePickupLocation().execute();
                    }
                }
            });
            confirmDialog.show();
        }
    }

    private class ChangePickupLocation extends AsyncTask<Void, Void, Integer> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            HttpClient httpClient = LoginActivity.getHttpClient();
            HttpResponse response = null;
            String url = AppProperties.getBaseUrl(ChangePickupOrigin.this);
            url += "ChangePickupLocation?nuxrpd=" + pickup.getNuxrpd() + "&cdloc=" + newLocation.getCdlocat();
            url += "&userFallback=" + LoginActivity.nauser;

            try {
                response = httpClient.execute(new HttpGet(url));
                return response.getStatusLine().getStatusCode();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer response) {
            progressBar.setVisibility(ProgressBar.INVISIBLE);
            Intent intent = new Intent(ChangePickupOrigin.this, EditPickupMenu.class);
            intent.putExtra("nuxrpd", Integer.toString(pickup.getNuxrpd()));
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            overridePendingTransition(R.anim.in_right, R.anim.out_left);
            HttpUtils.displayResponseResults(ChangePickupOrigin.this, response);
        }
    }

    private String parseCdLoc(String summary) {
        String[] split = summary.split("-");
        return split[0];
    }
}
