package gov.nysenate.inventory.android;

import gov.nysenate.inventory.android.ListtestActivity.verList;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class Pickup2Activity extends SenateActivity {
	public ClearableEditText barcode;
	public TextView pickupCounter;
	public TextView loc_details;
	public TextView TextView2;
	public  String res=null;
	public	String status=null;
	public ListView listView;
	public String loc_code=null; // populate this from the location code from previous activity
    ArrayList<InvItem> scannedItems= new ArrayList<InvItem>();
	ArrayList<verList> list = new ArrayList<verList>();
	ArrayList<InvItem> dispList = new ArrayList<InvItem>();
	ArrayAdapter<InvItem> adapter ;
	ArrayList<InvItem> invList = new ArrayList<InvItem>();
	int count;	
    int numItems;
    public String originLocation=null;
    public String destinationLocation=null;
    public String cdlocatto=null;
    public String cdlocatfrm=null;
   // These 3 ArrayLists will be used to transfer data to next activity and to the server
    ArrayList<InvItem> allScannedItems=new ArrayList<InvItem>();// for saving items which are not allocated to that location
    ArrayList<InvItem> newItems=new ArrayList<InvItem>();// for saving items which are not allocated to that location
    static Button btnPickup2Cont;
    static Button btnPickup2Cancel;   
    static ProgressBar progBarPickup2;
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pickup2);
		// Get the origin and destination location from the previous activity
		Intent intent = getIntent();
		originLocation = intent.getStringExtra("originLocation");
		destinationLocation= intent.getStringExtra("destinationLocation");
		cdlocatfrm = intent.getStringExtra("cdlocatfrm");
		cdlocatto = intent.getStringExtra("cdlocatto");
		
		listView = (ListView) findViewById(R.id.listView1);
		count = 0;// for initialization
		/*adapter = new ArrayAdapter<StringBuilder>(this,
				android.R.layout.simple_list_item_1, dispList);*/
		adapter = new InvListViewAdapter(this, R.layout.invlist_item, invList);
		
		progBarPickup2 = (ProgressBar) findViewById(R.id.progBarPickup2); 
	
		// Display the origin and destination 
		TextView TextView2= (TextView) findViewById(R.id.textView2);
		TextView2.setText("Origin : "+ originLocation+"\n"
		                 +"Destination : "+destinationLocation);
		// display the count on screen
		pickupCounter = (TextView) findViewById(R.id.pickupCounter);

		pickupCounter.setText(Integer.toString(count));
		// populate the listview
		listView.setAdapter(adapter);

		// code for textwatcher

		barcode = (ClearableEditText) findViewById(R.id.barcode);
		barcode.addTextChangedListener(filterTextWatcher);

		// Button Setup
		btnPickup2Cont = (Button) findViewById(R.id.btnPickup2Cont);
		btnPickup2Cont.getBackground().setAlpha(255);  	  
	    btnPickup2Cancel = (Button) findViewById(R.id.btnPickup2Cancel);
	    btnPickup2Cancel.getBackground().setAlpha(255);  	 
	    
        Pickup1.progBarPickup1.setVisibility(ProgressBar.INVISIBLE);	
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		btnPickup2Cont = (Button) findViewById(R.id.btnPickup2Cont);
		btnPickup2Cont.getBackground().setAlpha(255);  	  
	    btnPickup2Cancel = (Button) findViewById(R.id.btnPickup2Cancel);
	    btnPickup2Cancel.getBackground().setAlpha(255);   	  
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
				Log.i("test", "barcode_number:"+barcode_number);

				int flag = 0;
				boolean barcodeFound = false;

				// If the item is already scanned then display a
				// toster"Already Scanned"
				if (findBarcode(barcode_num)>-1) {
					// display toster
				    barcodeFound = true;
					Context context = getApplicationContext();
					CharSequence text = "Already Scanned  ";
					int duration = Toast.LENGTH_SHORT;

					Toast toast = Toast.makeText(context, text, duration);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
				verList vl = new verList();

				// if it is not already scanned and does not exist in the
				// list(location)
				// then add it to list and append new item to its description
				if ((flag == 0)
						&& (barcodeFound == false)) {

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
								.execute(URL+"/ItemDetails?barcode_num="
										+ barcode_num);
						 System.out.println("URL CALL:"+URL+"/ItemDetails?barcode_num="+ barcode_num);
						try {
							res = resr1.get().trim().toString();
							System.out.println("URL RESULT:"+res);
							
							
							// add it to list and displist and scanned items
							JSONObject object = null;
							if (res==null) {
                                Log.i("TESTING", "A CALL noServerResponse");						    
							    noServerResponse(barcode_num);
                                return;
							}
							else if (res.toUpperCase().contains("DOES NOT EXIST IN SYSTEM")) {
	                            Log.i("TESTING", "A CALL barcodeDidNotExist");
	                            barcodeDidNotExist(barcode_num);
	                            return;
							}
							else {
							    try {
							        object = (JSONObject) new JSONTokener( res).nextValue();
							    } catch (JSONException e) {
							        // TODO Auto-generated catch block
							        e.printStackTrace();
							    }

							    vl.NUSENATE = barcode_number;
							    vl.CDCATEGORY = object.getString("cdcategory");
							    vl.DECOMMODITYF =  object.getString("decommodityf").replaceAll("&#34;", "\"");
							    vl.CDLOCATTO = object.getString("cdlocatto");
							    vl.CDLOCTYPETO = object.getString("cdloctypeto");
							    vl.ADSTREET1 = object.getString("adstreet1to").replaceAll("&#34;", "\"");
							    vl.DTISSUE =  object.getString("dtissue");							 
							}

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
					String invStatus;
					
					if (vl.CDLOCATTO==null||vl.CDLOCATTO.trim().length()==0) {
						invStatus = "NOT IN SFMS";
					}
					// This is what should be expected. Trying to move the 
					else if (vl.CDLOCATTO.equalsIgnoreCase(cdlocatfrm)) {  
						invStatus = "UPDATE";
					}
					else if (vl.CDLOCATTO.equalsIgnoreCase(cdlocatto)) { // 
						invStatus = "AT DESTINATION";
					}
					else {
						invStatus = "LOCATED AT: "+vl.CDLOCATTO;
					}

					// 5/24/13 BH Coded below to use InvItem Objects to display
					// the list.
					InvItem invItem = new InvItem(
							vl.NUSENATE, vl.CDCATEGORY,
							invStatus, vl.DECOMMODITYF);
					invList.add(invItem);					
					
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

					//dispList.add(s_new); // this list will display the contents
											// on screen
					scannedItems.add(invItem);
					allScannedItems.add(invItem);
					newItems.add(invItem);// to keep
																	// track of
																	// (number+details)
																	// for
																	// summary
								}

				// notify the adapter that the data in the list is changed and
				// refresh the view
				adapter.notifyDataSetChanged();
				count = list.size();
				pickupCounter.setText(Integer.toString(count));
				listView.setAdapter(adapter);
				barcode.setText("");
			}
		}
	};

	
    public void barcodeDidNotExist(final String barcode_num) {
        Log.i("TESTING", "****barcodeDidNotExist MESSAGE");
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);
 
            // set title
            alertDialogBuilder.setTitle("Barcode#: "+barcode_num+" DOES NOT EXIST IN SFMS");
 
            // set dialog message
            alertDialogBuilder
                .setMessage(Html.fromHtml("***WARNING: Barcode#: <b>"+barcode_num+"</b> does not exist in SFMS. This should not occur with a Senate Tag#.<br/><br/> Do you want to add this barcode?"))
                .setCancelable(false)
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // 5/24/13 BH Coded below to use InvItem Objects to display
                        // the list.
                        verList vl = new verList();
                        vl.NUSENATE = barcode_num;
                        vl.CDCATEGORY = "";
                        vl.DECOMMODITYF = " ***NOT IN SFMS***  New Item";
                        
                        InvItem invItem = new InvItem(
                                vl.NUSENATE, vl.CDCATEGORY,
                                "NEW", vl.DECOMMODITYF);
                        invList.add(invItem);                   
                        
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

                        //dispList.add(s_new); // this list will display the contents
                                                // on screen
                        scannedItems.add(invItem);
                        allScannedItems.add(invItem);
                        newItems.add(invItem);// to keep
                        
                        list.add(vl);                       
                        barcode.setText("");
                        dialog.dismiss();
                    }
                  })
                .setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        Context context = getApplicationContext();
                        
                        CharSequence text = "Barcode#: "+barcode_num+" was NOT added";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        
                        barcode.setText("");                        
                        
                        dialog.dismiss();
                    }
                });
 
                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();
 
                // show it
                alertDialog.show();     
    }
    	

    public void noServerResponse(final String barcode_num) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);
 
            // set title
            alertDialogBuilder.setTitle("Barcode#: "+barcode_num+" DOES NOT EXIST IN SFMS");
 
            // set dialog message
            alertDialogBuilder
                .setMessage(Html.fromHtml("!!ERROR: There was no response from the Web Server  (Barcode#: <b>"+barcode_num+"</b>). <br/><br/> Please contact STS/BAC."))
                .setCancelable(false)
                .setPositiveButton("Ok",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        Context context = getApplicationContext();
                        
                        CharSequence text = "Barcode#: "+barcode_num+" was NOT added";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        
                        barcode.setText("");                        
                        
                        dialog.dismiss();
                    }
                });
 
                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();
 
                // show it
                alertDialog.show();     
    }
    
    public int findBarcode(String barcode_num) {
        for (int x=0;x<invList.size();x++) {
            if (invList.get(x).getNusenate().equals(barcode_num)) {
                return x;
            }
        }
        return -1;
    }
    
	public ArrayList<String> getJSONArrayList(ArrayList<InvItem> invList) {
		ArrayList<String> returnArray = new ArrayList<String>();
		if (invList!=null) {
			for (int x=0;x<invList.size();x++) {
				returnArray.add(invList.get(x).toJSON());
			}
		}
		
		return returnArray;
	}

	
	public ArrayList<InvItem> getInvItemArrayList(ArrayList<String> invList) {
		ArrayList<InvItem> returnArray = new ArrayList<InvItem>();
		if (invList!=null) {
			for (int x=0;x<invList.size();x++) {
				String curInvJson = invList.get(x);
				InvItem currentInvItem = new InvItem();
				currentInvItem.parseJSON(curInvJson);
				returnArray.add(currentInvItem);
			}
		}
		
		return returnArray;
	}
	
	
	
	
	// 3/15/13  Work in progress. Not fully implemented yet
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	  super.onSaveInstanceState(savedInstanceState);
	  // Save UI state changes to the savedInstanceState.
	  // This bundle will be passed to onCreate if the process is
	  // killed and restarted.
	  savedInstanceState.putString("savedOriginLoc", originLocation);
	  savedInstanceState.putString("savedDestLoc", destinationLocation);
	  savedInstanceState.putStringArrayList("savedScannedItems", getJSONArrayList(scannedItems));
	  savedInstanceState.putStringArrayList("savedallScannedItems", getJSONArrayList(allScannedItems));
	  savedInstanceState.putStringArrayList("savedNewItems", getJSONArrayList(newItems));
	}
    
	// 3/15/13  Work in progress. Not fully implemented yet
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
	  super.onRestoreInstanceState(savedInstanceState);
	  // Restore UI state from the savedInstanceState.
	  // This bundle has also been passed to onCreate.
	  originLocation = savedInstanceState.getString("savedOriginLoc");
	  scannedItems = getInvItemArrayList(savedInstanceState.getStringArrayList("savedScannedItems"));
	  allScannedItems = getInvItemArrayList(savedInstanceState.getStringArrayList("savedallScannedItems"));
	  newItems = getInvItemArrayList(savedInstanceState.getStringArrayList("savedNewItems"));
	  TextView TextView2= (TextView) findViewById(R.id.textView2);
	  TextView2.setText("Origin : "+ originLocation+"\n"
		                 +"Destination : "+destinationLocation);
	  
	}	
	
	public void continueButton(View view){
		// send the data to Pickup3 activity
	    progBarPickup2.setVisibility(ProgressBar.VISIBLE);
		btnPickup2Cont.getBackground().setAlpha(70);	
		Intent intent = new Intent(this, Pickup3.class); 
		intent.putExtra("originLocation", originLocation);
		intent.putExtra("destinationLocation", destinationLocation);
		String countStr= Integer.toString(count);
		intent.putExtra("count", countStr);
		intent.putStringArrayListExtra("scannedBarcodeNumbers", getJSONArrayList(scannedItems));
		intent.putStringArrayListExtra("scannedList", getJSONArrayList(allScannedItems));//scanned items list
	
		startActivity(intent);
        overridePendingTransition(R.anim.in_right, R.anim.out_left);
	}
	
	public void cancelButton(View view){
		// send back to the Move Menu		
		btnPickup2Cancel.getBackground().setAlpha(70);		
		Intent intent = new Intent(this, Move.class);
		startActivity(intent);
        overridePendingTransition(R.anim.in_left, R.anim.out_right);
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
		String CDLOCATTO;
		String CDLOCTYPETO;
		String ADSTREET1;
		String DTISSUE;   
	}
}
