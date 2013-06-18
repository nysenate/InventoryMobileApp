package gov.nysenate.inventory.android;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

import org.json.JSONException;
import org.json.JSONObject;
 

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class UpgradeActivity extends Activity {
 private static final String LOG_TAG = "AppUpgrade";
 private MyWebReceiver receiver;
 private int versionCode = 0;
 String appURI = "";
	 
	 private DownloadManager downloadManager;
	 private long downloadReference;
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upgrade);
		 //Overall information about the contents of a package 
		  //This corresponds to all of the information collected from AndroidManifest.xml.
		  PackageInfo pInfo = null;
		  try {
		   pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
		  } 
		  catch (NameNotFoundException e) {
		   e.printStackTrace();
		  }
		  //get the app version Name for display
		  String version = pInfo.versionName;
		  //get the app version Code for checking
		  versionCode = pInfo.versionCode;
		  //display the current version in a TextView
		  TextView versionText = (TextView) findViewById(R.id.versionName);
		  versionText.setText(version);
		 
		  //Broadcast receiver for our Web Request 
		  IntentFilter filter = new IntentFilter(MyWebReceiver.PROCESS_RESPONSE);
		  filter.addCategory(Intent.CATEGORY_DEFAULT);
		  receiver = new MyWebReceiver();
		  registerReceiver(receiver, filter);
		 
		  //Broadcast receiver for the download manager
		  filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
		  registerReceiver(downloadReceiver, filter);
		 
		  //check of internet is available before making a web service request
		  if(isNetworkAvailable(this)){
		   Intent msgIntent = new Intent(this, InvWebService.class);
			String URL = MainActivity.properties.get("WEBAPP_BASE_URL").toString();

		   msgIntent.putExtra(InvWebService.REQUEST_STRING, URL+"/CheckAppVersion?appName=InventoryMobileApp.apk");
		   startService(msgIntent);
		  }
		 
		 }
		 
		 @Override
		 public boolean onCreateOptionsMenu(Menu menu) {
		  // Inflate the menu; this adds items to the action bar if it is present.
		  getMenuInflater().inflate(R.menu.activity_main, menu);
		  return true;
		 }
		 
		 @Override
		 public void onDestroy() {
		  //unregister your receivers
		  this.unregisterReceiver(receiver);
		  this.unregisterReceiver(downloadReceiver);
		  super.onDestroy();
		 }
		 
		 //check for internet connection
		 private boolean isNetworkAvailable(Context context) {
		  ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		  if (connectivity != null) {
		   NetworkInfo[] info = connectivity.getAllNetworkInfo();
		   if (info != null) {
		    for (int i = 0; i < info.length; i++) {
		     Log.v(LOG_TAG,String.valueOf(i));
		     if (info[i].getState() == NetworkInfo.State.CONNECTED) {
		      Log.v(LOG_TAG, "connected!");
		      return true;
		     }
		    }
		   }
		  }
		  return false;
		 }
		 
		 //broadcast receiver to get notification when the web request finishes
		 public class MyWebReceiver extends BroadcastReceiver{
		 
		  public static final String PROCESS_RESPONSE = "gov.nysenate.inventory.android.intent.action.PROCESS_RESPONSE";
		 
		  @Override
		  public void onReceive(Context context, Intent intent) {
		 
		   String reponseMessage = intent.getStringExtra(InvWebService.RESPONSE_MESSAGE);
		   Log.v(LOG_TAG, reponseMessage);
		 
		   //parse the JSON response
		   JSONObject responseObj;
		   try {
		    responseObj = new JSONObject(reponseMessage);
		    boolean success = responseObj.getBoolean("success");
		    //if the reponse was successful check further
		    if(success){
		     //get the latest version from the JSON string
		     int latestVersion = responseObj.getInt("latestVersion");
		     //get the lastest application URI from the JSON string
		     appURI = responseObj.getString("appURI");
		     //check if we need to upgrade?
		     if(latestVersion > versionCode){
		      //oh yeah we do need an upgrade, let the user know send an alert message
		      AlertDialog.Builder builder = new AlertDialog.Builder(UpgradeActivity.this);
		      builder.setMessage("There is newer version of this application available, click OK to upgrade now?")
		      .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		       //if the user agrees to upgrade
		       public void onClick(DialogInterface dialog, int id) {
		        //start downloading the file using the download manager
		        downloadManager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
		        Uri Download_Uri = Uri.parse(appURI);
		        DownloadManager.Request request = new DownloadManager.Request(Download_Uri);
		        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
		        request.setAllowedOverRoaming(false);
		        request.setTitle("My Andorid App Download");
		        request.setDestinationInExternalFilesDir(UpgradeActivity.this,Environment.DIRECTORY_DOWNLOADS,"InventoryMobileApp.apk");
		        downloadReference = downloadManager.enqueue(request);
		       }
		      })
		      .setNegativeButton("Remind Later", new DialogInterface.OnClickListener() {
		       public void onClick(DialogInterface dialog, int id) {
		        // User cancelled the dialog
		       }
		      });
		      //show the alert message
		      builder.create().show();
		     }
		 
		    }
		   } catch (JSONException e) {
		    e.printStackTrace();
		   }
		 
		  }
		 
		 }
		 
		 //broadcast receiver to get notification about ongoing downloads
		 private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
		 
		  @Override
		  public void onReceive(Context context, Intent intent) {
		 
		   //check if the broadcast message is for our Enqueued download
		   long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
		   if(downloadReference == referenceId){
		 
		    Log.v(LOG_TAG, "Downloading of the new app version complete");
		    //start the installation of the latest version
		    Intent installIntent = new Intent(Intent.ACTION_VIEW);
		    installIntent.setDataAndType(downloadManager.getUriForDownloadedFile(downloadReference), 
		        "application/vnd.android.package-archive");
		    installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		    startActivity(installIntent); 
		     
		   }
		  }
		 }; 
		 
		}
