package gov.nysenate.inventory.android;

import java.util.List;
import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.NavUtils;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

public abstract class SenateActivity extends Activity implements CommodityDialogListener, OnKeywordChangeListener
{
    public String timeoutFrom = "N/A";
    public static final String FINISH_ALL_ACTIVITIES_ACTIVITY_ACTION = "gov.nysenate.inventory.android.FINISH_ALL_ACTIVITIES_ACTIVITY_ACTION";
    public final int OK = 100, SERVER_SESSION_TIMED_OUT = 1000,
            NO_SERVER_RESPONSE = 1001, EXCEPTION_IN_CODE = 1002;    
    public final int CHECK_SERVER_RESPONSE = 200;
    public static final int COMMODITYLIST = 3030, NEWITEMCOMMENTS = 3031;       

    private BaseActivityReceiver baseActivityReceiver = new BaseActivityReceiver();
    // private CheckInternet receiver;
    public static final IntentFilter INTENT_FILTER = createIntentFilter();
    public static long maxWifiWaitTime = 20 * 1000; // 20 Seconds
    
    public int dialogSelectedRow = -1;
    public String dialogKeywords = null;
    public String dialogComments = null;
    public String dialogTitle = null;
    public String dialogMsg = null;
    
    public NewInvDialog newInvDialog = null;

    @Override
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
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.in_left, R.anim.out_right);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterBaseActivityReceiver();
        InvApplication.activityDestroyed();
        // unregisterReceiver(receiver);
        InvApplication.activityPaused();
    }

    // Quick shortcut for playing a single sound.
    public void playSound(int sound) {
        int[] sounds = new int[1];
        sounds[0] = sound;
        playSounds(sounds);
    }

    public void playSounds(int[] sounds) {
        // void for now.. Might return error msgs, or completion result in
        // future.
        Object[] soundParams = new Object[sounds.length + 1];
        soundParams[0] = getApplicationContext();
        for (int x = 0; x < sounds.length; x++) {
            soundParams[x + 1] = new Integer(sounds[x]);
        }

        AsyncTask<Object, Integer, String> soundResults = new SoundAlert()
                .execute(soundParams);

    }

    private static IntentFilter createIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(FINISH_ALL_ACTIVITIES_ACTIVITY_ACTION);
        return filter;
    }

    protected void registerBaseActivityReceiver() {
        registerReceiver(baseActivityReceiver, INTENT_FILTER);
        // IntentFilter filter = new
        // IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        // receiver = new CheckInternet();
        // registerReceiver(receiver, filter);

    }

    protected void unRegisterBaseActivityReceiver() {
        unregisterReceiver(baseActivityReceiver);
    }

    public class BaseActivityReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction()
                    .equals(FINISH_ALL_ACTIVITIES_ACTIVITY_ACTION)) {
                finish();
            }
        }
    }

    protected void closeAllActivities() {
        sendBroadcast(new Intent(FINISH_ALL_ACTIVITIES_ACTIVITY_ACTION));
        InvApplication.activityDestroyed();
    }

    public void startTimeout(int timeoutType) {
        Intent intentTimeout = new Intent(this, LoginActivity.class);
        intentTimeout.putExtra("TIMEOUTFROM", timeoutFrom);
        startActivityForResult(intentTimeout, timeoutType);
    }
    
    @Override
    public void commoditySelected(int rowSelected, Commodity commoditySelected) {
        // TODO Auto-generated method stub
        
    }    

    public void getDialogDataFromServer() {
    
    }
    
    public void reOpenNewInvDialog(){
        if (newInvDialog!=null) {
            newInvDialog.dismiss();
        }
        android.app.FragmentManager fm = this.getFragmentManager();
        newInvDialog = new NewInvDialog(this, dialogTitle, dialogMsg);
        newInvDialog.addListener(this);
        newInvDialog.setRetainInstance(true);
        newInvDialog.show(fm, "fragment_name");        
    }
    
    public void startKeywordSpeech(View view) {
        if (view.getId() == R.id.btnKeywordSpeech) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                    "Commodity Keyword Search");
            startActivityForResult(intent, COMMODITYLIST);
        }     
    }   
    
    public void startNewItemSpeech(View view) {
        if (view.getId() == R.id.btnNewItemCommentSpeech) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                    "New Item Comments");
            startActivityForResult(intent, NEWITEMCOMMENTS);
        }     
    }       
    public void noServerResponseMsg() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle("NO SERVER RESPONSE");

        // set dialog message
        alertDialogBuilder
                .setMessage(
                        Html.fromHtml("!!ERROR: There was <font color='RED'><b>NO SERVER RESPONSE</b></font>. <br/> Please contact STS/BAC."))
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        Context context = getApplicationContext();

                        CharSequence text = "No action taken due to NO SERVER RESPONSE";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();

                        dialog.dismiss();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    protected void onResume(Bundle savedInstanceState) {
        super.onResume();
        InvApplication.activityResumed();
        checkInternetConnection();
    }

    @Override
    protected void onResume() {
        super.onResume();
        InvApplication.activityResumed();
        checkInternetConnection();
    }

    @Override
    protected void onPause() {
        super.onPause();
        InvApplication.activityPaused();
    }

    public int checkServerResponse() {
        return checkServerResponse(true);
    }

    public int checkServerResponse(boolean handleServerResponse) {                  
        String serverResponse = null;
        AsyncTask<String, String, String> requestServerResponse = null;
        // check network connection
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // fetch data

            try {
                // Get the URL from the properties
                String URL = LoginActivity.properties.get("WEBAPP_BASE_URL")
                        .toString();
                requestServerResponse = new RequestTask().execute(URL
                        + "/KeepSessionAlive");

                try {
                    serverResponse = null;
                    serverResponse = requestServerResponse.get().trim()
                            .toString();
                    if (serverResponse == null) {
                        if (handleServerResponse) {
                            noServerResponseMsg();
                        }                  
                        return NO_SERVER_RESPONSE;
                    } else if (serverResponse.indexOf("Session timed out") > -1) {
                        if (handleServerResponse) {
                            startTimeout(this.CHECK_SERVER_RESPONSE);
                        }
                        return SERVER_SESSION_TIMED_OUT;
                    }

                } catch (NullPointerException e) {
                    if (handleServerResponse) {
                        noServerResponseMsg();
                    }
                    return EXCEPTION_IN_CODE;
                }

            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            // display error
        }
        return OK;
    }

    WifiManager mainWifi;
    // WifiReceiver receiverWifi;
    List<ScanResult> wifiList;
    ScanResult currentWifiResult;
    boolean enablingWifi = false;
    boolean prevConnected = true; // Assume that connection was already found so
                                  // that it doesn't show Wifi Connection Found
                                  // for every Activity
    boolean curConnected = true; // Assume that connection was already found so
                                 // that it doesn't show Wifi Connection Found
                                 // for every Activity
                                 // The Check Internet Service will check for
                                 // connections and disconnections.
    int currentSignalStrength = 0;
    int prevSignalStrength = 0;
    Context context;

    /*
     * The app needs an internet connection, so check it on Activity Startup
     * (fires when Any Activity is opened) If the internet connection is turned
     * off, then attempt to turn it on.
     */
    
    

    public void checkInternetConnection() {
        /*
         * new Thread() { public void run() {
         */
        int duration = Toast.LENGTH_SHORT;
        Toast toast;
        if (context == null) {
            context = getApplicationContext();
        }
        duration = Toast.LENGTH_SHORT;
        /*
         * toast = Toast.makeText(context,
         * "Inventory App checking Internet Connection. Please be patient..",
         * duration); toast.setGravity(Gravity.CENTER, 0, 0); toast.show();
         */

        SenateActivity.this.context = context;
        ConnectivityManager cm = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE));

        try {
            if (cm == null) {
                return;
            }

            prevConnected = curConnected;
            /*
             * If the internet connection is on, then check to see if it
             * previously wasn't, if it was off then show the Wifi Connection
             * Found toast.
             * 
             * If there is no internet connection, but Wifi on the Tablet is
             * turned on then return the toast that the Wifi Connection is lost.
             * 
             * If there is no internet connection and the Wifi is off on the
             * tablet, then attempt to turn on the Wifi Connection.
             */
            if (cm.getActiveNetworkInfo() != null
                    && cm.getActiveNetworkInfo().isConnected()) {
                curConnected = cm.getActiveNetworkInfo().isConnected();
                if (!prevConnected) {
                    duration = Toast.LENGTH_LONG;
                    toast = Toast.makeText(context, "Wifi Connection found.",
                            duration);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            } else {

                mainWifi = (WifiManager) context
                        .getSystemService(Context.WIFI_SERVICE);
                curConnected = false;
                if (mainWifi.isWifiEnabled()) {
                    if (prevConnected) {
                        duration = Toast.LENGTH_LONG;
                        toast = Toast.makeText(context,
                                "Wifi Connection lost.", duration);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        prevConnected = false;
                    }
                } else {
                    // check if connected! (will not work in a service)
                    // wifiAlert("WIFI IS CURRENTLY TURNED OFF",
                    // "***WARNING: Wifi is currently turned <font color='RED'>OFF</font>. This app cannot work without the Wifi connection. Do you want to turn it on?");

                    // so using turnwifi on directly
                    turnWifiOn();
                }
            }
            ;

        } catch (Exception e0) {
            toast = Toast.makeText(context,
                    "(EXCEPTION0) Check Internet:" + e0.getMessage(), duration);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();

        }
        // 1. Instantiate an AlertDialog.Builder with its constructor
        /*
         * AlertDialog.Builder builder = new AlertDialog.Builder(context);
         * 
         * // 2. Chain together various setter methods to set the dialog
         * characteristics builder.setMessage(
         * "Internet Connection is Lost. Please fix before continuing.")
         * .setTitle("Internet Connection Lost");
         * 
         * // 3. Get the AlertDialog from create() AlertDialog dialog =
         * builder.create(); dialog.show();
         */
        /*
         * } }.start();
         */

    }

    /*
     * Wifi Alert does not work with the Check Internet connection Service..
     * Original plans were to ask the user before connecting, but those plans
     * have changed.. Now while the app is opened and in the foreground, we will
     * try keep the wifi on (the app is useless without an internet connection).
     * We will not try keeping the internet connection on if the App is closed
     * or in the background. Currently, WifiAlert is not being used for this
     * reason.
     */

    public void wifiAlert(String title, String msg) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        // set title
        alertDialogBuilder.setTitle(title);

        // set dialog message
        alertDialogBuilder.setMessage(Html.fromHtml(msg)).setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing

                        turnWifiOn();

                        dialog.dismiss();
                    }

                })
                .setNegativeButton("No", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing

                        CharSequence text = "Wifi will remain off.";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();

                        dialog.dismiss();
                    }

                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    /*
     * Turn Wifi Connection back on if it is not Enabled
     */

    public void turnWifiOn() {
        turnWifiOn(maxWifiWaitTime);
    }

    public void turnWifiOn(long maxWifiWaitTime) {
        int duration = Toast.LENGTH_SHORT;

        Toast toast = null;
        enablingWifi = false;
        long startTime = System.currentTimeMillis();

        try {
            // Wait until Wifi is Enabled or the Maximum Wifi Wait Time has
            // occurred
            while (!mainWifi.isWifiEnabled()
                    && (System.currentTimeMillis() - startTime <= maxWifiWaitTime)) {
                // While waiting, attempt to turn on the Wifi Connection if it
                // already hasn't been attempted
                if (!enablingWifi) {
                    mainWifi.setWifiEnabled(true);
                    enablingWifi = true;
                }
                // Wait to connect
                Thread.sleep(1000);
            }
            // Check to see if the Wifi Connection turned on, give appropriate
            // message depending on if it was or not.
            if (mainWifi.isWifiEnabled()) {
                toast = Toast.makeText(context,
                        "(Inventory App) Wifi has been turned back on.",
                        duration);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            } else {
                new MsgAlert(
                        context,
                        "WIFI could not be turned back on",
                        "***WARNING: (Inventory App) Unable to turn Wifi on. Please turn it on manually.");
            }

        } catch (Exception e) {
            toast = Toast.makeText(
                    context,
                    "(Inventory App) (EXCEPTION1) Check Internet:"
                            + e.getMessage(), duration);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }
    
    @Override
    public void OnKeywordChange(KeywordDialog keywordDialog, ListView lvKeywords, String keywords) {
        newInvDialog.tvKeywordsToBlock.setText(keywords);
        getDialogDataFromServer();
        
    }      

}