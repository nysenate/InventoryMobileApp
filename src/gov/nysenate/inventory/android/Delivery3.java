package gov.nysenate.inventory.android;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
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
import android.graphics.Color;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class Delivery3 extends SenateActivity {
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
    private SignatureView sign;
    private byte[] imageInByte = {};
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
    ClearableEditText commentsEditText;
    int nuxrefem = -1; 
    public ArrayList<Employee> employeeHiddenList = new ArrayList<Employee>();
    public ArrayList<String> employeeNameList = new ArrayList<String>();
    ClearableAutoCompleteTextView naemployeeView;
    String requestTaskType = "";
    String employeeList = "";
    private String DECOMMENTS = null;
    Button btnDelivery3ClrSig;
    Button btnDelivery3Back;
    Button btnDelivery3Cont;
    
    
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
		
		// Configure Image Buttons
	    btnDelivery3ClrSig = (Button) findViewById(R.id.btnDelivery3ClrSig);
	    btnDelivery3Back = (Button) findViewById(R.id.btnDelivery3Back);
	    btnDelivery3Cont = (Button) findViewById(R.id.btnDelivery3Cont);

		
        // Setup the Signature Field (sign) and Delivery Comments (commentsEditText)
        sign = (SignatureView) findViewById(R.id.blacceptsignImageView);
        sign.setMinDimensions(200, 100);
        commentsEditText = (ClearableEditText) findViewById(R.id.deliveryCommentsEditText);
        commentsEditText.setClearMsg("Do you want to clear the Delivery Comments?");
        commentsEditText.showClearMsg(true);

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
        
        
		// check network connection
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			// fetch data
			status = "yes";
		
			// Get the URL from the properties
			URL = MainActivity.properties.get("WEBAPP_BASE_URL")
					.toString();
			
			this.requestTaskType = "EmployeeDeliveryList";
			AsyncTask<String, String, String> resr1 = new RequestTask()
					.execute(URL + "/EmployeeList", URL+"/DeliveryDetails?NUXRPD="+nuxrpd);
			
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
		
	    // list of name of the employee for autocomplete (get from server and populate the autocomplete textview)
		
		
		// Signature from 'Accepted By'
		
		// Save the signature on server (Received By), comments, Name
		// currently hardcoding
		// Brian : Please assign values to following variables after saving the signature and name
		NUXRACCPTSIGN="1111";
		NADELIVERBY="BH";
		
		NAACCEPTBY="Abc,xyz";// note : we need to have comma in name (query is formated that way)
		
		// Get the results for the Employee List and now do the actual setting of the Signing Employee 
		// Dropdown.
		try {
        JSONArray jsonArray = new JSONArray(employeeList);
        for (int x=0;x<jsonArray.length();x++) {
            JSONObject jo = new JSONObject();
            jo = jsonArray.getJSONObject(x);
            Employee currentEmployee = new Employee();
            currentEmployee.setEmployeeData(jo.getInt("nuxrefem"), jo.getString("naemployee"));
            employeeHiddenList.add(currentEmployee);
            employeeNameList.add( jo.getString("naemployee"));
        }
		} catch (JSONException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}        
        
        Collections.sort(employeeNameList);
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line,
                employeeNameList);
        
        // for origin dest code
        naemployeeView.setThreshold(1);
        naemployeeView.setAdapter(adapter);
		Delivery2.progressBarDelivery2.setVisibility(ProgressBar.INVISIBLE);
		
		
	}

    public int findEmployee(String employeeName) {
        for (int x=0;x<employeeHiddenList.size();x++) {
            if (employeeName.equals(employeeHiddenList.get(x).getEmployeeName())) {
                return x;
            }
        }
        return -1;
    }
	
	public void continueButton(View view) {
	    // when ok button is pressed 
	    // 1. First validate to ensure that an employee name was picked and the
	    //    employee signed his name.
		float alpha = 0.45f;
		AlphaAnimation alphaUp = new AlphaAnimation(alpha, alpha);
		alphaUp.setFillAfter(true);
		this.btnDelivery3Cont.startAnimation(alphaUp);		
	    
	    String employeePicked = naemployeeView.getEditableText().toString();
	    NAACCEPTBY = "";
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
	                            duration);
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
	    
	       if (!sign.isSigned()) {
	           Context context = getApplicationContext();           
	           Toast toast = Toast
	                   .makeText(
	                           context,
	                           "!!ERROR: Employee must also sign within the Red box.",
	                           3000);
	           toast.setGravity(Gravity.CENTER, 0, 0);
	           toast.show();               
	           return;
	       }       
	       	    
	       try {
	           NAACCEPTBY = URLEncoder.encode(this.naemployeeView.getText().toString(), "UTF-8");
	       } catch (UnsupportedEncodingException e1) {
	           // TODO Auto-generated catch block
	           e1.printStackTrace();
	       }
	       
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
			checkedStr=checkedStr+checkedItemsId[i]+",";
		} 
	
		DECOMMENTS = null;
	      try {
	          DECOMMENTS = URLEncoder.encode(this.commentsEditText.getText().toString(), "UTF-8");
	      } catch (UnsupportedEncodingException e1) {
	         // TODO Auto-generated catch block
	         e1.printStackTrace();
	      }
		
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
			 				this.requestTaskType = "Delivery";
							resr1 = new RequestTask().execute(URL+"/ImgUpload?nauser="+MainActivity.nauser+"&nuxrefem="+nuxrefem,URL+"/DeliveryConfirmation?NUXRPD="+nuxrpd+"&NADELIVERBY="+MainActivity.nauser+"&NAACCEPTBY="+NAACCEPTBY+"&deliveryItemsStr="+deliveryItemsStr+"&checkedStr="+checkedStr);
							
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
		
		
		// 3. send the intent to the Move Menu Activity
		
//		Intent intent = new Intent(this, MenuActivity.class);
		Intent intent = new Intent(this, Move.class);
		startActivity(intent);
        overridePendingTransition(R.anim.in_right, R.anim.out_left);
        alphaUp = new AlphaAnimation(1f, 1f);
		alphaUp.setFillAfter(true);		
		this.btnDelivery3Cont.startAnimation(alphaUp);		
		
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_delivery3, menu);
		return true;
	}

	public void clearSignatureButton(View view) {
	        Bitmap clearedSignature = BitmapFactory.decodeResource(getResources(), R.drawable.simplethinborder);
	        if (clearedSignature==null) {
	            Log.i("ClearSig", "Signature drawable was NULL");
	        }
	        else {
	            Log.i("ClearSig", "Signature size:"+clearedSignature.getWidth()+" x "+clearedSignature.getHeight());
	        }
	        sign.clearSignature();
	 }
	    

	    /**
	     * Fire an intent to start the speech recognition activity.
	     */
	    public void startCommentsSpeech(View view) {
	        if (view.getId()==R.id.deliveryCommentsSpeechButton) {
	            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
	            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
	                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
	            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Delivery Comments Speech");
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
	   
	   
		public void backButton(View view) {
			this.finish();
	        overridePendingTransition(R.anim.in_left, R.anim.out_right);
		}
	    
	
	
	// class for connecting to internet and sending HTTP request to server
	class RequestTask extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... uri) {
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response;
			String responseString = null;
			if (requestTaskType.equalsIgnoreCase("Delivery")) {
			    try {
	                String NUXRRELSIGN = "";
	                
	                ByteArrayOutputStream bs = new ByteArrayOutputStream();
	                Bitmap bitmap = sign.getImage();
	                Bitmap scaledBitmap =  Bitmap.createScaledBitmap(bitmap, 200, 40, true);
	                //System.out.println("SCALED SIZE:"+bitmap.getByteCount()+" -> "+scaledBitmap.getByteCount());
	                for (int x=0;x<scaledBitmap.getWidth();x++) {
	                    for (int y=0;y<scaledBitmap.getHeight();y++) {
	                         String strColor = String.format("#%06X", 0xFFFFFF & scaledBitmap.getPixel(x, y));
	                         if (strColor.equals("#000000")||scaledBitmap.getPixel(x, y)==Color.TRANSPARENT) {
//	                             System.out.println("********"+x+" x "+y+" SETTING COLOR TO WHITE");
	                             scaledBitmap.setPixel(x, y, Color.WHITE);
	                         }
	                    }
	                }
	                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bs);
	                imageInByte = bs.toByteArray();
	                System.out.println ("*************imageInByte:"+imageInByte.length);
			        
			        URL url = new URL(uri[0]);

			        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			        // Set connection parameters.
			        conn.setDoInput(true);
			        conn.setDoOutput(true);
			        conn.setUseCaches(false);
         
			        //Set content type to PNG
			        conn.setRequestProperty("Content-Type", "image/jpg");
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
			        //      System.out.println("Server response:\n'" + responseString + "'");
			        int nuxrsignLoc = responseString.indexOf("NUXRSIGN:");
			        if (nuxrsignLoc>-1) {
			            NUXRACCPTSIGN = responseString.substring(nuxrsignLoc+9).replaceAll("\r", "").replaceAll("\n", "");
			        }
			        else {
			            NUXRACCPTSIGN = responseString.replaceAll("\r", "").replaceAll("\n", "");
			        }
                    
			     } catch (Exception e) {
			         e.printStackTrace();
			   }   

			    
	            try {
	                response = httpclient.execute(new HttpGet(uri[1]+"&NUXRACCPTSIGN="+NUXRACCPTSIGN+"&DECOMMENTS="+DECOMMENTS));
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
		    else if (requestTaskType.equalsIgnoreCase("EmployeeDeliveryList")) {
					
			    // Get List of Employees for the Signing Employee Dropdown 

			    try {
			        response = httpclient.execute(new HttpGet(uri[0]));
			        StatusLine statusLine = response.getStatusLine();
			        if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
			            ByteArrayOutputStream out = new ByteArrayOutputStream();
			            response.getEntity().writeTo(out);
			            out.close();
			            employeeList = out.toString();
			        } else {
			            // Closes the connection.
			            response.getEntity().getContent().close();
			            throw new IOException(statusLine.getReasonPhrase());
			        }
			    } catch (ClientProtocolException e) {
			        // TODO Handle problems..
			        employeeList = "";
			    } catch (IOException e) {
			        // TODO Handle problems..
			        employeeList = "";
			    }

			  try {
                response = httpclient.execute(new HttpGet(uri[1]));
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
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Handle problems..
                e.printStackTrace();
            }
			    
			    return responseString;			    
		    }
			
			return responseString;

		

		}
	}
}
