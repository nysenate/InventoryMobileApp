package gov.nysenate.inventory.android;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

import org.apache.http.Header;
import org.apache.http.client.methods.HttpPost;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import gov.nysenate.inventory.activity.LoginActivity;
import gov.nysenate.inventory.activity.SenateActivity;
import gov.nysenate.inventory.util.Toasty;

public class InvApplication extends Application {

    private int cdseclevel = 0;
    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MM/dd/yy hh:mm:ssa", Locale.US);
    private SimpleDateFormat longDayFormat = new SimpleDateFormat("MM/dd/yy EEEE", Locale.US);

    private static final String SET_COOKIE_KEY = "Set-Cookie";
    private static final String COOKIE_KEY = "Cookie";
    private static final String SESSION_COOKIE = "JSESSIONID";
    private static final int SC_SESSION_TIMEOUT = 599;

    public static String timeoutFrom = "";
    public static int timeoutType;

    private String TAG = this.getClass().getName();

    private RequestQueue _requestQueue;
    private SharedPreferences _preferences;

    private static Context context;
    public static SenateActivity currentSenateActivity = null;

    public Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError volleyError) {
            String message = null;
            if (volleyError instanceof NetworkError) {
                message = "Cannot connect due to NetworkErrror!";
            } else if (volleyError instanceof ServerError) {
                message = "The server could not be found!";
            } else if (volleyError instanceof AuthFailureError) {
                message = "Cannot connect due to AuthFailureError!";
            } else if (volleyError instanceof ParseError) {
                message = "Cannot connect due to parsing error!";
            } else if (volleyError instanceof NoConnectionError) {
                message = "Cannot connect to no connection!";
            } else if (volleyError instanceof TimeoutError) {
                message = "Connection Timed Out!";
            }

            if (context == null) {
                context = tryNotNullContext();
            }

            if (volleyError.networkResponse.statusCode == InvApplication.SC_SESSION_TIMEOUT) {
                currentSenateActivity = (SenateActivity) tryNotNullContext();

                if (currentSenateActivity != null) {
                    Intent intentTimeout = new Intent(currentSenateActivity, LoginActivity.class);
                    intentTimeout.putExtra("TIMEOUTFROM", timeoutFrom);
                    currentSenateActivity.startActivityForResult(intentTimeout, timeoutType);
                } else {
                    currentSenateActivity = (SenateActivity) getApplicationContext();
                    if (currentSenateActivity != null)
                        new Toasty(context).showMessage("errorListener currentSenateActivity is null");
                }

                return;

            } else {
                if (currentSenateActivity != null) {
                    new Toasty(context).showMessage("errorListener CALLING onVolleyInvError");
                    currentSenateActivity.onVolleyInvError();
                } else {

                    if (context == null) {
                        new Toasty(context).showMessage("errorListener currentSenateActivity is null");
                    } else {
                        new Toasty(context).showMessage("errorListener currentSenateActivity is " + currentSenateActivity.getClass().getName());
                    }
                }
            }

            AppSingleton.getInstance(context).noServerResponse(message);

            if (currentSenateActivity != null) {
                currentSenateActivity.afterVolleyInvError();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        context = this.getApplicationContext();
    }

    private static InvApplication mContext;

    public static InvApplication getInstance() {
        return mContext;
    }

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

    public static Context getAppContext() {
        return context;
    }

    public RequestQueue getRequestQueue() {
        return _requestQueue;
    }

    private Context tryNotNullContext() {
        if (currentSenateActivity != null) {
            return currentSenateActivity;
        }
        if (this.getApplicationContext() != null) {
            return this.getApplicationContext();
        } else if (getAppContext() != null) {
            return getAppContext();
        } else if (context != null) {
            return context;
        } else if (mContext != null) {
            return mContext;
        } else if (InvApplication.getInstance().getApplicationContext() != null) {
            return InvApplication.getInstance().getApplicationContext();
        } else {
            return currentSenateActivity;
        }
    }

    /**
     * Checks the response headers for session cookie and saves it
     * if it finds it.
     *
     * @param headers Response Headers.
     */
    public final void checkSessionCookie(Map<String, String> headers) {
        if (headers.containsKey(SET_COOKIE_KEY)
                && headers.get(SET_COOKIE_KEY).startsWith(SESSION_COOKIE)) {
            String cookie = headers.get(SET_COOKIE_KEY);
            if (cookie.length() > 0) {
                String[] splitCookie = cookie.split(";");
                String[] splitSessionId = splitCookie[0].split("=");
                cookie = splitSessionId[1];
                SharedPreferences.Editor prefEditor = _preferences.edit();
                prefEditor.putString(SESSION_COOKIE, cookie);
                prefEditor.commit();
            }
        }
    }

    /**
     * Adds session cookie to headers if exists.
     *
     * @param headers
     */
    public final void addSessionCookie(Map<String, String> headers) {

        if (this._preferences == null) {
            this._preferences = getApplicationContext().getSharedPreferences("prefs", 0); // 0
        }

        String sessionId = _preferences.getString(SESSION_COOKIE, "");

        if (sessionId.length() > 0) {
            StringBuilder builder = new StringBuilder();
            builder.append(SESSION_COOKIE);
            builder.append("=");
            builder.append(sessionId);
            if (headers.containsKey(COOKIE_KEY)) {
                builder.append("; ");
                builder.append(headers.get(COOKIE_KEY));
            }
            headers.put(COOKIE_KEY, builder.toString());
        }
    }

    public void addSessionToPost(HttpPost httpPost) {
        Header[] headers = httpPost.getAllHeaders();
        Map<String, String> headersMap = new HashMap<String, String>();
        addSessionCookie(headersMap);
        if (headersMap != null && httpPost != null && headersMap.get(this.SESSION_COOKIE) != null) {
            httpPost.addHeader(this.SESSION_COOKIE, headersMap.get(this.SESSION_COOKIE));
        }
    }

    public SharedPreferences getSharedPreferences() {
        if (this._preferences == null) {
            this._preferences = getApplicationContext().getSharedPreferences("prefs", 0); // 0
        }

        return this._preferences;
    }
}
