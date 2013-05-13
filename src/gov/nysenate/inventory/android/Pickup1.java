package gov.nysenate.inventory.android;


import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutionException;


import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class Pickup1 extends Activity {
	public final static String loc_code_intent = "gov.nysenate.inventory.android.loc_code_str";
	public EditText loc_code;
	public EditText locCodeDest;
	public TextView loc_details;
	public TextView locDetailsDest;
	public String res = null;
	public String status = null;
	public String loc_code_str = null;
	ClearableAutoCompleteTextView autoCompleteTextView1;// for origin location code
	ClearableAutoCompleteTextView autoCompleteTextView2;// for destination location code
	public String originLocation = null;
	public String destinationLocation = null;
	public ArrayList<String> locCodeList = new ArrayList<String>();
	String URL = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pickup1);

		// loc_code = (EditText) findViewById(R.id.editText1);

		// check network connection
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			// fetch data
			status = "yes";

			// Get the URL from the properties
			URL = MainActivity.properties.get("WEBAPP_BASE_URL").toString();

			AsyncTask<String, String, String> resr1 = new RequestTask()
					.execute(URL + "/LocCodeList");
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
				autoCompleteTextView1 = (ClearableAutoCompleteTextView) findViewById(R.id.autoCompleteTextView1);
				autoCompleteTextView1.setThreshold(1);
				autoCompleteTextView1.setAdapter(adapter);
				autoCompleteTextView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			            @Override
			            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	                        Log.i("ItemClicked", "ITEM CLICKED");
	                        boolean focusRequested = autoCompleteTextView2.requestFocus();
			            }
			        });

				// for destination code

				autoCompleteTextView2 = (ClearableAutoCompleteTextView) findViewById(R.id.autoCompleteTextView2);
				autoCompleteTextView2.setThreshold(1);
				autoCompleteTextView2.setAdapter(adapter);

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

		// for destination code
		autoCompleteTextView2.addTextChangedListener(filterTextWatcher2);
		locDetailsDest = (TextView) findViewById(R.id.textView3);

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
				originLocation = autoCompleteTextView1.getText().toString()
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

	// TextWatcher for destination
	private TextWatcher filterTextWatcher2 = new TextWatcher() {

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		public void afterTextChanged(Editable s) {
			if (autoCompleteTextView2.getText().toString().length() >= 3) {
				// loc_details.setText(loc_code.getText().toString());
				destinationLocation = autoCompleteTextView2.getText()
						.toString().trim();
				String barcodeNumberDetails[] = autoCompleteTextView2.getText()
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
				locDetailsDest.setText(res);
				// locDetailsDest.append("\n"+loc_code.getText().toString());
				// autoCompleteTextView1.setText(barcode_num);
			}
		}
	};

	

	public void okButton(View view) {
		Intent intent = new Intent(this, Pickup2Activity.class);
		intent.putExtra("originLocation", originLocation); 				// for origin code
		intent.putExtra("destinationLocation", destinationLocation); 	// for
																		// destination
																		// code
		startActivity(intent);
	}

	public void cancelButton(View view) {

		Intent intent = new Intent(this, MenuActivity.class);
		startActivity(intent);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_pickup1, menu);
		return true;
	}

}
