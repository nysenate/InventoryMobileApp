package gov.nysenate.inventory.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;
import gov.nysenate.inventory.activity.LoginActivity;
import gov.nysenate.inventory.util.Toasty;

public class CheckInternet extends BroadcastReceiver
{
    private static boolean appConnected = true;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.net.wifi.supplicant.CONNECTION_CHANGE")) {
            handleConnectionChange(context, intent);
        } else if (intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {
            handleConnectivityChange(context, intent);
        }
    }

    private void handleConnectionChange(Context context, Intent intent) {
        if (wifiEnabled(intent)) {
            Log.i("CheckInternet", "Wifi connection enabled");
            // Do nothing, A CONNECTIVITY_CHANGE intent will also be sent once a network connection is established.
        } else {
            Log.i("CheckInternet", "Wifi connection disabled");
            turnOnWifi(context);
        }
    }

    private boolean wifiEnabled(Intent intent) {
        return intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false);
    }

    private void handleConnectivityChange(Context context, Intent intent) {
        if (!activeConnection(intent)) {
            if (appConnected) {
                appConnectionLost(context);
            }
        } else {
            if (connectionIsWifi(context)) {
                appConnectionFound(context);
            }
        }
    }

    private void turnOnWifi(Context context) {
        WifiManager mainWifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (!mainWifi.setWifiEnabled(true)) {
            Toasty.displayCenteredMessage(context, "Unable to turn on Wifi, Please turn it on manually", Toast.LENGTH_SHORT);
        }
    }

    private boolean activeConnection(Intent intent) {
        return !intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
    }

    private void appConnectionLost(Context context) {
        if (appConnected) {
            LoginActivity.chkintWifiLostCount++;
            Toasty.displayCenteredMessage(context, "Wifi connection lost.", Toast.LENGTH_SHORT);
            appConnected = false;
        }
        else {
            Log.i("CheckInternet", "Wifi App disconnected ignored since it already had been disconnected");
        }
    }

    private boolean connectionIsWifi(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.getType() == ConnectivityManager.TYPE_WIFI;
    }

    private void appConnectionFound(Context context) {
        if (appConnected) {
            Log.i("CheckInternet", "Wifi App connected ignored since it already had been connected");
        }
        else {
            LoginActivity.chkintWifiFoundCount++;
            appConnected = true;
            Toasty.displayCenteredMessage(context, "Wifi connection found.", Toast.LENGTH_SHORT);
        }
    }
}
