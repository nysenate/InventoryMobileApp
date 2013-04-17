package gov.nysenate.inventory.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;

import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
//   WIFI Code Added Below
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.speech.RecognizerIntent;

public class MainActivity extends Activity {

	// WIFI Code Added Below
	WifiManager mainWifi;
	// WifiReceiver receiverWifi;
	List<ScanResult> wifiList;
	ScanResult currentWifiResult;
	int senateWifiFound = -1;
	int senateVisitorWifiFound = -1;
	int connectedTo = -1;
	String senateSSID = "";
	String senateSSIDpwd = "";
	String senateVisitorSSID = "";
	String senateVisitorSSIDpwd = "";
	String wifiMessage = "" /*"Horrible News!!! Currently no Wifi Networks found!!! You need a Wifi network (Preferrably a NY Senate one) in order to use this app."*/;
	String currentSSID = "";
    private ListView mList;
	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
	public static String nauser = null;
	ClearableEditText user_name;
    ClearableEditText password;
	String URL = "";
	public static Properties properties; // Since we want to refer to this in
											// other activities

	public final static String u_name_intent = "gov.nysenate.inventory.android.u_name";
	public final static String pwd_intent = "gov.nysenate.inventory.android.pwd";

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        user_name = (ClearableEditText) findViewById(R.id.user_name);
        //user_name.setClearMsg("Do you want to clear your username?");
        //user_name.showClearMsg(true);
        password = (ClearableEditText) findViewById(R.id.password);
        //password.setClearMsg("Do you want to clear your password?");
        //password.showClearMsg(true);

		// Read from the /assets directory for properties of the project
		// we can modify this file and the URL will be changed
		Resources resources = this.getResources();
		AssetManager assetManager = resources.getAssets();
		try {
			InputStream inputStream = assetManager.open("invApp.properties");
			properties = new Properties();
			properties.load(inputStream); // we load the properties here and we
											// use same object elsewhere in
											// project
			URL = properties.get("WEBAPP_BASE_URL").toString();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			// WIFI Code Added Below
			senateWifiFound = -1;
			senateVisitorWifiFound = -1;
			connectedTo = -1;
			boolean enablingWifi = false;
			long startTime = System.currentTimeMillis();
			mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

			/*
			 * if (!mainWifi.isWifiEnabled()) { Thread t = new Thread() {
			 * 
			 * @Override public void run() { try { //check if connected! while
			 * (mainWifi.isWifiEnabled()) { //Wait to connect
			 * Thread.sleep(1000); }
			 * 
			 * } catch (Exception e) { } } }; t.start(); Thread.yield(); }
			 */

			while (!mainWifi.isWifiEnabled()
					|| System.currentTimeMillis() - startTime > 3000) {
				if (!enablingWifi) {
					mainWifi.setWifiEnabled(true);
					enablingWifi = true;
					Thread.sleep(1000);
				}
			}
			if (mainWifi.isWifiEnabled()) {
				Context context = getApplicationContext();
				int duration = Toast.LENGTH_SHORT;

				if (enablingWifi) {
					Toast toast = Toast.makeText(context,
							"Wifi has been enabled.", duration);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE); // done
																						// Again
																						// BH
				}
			} else {
				Context context = getApplicationContext();
				int duration = Toast.LENGTH_SHORT;

				Toast toast = Toast
						.makeText(
								context,
								"Unable to Enable Wifi Connection necessary to login to this app. Please fix before continuing or contact STS/BAC.",
								3000);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			}

			mainWifi.startScan();
			// get list of the results in object format ( like an array )
			wifiList = mainWifi.getScanResults();
			WifiInfo connectionInfo = mainWifi.getConnectionInfo();

			if (connectionInfo != null) {
				currentSSID = connectionInfo.getSSID().trim()
						.replaceAll("\"", "");
			} else {
				currentSSID = "";
			}
			for (int x = 0; x < wifiList.size(); x++) {
				currentWifiResult = wifiList.get(x);
				if (currentSSID.length() > 0) {
					if (currentSSID.equals(currentWifiResult.SSID)) {
						connectedTo = x;
					}
				}

				if (currentWifiResult.SSID.equalsIgnoreCase(senateSSID)) {
					senateWifiFound = x;
				}
				if (currentWifiResult.SSID.equalsIgnoreCase(senateVisitorSSID)) {
					senateVisitorWifiFound = x;
				}
			}

/*			if (senateWifiFound > -1) {
				wifiMessage = "YES!";
				// wifiMessage =
				// "Great news!!! NY Senate Wireless Network was found. You should have access to information needed for this app.";
			} else if (senateVisitorWifiFound > -1) {
				wifiMessage = "OK!";
			} else if (wifiList.size() > 0) {
				wifiMessage = "Okay news!!! Wireless Networks were found but none of them are NY Senate Wireless Networks. You *might* have access to information needed for this app if you can connect to one of these networks.";
			} else {
				wifiMessage = "Horrible News!!! Currently no Wifi Networks found!!! You need a Wifi network (Preferrably a NY Senate one) in order to use this app.";
			}*/

			if (connectedTo == -1) {
				// wifiMessage = wifiMessage +
				// " <font color='RED'>AAAA Currently not connected to a network ("+senateWifiFound+")</font>";
				Context context = getApplicationContext();
				int duration = Toast.LENGTH_SHORT;

				Toast toast = Toast.makeText(context,
						"NO WIFI NETWORK CONNECTION!!!", duration);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				if (senateWifiFound > -1) { // A MUCH better option would be to
											// connect to Senate Net Instead
					// 1. Instantiate an AlertDialog.Builder with its
					// constructor
					AlertDialog.Builder builder = new AlertDialog.Builder(this);

					// 2. Chain together various setter methods to set the
					// dialog characteristics
					builder.setMessage(
							"Would you like to connect NY Senate Network instead? (You are connected to NY Senate Visitor Network)")
							.setTitle("Connect to NY Senate Network");
					// Add the buttons
					builder.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									WifiConfiguration conf = new WifiConfiguration();
									conf.SSID = "\"" + senateSSID + "\""; // Please
																			// note
																			// the
																			// quotes.
																			// String
																			// should
																			// contain
																			// ssid
																			// in
																			// quotes
									conf.preSharedKey = "\"" + senateSSIDpwd
											+ "\"";
									mainWifi.addNetwork(conf);
									List<WifiConfiguration> list = mainWifi
											.getConfiguredNetworks();
									for (WifiConfiguration i : list) {
										if (i.SSID != null
												&& i.SSID.equals("\""
														+ senateSSID + "\"")) {
											mainWifi.disconnect();
											mainWifi.enableNetwork(i.networkId,
													true);
											mainWifi.reconnect();

											break;
										}
									}

								}
							});
					builder.setNegativeButton("No",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// User cancelled the dialog
								}
							});

					// 3. Get the AlertDialog from create()
					AlertDialog dialog = builder.create();
					dialog.show();
				}

			} else {
				if (connectedTo == senateWifiFound) { // GREAT!!! We are
														// connected to Senate
														// Net
					Context context = getApplicationContext();
					int duration = Toast.LENGTH_SHORT;

					Toast toast = Toast.makeText(context,
							"Connected to Senate Network.", duration);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				} else if (connectedTo == senateVisitorWifiFound) { // NOT BAD,
																	// We are
																	// connected
																	// to Senate
																	// Visitor
																	// Net
					Context context = getApplicationContext();
					int duration = Toast.LENGTH_SHORT;

					Toast toast = Toast.makeText(context,
							"Connected to Senate Visitor Network.", duration);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					if (senateWifiFound > -1) { // A MUCH better option would be
												// to connect to Senate Net
												// Instead
						// 1. Instantiate an AlertDialog.Builder with its
						// constructor
						AlertDialog.Builder builder = new AlertDialog.Builder(
								this);

						// 2. Chain together various setter methods to set the
						// dialog characteristics
						builder.setMessage(
								"Would you like to connect NY Senate Network instead? (You are connected to NY Senate Visitor Network)")
								.setTitle("Connect to NY Senate Network");
						// Add the buttons
						builder.setPositiveButton("Yes",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										WifiConfiguration conf = new WifiConfiguration();
										conf.SSID = "\"" + senateSSID + "\""; // Please
																				// note
																				// the
																				// quotes.
																				// String
																				// should
																				// contain
																				// ssid
																				// in
																				// quotes
										conf.preSharedKey = "\""
												+ senateSSIDpwd + "\"";
										mainWifi.addNetwork(conf);
										List<WifiConfiguration> list = mainWifi
												.getConfiguredNetworks();
										for (WifiConfiguration i : list) {
											if (i.SSID != null
													&& i.SSID
															.equals("\""
																	+ senateSSID
																	+ "\"")) {
												mainWifi.disconnect();
												mainWifi.enableNetwork(
														i.networkId, true);
												mainWifi.reconnect();

												break;
											}
										}

									}
								});
						builder.setNegativeButton("No",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										// User cancelled the dialog
									}
								});

						// 3. Get the AlertDialog from create()
						AlertDialog dialog = builder.create();
						dialog.show();
					}
				} else {
					Context context = getApplicationContext();
					int duration = Toast.LENGTH_SHORT;

					Toast toast = Toast.makeText(context, "Connected to "
							+ currentSSID + ".", duration);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
			}

			// EditText text = (EditText) findViewById(R.id.wifiMessage);
			TextView t = (TextView) findViewById(R.id.textView1);
			t.setTextColor(Color.BLACK);
			if (connectionInfo != null
					&& connectionInfo.getSSID().trim().length() > 0) {
				t.setText(Html.fromHtml("<h1>" + wifiMessage + "</h1>"));

			} else {
				t.setText(Html.fromHtml("<h1> (No Current Connection Info) "
						+ wifiMessage + "</h1>"));
			}

		} catch (Exception e) {
			TextView t = (TextView) findViewById(R.id.textView1);
			t.setText(e.getMessage());
			StackTraceElement[] trace = e.getStackTrace();
			for (int x = 0; x < trace.length; x++) {
				t.setText(t.getText() + trace[x].toString());
			}
			t.setText(Html.fromHtml("<h1>" + t.getText() + "</h1>"));
			t.setTextColor(Color.RED);

		}
		// AppWifiChecker wifiChecker = new
		// AppWifiChecker(this.getApplicationContext(), 1000);
		// Thread checkWifi = new Thread(wifiChecker);
		// checkWifi.start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	// our code begins

	
    public void clickVoice(View view) {
        startVoiceRecognitionActivity();
  }

    /**
     * Fire an intent to start the speech recognition activity.
     */
    private void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech recognition demo");
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
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
    
	
	public void validate(View view) {
		Intent intent = new Intent(this, DisplayMessageActivity.class);
		// Intent intent = new Intent(this, MenuActivity.class);
		String u_name = user_name.getText().toString();
		String pwd = password.getText().toString();
		intent.putExtra(u_name_intent, u_name);
		intent.putExtra(pwd_intent, pwd);
		startActivity(intent);
		overridePendingTransition(R.anim.in_right, R.anim.out_left);
	}

	// our code ends

}