package gov.nysenate.inventory.android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.text.Html;
import android.util.Log;
import android.util.LruCache;
import android.view.Gravity;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import gov.nysenate.inventory.activity.SenateActivity;
import gov.nysenate.inventory.util.HttpUtils;
import gov.nysenate.inventory.util.Toasty;

public class AppSingleton {
    private static AppSingleton instance;
    private RequestQueue requestQueue;
    private ImageLoader imageLoader;
    private static Context ctx;
    public String timeoutFrom = "";

    private AppSingleton(Context context) {
        ctx = context;
        requestQueue = getRequestQueue();

        imageLoader = new ImageLoader(requestQueue,
                new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap>
                            cache = new LruCache<String, Bitmap>(20);

                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });
    }

    public static synchronized AppSingleton getInstance(Context context) {
        if (instance == null) {
            instance = new AppSingleton(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }

    public void noServerResponse() {
        noServerResponse((String) null);
    }

    public void noServerResponse(Context context) {
        noServerResponse(null, context);
    }

    public void noServerResponse(String message) {
        Context context = SenateActivity.stContext;

        if (context == null) {
            Log.i(this.getClass().getName(), "Context is Null (1)");
            if (InvApplication.getInstance().getApplicationContext() != null) {
                context = InvApplication.getInstance().getApplicationContext();
            }
        }
        if (context == null) {
            Log.i(this.getClass().getName(), "Context is Null (2)");
            context = InvApplication.getAppContext();
        }

        if (context == null) {
            Log.i(this.getClass().getName(), "Context is Null (3)");
        }

        noServerResponse(message, context);
    }

    public void noServerResponse(String message, Context context) {

        Log.i(this.getClass().getName(), "NOSERVERRESPONSE (APPSINGLETON)");

        if (context == null) {
            context = InvApplication.getAppContext();
        }

        if (context == null) {
            Log.i(this.getClass().getName(), "(0) Context is null!!!!");
            context = InvApplication.getAppContext();
            if (context == null) {
                Log.i(this.getClass().getName(), "Context is null!!!!");
            }
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        if (message == null) {
            message = "";
        } else {
            message = "<br/>" + message;
        }

        new HttpUtils().playSound(R.raw.noconnect);

        if (context == null) {
            Log.i(this.getClass().getName(), "Context is Null!!!");
        } else {

            // set title
            alertDialogBuilder.setTitle(Html
                    .fromHtml("<b><font color='#000055'>NO SERVER RESPONSE</font></b>"));

            // set dialog message
            alertDialogBuilder
                    .setMessage(
                            Html.fromHtml("!!ERROR: There was <font color='RED'><b>NO SERVER RESPONSE</b></font>. " + message + " <br/> Please contact STS/BAC."))
                    .setCancelable(false)
                    .setPositiveButton(Html.fromHtml("<b>Ok</b>"), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            // if this button is clicked, just close
                            // the dialog box and do nothing
                            Context context = InvApplication.getAppContext();

                            CharSequence text = "No action taken due to NO SERVER RESPONSE";
                            int duration = Toast.LENGTH_SHORT;

                            Toast toast = Toast.makeText(context, text, duration);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        }
                    });

            try {
                new Toasty(context).showMessage("!!ERROR: App is Offline.");
            } catch (Exception e) {
                Log.e(this.getClass().getName(), "!!ERROR: Could not show toasty message that App is Offline!! " + message);
            }

            try {
                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
            } catch (Exception e) {
                if (message == null || message.trim().length() == 0) {
                    new Toasty(context).showMessage("!!ERROR: NO SERVER RESPONSE!!!", Toast.LENGTH_LONG);
                } else {
                    new Toasty(context).showMessage("!!ERROR: NO SERVER RESPONSE: " + message, Toast.LENGTH_LONG);
                }
                new Toasty(context).showMessage("!!ERROR: Cannot show Alert Dialog. " + e.getMessage() + ". Please contact STSBAC.", Toast.LENGTH_LONG);
                e.printStackTrace();
            }
        }
    }
}

