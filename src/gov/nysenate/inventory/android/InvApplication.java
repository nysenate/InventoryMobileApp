package gov.nysenate.inventory.android;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.app.Application;

public class InvApplication extends Application
{

    private int cdseclevel = 0;
    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MM/dd/yy hh:mm:ssa", Locale.US);
    private SimpleDateFormat longDayFormat = new SimpleDateFormat("MM/dd/yy EEEE", Locale.US);

    public void setCdseclevel(int level) {
        cdseclevel = level;
    }

    public int getCdseclevel() {
        return cdseclevel;
    }

    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static boolean isActivityDestroyed() {
        return activityDestroyed;
    }

    public static void activityResumed() {
        activityVisible = true;
        activityDestroyed = false;
    }

    public static void activityPaused() {
        activityVisible = false;
        activityDestroyed = false;
    }

    public static void activityDestroyed() {
        activityDestroyed = true;
    }

    private static boolean activityVisible;
    private static boolean activityDestroyed = false;

    public SimpleDateFormat getDateTimeFormat() {
        return dateTimeFormat;
    }

    public SimpleDateFormat getLongDayFormat() {
        return longDayFormat;
    }
}
