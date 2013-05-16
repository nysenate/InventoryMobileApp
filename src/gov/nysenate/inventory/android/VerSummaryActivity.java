package gov.nysenate.inventory.android;



import java.util.ArrayList;

import java.util.concurrent.ExecutionException;


import org.json.JSONArray;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

public class VerSummaryActivity extends SenateActivity {
	ArrayList<String> AllScannedItems=new ArrayList<String>();// for saving items which are not allocated to that location
	ArrayList<String> missingItems=new ArrayList<String>();// for saving items which are not allocated to that location
	ArrayList<String> newItems=new ArrayList<String>();// for saving items which are not allocated to that location
	ArrayList<String> scannedBarcodeNumbers= new ArrayList<String>();	    
	public  String res=null;
	String loc_code=null;
	
	static Button  btnVerSumBack;
	static Button  btnVerSumCont;
	ProgressBar  progressVerSum;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ver_summary);

		
		// Code for tab
		
				TabHost tabHost=(TabHost)findViewById(android.R.id.tabhost);
		    	tabHost.setup();

		    	TabSpec spec1=tabHost.newTabSpec("Tab 1");
		    	spec1.setContent(R.id.tab1);
		    	spec1.setIndicator("Scanned");

		    	TabSpec spec2=tabHost.newTabSpec("Tab 2");
		    	spec2.setIndicator("Missing");
		    	spec2.setContent(R.id.tab2);

		    	TabSpec spec3=tabHost.newTabSpec("Tab 3");
		    	spec3.setIndicator("New/Found");
		    	spec3.setContent(R.id.tab3);

		    	tabHost.addTab(spec1);
		    	tabHost.addTab(spec2);
		    	tabHost.addTab(spec3);
		    	
		    	// Find the ListView resource.   
		      ListView  ListViewTab1 = (ListView) findViewById( R.id.listView1);  
		      ListView  ListViewTab2 = (ListView) findViewById( R.id.listView2);
		      ListView  ListViewTab3 = (ListView) findViewById( R.id.listView3);
		      
		      // Setup Buttons and Progress Bar
		      this.progressVerSum = (ProgressBar)findViewById ( R.id.progressVerSum );
		      this.btnVerSumBack = (Button)findViewById ( R.id.btnVerSumBack );
		      this.btnVerSumBack.getBackground().setAlpha(255);
		      this.btnVerSumCont = (Button)findViewById ( R.id.btnVerSumCont );
		      this.btnVerSumCont.getBackground().setAlpha(255);
		      
		        // get Lists from intent of  previous activity   
		        
		      AllScannedItems = getIntent().getStringArrayListExtra("scannedList");
		      missingItems = getIntent().getStringArrayListExtra("missingList");
		      newItems = getIntent().getStringArrayListExtra("newItems");
		      scannedBarcodeNumbers=getIntent().getStringArrayListExtra("scannedBarcodeNumbers");
		      loc_code=getIntent().getStringExtra("loc_code");
		      String summary=getIntent().getStringExtra("summary");
		      
		      TextView locCodeView = (TextView)findViewById(R.id.textView2 );
		      locCodeView.setText(loc_code);
		      
		      TextView summeryView = (TextView)findViewById(R.id.textView3 );
		      summeryView.setText(summary);
		      
    
		        // Create ArrayAdapter using the planet list.  
		     Adapter  listAdapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, AllScannedItems);  
		     Adapter  listAdapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,missingItems );
		     Adapter  listAdapter3 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,newItems );
		     
		        // Set the ArrayAdapter as the ListView's adapter.  
		     ListViewTab1.setAdapter( (ListAdapter) listAdapter1 ); 
		     ListViewTab2.setAdapter((ListAdapter) listAdapter2);
		     ListViewTab3.setAdapter((ListAdapter) listAdapter3); 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_ver_summary, menu);
		return true;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	    // Setup Buttons and Progress Bar
	    this.progressVerSum = (ProgressBar)findViewById ( R.id.progressVerSum );
	    this.btnVerSumBack = (Button)findViewById ( R.id.btnVerSumBack );
	    this.btnVerSumBack.getBackground().setAlpha(255);
	    this.btnVerSumCont = (Button)findViewById ( R.id.btnVerSumCont );
	    this.btnVerSumCont.getBackground().setAlpha(255);
	}

	
	public void backButton(View view){
		
	    this.btnVerSumBack.getBackground().setAlpha(45);	
		Intent intent = new Intent(this, ListtestActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.in_left, R.anim.out_right);
	
		
	}
	public void continueButton(View view){
		
	    this.btnVerSumCont.getBackground().setAlpha(45);		
		progressVerSum.setVisibility(ProgressBar.VISIBLE);
		//new VerSummeryActivity().sendJsonString(scannedBarcodeNumbers);
		String jsonString=null;
		String status=null;
		JSONArray jsArray = new JSONArray(scannedBarcodeNumbers);
	
	  String barcodeNum="" ;
	  
	  for(int i=0;i<scannedBarcodeNumbers.size();i++){
		  barcodeNum=barcodeNum+scannedBarcodeNumbers.get(i).toString()+",";
	  }
		
		// Create a JSON  string from the arraylist	
	/*
	 * WORK ON IT LATER (SENDING THE STRING AS JSON)
	 * 	JSONObject jo=new JSONObject();//  =jsArray.toJSONObject("number");
		try {
	
			//jo.putOpt("barcodes",scannedBarcodeNumbers.toString());
			jsonString=jsArray.toString();	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
     */		
		
	
	  
	// Send it to the server	
		
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
			 				
							resr1 = new RequestTask().execute(URL+"/VerificationReports?loc_code="+loc_code+"&barcodes="+barcodeNum);
								
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
		
		// ===================ends
		Intent intent = new Intent(this, MenuActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.in_right, R.anim.out_left);
		
	}

	
	
	
}

