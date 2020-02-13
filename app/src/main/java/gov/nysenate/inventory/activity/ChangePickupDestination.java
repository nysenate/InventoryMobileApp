package gov.nysenate.inventory.activity;

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

import com.android.volley.Request;
import com.android.volley.Response;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.nysenate.inventory.android.AppSingleton;
import gov.nysenate.inventory.android.ClearableAutoCompleteTextView;
import gov.nysenate.inventory.android.InvApplication;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.android.StringInvRequest;
import gov.nysenate.inventory.model.Location;
import gov.nysenate.inventory.model.Transaction;
import gov.nysenate.inventory.util.AppProperties;
import gov.nysenate.inventory.util.HttpUtils;
import gov.nysenate.inventory.util.Serializer;
import gov.nysenate.inventory.util.Toasty;

public class ChangePickupDestination extends SenateActivity {

    private Transaction pickup;
    private ProgressBar progressBar;
    private Map<String, Location> summaryToLocationMap = new HashMap<>();
    private ClearableAutoCompleteTextView newDeliveryLocation;
    private TextView newLocRespCenterHd;
    private TextView newLocAddress;
    private Location newLocation;

    Response.Listener<String> changeLocResponseListener = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            progressBar.setVisibility(ProgressBar.INVISIBLE);
            Intent intent = new Intent(ChangePickupDestination.this, EditPickupMenu.class);
            intent.putExtra("nuxrpd", Integer.toString(pickup.getNuxrpd()));
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            overridePendingTransition(R.anim.in_right, R.anim.out_left);
            HttpUtils.displayResponseResults(ChangePickupDestination.this, HttpStatus.SC_OK);
        }
    };

    Response.Listener<String> locationDetailsResponseListener = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {

            String respctrhd = "";
            String adstreet1 = "";
            try {
                // TODO: Fully implement with Location object.
                JSONObject json = (JSONObject) new JSONTokener(response).nextValue();
                newLocation.setCdlocat(json.getString("cdlocat"));
                respctrhd = json.getString("cdrespctrhd");
                adstreet1 = json.getString("adstreet1");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Map<TextView, String> textViews = new HashMap<TextView, String>();
            textViews.put(newLocRespCenterHd, respctrhd);
            textViews.put(newLocAddress, adstreet1);
            for (TextView key : textViews.keySet()) {
                key.setText(textViews.get(key));
            }
            progressBar.setVisibility(ProgressBar.INVISIBLE);

        }
    };

    Response.Listener<String> getLocationsResponseListener = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {

            progressBar.setVisibility(ProgressBar.INVISIBLE);

            List<Location> locs = Serializer.deserialize(response, Location.class);


            for (Location loc : locs) {
                summaryToLocationMap.put(loc.getLocationSummaryString(), loc);
            }

            List<String> locSummaries = new ArrayList<>(summaryToLocationMap.keySet());
            Collections.sort(locSummaries);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(ChangePickupDestination.this, android.R.layout.simple_dropdown_item_1line, locSummaries);

            if (newDeliveryLocation == null) {
                newDeliveryLocation = (ClearableAutoCompleteTextView) findViewById(R.id.autoCompleteTextView1);
            }

            newDeliveryLocation.setThreshold(1);
            newDeliveryLocation.setAdapter(adapter);
            newDeliveryLocation.addTextChangedListener(destinationTextWatcher);
            newDeliveryLocation.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    Location selectedLocation = summaryToLocationMap.get(newDeliveryLocation.getText().toString());
                    String url = AppProperties.getBaseUrl();
                    url += "LocationDetails?location_code=" + selectedLocation.getCdlocat()
                            + "&location_type=" + selectedLocation.getCdloctype();

                    StringInvRequest stringInvRequest = new StringInvRequest(Request.Method.GET, url, null, locationDetailsResponseListener);

                    AppSingleton.getInstance(InvApplication.getAppContext()).addToRequestQueue(stringInvRequest);
                }
            });
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_destination);
        registerBaseActivityReceiver();

        newLocation = new Location();

        TextView oldPickupLocation = (TextView) findViewById(R.id.old_pickup_location);
        TextView oldDeliveryLocation = (TextView) findViewById(R.id.old_delivery_location);
        TextView oldPickupBy = (TextView) findViewById(R.id.pickup_by);
        TextView oldCount = (TextView) findViewById(R.id.pickup_count);
        TextView oldDate = (TextView) findViewById(R.id.pickup_date);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        newDeliveryLocation = (ClearableAutoCompleteTextView) findViewById(R.id.autoCompleteTextView1);
        newLocRespCenterHd = (TextView) findViewById(R.id.tvOfficeD);
        newLocAddress = (TextView) findViewById(R.id.tvDescriptD);

        pickup = EditPickupMenu.pickup;

        if (pickup.isRemote()) {
            oldPickupLocation.setText(Html.fromHtml(pickup.getOrigin().getLocationSummaryStringRemoteAppended()));
            oldDeliveryLocation.setText(Html.fromHtml(pickup.getDestination().getLocationSummaryStringRemoteAppended()));
        } else {
            oldPickupLocation.setText(pickup.getOrigin().getLocationSummaryString());
            oldDeliveryLocation.setText(pickup.getDestination().getLocationSummaryString());
        }
        oldPickupBy.setText(pickup.getNapickupby());
        oldCount.setText(Integer.toString(pickup.getPickupItems().size()));
        SimpleDateFormat sdf = ((InvApplication) getApplicationContext()).getDateTimeFormat();
        oldDate.setText(sdf.format(pickup.getPickupDate()));

        this.getLocations();

    }

    private void getLocations() {
        String url = AppProperties.getBaseUrl(ChangePickupDestination.this);
        url += "LocCodeList?";
        url += "&userFallback=" + LoginActivity.nauser;

        StringInvRequest stringInvRequest = new StringInvRequest(Request.Method.GET, url, null, getLocationsResponseListener);

        /* Add your Requests to the RequestQueue to execute */
        AppSingleton.getInstance(InvApplication.getAppContext()).addToRequestQueue(stringInvRequest);
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
            HttpClient httpClient = LoginActivity.getHttpClient();
            HttpResponse response = null;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            String url = AppProperties.getBaseUrl(ChangePickupDestination.this);
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

            List<Location> locs = Serializer.deserialize(out.toString(), Location.class);
            for (Location loc : locs) {
                summaryToLocationMap.put(loc.getLocationSummaryString(), loc);
            }

            return out.toString();
        }

        @Override
        protected void onPostExecute(String response) {
            progressBar.setVisibility(ProgressBar.INVISIBLE);

            List<String> locSummaries = new ArrayList<>(summaryToLocationMap.keySet());
            Collections.sort(locSummaries);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(ChangePickupDestination.this, android.R.layout.simple_dropdown_item_1line, locSummaries);
            if (newDeliveryLocation == null) {
                newDeliveryLocation = (ClearableAutoCompleteTextView) findViewById(R.id.autoCompleteTextView1);
            }

            newDeliveryLocation.setThreshold(1);
            newDeliveryLocation.setAdapter(adapter);
            newDeliveryLocation.addTextChangedListener(destinationTextWatcher);
            newDeliveryLocation.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        new LocationDetails().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        new LocationDetails().execute();
                    }
                }
            });

            Location selectedLocation = summaryToLocationMap.get(newDeliveryLocation.getText().toString());

            if (selectedLocation == null) {
                Toasty.displayCenteredMessage(ChangePickupDestination.this, "Entered Text is invalid.", Toast.LENGTH_SHORT);
                return;
            }

            String url = AppProperties.getBaseUrl();

            url += "LocationDetails?location_code=" + selectedLocation.getCdlocat()
                    + "&location_type=" + selectedLocation.getCdloctype();

            StringInvRequest stringInvRequest = new StringInvRequest(Request.Method.GET, url, null, getLocationsResponseListener);

            /* Add your Requests to the RequestQueue to execute */
            AppSingleton.getInstance(InvApplication.getAppContext()).addToRequestQueue(stringInvRequest);

            LoginActivity.activeAsyncTask = null;
        }
    }

    protected void changeLocation() {
        String url = AppProperties.getBaseUrl();
        url += "ChangeDeliveryLocation?nuxrpd=" + pickup.getNuxrpd() + "&cdloc=" + newLocation.getCdlocat();
        url += "&userFallback=" + LoginActivity.nauser;

        StringInvRequest stringInvRequest = new StringInvRequest(Request.Method.GET, url, null, changeLocResponseListener);

        /* Add your Requests to the RequestQueue to execute */
        AppSingleton.getInstance(InvApplication.getAppContext()).addToRequestQueue(stringInvRequest);
    }

    private class LocationDetails extends AsyncTask<Void, Map<TextView, String>, String> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... params) {
            HttpClient httpClient = LoginActivity.getHttpClient();
            HttpResponse response = null;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            String url = AppProperties.getBaseUrl(ChangePickupDestination.this);

            Location selectedLocation = summaryToLocationMap.get(newDeliveryLocation.getText().toString());
            if (selectedLocation == null) {
                Toasty.displayCenteredMessage(ChangePickupDestination.this, "Entered Text is invalid.", Toast.LENGTH_SHORT);
                return out.toString();
            }
            url += "LocationDetails?location_code=" + selectedLocation.getCdlocat()
                    + "&location_type=" + selectedLocation.getCdloctype();

            try {
                new HttpUtils().updateOffline(url);  // testing only

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

    private TextWatcher destinationTextWatcher = new TextWatcher() {
        private int textLength;

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            textLength = newDeliveryLocation.getText().length();
        }

        @Override
        public void afterTextChanged(Editable s) {
            // Backspace pressed
            if (textLength > newDeliveryLocation.getText().length()) {
                newLocRespCenterHd.setText("N/A");
                newLocAddress.setText("N/A");
            }
        }
    };

    public void backButton(View view) {
//        if (checkServerResponse(true) == OK) {
        super.onBackPressed();
        //      }
    }

    public void continueButton(View view) {
        if (!summaryToLocationMap.containsKey(newDeliveryLocation.getText().toString())) {
            if (newDeliveryLocation.getText().length() > 0) {
                Toasty.displayCenteredMessage(this, "You must enter a valid Delivery Location.", Toast.LENGTH_SHORT);
            } else {
                Toasty.displayCenteredMessage(this, "You must enter a new Delivery Location.", Toast.LENGTH_SHORT);
            }
            return;
        }
        AlertDialog.Builder confirmDialog = new AlertDialog.Builder(this);
        confirmDialog.setCancelable(false);
        confirmDialog.setTitle(Html.fromHtml("<font color='#000055'>Change Delivery Location</font>"));
        confirmDialog.setMessage(Html.fromHtml("Are you sure you want to change the delivery location to " + newLocation.getCdlocat() + "?"));
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
                changeLocation();
            }
        });
        confirmDialog.show();
    }

    private class ChangeDeliveryLocation extends AsyncTask<Void, Void, Integer> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            HttpClient httpClient = LoginActivity.getHttpClient();
            HttpResponse response = null;
            String url = (String) LoginActivity.properties.get("WEBAPP_BASE_URL");
            if (!url.endsWith("/")) {
                url += "/";
            }
            url += "ChangeDeliveryLocation?nuxrpd=" + pickup.getNuxrpd() + "&cdloc=" + newLocation.getCdlocat();
            url += "&userFallback=" + LoginActivity.nauser;

            try {
                new HttpUtils().updateOffline(url);  // testing only

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
            Intent intent = new Intent(ChangePickupDestination.this, EditPickupMenu.class);
            intent.putExtra("nuxrpd", Integer.toString(pickup.getNuxrpd()));
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            overridePendingTransition(R.anim.in_right, R.anim.out_left);
            HttpUtils.displayResponseResults(ChangePickupDestination.this, response);
        }
    }

    private String parseCdLoc(String summary) {
        String[] split = summary.split("-");
        return split[0];
    }
}
