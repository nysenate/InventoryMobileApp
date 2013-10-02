package gov.nysenate.inventory.util;

import gov.nysenate.inventory.model.Toasty;

import org.apache.http.HttpStatus;

import android.content.Context;
import android.widget.Toast;

public class HttpUtils {

    // Displays appropriate messages to user after an attempt to update the database
    public static void displayResponseResults(Context context, int code) {
        if (code == HttpStatus.SC_OK) {
            Toasty.displayCenteredMessage(context, "Successfully updated database", Toast.LENGTH_SHORT);
        } else if (code == HttpStatus.SC_BAD_REQUEST) {
            Toasty.displayCenteredMessage(context, "!!ERROR: Invalid parameter", Toast.LENGTH_SHORT);
        } else if (code == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            Toasty.displayCenteredMessage(context, "!!ERROR: Database Error, your update may not have been saved.", Toast.LENGTH_SHORT);
        } else {
            Toasty.displayCenteredMessage(context, "!!ERROR: Unknown Error, your update may not have been saved.", Toast.LENGTH_SHORT);
        }
    }
}
