package gov.nysenate.inventory.android;


import java.util.ArrayList;
import java.util.Collections;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

public class Delivery1 extends Activity {
	AutoCompleteTextView autoCompleteTextView1;// for location code
	public ArrayList<String> locCodeList = new ArrayList<String>();
	String URL = "";
	public String res = null;
	public String status = null;
	public String loc_code_str = null;
	public TextView loc_details;
	public String deliveryLocation = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_delivery1);
		
		// check network connection
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			// fetch data
			status = "yes";

			// Get the URL from the properties
			URL = MainActivity.properties.get("WEBAPP_BASE_URL").toString();
			AsyncTask<String, String, String> resr1 = new RequestTask()
					.execute(URL + "/LocCodeList?NATYPE=DELIVERY");
			try {
				res = resr1.get().trim().toString();
				// code for JSON

				String jsonString = resr1.get().trim().toString();
				JSONArray jsonArray = new JSONArray(jsonString);

				for (int i = 0; i < jsonArray.length(); i++) {
					locCodeList.add(jsonArray.getString(i).toString());
				}

				Collections.sort(locCodeList);
				
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
						android.R.layout.simple_dropdown_item_1line,
						locCodeList);
				// for origin dest code
				autoCompleteTextView1 = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView1);
				autoCompleteTextView1.setThreshold(1);
				autoCompleteTextView1.setAdapter(adapter);
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
		autoCompleteTextView1.addTextChangedListener(filterTextWatcher);
		// autoCompleteTextView2.addTextChangedListener(filterTextWatcher2);
		loc_details = (TextView) findViewById(R.id.textView2);
		// loc_details.findFocus(); we can use this to find focus

	}

	
	private TextWatcher filterTextWatcher = new TextWatcher() {

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		public void afterTextChanged(Editable s) {
			if (autoCompleteTextView1.getText().toString().length() >= 3) {
				// loc_details.setText(loc_code.getText().toString());
				deliveryLocation = autoCompleteTextView1.getText().toString()
						.trim();

				String barcodeNumberDetails[] = autoCompleteTextView1.getText()
						.toString().trim().split("-");
				String barcode_num = barcodeNumberDetails[0];// this will be
																// passed to the
																// server
				loc_code_str = barcodeNumberDetails[0];// this will be passed to
														// next activity with
														// intent

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
							.execute(URL + "/LocationDetails?barcode_num="
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
				loc_details.setText(res);
				// loc_details.append("\n"+loc_code.getText().toString());
				// autoCompleteTextView1.setText(barcode_num);
			}
		}
	};
	
	

	
		public void okButton(View view) {

			Intent intent = new Intent(this, Delivery2.class);
			intent.putExtra("location", deliveryLocation); // for location code of delivery
			startActivity(intent);
		}

		public void cancelButton(View view) {
			Intent intent = new Intent(this, Move.class);
			startActivity(intent);
		}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_delivery1, menu);
		return true;
	}
	
/*   Pickup3 AsyncTask
 *  class RequestTask extends AsyncTask<String, String, String>{

        @Override
        protected String doInBackground(String... uri) {
            // First Upload the Signature and get the nuxsign from the Server
            if (requestTaskType.equalsIgnoreCase("Pickup")) {
                String NUXRRELSIGN = "";
                
                ByteArrayOutputStream bs = new ByteArrayOutputStream();
                Bitmap bitmap = sign.getImage();
                Bitmap scaledBitmap =  Bitmap.createScaledBitmap(bitmap, 200, 40, true);
                //System.out.println("SCALED SIZE:"+bitmap.getByteCount()+" -> "+scaledBitmap.getByteCount());
                for (int x=0;x<scaledBitmap.getWidth();x++) {
                    for (int y=0;y<scaledBitmap.getHeight();y++) {
                         String strColor = String.format("#%06X", 0xFFFFFF & scaledBitmap.getPixel(x, y));
                         if (strColor.equals("#000000")||scaledBitmap.getPixel(x, y)==Color.TRANSPARENT) {
//                             System.out.println("********"+x+" x "+y+" SETTING COLOR TO WHITE");
                             scaledBitmap.setPixel(x, y, Color.WHITE);
                         }
                    }
                }
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bs);
                imageInByte = bs.toByteArray();
                String responseString = "";         
                try {
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
                        NUXRRELSIGN = responseString.substring(nuxrsignLoc+9).replaceAll("\r", "").replaceAll("\n", "");
                    }
                    else {
                        NUXRRELSIGN = responseString.replaceAll("\r", "").replaceAll("\n", "");
                    }
                        
                } catch (Exception e) {
                    e.printStackTrace();
                }   

                // Then post the rest of the information along with the NUXRSIGN 
                HttpClient httpclient = new DefaultHttpClient();
                HttpResponse response;
                responseString = null;
                try {
                
                    String pickupURL = uri[1]+"&NUXRRELSIGN="+NUXRRELSIGN+"&DECOMMENTS="+DECOMMENTS;
                    System.out.println("pickupURL:"+pickupURL);
                    response = httpclient.execute(new HttpGet(pickupURL));
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
 */

}
