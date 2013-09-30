package gov.nysenate.inventory.android;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import gov.nysenate.inventory.model.Location;
import gov.nysenate.inventory.model.Pickup;
import gov.nysenate.inventory.util.JSONParser;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ChangePickupOrigin extends SenateActivity {

    Pickup pickup;
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

        pickup = getIntent().getParcelableExtra("pickup");
        String date = getIntent().getStringExtra("date");

        oldPickupLocation.setText(pickup.getOriginAddressLine1());
        oldDeliveryLocation.setText(pickup.getDestinationAddressLine1());
        oldPickupBy.setText(pickup.getNaPickupBy());
        oldCount.setText(Integer.toString(pickup.getPickupItems().size()));
        oldDate.setText(date);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            new GetLocations().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            new GetLocations().execute();
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, locations);
        newPickupLocation.setThreshold(1);
        newPickupLocation.setAdapter(adapter);
        newPickupLocation.addTextChangedListener(originTextWatcher);

    }

    // TODO: extract in class logic to use in other asyctasks.
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
            String url = (String) LoginActivity.properties.get("WEBAPP_BASE_URL");
            if (!url.endsWith("/")) {
                url += "/";
            }
            url += "/LocCodeList?";
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
            // TODO: how to test if /LocCodeList was successful?
        }
    }

    // TODO: extract in class logic to use in other asyctasks.
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
            String url = (String) LoginActivity.properties.get("WEBAPP_BASE_URL");
            if (!url.endsWith("/")) {
                url += "/";
            }
            url += "/LocationDetails?barcode_num=" + parseCdLoc(newPickupLocation.getText().toString());
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
                newLocation.setCdLoc(json.getString("cdlocat"));
                respctrhd = json.getString("cdrespctrhd");
                adstreet1 = json.getString("adstreet1");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Map<TextView, String> textViews = new HashMap<TextView, String>();
            textViews.put(newLocRespCenterHd, respctrhd);
            textViews.put(newLocAddress, adstreet1);
            publishProgress(textViews);

         // TODO: what is returned from /LocationDetails?
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
        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                int count) {
            if (newPickupLocation.getText().toString().length() >= 3) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    new LocationDetails().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    new LocationDetails().execute();
                }
            }
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {
        }
        @Override
        public void afterTextChanged(Editable s) {

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
        AlertDialog.Builder confirmDialog = new AlertDialog.Builder(this);
        confirmDialog.setCancelable(false);
        confirmDialog.setTitle(Html.fromHtml("<font color='#000055'>Change Pickup Location</font>"));
        confirmDialog.setMessage(Html.fromHtml("Are you sure you want to change the pickup location to " + newLocation.getCdLoc()));
        confirmDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        confirmDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

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

    private class ChangePickupLocation extends AsyncTask<Void, Void, Integer> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            HttpClient httpClient = LoginActivity.getHttpClient();
            HttpResponse response = null;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            String url = (String) LoginActivity.properties.get("WEBAPP_BASE_URL");
            if (!url.endsWith("/")) {
                url += "/";
            }
            url += "/ChangePickupLocation?nuxrpd=" + pickup.getNuxrpd() + "&cdloc=" + newLocation.getCdLoc();
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
            Intent intent = new Intent(ChangePickupOrigin.this, Move.class);
            startActivity(intent);
            overridePendingTransition(R.anim.in_right, R.anim.out_left);
            if (response == HttpStatus.SC_OK) {
                displayShortToast("Successfully updated database");
            } else if (response == HttpStatus.SC_BAD_REQUEST) {
                displayShortToast("!!ERROR: Invalid nuxrpd or location");
            } else if (response == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                displayShortToast("!!ERROR: Database Error, your update may not have been saved.");
            } else {
                displayShortToast("!!ERROR: Unknown Error, your update may not have been saved.");
            }
        }
    }

    public void displayShortToast(CharSequence text) {
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private String parseCdLoc(String summary) {
        String[] split = summary.split("-");
        return split[0];
    }
}
