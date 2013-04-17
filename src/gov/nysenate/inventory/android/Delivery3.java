package gov.nysenate.inventory.android;



import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class Delivery3 extends Activity {
	public TextView loc_details;
	String location="";
    String nuxrpickup="";
    String status="";
    String URL="";
    String res="";
	ListView listview;
	public ArrayList<String> deliveryDetails = new ArrayList<String>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_delivery3);
		// Get the data from previous activity
		Intent intent = getIntent();
		location = intent.getStringExtra("location");
		nuxrpickup = intent.getStringExtra("nuxrpickup").trim();
		Log.i("1", "loc: "+location);
		Log.i("1", "nuxrpickup: "+nuxrpickup);
		
		// Set the location in textview
		loc_details=(TextView)findViewById(R.id.textView2 );
		loc_details.setText(location);	
		//Get the barcode numbers from the server and set it to the listview
		Log.i("2", "before  ");
		
		// check network connection
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			// fetch data
			status = "yes";
			Log.i("3", "inside try  ");
			// Get the URL from the properties
			URL = MainActivity.properties.get("WEBAPP_BASE_URL")
					.toString();
			
			
			AsyncTask<String, String, String> resr1 = new RequestTask()
					.execute(URL+"/DeliveryDetails?nuxrpickup="+nuxrpickup);
			
			try {
				res = resr1.get().trim().toString();
				// code for JSON

				String jsonString = resr1.get().trim().toString();
				JSONArray jsonArray = new JSONArray(jsonString);

				for (int i = 0; i < jsonArray.length(); i++) {
					deliveryDetails.add(jsonArray.getString(i).toString());
				}
			
				// Display the pickup data 
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
						android.R.layout.simple_dropdown_item_1line,
						deliveryDetails);
				
				listview	 = (ListView) findViewById(R.id.listView1);	
				listview.setAdapter(adapter);
			
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.i("InterruptedException",e.getMessage());
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				Log.i("ExecutionException",e.getMessage() );
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Log.i("JSONException",e.getMessage() );
				e.printStackTrace();
			}
			status = "yes1";
		} else {
			// display error
			status = "no";
		}
		
	
		// Signature from 'Accepted By'
		
		// Save the signature on server (Received By), comments, Name
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_delivery3, menu);
		return true;
	}
	
	
	// class for connecting to internet and sending HTTP request to server
	class RequestTask extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... uri) {
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response;
			String responseString = null;
			try {
				response = httpclient.execute(new HttpGet(uri[0]));
				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					response.getEntity().writeTo(out);
					out.close();
					responseString = out.toString();
				} else {
					// Closes the connection.
					response.getEntity().getContent().close();
					throw new IOException(statusLine.getReasonPhrase());
				}
			} catch (ClientProtocolException e) {
				// TODO Handle problems..
			} catch (IOException e) {
				// TODO Handle problems..
			}
			res = responseString;
			
			return responseString;

		}
	}
}
