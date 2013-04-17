package gov.nysenate.inventory.android;

import gov.nysenate.inventory.android.ListtestActivity.verList;
import gov.nysenate.inventory.android.Pickup1.RequestTask;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
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


import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
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
	private SignatureView sign;
	private byte[] imageInByte = {};
	Intent intent = getIntent();	
	String originLocationCode="";
	String destinationLocationCode="";
	public ArrayList<Employee> employeeHiddenList = new ArrayList<Employee>();
	public ArrayList<String> employeeNameList = new ArrayList<String>();
	ClearableAutoCompleteTextView naemployeeView;
	int nuxrefem = -1; 
	String requestTaskType = ""; 
	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
	ClearableEditText commentsEditText;
	
	public String status = null;
	String URL;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pickup3);
		sign = (SignatureView) findViewById(R.id.blsignImageView);
		sign.setMinDimensions(200, 100);
        commentsEditText = (ClearableEditText) findViewById(R.id.pickupCommentsEditText);
        commentsEditText.setClearMsg("Do you want to clear the Pickup Comments?");
        commentsEditText.showClearMsg(true);
		 //sign.setInitialBitmap( BitmapFactory.decodeResource(getResources(),  R.drawable.simplethinborder));
		 //sign.setBackgroundResource(R.drawable.smove);
		 //sign.setPadding(2,2,0,0);

    	// Find the ListView resource.   
      ListView  ListViewTab1 = (ListView) findViewById( R.id.listView1);  
      
      // get data from intent of  previous activity   
      AllScannedItems = getIntent().getStringArrayListExtra("scannedList");
      originLocation= getIntent().getStringExtra("originLocation");
      destinationLocation=getIntent().getStringExtra("destinationLocation");
      count=getIntent().getStringExtra("count");
      scannedBarcodeNumbers=getIntent().getIntegerArrayListExtra("scannedBarcodeNumbers");
      loc_code=getIntent().getStringExtra("loc_code");
      
      String originLocationCodeArr[]=originLocation.split("-");
      originLocationCode=originLocationCodeArr[0];
      String destinationLocationCodeArr[]=destinationLocation.split("-");
      destinationLocationCode=destinationLocationCodeArr[0];
  
      // Create summary from the given information
     	String summery="Origin Location      : "+ originLocation+"\n"+
		       "Destination location : "+destinationLocation+"\n"+"\n"+
		       "Total items          : "+count;
      // Set the summary to the textview
      TextView summeryView = (TextView)findViewById(R.id.textView3 );
      naemployeeView = (ClearableAutoCompleteTextView) findViewById(R.id.naemployee);
      naemployeeView.setClearMsg("Do you want to clear the name of the signer?");
      naemployeeView.showClearMsg(true);
      naemployeeView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
    	    @Override
    	    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    	    	 InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                 imm.hideSoftInputFromWindow(
                		 naemployeeView.getWindowToken(), 0);    	        
    	    }
    	});

      summeryView.setText(summery);
      
      
     Adapter  listAdapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, AllScannedItems);  
     
        // Set the ArrayAdapter as the ListView's adapter.  
     ListViewTab1.setAdapter( (ListAdapter) listAdapter1 ); 
     
     // Get the Employee Name List from the Web Service and populate the Employee Name Autocomplete Field with it
     
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			// fetch data
			status = "yes";

			// Get the URL from the properties
			URL = MainActivity.properties.get("WEBAPP_BASE_URL").toString();
			requestTaskType = "EmployeeList";
			AsyncTask<String, String, String> resr1 = new RequestTask()
					.execute(URL + "/EmployeeList");
			try {
				res = resr1.get().trim().toString();
				// code for JSON

				String jsonString = resr1.get().trim().toString();
				JSONArray jsonArray = new JSONArray(jsonString);
				for (int x=0;x<jsonArray.length();x++) {
					JSONObject jo = new JSONObject();
					jo = jsonArray.getJSONObject(x);
					Employee currentEmployee = new Employee();
					currentEmployee.setEmployeeData(jo.getInt("nuxrefem"), jo.getString("naemployee"));
					employeeHiddenList.add(currentEmployee);
					employeeNameList.add( jo.getString("naemployee"));
				}
				
				Collections.sort(employeeNameList);
				
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
						android.R.layout.simple_dropdown_item_1line,
						employeeNameList);
				
				// for origin dest code
				naemployeeView.setThreshold(1);
				naemployeeView.setAdapter(adapter);
				// for destination code

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

		// code for textwatcher
		// for origin location code
		// loc_code = (EditText) findViewById(R.id.editText1);
		// loc_code.addTextChangedListener(filterTextWatcher);
	
		naemployeeView.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				String employeeSelected = naemployeeView.getText().toString();
				int employeeFoundAt = findEmployee(employeeSelected);
				System.out.println("EMPLOYEE SELECTED:"+employeeSelected+" FOUND AT:"+employeeFoundAt);
				if (employeeSelected==null||employeeSelected.length()==0) {
					nuxrefem = -1;
					Context context = getApplicationContext();
					int duration = Toast.LENGTH_SHORT;

					Toast toast = Toast
							.makeText(
									context,
									"No Employee entered.",
									3000);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
				else if (employeeFoundAt==-1) {
					nuxrefem = -1;
					Context context = getApplicationContext();
					int duration = Toast.LENGTH_SHORT;

					Toast toast = Toast
							.makeText(
									context,
									"Employee not found.",
									3000);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
				else {
					nuxrefem = employeeHiddenList.get(employeeFoundAt).getEmployeeXref();
					Context context = getApplicationContext();
					int duration = Toast.LENGTH_SHORT;
					Toast toast = Toast
							.makeText(
									context,
									"Employee xref#:"+nuxrefem+" Name:"+employeeHiddenList.get(employeeFoundAt).getEmployeeName(),
									3000);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				nuxrefem = -1; 
			}
		});
	}
	
	public int findEmployee(String employeeName) {
		for (int x=0;x<employeeHiddenList.size();x++) {
			if (employeeName.equals(employeeHiddenList.get(x).getEmployeeName())) {
				return x;
			}
		}
		return -1;
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_pickup3, menu);
		return true;
	}

	public void clearSignature(View view) {
	    Bitmap clearedSignature = BitmapFactory.decodeResource(getResources(), R.drawable.simplethinborder);
	    if (clearedSignature==null) {
	        Log.i("ClearSig", "Signature drawable was NULL");
	    }
	    else {
	        Log.i("ClearSig", "Signature size:"+clearedSignature.getWidth()+" x "+clearedSignature.getHeight());
	    }
		sign.setImage(clearedSignature);
	}
	

    /**
     * Fire an intent to start the speech recognition activity.
     */
    public void startCommentsSpeech(View view) {
        if (view.getId()==R.id.pickupCommentsSpeechButton) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Pickup Comments Speech");
            startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
        }
    }	

   /**
    * Handle the results from the recognition activity.
    */
   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
           // Fill the list view with the strings the recognizer thought it could have heard
           ArrayList<String> matches = data.getStringArrayListExtra(
                   RecognizerIntent.EXTRA_RESULTS);
       	commentsEditText.setText(matches.get(0));
       }

       super.onActivityResult(requestCode, resultCode, data);
   }    
   	
	
public void okButton(View view){
	   String employeePicked = naemployeeView.getEditableText().toString();
	   if (employeePicked.trim().length()>0) {
		   int foundEmployee = this.findEmployee(employeePicked);
		   
		   if (foundEmployee<0) {
			   nuxrefem = -1;
		   }
		   else {
		   		nuxrefem = this.employeeHiddenList.get(foundEmployee).getEmployeeXref();
		   }
	   }
	   else {
		   nuxrefem = -1;
	   }
	
	   if (nuxrefem<0) {
			Context context = getApplicationContext();
			int duration = Toast.LENGTH_SHORT;
			if (naemployeeView.getEditableText().toString().trim().length()>0) {
			Toast toast = Toast
					.makeText(
							context,
							"!!ERROR: No xref# found for employee",
							3000);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			}
			else {
				Toast toast = Toast
						.makeText(
								context,
								"!!ERROR: You must first pick an employee name for the signature.",
								3000);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();				
			}
			return;
		   
	   }
	
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
	  
	  String NAPICKUPBY="Vik";
	  String NUXRPUSIGN= "";
	  String NUXREFEM= Integer.toString(nuxrefem);
	  String NUXRRELSIGN="2345";
	  String NARELEASEBY="vRelease";
	  
	// Send it to the server	
		
		// check network connection
 		 ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 			    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
 			    if (networkInfo != null && networkInfo.isConnected()) {
 			        // fetch data
 			    	status="yes";
 			    	requestTaskType = "Pickup";				
 					AsyncTask<String, String, String> resr1;
						try {
							// Get the URL from the properties 						
							
							String   URL=MainActivity.properties.get("WEBAPP_BASE_URL").toString();
			 			    System.out.println("("+MainActivity.nauser+")");
			 				resr1 = new RequestTask().execute(URL+"/ImgUpload?nauser="+MainActivity.nauser+"&nuxrefem="+nuxrefem, URL+"/Pickup?originLocation="+originLocationCode+"&destinationLocation="+destinationLocationCode+"&barcodes="+barcodeNum+"&NAPICKUPBY="+NAPICKUPBY+"&NUXRRELSIGN="+NUXRRELSIGN+"&NARELEASEBY="+NARELEASEBY);

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

/*class ImageUploadTask extends AsyncTask<String, String, String> {

			@Override
			protected String doInBackground(String... uri) {
				//HttpClient httpclient = new DefaultHttpClient();
				//HttpResponse response;
				//String responseString = null;
				//Get connection to image upload web service
				 ByteArrayOutputStream bs = new ByteArrayOutputStream();
				 Bitmap bitmap = sign.getImage();
				 bitmap.compress(Bitmap.CompressFormat.PNG, 50, bs);
				 imageInByte = bs.toByteArray();
				 System.out.println("Image Byte Array Size:"+imageInByte.length);
				 String response = "";			
				 try {
				 URL url = new URL(uri[0]+"?filename=newimage");

				 HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				 // Set connection parameters.
				 conn.setDoInput(true);
				 conn.setDoOutput(true);
				 conn.setUseCaches(false);
				 
				 //Set content type to PNG
				 conn.setRequestProperty("Content-Type", "image/png");
				 OutputStream outputStream = conn.getOutputStream();
				 OutputStream out =  outputStream;
				 // Write out the bytes of the content string to the stream.
				 out.write(imageInByte);
				 out.flush();
				 out.close();
				 // Read response from the input stream.
				 BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				 String temp;
				 while ((temp = in.readLine()) != null) {
					 response += temp + "\n";
				 }
				 temp = null;
				 in.close();
				 System.out.println("Server response:\n'" + response + "'");
				 
				 } catch (Exception e) {
				 e.printStackTrace();
				 }	
				 return response;
			}
		}    */
	    		

	class RequestTask extends AsyncTask<String, String, String>{

	    @Override
	    protected String doInBackground(String... uri) {
	    	// First Upload the Signature and get the nuxsign from the Server
	    	if (requestTaskType.equalsIgnoreCase("Pickup")) {
    			String NUXRPUSIGN = "";
	    		
	    		ByteArrayOutputStream bs = new ByteArrayOutputStream();
	    		Bitmap bitmap = sign.getImage();
	    		bitmap.compress(Bitmap.CompressFormat.PNG, 50, bs);
	    		imageInByte = bs.toByteArray();
	    		System.out.println("Image Byte Array Size:"+imageInByte.length);
	    		String responseString = "";			
	    		try {
	    			URL url = new URL(uri[0]);

	    			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	    			// Set connection parameters.
	    			conn.setDoInput(true);
	    			conn.setDoOutput(true);
	    			conn.setUseCaches(false);
			 
	    			//Set content type to PNG
	    			conn.setRequestProperty("Content-Type", "image/png");
	    			OutputStream outputStream = conn.getOutputStream();
	    			OutputStream out =  outputStream;
	    			// Write out the bytes of the content string to the stream.
	    			out.write(imageInByte);
	    			out.flush();
	    			out.close();
	    			// Read response from the input stream.
	    			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	    			String temp;
	    			while ((temp = in.readLine()) != null) {
	    				responseString += temp + "\n";
	    			}
	    			temp = null;
	    			in.close();
	    			System.out.println("Server response:\n'" + responseString + "'");
	    			int nuxrsignLoc = responseString.indexOf("NUXRSIGN:");
	    			if (nuxrsignLoc>-1) {
	    					NUXRPUSIGN = responseString.substring(nuxrsignLoc+9).replaceAll("\r", "").replaceAll("\n", "");
	    			}
	    			else {
	    				NUXRPUSIGN = responseString.replaceAll("\r", "").replaceAll("\n", "");
	    			}
	    				
	    		} catch (Exception e) {
	    			e.printStackTrace();
	    		}	

	    		// Then post the rest of the information along with the NUXRSIGN 
	    		HttpClient httpclient = new DefaultHttpClient();
	    		HttpResponse response;
	    		responseString = null;
	    		try {
	        	
	                String pickupURL = uri[1]+"&NUXRPUSIGN="+NUXRPUSIGN;
	                System.out.println("pickupURL:"+pickupURL);
					response = httpclient.execute(new HttpGet(pickupURL));
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
	    	else if (requestTaskType.equalsIgnoreCase("EmployeeList")) {
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
	    	else {
	    		System.out.println ("!!ERROR: Invalid requestTypeTask:"+requestTaskType);
	    		return "!!ERROR: Invalid requestTypeTask:"+requestTaskType;
	    	}
	    }
	}
	

	
		
   
}
