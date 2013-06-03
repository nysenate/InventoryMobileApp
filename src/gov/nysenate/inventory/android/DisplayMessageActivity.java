package gov.nysenate.inventory.android;


import java.util.concurrent.ExecutionException;



import android.app.Activity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

public class DisplayMessageActivity extends Activity {

	String res = null;

	@SuppressWarnings("static-access")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_message);

		String status = null;

		// Get the message from the intent
		Intent intent = getIntent();
		String user_name = intent.getStringExtra(MainActivity.u_name_intent);
		String password = intent.getStringExtra(MainActivity.pwd_intent);
		String res = null;

		try {
			// check network connection
			ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			if (networkInfo != null && networkInfo.isConnected()) {
				// fetch data
				status = "yes";
				try {
					// Get the URL from the properties
					String URL = MainActivity.properties.get("WEBAPP_BASE_URL")
							.toString();
					Log.i("Login test", URL + "/Login?user=" + user_name+ "&pwd=" + password);
					AsyncTask<String, String, String> resr1 = new RequestTask()
							.execute(URL + "/Login?user=" + user_name
									+ "&pwd=" + password);
					try {
						res = resr1.get().trim().toString();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (Exception e) {

					Intent intent2 = new Intent(this, MainActivity.class);
					intent2.setFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent2);
					overridePendingTransition(R.anim.slide_in_left,
							R.anim.slide_out_left);
					finish();
				}
				status = "yes1";
				if (user_name==null||user_name.trim().length()==0) { // TESTING PURPOSE ONLY!!!
					user_name = "height";
				}
				MainActivity.nauser = user_name;
				System.out.println ("NAUSER NOW SET TO "+user_name);
			} else {
				// display error
				status = "no";
				MainActivity.nauser = null;
				System.out.println ("NAUSER NULL!!");
				}

			// Create the text view
			TextView textView = new TextView(this);
			textView.setTextSize(40);

			// calling the menu activity after validation
			System.out.println ("RES:"+res);
			if (res.equals("VALID")) {
				Intent intent2 = new Intent(this, MenuActivity.class);
				startActivity(intent2);
				overridePendingTransition(R.anim.slide_in_left,
						R.anim.slide_out_left);
			} else {
				Intent intent2 = new Intent(this, MainActivity.class);
				intent2.setFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent2);
				overridePendingTransition(R.anim.slide_in_left,
						R.anim.slide_out_left);
				int duration = Toast.LENGTH_LONG;
				Toast toast = Toast.makeText(this,
						"!!ERROR: Invalid Username and/or Password.", duration);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();

				
				finish();
			}
		} catch (Exception e) {
			int duration = Toast.LENGTH_LONG;
			Toast toast = Toast.makeText(this,
					"Problem connecting to Mobile App Server. Please contact STSBAC.("
							+ e.getMessage() + ")", duration);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();

			Intent intent2 = new Intent(this, MainActivity.class);
			intent2.setFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent2);
			finish();

		}
	}



	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.in_left, R.anim.out_right);
	}

}
