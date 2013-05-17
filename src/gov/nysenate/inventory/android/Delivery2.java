package gov.nysenate.inventory.android;




import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


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
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Delivery2 extends SenateActivity {
	public TextView loc_details;
	String status=null;
	String URL=null;
	String res=null;
	public ArrayList<String> deliveryList = new ArrayList<String>();
	public String locCode=null;
	ListView listview;
	String location ="";
	Intent intent;
	static ProgressBar progressBarDelivery2;
	static Button btnDelivery2Cancel;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_delivery2);
		
		//define progressBar
		
		progressBarDelivery2 = (ProgressBar) findViewById( R.id.progressBarDelivery2 );
			
		
		
		//define intent
		
			
		 intent = new Intent(this, Delivery3.class);
		//1. Get the intent from Delivery1 activity and display it
		
		location = getIntent().getStringExtra("location");
		loc_details=(TextView)findViewById(R.id.textView2 );
		loc_details.setText(location);
	
		// separate location code from the description
		String locDesc[]=location.split("-");
		locCode=locDesc[0];
		
		//2. Display all the in transit moves for the current location 
		// check network connection
				ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
				if (networkInfo != null && networkInfo.isConnected()) {
					// fetch data
					status = "yes";

					// Get the URL from the properties
					URL = MainActivity.properties.get("WEBAPP_BASE_URL").toString();

					AsyncTask<String, String, String> resr1 = new RequestTask()
							.execute(URL + "/DeliveryList?loc_code="+locCode);
					try {
						res = resr1.get().trim().toString();
						// code for JSON

						String jsonString = resr1.get().trim().toString();
						JSONArray jsonArray = new JSONArray(jsonString);

						for (int i = 0; i < jsonArray.length(); i++) {
							deliveryList.add(jsonArray.getString(i).toString());
						}

						ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
								android.R.layout.simple_dropdown_item_1line,
								deliveryList);
						
						listview	 = (ListView) findViewById(R.id.listView1);	
						listview.setAdapter(adapter);
										
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
				} else {
					// display error
					status = "no";
				}
		// listener for list click
				listview.setTextFilterEnabled(true);
				listview.setOnItemClickListener(new OnItemClickListener() {

				
					 public void onItemClick(AdapterView<?> parent, View view, int position, long id) { 
						 progressBarDelivery2.setVisibility(ProgressBar.VISIBLE);
						 // this will go to the Delivery 3 activity with the data 
                           String selectedPickup= deliveryList.get(position);
                           String [] itemDetails=selectedPickup.split(":");
                   		   String nuxrpickup=itemDetails[0];
                   		
                           intent.putExtra("location", location); 
                           intent.putExtra("nuxrpickup", nuxrpickup); 

                           startActivity(intent);
                           overridePendingTransition(R.anim.in_right, R.anim.out_left);
                   			Log.i("I am here","ListView"+position);
						     }
					 
				
				});
	}

	
	@Override
	protected void onResume() {
		super.onResume();  
		btnDelivery2Cancel = (Button) findViewById(R.id.btnDelivery2Cancel);
		btnDelivery2Cancel.getBackground().setAlpha(255);   	  
	}		
	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_delivery2, menu);
		return true;
	}

	
	public void cancelButton(View view) {
		this.btnDelivery2Cancel.getBackground().setAlpha(45);
		Intent intent = new Intent(this, Delivery1.class);
		startActivity(intent);
		overridePendingTransition(R.anim.in_left, R.anim.out_right);
	}

		

}
