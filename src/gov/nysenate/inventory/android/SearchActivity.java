package gov.nysenate.inventory.android;

import java.util.concurrent.ExecutionException;



import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class SearchActivity extends SenateActivity {
	ClearableEditText barcode;
	String res = null;
	public String status = null;
	TextView textView;
	ImageButton buttonSearchBack;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);

		barcode = ( ClearableEditText) findViewById(R.id.barcode);
		barcode.addTextChangedListener(filterTextWatcher);// Adding Listener to
															// barcode field
		textView = (TextView) findViewById(R.id.textView1);
		buttonSearchBack = (ImageButton)findViewById(R.id.buttonSearchBack);
		
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
				Log.i("Activity Search afterTextChanged ","barcode_num "+barcode_num);
				// check network connection
				ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
				if (networkInfo != null && networkInfo.isConnected()) {
					// fetch data
					status = "yes";
					// Get the URL from the properties
					String URL = MainActivity.properties.get("WEBAPP_BASE_URL")
							.toString();
					Log.i("Activity Search afterTextChanged ","URL "+URL);
					AsyncTask<String, String, String> resr1 = new RequestTask()
							.execute(URL + "/Search?barcode_num=" + barcode_num);
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
				textView.setText("\n" + res);
				barcode.setText("");
			}
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_search, menu);
		return true;
	}

/*	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			Toast toast = Toast.makeText(getApplicationContext(), "Going Back",
					Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			NavUtils.navigateUpFromSameTask(this);

			overridePendingTransition(R.anim.in_left, R.anim.out_right);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}*/

	public void okButton(View view) {
		if (view.getId()==R.id.buttonSearchBack) {
			float alpha = 0.45f;
			AlphaAnimation alphaUp = new AlphaAnimation(alpha, alpha);
			alphaUp.setFillAfter(true);
			buttonSearchBack.startAnimation(alphaUp);
		
			//this.finish();// close the current activity
			Intent intent = new Intent(this, MenuActivity.class);
			startActivity(intent);
		    overridePendingTransition(R.anim.in_left, R.anim.out_right);			
		}

	}

	public void cancelButton(View view) {
		this.finish();
		Intent intent = new Intent(this, MenuActivity.class);
		startActivity(intent);

	}

/*	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.in_left, R.anim.out_right);
	}*/



}
