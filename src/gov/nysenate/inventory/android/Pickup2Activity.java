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
import org.json.JSONObject;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Pickup2Activity extends Activity {
	public EditText barcode;
	public EditText count_text;
	public TextView loc_details;
	public TextView TextView2;
	public  String res=null;
	public	String status=null;
	public ListView listView;
	public String loc_code=null; // populate this from the location code from previous activity
    ArrayList<String> scannedItems= new ArrayList<String>();
	ArrayList<verList> list = new ArrayList<verList>();
	ArrayList<StringBuilder> dispList = new ArrayList<StringBuilder>();
	ArrayAdapter<StringBuilder> adapter ;
    int count;	
    int numItems;
    public String originLocation=null;
    public String destinationLocation=null;
   // These 3 ArrayLists will be used to transfer data to next activity and to the server
    ArrayList<String> AllScannedItems=new ArrayList<String>();// for saving items which are not allocated to that location
    ArrayList<String> newItems=new ArrayList<String>();// for saving items which are not allocated to that location
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pickup2);
		// Get the origin and destination location from the previous activity
		Intent intent = getIntent();
		originLocation = intent.getStringExtra("originLocation");
		destinationLocation= intent.getStringExtra("destinationLocation");
		
		listView = (ListView) findViewById(R.id.listView1);
		count = 0;// for initialization
		adapter = new ArrayAdapter<StringBuilder>(this,
				android.R.layout.simple_list_item_1, dispList);
	
		// Display the origin and destination 
		TextView TextView2= (TextView) findViewById(R.id.textView2);
		TextView2.setText("Origin : "+ originLocation+"\n"
		                 +"Destination : "+destinationLocation);
		// display the count on screen
		count_text = (EditText) findViewById(R.id.editText2);

		count_text.setText(Integer.toString(count));
		// populate the listview
		listView.setAdapter(adapter);

		// code for textwatcher

		barcode = (EditText) findViewById(R.id.editText1);
		barcode.addTextChangedListener(filterTextWatcher);

	}

	private TextWatcher filterTextWatcher = new TextWatcher() {

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		public void afterTextChanged(Editable s) {
			if (barcode.getText().toString().length() >= 6) {
				String barcode_num = barcode.getText().toString().trim();
				String barcode_number =barcode_num;

				int flag = 0;

				// If the item is already scanned then display a
				// toster"Already Scanned"
				if (scannedItems.contains(barcode_number) == true) {
					// display toster
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
				if ((flag == 0)
						&& (scannedItems.contains(barcode_number) == false)) {

					// check network connection
					ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
					NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
					if (networkInfo != null && networkInfo.isConnected()) {
						// fetch data
						status = "yes";
						// int barcode= Integer.parseInt(barcode_num);
						// scannedItems.add(barcode);
					// Get the URL from the properties 
	 			    String   URL=MainActivity.properties.get("WEBAPP_BASE_URL").toString();    
	 				
						AsyncTask<String, String, String> resr1 = new RequestTask()
								.execute(URL+"/itemDetails?barcode_num="
										+ barcode_num);
						try {
							res = resr1.get().trim().toString();

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

					// add it to list and displist and scanned items
					verList vl = new verList();
					vl.NUSENATE = barcode_number;
					vl.CDCATEGORY = res;
					vl.DECOMMODITYF = "";

					list.add(vl);
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

					dispList.add(s_new); // this list will display the contents
											// on screen
					scannedItems.add(barcode_number);
					AllScannedItems.add(s_new.toString());
					newItems.add(vl.NUSENATE + " " + vl.CDCATEGORY);// to keep
																	// track of
																	// (number+details)
																	// for
																	// summery
				}

				// notify the adapter that the data in the list is changed and
				// refresh the view
				adapter.notifyDataSetChanged();
				count = list.size();
				count_text.setText(Integer.toString(count));
				listView.setAdapter(adapter);
				barcode.setText("");
			}
		}
	};
    
	class RequestTask extends AsyncTask<String, String, String>{
    @Override
	    protected String doInBackground(String... uri) {
	        HttpClient httpclient = new DefaultHttpClient();
	        HttpResponse response;
	        String responseString = null;
	        try {
	            response = httpclient.execute(new HttpGet(uri[0]));
	            StatusLine statusLine = response.getStatusLine();
	            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
	                ByteArrayOutputStream out = new ByteArrayOutputStream();
	                response.getEntity().writeTo(out);
	                out.close();
	                responseString = out.toString();
	            } else{
	                //Closes the connection.
	                response.getEntity().getContent().close();
	                throw new IOException(statusLine.getReasonPhrase());
	            }
	        } catch (ClientProtocolException e) {
	            //TODO Handle problems..
	        } catch (IOException e) {
	            //TODO Handle problems..
	        }
	        res=responseString;
	        return responseString;
	    }
	}
	
	public void okButton(View view){
		// send the data to Pickup3 activity
		Intent intent = new Intent(this, Pickup3.class); 
		intent.putExtra("originLocation", originLocation);
		intent.putExtra("destinationLocation", destinationLocation);
		String countStr= Integer.toString(count);
		intent.putExtra("count", countStr);
		intent.putStringArrayListExtra("scannedBarcodeNumbers", scannedItems);
		intent.putStringArrayListExtra("scannedList", AllScannedItems);//scanned items list
		startActivity(intent);
	}
	
	public void cancelButton(View view){
		Intent intent = new Intent(this, Pickup1.class);
		startActivity(intent);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_pickup2, menu);
		return true;
	}
	public class verList {
    String NUSENATE;
    String CDCATEGORY;
    String DECOMMODITYF;
}
}
