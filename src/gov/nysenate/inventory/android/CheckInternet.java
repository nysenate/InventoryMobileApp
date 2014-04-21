package gov.nysenate.inventory.android;

import android.content.*;
import gov.nysenate.inventory.activity.LoginActivity;

import android.net.wifi.WifiManager;
import android.widget.Toast;
import gov.nysenate.inventory.util.Toasty;

public class CheckInternet extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.net.wifi.CONNECTION_CHANGE")) {
            handleConnectionChange(context, intent);

        } else if (intent.getAction().equals("android.net.wifi.WIFI_STATE_CHANGED")) {
            handleEnablingOrDisablingOfWifi(context, intent);
        }
    }

    private void handleConnectionChange(Context context, Intent intent) {
        boolean connected = intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false);

        if (connected) {
            Toasty.displayCenteredMessage(context, "Wifi connection found", Toast.LENGTH_SHORT);
            LoginActivity.chkintWifiFoundCount++;
        } else {
            Toasty.displayCenteredMessage(context, "Wifi connection lost", Toast.LENGTH_SHORT);
            LoginActivity.chkintWifiLostCount++;
            // TODO: try to re-connect to SenateNet
        }
    }

    private void handleEnablingOrDisablingOfWifi(Context context, Intent intent) {
        int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);

        switch (state) {
            case WifiManager.WIFI_STATE_DISABLED:
                turnWifiOn(context);
                break;
            case WifiManager.WIFI_STATE_ENABLED:
                Toasty.displayCenteredMessage(context, "Wifi has been turned back on.", Toast.LENGTH_SHORT);
                break;
        }
    }

    public void turnWifiOn(Context context) {
        WifiManager mainWifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mainWifi.setWifiEnabled(true);
    }
}
