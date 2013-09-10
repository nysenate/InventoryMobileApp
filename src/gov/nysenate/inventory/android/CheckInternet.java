//Code needs to be merged with Vikram's Code  (3/7/13)
package gov.nysenate.inventory.android;

import java.util.List;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class CheckInternet extends BroadcastReceiver
{
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

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!InvApplication.isActivityVisible()) {
            Log.i("CheckInternet", "Invapp is invisible");
            return;
        }
        int duration = Toast.LENGTH_SHORT;
        Toast toast;
        if (context == null) {
            System.out.println("CheckInternet CONTEXT IS NULL");
        }
        this.context = context;
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
         * .setTitle
         * (Html.fromHtml("<font color='#000055>Internet Connection Lost</font>"
         * ));
         * 
         * // 3. Get the AlertDialog from create() AlertDialog dialog =
         * builder.create(); dialog.show();
         */
    }

    public void wifiAlert(String title, String msg) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        // set title
        alertDialogBuilder.setTitle(Html.fromHtml("<font color='#000055>"
                + title + "</font>"));

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

}
