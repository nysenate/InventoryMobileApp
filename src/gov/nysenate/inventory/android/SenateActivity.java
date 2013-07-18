package gov.nysenate.inventory.android;

import java.util.List;

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
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.Toast;

public abstract class SenateActivity extends Activity
{

    public static final String FINISH_ALL_ACTIVITIES_ACTIVITY_ACTION = "gov.nysenate.inventory.android.FINISH_ALL_ACTIVITIES_ACTIVITY_ACTION";

    private BaseActivityReceiver baseActivityReceiver = new BaseActivityReceiver();
    //private CheckInternet receiver;
    public static final IntentFilter INTENT_FILTER = createIntentFilter();

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
        //unregisterReceiver(receiver);
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
        //IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        //receiver = new CheckInternet();
        //registerReceiver(receiver, filter);
                  
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

    protected void onResume(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
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

    WifiManager mainWifi;
    // WifiReceiver receiverWifi;
    List<ScanResult> wifiList;
    ScanResult currentWifiResult;
    boolean enablingWifi = false;
    boolean prevConnected = false;
    boolean curConnected = false;
    int currentSignalStrength = 0;
    int prevSignalStrength = 0;
    Context context;

    public void checkInternetConnection() {
     /*   new Thread()
        {
            public void run() {*/
                int duration = Toast.LENGTH_SHORT;
                Toast toast;
                if (context == null) {
                    context = getApplicationContext();
                }
                duration = Toast.LENGTH_SHORT;
                toast = Toast.makeText(context, "Inventory App checking Internet Connection. Please be patient..",
                        duration);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                
                SenateActivity.this.context = context;
                ConnectivityManager cm = ((ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE));

                try {
                    if (cm == null)
                        return;
                    prevConnected = curConnected;
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
     /*       }
        }.start();*/

    }

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

    public void turnWifiOn() {
        int duration = Toast.LENGTH_SHORT;

        Toast toast = null;

        try {
            while (!mainWifi.isWifiEnabled()) {
                if (!enablingWifi) {
                    mainWifi.setWifiEnabled(true);
                    enablingWifi = true;
                }
                // Wait to connect
                Thread.sleep(1000);
            }
            if (mainWifi.isWifiEnabled()) {
                toast = Toast.makeText(context,
                        "(Inventory App) Wifi has been turned back on.", duration);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            } else {
                new MsgAlert(context, "WIFI could not be turned back on",
                        "***WARNING: (Inventory App) Unable to turn Wifi on. Please turn it on manually.");
            }

        } catch (Exception e) {
            toast = Toast.makeText(context,
                    "(Inventory App) (EXCEPTION1) Check Internet:" + e.getMessage(), duration);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }
    
}