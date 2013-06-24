//Code needs to be merged with Vikram's Code  (3/7/13)
package gov.nysenate.inventory.android;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
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

    @Override
    public void onReceive(Context context, Intent intent) {
        int duration = Toast.LENGTH_SHORT;
        Toast toast;
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
                    /*
                     * toast = Toast.makeText(context,
                     * "YOU IDIOT. YOU DISCONNECTED YOUR WIFI.", duration);
                     * toast.setGravity(Gravity.CENTER, 0, 0); toast.show();
                     */

                    // check if connected!
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
                            toast = Toast
                                    .makeText(
                                            context,
                                            "OK, IDIOT. I turned the Wifi back on for you. DON'T DO IT AGAIN!!!",
                                            duration);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        } else {
                            toast = Toast
                                    .makeText(
                                            context,
                                            "OK, IDIOT. You will need to turn the Wifi back on, since I cannot fix your messup!!!",
                                            duration);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        }

                    } catch (Exception e) {
                        toast = Toast.makeText(context,
                                "(EXCEPTION1)Check Internet:" + e.getMessage(),
                                duration);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();

                    }
                }
            }
            ;

        } catch (Exception e0) {
            toast = Toast.makeText(context,
                    "(EXCEPTION0)Check Internet:" + e0.getMessage(), duration);
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
    }

}
