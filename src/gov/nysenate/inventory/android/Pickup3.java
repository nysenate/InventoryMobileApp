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
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TabHost.TabSpec;

public class Pickup3 extends Activity {
	ArrayList<String> AllScannedItems=new ArrayList<String>();// for saving items which are not allocated to that location
	ArrayList<Integer> scannedBarcodeNumbers= new ArrayList<Integer>();	    
	public  String res=null;
	String loc_code=null;
	String originLocation=null;
	String destinationLocation=null;
	String count=null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pickup3);
		

    	// Find the ListView resource.   
      ListView  ListViewTab1 = (ListView) findViewById( R.id.listView1);  
      
      // get data from intent of  previous activity   
      AllScannedItems = getIntent().getStringArrayListExtra("scannedList");
      originLocation= getIntent().getStringExtra("originLocation");
      destinationLocation=getIntent().getStringExtra("destinationLocation");
      count=getIntent().getStringExtra("count");
      scannedBarcodeNumbers=getIntent().getIntegerArrayListExtra("scannedBarcodeNumbers");
      loc_code=getIntent().getStringExtra("loc_code");
      
      // Create summary from the given information
     	String summery="Origin Location      : "+ originLocation+"\n"+
		       "Destination location : "+destinationLocation+"\n"+"\n"+
		       "Total items          : "+count;
      // Set the summary to the textview
      TextView summeryView = (TextView)findViewById(R.id.textView3 );
      summeryView.setText(summery);
      
      
     Adapter  listAdapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, AllScannedItems);  
     
        // Set the ArrayAdapter as the ListView's adapter.  
     ListViewTab1.setAdapter( (ListAdapter) listAdapter1 ); 

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_pickup3, menu);
		return true;
	}

	
public void okButton(View view){
		
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
		
	// call the servlet image upload and return the nuxrsign
	  
	  String NAPICKUPBY="";
	  String NUXRPUSIGN="";
	  String NUXRRELSIGN="";
	  String NARELEASEBY="";
	  
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
			 				resr1 = new RequestTask().execute(URL+"/Pickup?originLocation="+originLocation+"&destinationLocation="+destinationLocation+"&barcodes="+barcodeNum+"&NAPICKUPBY="+NAPICKUPBY+"&NUXRPUSIGN="+NUXRPUSIGN+"&NUXRRELSIGN="+NUXRRELSIGN+"&NARELEASEBY="+NARELEASEBY);
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
	}

	
	class RequestTask extends AsyncTask<String, String, String>{

	    @Override
	    protected String doInBackground(String... uri) {
	        HttpClient httpclient = new DefaultHttpClient();
	        HttpResponse response;
	        String responseString = null;
	        try {
	        	
	           response = httpclient.execute(new HttpGet(uri[0]));
	       //   HttpPost hp=   	new HttpPost(uri[0]);
	        	//HttpGet hp=   	new HttpGet(uri[0]);
	        //	hp.setHeader("Content-Type", "application/json"); // just for this we want the variable to be json object
	        //	hp.setEntity(new ByteArrayEntity(
	        //			scannedBarcodeNumbers.toString().getBytes("UTF8"))) ;
	      //  	response = httpclient.execute(hp);
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
	
}
