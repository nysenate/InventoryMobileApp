package gov.nysenate.inventory.util;

import android.content.Context;
import android.widget.Toast;
import org.apache.http.HttpStatus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpUtils {

    public static final int SC_SESSION_TIMEOUT = 599;

    // Displays appropriate messages to user after an attempt to update the database
    public static void displayResponseResults(Context context, Integer code) {
        if (code != null) {
            if (code == HttpStatus.SC_OK) {
                Toasty.displayCenteredMessage(context, "Successfully updated database", Toast.LENGTH_SHORT);
            } else if (code == HttpStatus.SC_BAD_REQUEST) {
                Toasty.displayCenteredMessage(context, "!!ERROR: Invalid parameter", Toast.LENGTH_SHORT);
            } else if (code == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                Toasty.displayCenteredMessage(context, "!!ERROR: Database Error, your update may not have been saved.", Toast.LENGTH_SHORT);
            } else {
                Toasty.displayCenteredMessage(context, "!!ERROR: Unknown Error, your update may not have been saved.", Toast.LENGTH_SHORT);
            }
        } else {
            Toasty.displayCenteredMessage(context, "!!ERROR: Exception when communicating with server, your update may not have been saved.", Toast.LENGTH_SHORT);
        }
    }

    public static void displayQueryResults(Context context, Integer code) {
        if (code == null || code != HttpStatus.SC_OK) {
            Toasty.displayCenteredMessage(context, "Error retrieving location list.", Toast.LENGTH_SHORT);
        }
    }

    public boolean validateIpAddress(final String ipAddress){
        final String IPADDRESS_PATTERN =
                        "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
        Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
        Matcher matcher = pattern.matcher(ipAddress);
        return matcher.matches();
    }
}
