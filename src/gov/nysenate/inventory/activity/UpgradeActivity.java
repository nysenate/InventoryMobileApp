package gov.nysenate.inventory.activity;

import gov.nysenate.inventory.android.InvWebService;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.android.R.anim;
import gov.nysenate.inventory.android.R.id;
import gov.nysenate.inventory.android.R.layout;
import gov.nysenate.inventory.android.R.menu;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class UpgradeActivity extends SenateActivity
{
    private static final String LOG_TAG = "AppUpgrade";
    private MyWebReceiver receiver;
    private int versionCode = 0;
    String appURI = "";
    public final int INSTALL_INTENT = 2001;
    int latestVersion;
    String latestVersionName;

    final long downloadId = -1;
    TextView newVersion;
    TextView currentVersion;
    public TextView tvDownloadProgress = null;

    Thread downloadStatusThread = null;

    private DownloadManager downloadManager;
    private long downloadReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerBaseActivityReceiver();
        setContentView(R.layout.activity_upgrade);
        // Overall information about the contents of a package
        // This corresponds to all of the information collected from
        // AndroidManifest.xml.
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        // get the app version Name for display
        String version = pInfo.versionName;
        // get the app version Code for checking
        versionCode = pInfo.versionCode;
        // display the current version in a TextView
        currentVersion = (TextView) findViewById(R.id.currentVersion);
        currentVersion.setText("Current Version: " + version + " ("
                + versionCode + ") ");
        tvDownloadProgress = (TextView) findViewById(R.id.tvDownloadProgress);

        // Broadcast receiver for our Web Request
        IntentFilter filter = new IntentFilter(MyWebReceiver.PROCESS_RESPONSE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new MyWebReceiver();
        registerReceiver(receiver, filter);

        // Broadcast receiver for the download manager
        filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(downloadReceiver, filter);

        // First check to see if the Login activity has the latest version info
        // of this App
        // If it does, we don't need to connect to the web service simply to get
        // the
        // version info, instead, we can simply compare..
        // if login activity does not have
        // the latest version info of this App, check of internet is available
        // before
        // making a web service request to get the latest version info from the
        // web service

        if (LoginActivity.latestVersionName != null) {
            this.latestVersionName = LoginActivity.latestVersionName;
            this.latestVersion = LoginActivity.latestVersion;
            this.appURI = LoginActivity.appURI;
            this.compareVersions(true);
        } else if (isNetworkAvailable(this)) {

            Intent msgIntent = new Intent(this, InvWebService.class);
            String URL = LoginActivity.properties.get("WEBAPP_BASE_URL")
                    .toString();

            msgIntent.putExtra(InvWebService.REQUEST_STRING, URL
                    + "/CheckAppVersion?appName=InventoryMobileApp.apk");
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
        // unregister your receivers
        this.unregisterReceiver(receiver);
        this.unregisterReceiver(downloadReceiver);
        super.onDestroy();
    }

    // check for internet connection
    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    Log.v(LOG_TAG, String.valueOf(i));
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        Log.v(LOG_TAG, "connected!");
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // broadcast receiver to get notification when the web request finishes
    public class MyWebReceiver extends BroadcastReceiver
    {

        public static final String PROCESS_RESPONSE = "gov.nysenate.inventory.android.intent.action.PROCESS_RESPONSE";

        @Override
        public void onReceive(Context context, Intent intent) {

            String reponseMessage = intent
                    .getStringExtra(InvWebService.RESPONSE_MESSAGE);
            Log.v(LOG_TAG, reponseMessage);

            // parse the JSON response
            JSONObject responseObj;
            try {
                responseObj = new JSONObject(reponseMessage);
                boolean success = responseObj.getBoolean("success");
                // if the reponse was successful check further
                if (success) {
                    // get the latest version from the JSON string
                    latestVersion = responseObj.getInt("latestVersion");

                    // get the lastest application URI from the JSON string
                    appURI = responseObj.getString("appURI");
                    latestVersionName = responseObj
                            .getString("latestVersionName");
                    /*
                     * Log.i(LOG_TAG, "latestVersion:" + latestVersion +
                     * " > versionCode:" + versionCode);
                     */
                    // check if we need to upgrade?

                    compareVersions(success);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    public void compareVersions(boolean success) {
        if (success) {
            if (latestVersion > versionCode) {
                newVersion = (TextView) findViewById(R.id.newVersion);
                newVersion.setText("Updating to Version: " + latestVersionName
                        + " (" + latestVersion + ")");
                // buttonLogin.setText("Close");
                // progressBarLogin.setVisibility(View.VISIBLE);

                // oh yeah we do need an upgrade, let the user know send
                // an alert message
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        UpgradeActivity.this);
                builder.setMessage(
                        Html.fromHtml("In order to use the Inventory Mobile App, you <b>must</b> download the new version."
                                + " Click <b>OK</b> to download now or <b>Close App</b> to cancel."))
                        .setTitle(
                                Html.fromHtml("<font color='#000055'>UPDATE TO INVENTORY MOBILE APP FOUND. &nbsp;["
                                        + latestVersionName
                                        + "."
                                        + latestVersion + "] </font>"))
                        .setPositiveButton(Html.fromHtml("<b>OK</b>"),
                                new DialogInterface.OnClickListener()
                                {
                                    // if the user agrees to upgrade
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                            int id) {
                                        // start downloading the file
                                        // using the download manager
                                        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                                        Uri Download_Uri = Uri.parse(appURI);
                                        DownloadManager.Request request = new DownloadManager.Request(
                                                Download_Uri);
                                        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
                                        request.setAllowedOverRoaming(false);
                                        request.setTitle(Html
                                                .fromHtml("<font color='#000055'>Inventory Andorid App Download</font>"));
                                        request.setDestinationInExternalFilesDir(
                                                UpgradeActivity.this,
                                                Environment.DIRECTORY_DOWNLOADS,
                                                "InventoryMobileApp.apk");
                                        downloadReference = downloadManager
                                                .enqueue(request);
                                        checkDownloadStatus(downloadManager,
                                                request);
                                        /*
                                         * fileObserver = new DownloadsObserver(
                                         * getExternalFilesDir (Environment
                                         * .DIRECTORY_DOWNLOADS
                                         * ).getAbsolutePath(), downloadManager,
                                         * request, downloadReference);
                                         * fileObserver.startWatching();
                                         */

                                    }
                                })
                        .setNegativeButton(Html.fromHtml("<b>Close App</b>"),
                                new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                            int id) {
                                        // User cancelled the dialog
                                        // finish();
                                        closeAllActivities();
                                    }
                                });
                // show the alert message
                Dialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
            } else {
                returnToLoginScreen(null);
            }

        } else {
            returnToLoginScreen(null);
        }
    }

    @Override
    public void onBackPressed() {
        final Activity currentActivity = this;
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // 2. Chain together various setter methods to set the dialog
        // characteristics
        builder.setMessage("Do you really close the Inventory Mobile App?")
                .setTitle(
                        Html.fromHtml("<font color='#000055'>CLOSE APP</font>"));
        // Add the buttons
        builder.setPositiveButton(Html.fromHtml("<b>Yes</b>"), new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                closeAllActivities();
            }
        });
        builder.setNegativeButton(Html.fromHtml("<b>No</b>"), new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    public void returnToLoginScreen(View View) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("UPDATECHECKED", "TRUE");
        startActivity(intent);
        overridePendingTransition(R.anim.in_up, R.anim.out_up);
    }

    /*
     * Does not work because the setShorcut is fired prior to the new version of
     * the App being installed so it appears to do nothing. (Brian H)
     */

    /*
     * public boolean setShortCut(Context context, String appName) {
     * System.out.println("in the shortcutapp on create method "); boolean flag
     * = false; int app_id = -1; PackageManager p = context.getPackageManager();
     * Intent i = new Intent(Intent.ACTION_MAIN);
     * i.addCategory(Intent.CATEGORY_LAUNCHER); List<ResolveInfo> res =
     * p.queryIntentActivities(i, 0); System.out.println("the res size is: " +
     * res.size());
     * 
     * for (int k = 0; k < res.size(); k++) {
     * System.out.println("the application name is: " +
     * res.get(k).activityInfo.loadLabel(p)); if
     * (res.get(k).activityInfo.loadLabel(p).toString().equals(appName)) { flag
     * = true; app_id = k; break; } }
     * 
     * if (flag) { ActivityInfo ai = res.get(app_id).activityInfo;
     * 
     * Intent shortcutIntent = new Intent();
     * shortcutIntent.setClassName(ai.packageName, ai.name);
     * shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
     * shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); Intent intent =
     * new Intent(); intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT,
     * shortcutIntent);
     * 
     * intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, appName);
     * 
     * intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
     * Intent.ShortcutIconResource.fromContext(context, R.drawable.invapplogo));
     * // intent.addCategory(Intent.CATEGORY_DEFAULT);
     * intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
     * context.sendBroadcast(intent);
     * System.out.println("in the shortcutapp on create method completed"); }
     * else System.out.println("appllicaton not found"); return true; }
     */

    // broadcast receiver to get notification about ongoing downloads
    private BroadcastReceiver downloadReceiver = new BroadcastReceiver()
    {

        @Override
        public void onReceive(Context context, Intent intent) {

            // check if the broadcast message is for our Enqueued download
            long referenceId = intent.getLongExtra(
                    DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (downloadReference == referenceId) {

                Log.v(LOG_TAG, "Downloading of the new app version complete");
                // start the installation of the latest version
                if (downloadStatusThread != null) {
                    try {
                        downloadStatusThread.interrupt();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Intent installIntent = new Intent(Intent.ACTION_VIEW);
                installIntent.setDataAndType(downloadManager
                        .getUriForDownloadedFile(downloadReference),
                        "application/vnd.android.package-archive");
                installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(installIntent, INSTALL_INTENT);

            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case INSTALL_INTENT:
            /*
             * Does not work because the setShorcut is fired prior to the new
             * version of the App being installed so it appears to do nothing.
             * (Brian H)
             */
            // setShortCut(this, "Inventory App");
            closeAllActivities();
            break;
        }
    }

    void checkDownloadStatus(final DownloadManager downloadManager,
            DownloadManager.Request request) {
        final ProgressBar mProgressBar = (ProgressBar) findViewById(R.id.pbDownloadProgress);

        downloadStatusThread = new Thread(new Runnable()
        {

            @Override
            public void run() {

                boolean downloading = true;

                while (downloading) {

                    DownloadManager.Query q = new DownloadManager.Query();
                    q.setFilterById(downloadReference);

                    Cursor cursor = downloadManager.query(q);
                    cursor.moveToFirst();
                    final double bytes_downloaded = cursor.getLong(cursor
                            .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    final double bytes_total = cursor.getLong(cursor
                            .getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                    final double mb_downloaded = Math
                            .round((bytes_downloaded * 100.0)
                                    / (1024.0 * 1024.0)) / 100.0;
                    final double mb_total = Math.round((bytes_total * 100.0)
                            / (1024.0 * 1024.0)) / 100.0;

                    if (cursor.getInt(cursor
                            .getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                        downloading = false;
                    }

                    final double dl_progress = (bytes_downloaded / bytes_total) * 100.0;

                    runOnUiThread(new Runnable()
                    {

                        @Override
                        public void run() {

                            mProgressBar.setProgress((int) dl_progress);
                            tvDownloadProgress.setText(mb_downloaded + " mb / "
                                    + mb_total + " mb    (" + (int) dl_progress
                                    + ")%");

                        }
                    });

                    cursor.close();
                }

            }
        });
        downloadStatusThread.start();
    }
}
