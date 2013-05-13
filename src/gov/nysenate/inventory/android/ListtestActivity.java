package gov.nysenate.inventory.android;


import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.Comparator;


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
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ListtestActivity extends SenateActivity {

	public ClearableEditText barcode;
	public EditText count_text;
	public TextView loc_details;
	public String res = null;
	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;	
	public String status = null;
	public ListView listView;
	public String loc_code = null; // populate this from the location code from
									// previous activity
	ArrayList<String> scannedItems = new ArrayList<String>();
	ArrayList<verList> list = new ArrayList<verList>();
	ArrayList<StringBuilder> dispList = new ArrayList<StringBuilder>();
	ArrayList<InvItem> invList = new ArrayList<InvItem>();
	ArrayList<String> dispType = new ArrayList<String>();
	String currentSortValue = "Description";
	public Spinner sortList;
	ImageButton buttonVerListContinue;
	ImageButton buttonVerListCancel;
	
	String URL = ""; // this will be initialized once in onCreate() and used for
						// all server calls.
	InvListViewAdapter adapter;
	int count;
	int numItems;
	// These 3 ArrayLists will be used to transfer data to next activity and to
	// the server
	ArrayList<String> AllScannedItems = new ArrayList<String>();// for saving
																// items which
																// are not
																// allocated to
																// that location

	ArrayList<String> newItems = new ArrayList<String>();// for saving items
															// which are not
															// allocated to that
															// location

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_listtest);

		// Get the location code from the previous activity
		Intent intent = getIntent();
		loc_code = intent.getStringExtra(Verification.loc_code_intent);

		// ----------code from other activity starts
		listView = (ListView) findViewById(R.id.preferenceList);
		sortList = (Spinner) findViewById(R.id.sortList);
		sortList.setOnItemSelectedListener(new SortChangedListener());
	
		buttonVerListContinue = (ImageButton) findViewById(R.id.buttonVerListContinue);
		buttonVerListCancel = (ImageButton) findViewById(R.id.buttonVerListCancel);		
	

		// check network connection

		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			// fetch data
			status = "yes";

			// Get the URL from the properties
			URL = MainActivity.properties.get("WEBAPP_BASE_URL").toString();

			AsyncTask<String, String, String> resr1 = new RequestTask()
					.execute(URL + "/itemsList?loc_code=" + loc_code);

			try {

				// code for JSON
				res = resr1.get().trim().toString();
				String jsonString = resr1.get().trim().toString();

				JSONArray jsonArray = new JSONArray(jsonString);
				count = jsonArray.length();
				numItems = jsonArray.length();
				// this will populate the lists from the JSON array coming from
				// server
				for (int i = 0; i < jsonArray.length(); i++) {

					JSONObject jo = new JSONObject();
					jo = jsonArray.getJSONObject(i);
					verList vl = new verList();
					vl.NUSENATE = jo.getString("NUSENATE");
					vl.CDCATEGORY = jo.getString("CDCATEGORY");
					vl.DECOMMODITYF = jo.getString("DECOMMODITYF");

					list.add(vl);
					StringBuilder s = new StringBuilder();
					s.append(vl.NUSENATE);
					s.append(" ");
					s.append(vl.CDCATEGORY);
					s.append(" ");
					s.append(vl.DECOMMODITYF);
					// dispList.add(s);

					dispList.add(i, s); // this list will display the contents
										// on screen
					dispType.add(i, "EXISTING");

					// 3/15/13 BH Coded below to use InvItem Objects to display
					// the list.
					InvItem invItem = new InvItem(
							vl.NUSENATE, vl.CDCATEGORY,
							"EXISTING", vl.DECOMMODITYF);
					invList.add(invItem);
					// invItem = null;

					// String s =
					// vl.NUSENATE+" "+vl.CDCATEGORY+" "+vl.DECOMMODITYF;
					// dispList.add(s);
					// number= dispList.size();
					// list.add((verList) jsonArray.getJSONObject(i));
				}
				// code for JSON ends
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

	    
		adapter = new InvListViewAdapter(this, R.layout.invlist_item, invList);

		// ArrayAdapter<StringBuilder>
		/*
		 * adapter = new InvListAdapter<StringBuilder>(this,
		 * android.R.layout.simple_list_item_1, dispList);
		 */

		// display the count on screen
		count_text = (EditText) findViewById(R.id.editText2);

		count_text.setText(Integer.toString(count));
		// populate the listview
		listView.setAdapter(adapter);
	
		// --code from other activity
		// ends-----------------------------------------------------------------

		// code for textwatcher

		barcode = (ClearableEditText) findViewById(R.id.preferencePWD);
		barcode.addTextChangedListener(filterTextWatcher);

		// listView = (ListView) findViewById(R.id.listView1 );

	}
    private void startVoiceRecognitionActivity() {
        Intent intentSpeech = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intentSpeech.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intentSpeech.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech recognition demo");
        startActivityForResult(intentSpeech, VOICE_RECOGNITION_REQUEST_CODE);
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
        	TextView t = (TextView) findViewById(R.id.textView2);
        	t.setText(matches.get(0));
        }

        super.onActivityResult(requestCode, resultCode, data);
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
				// loc_details.setText(loc_code.getText().toString());
				// listView.
				String barcode_num = barcode.getText().toString().trim();
				String barcode_number = barcode_num;

				// to delete an element from the list
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

				for (int i = 0; i < list.size(); i++) {
					// this if will not remove the "New items" and previously
					// scanned items
					if ((list.get(i).NUSENATE.equals(barcode_number) )
							&& (scannedItems.contains(barcode_number) == false)) {
						list.remove(i);

						// display toster
						Context context = getApplicationContext();
						CharSequence text = "Removed: " + dispList.get(i);
						int duration = Toast.LENGTH_SHORT;

						Toast toast = Toast.makeText(context, text, duration);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
						AllScannedItems.add(dispList.get(i).toString());// to
																		// keep
																		// track
																		// of
																		// (number+details)
																		// for
																		// summery
						invList.remove(i);
						dispList.remove(i);
						scannedItems.add(barcode_number);// to keep track of all
															// scanned items
															// numbers for
															// oracle table

						flag = 1;
					}

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

						AsyncTask<String, String, String> resr1 = new RequestTask()
								.execute(URL + "/itemDetails?barcode_num="
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
					vl.DECOMMODITYF = " New Item";

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
					dispType.add("NEW");

					// 3/15/13 BH Coded below to use InvItem Objects to display
					// the list.
					InvItem invItem = new InvItem(
							vl.NUSENATE, vl.CDCATEGORY,
							"NEW", s_new.toString());
					invList.add(invItem);

					scannedItems.add(barcode_number);
					AllScannedItems.add(s_new.toString());
					newItems.add(vl.NUSENATE + " " + vl.CDCATEGORY);// to keep
																	// track of
																	// (number+details)
																	// for
																	// summary
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



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_listtest, menu);
		return true;
	}

 /*   @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.in_left, R.anim.out_right);
    }*/
	
	
	public void okButton(View view) {
		float alpha = 0.45f;
		AlphaAnimation alphaUp = new AlphaAnimation(alpha, alpha);
		alphaUp.setFillAfter(true);
		buttonVerListContinue.startAnimation(alphaUp);
		
		// create lists for summary activity
		ArrayList<String> missingItems = new ArrayList<String>();// for saving
																	// items
																	// which are
																	// not
																	// allocated
																	// to that
																	// location
		for (int i = 0; i < this.invList.size(); i++) {
			if ((invList.get(i).getType().contains("New")) == false) {
				missingItems.add(invList.get(i).getNusenate() + " "
						+ invList.get(i).getDecommodityf()); // if the
																// description
																// of dispList
																// is not new
																// item then add
																// it to missing
																// list
			}
		}
		String summary = "Total items   : " + numItems + "\n"
				+ "Scanned items(Existing+New) : " + AllScannedItems.size()
				+ "\n" + "Missing items : " + missingItems.size() + "\n"
				+ "New items     : " + newItems.size() + "\n";
		Intent intent = new Intent(this, VerSummaryActivity.class);
		intent.putExtra("loc_code", loc_code);
		intent.putExtra("summary", summary);
		intent.putStringArrayListExtra("scannedBarcodeNumbers", scannedItems);
		intent.putStringArrayListExtra("scannedList", AllScannedItems);// scanned
																		// items
																		// list
		intent.putStringArrayListExtra("missingList", missingItems);// missing
																	// items
																	// list
		intent.putStringArrayListExtra("newItems", newItems);// new items list
		startActivity(intent);
		overridePendingTransition(R.anim.in_right, R.anim.out_left);		
		alphaUp = new AlphaAnimation(1f, 1f);
		alphaUp.setFillAfter(true);
		buttonVerListContinue.startAnimation(alphaUp);
		
	}

	public void cancelButton(View view) {
		float alpha = 0.45f;
		AlphaAnimation alphaUp = new AlphaAnimation(alpha, alpha);
		alphaUp.setFillAfter(true);
		buttonVerListCancel.startAnimation(alphaUp);
		Intent intent = new Intent(this, Verification.class);
		startActivity(intent);
		overridePendingTransition(R.anim.in_left, R.anim.out_right);
		alphaUp = new AlphaAnimation(1f, 1f);
		alphaUp.setFillAfter(true);
		buttonVerListCancel.startAnimation(alphaUp);
	}


	public void dispToster(String msg) {
		Context context = getApplicationContext();
		CharSequence text = msg;
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}

	public class SortListComparator implements Comparator {

		@Override
		public int compare(Object lObject, Object rObject) {
			InvItem lInvItem = (InvItem) lObject;
			InvItem rInvItem = (InvItem) rObject;

			if (currentSortValue.equalsIgnoreCase("Description")) {
				return lInvItem.getDecommodityf().compareTo(
						rInvItem.getDecommodityf());
			} else if (currentSortValue.equalsIgnoreCase("Senate Tag#")) {
			    Integer lNusenate = new Integer(lInvItem.getNusenate());
                Integer rNusenate = new Integer(rInvItem.getNusenate());
				return lNusenate.compareTo(rNusenate);
			} else if (currentSortValue.equalsIgnoreCase("Last Inventory Date")) {
				// Need to Pull Inventory Date C
				return lInvItem.getDecommodityf().compareTo(
						rInvItem.getDecommodityf());
			} else if (currentSortValue
					.equalsIgnoreCase("Last Inventory Date Descending")) {
				// Need to Pull Inventory Date C
				return rInvItem.getDecommodityf().compareTo(
						lInvItem.getDecommodityf());
			} else {
				return lInvItem.getDecommodityf().compareTo(
						rInvItem.getDecommodityf());
			}
		}

	}

	public class SortChangedListener implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			currentSortValue = parent.getItemAtPosition(pos).toString();
			Collections.sort(invList, new SortListComparator());
			adapter.notifyDataSetChanged();
			// listView.setAdapter(adapter);

		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
		}

	}

	public class verList {
		String NUSENATE;
		String CDCATEGORY;
		String DECOMMODITYF;
	}
}