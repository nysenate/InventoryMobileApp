package gov.nysenate.inventory.android;




import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

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
	public ArrayList<PickupGroup> pickupGroups = new ArrayList<PickupGroup>();
	public String locCode=null;
	ListView listview;
	String location ="";
	Intent intent;
	static ProgressBar progBarDelivery2;
	static Button btnDelivery2Cancel;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_delivery2);
		
		//define progressBar
		
		progBarDelivery2 = (ProgressBar) findViewById( R.id.progBarDelivery2 );
			
		
		
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
							
							for (int x=0;x<jsonArray.length();x++) {
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
								object = (JSONObject) jsonArray.getJSONObject(x);
								try {
									nuxrpd = object.getInt("nuxrpd");
								}
								catch (Exception e) {
									e.printStackTrace();
								}
								try {
									pickupDateTime = object.getString("pickupDateTime");
								}
								catch (Exception e) {
									e.printStackTrace();
								}
								try {
									pickupFrom = object.getString("pickupFrom");
								}
								catch (Exception e) {
									e.printStackTrace();
								}
								try {
									pickupRelBy = object.getString("pickupRelBy");
								}
								catch (Exception e) {
									e.printStackTrace();
								}
								try {
									pickupLocat = object.getString("pickupLocat");
								}
								catch (Exception e) {
									e.printStackTrace();
								}
								try {
									pickupItemCount = object.getInt("pickupItemCount");
								}
								catch (Exception e) {
									e.printStackTrace();
								}
								try {
									pickupAdstreet1 = object.getString("pickupAdstreet1");
								}
								catch (Exception e) {
									e.printStackTrace();
								}								
								try {
									pickupAdcity = object.getString("pickupAdcity");
								}
								catch (Exception e) {
									e.printStackTrace();
								}		
								try {
									pickupAdstate = object.getString("pickupAdstate");
								}
								catch (Exception e) {
									e.printStackTrace();
								}		
								try {
									pickupAdzipcode = object.getString("pickupAdzipcode");
								}
								catch (Exception e) {
									e.printStackTrace();
								}		
								
								
								//Log.i("JSON VALUES "+x,  object.toString());
								currentPickupGroup = new PickupGroup(nuxrpd,  pickupDateTime, pickupFrom, pickupRelBy, pickupLocat, pickupAdstreet1, pickupAdcity, pickupAdstate, pickupAdzipcode , pickupItemCount); 
								//System.out.println(nuxrpd+" ,  "+pickupDateTime+" ,"+ pickupFrom+", "+pickupRelBy+" , "+pickupLocat+" , "+pickupItemCount);
								pickupGroups.add(currentPickupGroup);
							}
							
						} catch (JSONException e) {
							// TODO Auto-generated catch block	
							e.printStackTrace();
						}
						
						//System.out.println ("pickupGroups Count:"+pickupGroups.size());
						PickupGroupViewAdapter adapter = new PickupGroupViewAdapter(this, R.layout.pickup_group_row, pickupGroups);
						//System.out.println ("Setup Listview with pickupGroups");
						
						listview	 = (ListView) findViewById(R.id.listView1);	
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
		// listener for list click
				listview.setTextFilterEnabled(true);
				listview.setOnItemClickListener(new OnItemClickListener() {

				
					 public void onItemClick(AdapterView<?> parent, View view, int position, long id) { 
						  progBarDelivery2.setVisibility(ProgressBar.VISIBLE);
						 // this will go to the Delivery 3 activity with the data
                           PickupGroup selectedPickup= pickupGroups.get(position);
                           //String [] itemDetails=selectedPickup.split(":");
                   		   String nuxrpd= Integer.toString(selectedPickup.getNuxrpd());
                   		
                           intent.putExtra("location", location); 
                           intent.putExtra("nuxrpd", nuxrpd); 

                           startActivity(intent);
                           overridePendingTransition(R.anim.in_right, R.anim.out_left);
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
		Intent intent = new Intent(this, Move.class);
		startActivity(intent);
		overridePendingTransition(R.anim.in_left, R.anim.out_right);
	}

		

}
