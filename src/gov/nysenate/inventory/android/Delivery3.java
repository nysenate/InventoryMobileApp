package gov.nysenate.inventory.android;



import gov.nysenate.inventory.android.VerSummaryActivity.RequestTask;

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
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Delivery3 extends Activity {
	public TextView loc_details;
	String location="";
    String nuxrpd="";
    String status="";
    String URL="";
    String res="";
	ListView listview;
	String NUXRACCPTSIGN="";
	String NADELIVERBY="";
	String NAACCEPTBY="";
	public ArrayList<String> deliveryDetails = new ArrayList<String>();
	//ArrayList<DeliveryItem> deliveryList = new ArrayList<DeliveryItem>();
	//DeliveryItemViewAdapter adapter2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_delivery3);
		// Get the data from previous activity
		Intent intent = getIntent();
		location = intent.getStringExtra("location");
		nuxrpd = intent.getStringExtra("nuxrpickup").trim();

		
		// Set the location in textview
		loc_details=(TextView)findViewById(R.id.textView2 );
		loc_details.setText(location);	
		//Get the barcode numbers from the server and set it to the listview
	
		
		// check network connection
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			// fetch data
			status = "yes";
		
			// Get the URL from the properties
			URL = MainActivity.properties.get("WEBAPP_BASE_URL")
					.toString();
			
			
			AsyncTask<String, String, String> resr1 = new RequestTask()
					.execute(URL+"/DeliveryDetails?nuxrpickup="+nuxrpd);
			
			try {
				res = resr1.get().trim().toString();
				// code for JSON

				String jsonString = resr1.get().trim().toString();
				JSONArray jsonArray = new JSONArray(jsonString);

				for (int i = 0; i < jsonArray.length(); i++) {
					deliveryDetails.add(jsonArray.getString(i).toString());
				//	DeliveryItem d= new DeliveryItem(jsonArray.getString(i).toString(),true);
				//	deliveryList.add(d);
				}
			
				// Display the pickup data 
				/*ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
						android.R.layout.simple_dropdown_item_1line,
						deliveryDetails);
				// custom adapter for checkbox with textview
				adapter2= new DeliveryItemViewAdapter(this,
						R.layout.delivery_row,
						deliveryList);   
			*/	
				listview	 = (ListView) findViewById(R.id.listView1);
				listview.setItemsCanFocus(false);
				 listview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
				 
			
				 
			//	listview.setAdapter(adapter2);
			
				listview.setAdapter(new ArrayAdapter<String>(this,
		                android.R.layout.simple_list_item_multiple_choice, deliveryDetails));
                // set everything as checked
				 for(int i=0;i<deliveryDetails.size();i++){
					 listview.setItemChecked(i, true);	 
				 }
			
			
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
		// currently hardcoding
		// Brian : Please assign values to following variables after saving the signature and name
		NUXRACCPTSIGN="1111";
		NADELIVERBY="BH";
		NAACCEPTBY="Abc,xyz";// note : we need to have comma in name (query is formated that way)
		
		
		
	}
	
	public void okButton(View view) {
		// when ok button is pressed 
		//1. create a list of all checked items (barcodes only no description)
		
		ArrayList<String> checkedItems= new ArrayList<String>();
		ArrayList<String> deliveryItemsBarcodes=new ArrayList<String>();
		String deliveryItemsStr="";
		String checkedStr="";
		for (int i = 0; i < deliveryDetails.size(); i++) {
			String itemDesc[]=deliveryDetails.get(i).split(":");
			String item=itemDesc[0];
			deliveryItemsBarcodes.add(item);
			deliveryItemsStr=deliveryItemsStr+item+",";
			}
		
		//int checkedItems = listview.getCheckedItemCount();
		//Log.i("checkedItems", "onItemSelected- checkedItems" + checkedItems);
		long checkedI[] = listview.getCheckedItemIds();

		long[] checkedItemsId =  listview.getCheckItemIds();// to get position of all checked items
		for (int i = 0; i < checkedItemsId.length; i++) {
			String itemDesc[]=deliveryDetails.get((int) checkedItemsId[i]).split(":");
			String item=itemDesc[0];
			checkedItems.add(item);
			Log.i("checkedId intent for loop ",
					"onItemSelected-checkedId for loop " + checkedItemsId[i]);
			checkedStr=checkedStr+checkedItemsId[i]+",";
		} 
	
		Log.i("ok button  ",
				"deliveryItemsBarcodes list " + deliveryItemsStr);
		Log.i("ok button  ",
				"checkedItemsId list " + checkedStr);
		
		//2. send both the lists to the server ( nuxrpd,deliveryList,checkedItemsId) 
		
		// check network connection
 		 ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 			    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
 			    if (networkInfo != null && networkInfo.isConnected()) {
 			        // fetch data
 			    	status="yes";
									
 					AsyncTask<String, String, String> resr1;
						try {
							// Get the URL from the properties 
			 			    String   URL=MainActivity.properties.get("WEBAPP_BASE_URL").toString();    
			 				
							resr1 = new RequestTask().execute(URL+"/DeliveryConfirmation?nuxrpd="+nuxrpd+"&NUXRACCPTSIGN="+NUXRACCPTSIGN+"&NADELIVERBY="+NADELIVERBY+"&NAACCEPTBY="+NAACCEPTBY+"&deliveryItemsStr="+deliveryItemsStr+"&checkedStr="+checkedStr);

								 res=resr1.get().trim().toString();
						
 						} catch (InterruptedException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						} catch (ExecutionException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						} 
 								status="yes1";
 				  } else {
 					   // display error
 					   	status="no";
 			      }
 		

		// Display Toster 
 				Context context = getApplicationContext();
 				CharSequence text = res;
 				if(res.length()==0){
					text="Database not updated";
				}
 				
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(context, text, duration);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
		
		
		// 3. send the intent to the Menu Activity
		
		Intent intent = new Intent(this, MenuActivity.class);
		startActivity(intent);
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
