package gov.nysenate.inventory.util;

import gov.nysenate.inventory.model.Toasty;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import android.content.Context;
import android.content.res.AssetManager;
import android.widget.Toast;

public class AppProperties {

    private static Properties props;

    public static Properties getProperties(Context context) {
        if (props == null) {
            props = new Properties();
            AssetManager assetMgr = context.getApplicationContext().getAssets();
            InputStream is;
            try {
                is = assetMgr.open("invApp.properties");
                props.load(is);
            } catch (IOException e) {
                Toasty.displayCenteredMessage(context, "!!ERROR: Exception loading properties.", Toast.LENGTH_SHORT);
            }
        }
        return props;
    }

    public static String getBaseUrl(Context context) {
        String url = getProperties(context).get("WEBAPP_BASE_URL").toString();
        if (!url.endsWith("/")) {
            url += "/";
        }
        return url;
    }

    public static String getDefrmint(Context context) {
        return getProperties(context).get("DEFRMINT").toString();
    }
}