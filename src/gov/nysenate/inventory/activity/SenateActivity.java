package gov.nysenate.inventory.activity;

import gov.nysenate.inventory.android.ChangePasswordDialog;
import gov.nysenate.inventory.android.CommentsDialog;
import gov.nysenate.inventory.android.InvApplication;
import gov.nysenate.inventory.android.KeywordDialog;
import gov.nysenate.inventory.android.MsgAlert;
import gov.nysenate.inventory.android.NewInvDialog;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.android.RequestTask;
import gov.nysenate.inventory.android.SoundAlert;
import gov.nysenate.inventory.listener.ChangePasswordDialogListener;
import gov.nysenate.inventory.listener.CommodityDialogListener;
import gov.nysenate.inventory.listener.OnKeywordChangeListener;
import gov.nysenate.inventory.model.Commodity;
import gov.nysenate.inventory.model.Employee;
import gov.nysenate.inventory.model.LoginStatus;
import gov.nysenate.inventory.util.Toasty;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.RecognizerIntent;
import android.support.v4.app.NavUtils;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public abstract class SenateActivity extends Activity implements
        CommodityDialogListener, OnKeywordChangeListener, ChangePasswordDialogListener
{
    public String timeoutFrom = "N/A";
    public static final String FINISH_ALL_ACTIVITIES_ACTIVITY_ACTION = "gov.nysenate.inventory.android.FINISH_ALL_ACTIVITIES_ACTIVITY_ACTION";
    public final int OK = 100, SERVER_SESSION_TIMED_OUT = 1000,
            NO_SERVER_RESPONSE = 1001, EXCEPTION_IN_CODE = 1002;
    public final int CHECK_SERVER_RESPONSE = 200;
    public static final int COMMODITYLIST = 3030, NEWITEMCOMMENTS = 3031,
            ITEMCOMMENTS = 3032;

    private BaseActivityReceiver baseActivityReceiver = new BaseActivityReceiver();
    // private CheckInternet receiver;
    public static final IntentFilter INTENT_FILTER = createIntentFilter();
    public static long maxWifiWaitTime = 20 * 1000; // 20 Seconds

    public int dialogSelectedRow = -1;
    public String dialogKeywords = null;
    public String dialogComments = null;
    public String dialogTitle = null;
    public String dialogMsg = null;
    public boolean senateTagNum = false;

    public NewInvDialog newInvDialog = null;
    public CommentsDialog commentsDialog = null;
    static Context stContext;
    public  ChangePasswordDialog changePasswordDialog = null;
    android.app.FragmentManager fragmentManager = this.getFragmentManager();    
    String defrmint = null;
    public static boolean changePasswordOnLogin = false;
    public LoginActivity currentLoginActivity = null;
    protected final int DBASTRING = 5000, DBANAME = 5001, DBASERVER = 5002, DBAPORT = 5003;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
        case android.R.id.home:
            Toast toast = Toast.makeText(getApplicationContext(), "Going Back",
                    Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            NavUtils.navigateUpFromSameTask(this);

            overridePendingTransition(R.anim.in_left, R.anim.out_right);
            return true;
        case R.id.aboutApp:
            showAboutDialog();
            return true;
        case R.id.changePasswordMenu:
            if (LoginActivity.user_name.getText().toString().trim().length()==0) {
                new Toasty(this, "!!ERROR: Username must first be entered when changing password.", Toast.LENGTH_SHORT).showMessage();
            }
            else {
                changePasswordOnLogin = false;                
                showChangePasswordDialog();
            }
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.in_left, R.anim.out_right);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterBaseActivityReceiver();
        InvApplication.activityDestroyed();
        // unregisterReceiver(receiver);
        InvApplication.activityPaused();
    }

    // Quick shortcut for playing a single sound.
    public void playSound(int sound) {
        int[] sounds = new int[1];
        sounds[0] = sound;
        playSounds(sounds);
    }

    public void playSounds(int[] sounds) {
        // void for now.. Might return error msgs, or completion result in
        // future.
        Object[] soundParams = new Object[sounds.length + 1];
        soundParams[0] = getApplicationContext();
        for (int x = 0; x < sounds.length; x++) {
            soundParams[x + 1] = new Integer(sounds[x]);
        }

        AsyncTask<Object, Integer, String> soundResults = new SoundAlert()
                .execute(soundParams);

    }

    private static IntentFilter createIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(FINISH_ALL_ACTIVITIES_ACTIVITY_ACTION);
        return filter;
    }

    protected void registerBaseActivityReceiver() {
        registerReceiver(baseActivityReceiver, INTENT_FILTER);
        // IntentFilter filter = new
        // IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        // receiver = new CheckInternet();
        // registerReceiver(receiver, filter);

    }

    protected void unRegisterBaseActivityReceiver() {
        unregisterReceiver(baseActivityReceiver);
    }

    public class BaseActivityReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction()
                    .equals(FINISH_ALL_ACTIVITIES_ACTIVITY_ACTION)) {
                finish();
            }
        }
    }
    
    protected void closeAllActivities() {
        sendBroadcast(new Intent(FINISH_ALL_ACTIVITIES_ACTIVITY_ACTION));
        InvApplication.activityDestroyed();
    }

    public void startTimeout(int timeoutType) {
        Intent intentTimeout = new Intent(this, LoginActivity.class);
        intentTimeout.putExtra("TIMEOUTFROM", timeoutFrom);
        startActivityForResult(intentTimeout, timeoutType);
    }

    @Override
    public void commoditySelected(int rowSelected, Commodity commoditySelected) {
        // TODO Auto-generated method stub

    }

    public void getDialogDataFromServer() {

    }
    
    public void showAboutDialog() {
        final Activity currentActivity = this;
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        // get the app version Name for display
        String version = pInfo.versionName;
        // get the app version Code for checking
        int versionCode = pInfo.versionCode;
        Resources resources;
        resources = this.getResources();
        AssetManager assetManager = resources.getAssets();
        String URL  = "<N/A>";
        String server = "<N/A>";
        String port = "<N/A>";
        
        String dba = parseServerDatabaseString(DBANAME);
        
        boolean httpServer = false;
        
        try {
            InputStream inputStream = assetManager.open("invApp.properties");
            Properties properties = new Properties();
            properties.load(inputStream); // we load the properties here and we
                                          // use same object elsewhere in
                                          // project
            URL = properties.get("WEBAPP_BASE_URL").toString().trim();
            this.defrmint = properties.get("DEFRMINT").toString();            
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (URL==null||URL.length()==0) {
            
        }
        else {
            if (URL.toUpperCase().startsWith("HTTP://")) {
                String IPADDRESS_PATTERN = 
                        "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";

                Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
                URL = URL.replaceAll("(?i)HTTP://", "");
                Matcher matcher = pattern.matcher(URL);
                boolean ipAddressFound = matcher.find();
                if (ipAddressFound) {
                    server =  matcher.group();
                    System.out.println("ipAddressFound:"+server);
                    URL = URL.replaceAll(server,"");
                    int colon = URL.indexOf(":");
                    port = URL.substring(colon+1);
                    port = port.replaceAll("\\D+", "");
                    httpServer = true;                    
                }
                else {
                    int period = URL.indexOf(".");
                    int colon = URL.indexOf(":");
                    System.out.println("ipAddressNOTFound:"+server);
                    if (period>-1) {
                        server = URL.substring(0, period);
                    }
                    if (colon>-1) {
                        port = URL.substring(colon+1);
                    }            
                    else {
                        port = "<N/A>";
                    }
                    port = port.replaceAll("\\D+", "");
                    httpServer = true;
                }
              }
            else if (URL.toUpperCase().startsWith("HTTPS://")) {
                String IPADDRESS_PATTERN = 
                        "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
                
                Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
                URL = URL.replaceAll("(?i)HTTPS://", "");
                Matcher matcher = pattern.matcher(URL);
                boolean ipAddressFound = matcher.find();
                if (ipAddressFound) {
                    server =  matcher.group();
                    URL = URL.replaceAll(server,"");
                    int colon = URL.indexOf(":");
                    port = URL.substring(colon+1);
                    port = port.replaceAll("\\D+", "");
                    httpServer = true;                    
                }
                else {                
                    int period = URL.indexOf(".");
                    int colon = URL.indexOf(":");
                    if (period>-1) {
                        server = URL.substring(0, period);
                    }
                    if (colon>-1) {
                        port = URL.substring(colon+1);
                    }
                    else {
                        port = "<N/A>";
                    }
                    port = port.replaceAll("\\D+", "");
                    server = server.replaceAll("(?i)HTTPS://", "");
                    httpServer = true;
                }
            }
            else {
                server = URL;
                httpServer = false;
            }
        }
        
        String message;
        
        if (httpServer) {
            message = "<b>Version:</b> " + version + " ("
                    + versionCode + ") <br/><br/><b>Server:</b> " + server +" <br/><br/><b>Port:</b> "+port+"<br/>";
            if (dba!=null && dba.trim().length()>0) {
                message = message + "<br/><b>Database:</b> "+dba+"<br/>";
            }
        }
        else {
            message = "<b>Version:</b> " + version + " ("
                    + versionCode + ") <br/><br/><b>Server:</b> " + URL +"<br/>";
            if (dba!=null && dba.trim().length()>0) {
                message = message + "<br/><b>Database:</b> "+dba+"<br/>";
            }
        }
        
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // 2. Chain together various setter methods to set the dialog
        // characteristics
        builder.setTitle(Html
                .fromHtml("<font color='#000055'>About Inventory Mobile App</font>"));
        builder.setMessage(Html.fromHtml(message));
        // Add the buttons
        builder.setPositiveButton(Html.fromHtml("<b>OK</b>"),
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.show();
        TextView messageText = (TextView)dialog.findViewById(android.R.id.message);
        messageText.setGravity(Gravity.CENTER);        
         
    }
    
   public ChangePasswordDialog showChangePasswordDialog() {
        return showChangePasswordDialog(null, null, null, null);
   }

   public ChangePasswordDialog showChangePasswordDialog( int initialFocus) {
       return showChangePasswordDialog(null, null, null, null, initialFocus);
  }   
   
   public ChangePasswordDialog showChangePasswordDialog(boolean oldPasswordRequired, int initialFocus) {
       return showChangePasswordDialog(null, null, null, null, oldPasswordRequired, initialFocus);
   }
   
   public ChangePasswordDialog showChangePasswordDialog(boolean oldPasswordRequired) {
       return showChangePasswordDialog(null, null, null, null, oldPasswordRequired);
   }
 
   public ChangePasswordDialog showChangePasswordDialog(LoginStatus loginStatus, int initialFocus) {
       return showChangePasswordDialog(loginStatus, null, null, null, initialFocus);
   }

   public ChangePasswordDialog showChangePasswordDialog(LoginStatus loginStatus) {
       return showChangePasswordDialog(loginStatus, null, null, null, ChangePasswordDialog.DEFAULTFOCUS);
   }

   public ChangePasswordDialog showChangePasswordDialog(LoginStatus loginStatus, boolean oldPasswordRequired, int initialFocus) {
       return showChangePasswordDialog(loginStatus, null, null, null, oldPasswordRequired, initialFocus);
   }  

   public ChangePasswordDialog showChangePasswordDialog(LoginStatus loginStatus, boolean oldPasswordRequired) {
       return showChangePasswordDialog(loginStatus, null, null, null, oldPasswordRequired, ChangePasswordDialog.DEFAULTFOCUS);
   }  

   public ChangePasswordDialog showChangePasswordDialog(String oldPassword, String newPassword, String confirmPassword, int initialFocus) {
       return showChangePasswordDialog(null, oldPassword, newPassword, confirmPassword, initialFocus);
   }
   
   public ChangePasswordDialog showChangePasswordDialog(String oldPassword, String newPassword, String confirmPassword) {
       return showChangePasswordDialog(null, oldPassword, newPassword, confirmPassword, ChangePasswordDialog.DEFAULTFOCUS);
   }
   
   public ChangePasswordDialog showChangePasswordDialog(String oldPassword, String newPassword, String confirmPassword, boolean oldPasswordRequired, int initialFocus) {
       return showChangePasswordDialog(null, oldPassword, newPassword, confirmPassword, oldPasswordRequired, initialFocus);
   }

   public ChangePasswordDialog showChangePasswordDialog(String oldPassword, String newPassword, String confirmPassword, boolean oldPasswordRequired) {
       return showChangePasswordDialog(null, oldPassword, newPassword, confirmPassword, oldPasswordRequired, ChangePasswordDialog.DEFAULTFOCUS);
   }
   
   public ChangePasswordDialog showChangePasswordDialog(LoginStatus loginStatus, String oldPassword, String newPassword, String confirmPassword) {
       return showChangePasswordDialog(loginStatus, oldPassword, newPassword, confirmPassword, true, ChangePasswordDialog.DEFAULTFOCUS);
   }

   public ChangePasswordDialog showChangePasswordDialog(LoginStatus loginStatus, String oldPassword, String newPassword, String confirmPassword, int initialFocus) {
       return showChangePasswordDialog(loginStatus, oldPassword, newPassword, confirmPassword, true, initialFocus);
   }
      
   public ChangePasswordDialog showChangePasswordDialog(LoginStatus loginStatus, String oldPassword, String newPassword, String confirmPassword, boolean oldPasswordRequired) {
       return showChangePasswordDialog(loginStatus, oldPassword, newPassword, confirmPassword, oldPasswordRequired, ChangePasswordDialog.DEFAULTFOCUS);
   }
   
   public ChangePasswordDialog showChangePasswordDialog(LoginStatus loginStatus, String oldPassword, String newPassword, String confirmPassword, boolean oldPasswordRequired, int initialFocus) {
       
           playSound(R.raw.warning);
           String title = "Change Password";
           String message = "";
            
           if (changePasswordOnLogin) {
                title = "**WARNING: Your password has expired. Please enter a New Password";
           }  
           
           changePasswordDialog = new ChangePasswordDialog(this, title, message, oldPasswordRequired, oldPassword, newPassword, confirmPassword, initialFocus);
           changePasswordDialog.addListener(this);
           changePasswordDialog.setRetainInstance(true);
           changePasswordDialog.show(fragmentManager, "change_password_dialog");
           
           return changePasswordDialog;
           
    }
    

    public void reOpenNewInvDialog() {
        if (newInvDialog != null) {
            newInvDialog.dismiss();
        }
        android.app.FragmentManager fm = this.getFragmentManager();
        newInvDialog = new NewInvDialog(this, dialogTitle, dialogMsg);
        newInvDialog.addListener(this);
        newInvDialog.setRetainInstance(true);
        newInvDialog.show(fm, "fragment_name");
        // newInvDialog.getDialog().setCanceledOnTouchOutside(false);
    }

    public void reOpenCommentsDialog() {
        if (newInvDialog != null) {
            newInvDialog.dismiss();
        }
        android.app.FragmentManager fm = this.getFragmentManager();
        commentsDialog = new CommentsDialog(this, dialogTitle, dialogMsg);
        commentsDialog.setRetainInstance(true);
        commentsDialog.show(fm, "fragment_name");
        // commentsDialog.getDialog().setCanceledOnTouchOutside(false);
    }

    public void startKeywordSpeech(View view) {
        if (view.getId() == R.id.btnKeywordSpeech) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                    "Commodity Keyword Search");
            startActivityForResult(intent, COMMODITYLIST);
        }
    }

    public void startNewItemSpeech(View view) {
        if (view.getId() == R.id.btnNewItemCommentSpeech) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "New Item Comments");
            startActivityForResult(intent, NEWITEMCOMMENTS);
        }
    }

    public void startItemCommentSpeech(View view) {
        if (view.getId() == R.id.btnItemCommentSpeech) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Item Comments");
            startActivityForResult(intent, ITEMCOMMENTS);
        }
    }
    
    public String parseServerDatabaseString(int returnValue) {
        String serverResponse = null;
        AsyncTask<String, String, String> requestServerResponse = null;
        // check network connection
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // fetch data

            try {
                // Get the URL from the properties
                String URL = LoginActivity.properties.get("WEBAPP_BASE_URL")
                        .toString();
                System.out.println("parseServerDatabaseString:"+URL+ "/GetDatabaseName");
                requestServerResponse = new RequestTask().execute(URL
                        + "/GetDatabaseName");

                try {
                    serverResponse = null;
                    String resp = requestServerResponse.get();
                    serverResponse = requestServerResponse.get().trim()
                            .toString();
                    if (serverResponse == null) {
                        //noServerResponseMsg();
                        return "";
                    } else if (serverResponse.indexOf("Session timed out") > -1) {
                        // TODO Handle Session Timeouts, for now, simply return nothing.
                        return "";
                    }
                    else if (serverResponse.trim().length()==0) {
                        //noServerResponseMsg();
                        return "";
                    }
                    else {
                        try {
                            String[] splitDBAString = serverResponse.split(":");
                            if (splitDBAString.length<1) {
                                switch (returnValue) {
                                case DBASTRING: 
                                    return serverResponse;
                                default:
                                    return "";
                                }
                            }
                            else {
                                // dbaString = jdbc:{driver}:{driver type}:@{dba server}:{dba port}:{dba name}
                                
                                switch (returnValue) {
                                case DBASTRING:
                                    return serverResponse;
                                case DBANAME:
                                    if (splitDBAString.length>5) {
                                        return splitDBAString[5];
                                    }
                                    else {
                                        return "";
                                    }
                                case DBASERVER:
                                    if (splitDBAString.length>3) {
                                        return splitDBAString[3];
                                    }
                                    else {
                                        return "";
                                    }
                                case DBAPORT:    
                                    if (splitDBAString.length>4) {
                                        return splitDBAString[4];
                                    }
                                    else {
                                        return "";
                                    }
                                }
                            }
                            
                        }
                        catch (NullPointerException e1) {
                            System.out.println("NULLPOINTER ERROR:"+e1.getMessage());
                            e1.printStackTrace();
                            return "";
                        }
                        catch (Exception e1) {
                            System.out.println("ERROR:"+e1.getMessage());
                            e1.printStackTrace();
                            return "";
                        }
                        
                    }

                } catch (NullPointerException e) {
                    noServerResponse();
                    return "";
                }

            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return "";                
            } catch (ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return "";                
            }
        } else {
            // TODO Handle Errors, for now, simply return nothing.
            return "";
        }
        return "";        
             
    }

    protected void onResume(Bundle savedInstanceState) {
        super.onResume();
        InvApplication.activityResumed();
        checkInternetConnection();
    }

    @Override
    protected void onResume() {
        super.onResume();
        InvApplication.activityResumed();
        checkInternetConnection();
        timer.cancel();
        if(!((Object)this).getClass().getSimpleName().equalsIgnoreCase("LoginActivity"))
        timer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        InvApplication.activityPaused();
        
        timer.cancel();
    }


    public int checkServerResponse() {
        return checkServerResponse(true);
    }

    public int checkServerResponse(boolean handleServerResponse) {
        Log.i("internet check", "checkServerResponse");
        String serverResponse = null;
        AsyncTask<String, String, String> requestServerResponse = null;
        // check network connection
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // fetch data

            try {
                // Get the URL from the properties
                String URL = LoginActivity.properties.get("WEBAPP_BASE_URL")
                        .toString();
                requestServerResponse = new RequestTask().execute(URL
                        + "/KeepSessionAlive");

                try {
                    serverResponse = null;
                    serverResponse = requestServerResponse.get().trim()
                            .toString();
                    if (serverResponse == null) {
                        if (handleServerResponse) {
                            noServerResponse();
                        }
                        return NO_SERVER_RESPONSE;
                    } else if (serverResponse.indexOf("Session timed out") > -1) {
                        if (handleServerResponse) {
                            startTimeout(this.CHECK_SERVER_RESPONSE);
                        }
                        return SERVER_SESSION_TIMED_OUT;
                    }

                } catch (NullPointerException e) {
                    if (handleServerResponse) {
                        noServerResponse();
                    }
                    return EXCEPTION_IN_CODE;
                }

            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            // display error
        }
        return OK;
    }

    WifiManager mainWifi;
    // WifiReceiver receiverWifi;
    List<ScanResult> wifiList;
    ScanResult currentWifiResult;
    boolean enablingWifi = false;
    boolean prevConnected = true; // Assume that connection was already found so
                                  // that it doesn't show Wifi Connection Found
                                  // for every Activity
    boolean curConnected = true; // Assume that connection was already found so
                                 // that it doesn't show Wifi Connection Found
                                 // for every Activity
                                 // The Check Internet Service will check for
                                 // connections and disconnections.
    int currentSignalStrength = 0;
    int prevSignalStrength = 0;
    Context context;

    /*
     * The app needs an internet connection, so check it on Activity Startup
     * (fires when Any Activity is opened) If the internet connection is turned
     * off, then attempt to turn it on.
     */

    public void checkInternetConnection() {
        /*
         * new Thread() { public void run() {
         */
        int duration = Toast.LENGTH_SHORT;
        Toast toast;
        if (context == null) {
            context = getApplicationContext();
        }
        duration = Toast.LENGTH_SHORT;
        /*
         * toast = Toast.makeText(context,
         * "Inventory App checking Internet Connection. Please be patient..",
         * duration); toast.setGravity(Gravity.CENTER, 0, 0); toast.show();
         */

        SenateActivity.this.context = context;
        ConnectivityManager cm = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE));

        try {
            if (cm == null) {
                return;
            }

            prevConnected = curConnected;
            /*
             * If the internet connection is on, then check to see if it
             * previously wasn't, if it was off then show the Wifi Connection
             * Found toast.
             * 
             * If there is no internet connection, but Wifi on the Tablet is
             * turned on then return the toast that the Wifi Connection is lost.
             * 
             * If there is no internet connection and the Wifi is off on the
             * tablet, then attempt to turn on the Wifi Connection.
             */
            if (cm.getActiveNetworkInfo() != null
                    && cm.getActiveNetworkInfo().isConnected()) {
                curConnected = cm.getActiveNetworkInfo().isConnected();
                if (!prevConnected) {
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
         * .setTitle("Internet Connection Lost");
         * 
         * // 3. Get the AlertDialog from create() AlertDialog dialog =
         * builder.create(); dialog.show();
         */
        /*
         * } }.start();
         */

    }

    /*
     * Wifi Alert does not work with the Check Internet connection Service..
     * Original plans were to ask the user before connecting, but those plans
     * have changed.. Now while the app is opened and in the foreground, we will
     * try keep the wifi on (the app is useless without an internet connection).
     * We will not try keeping the internet connection on if the App is closed
     * or in the background. Currently, WifiAlert is not being used for this
     * reason.
     */

    public void wifiAlert(String title, String msg) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        // set title
        alertDialogBuilder.setTitle(Html.fromHtml("<font color='#000055'>"
                + title + "</font>"));

        // set dialog message
        alertDialogBuilder.setMessage(Html.fromHtml(msg)).setCancelable(false)
                .setPositiveButton(Html.fromHtml("<b>Yes</b>"), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing

                        turnWifiOn();

                        dialog.dismiss();
                    }

                })
                .setNegativeButton(Html.fromHtml("<b>No</b>"), new DialogInterface.OnClickListener()
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

    /*
     * Turn Wifi Connection back on if it is not Enabled
     */

    public void turnWifiOn() {
        turnWifiOn(maxWifiWaitTime);
    }

    public void turnWifiOn(long maxWifiWaitTime) {
        int duration = Toast.LENGTH_SHORT;

        Toast toast = null;
        enablingWifi = false;
        long startTime = System.currentTimeMillis();

        try {
            // Wait until Wifi is Enabled or the Maximum Wifi Wait Time has
            // occurred
            while (!mainWifi.isWifiEnabled()
                    && (System.currentTimeMillis() - startTime <= maxWifiWaitTime)) {
                // While waiting, attempt to turn on the Wifi Connection if it
                // already hasn't been attempted
                if (!enablingWifi) {
                    mainWifi.setWifiEnabled(true);
                    enablingWifi = true;
                }
                // Wait to connect
                Thread.sleep(1000);
            }
            // Check to see if the Wifi Connection turned on, give appropriate
            // message depending on if it was or not.
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

    @Override
    public void OnKeywordChange(KeywordDialog keywordDialog,
            ListView lvKeywords, String keywords) {
        NewInvDialog.tvKeywordsToBlock.setText(keywords);
        getDialogDataFromServer();

    }
    
public static CountDownTimer timer = new CountDownTimer(15 *60 * 1000, 1000) {
		
		@Override
		public void onTick(long millisUntilFinished) {
			// TODO Auto-generated method stub
			//System.out.println(millisUntilFinished/1000);
		}
		
		@Override
		public void onFinish() {
			// TODO Auto-generated method stub
			inactivityTimeout();
		}
		
	};
	
	public static void inactivityTimeout()
	{
		Intent myIntent = new Intent(stContext, LoginActivity.class); 
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        myIntent.putExtra("TIMEOUTFROM", "softkey");
		stContext.startActivity(myIntent);
	}

   @Override
    public void onUserInteraction(){
  		timer.cancel();
  		if(!this.getClass().getSimpleName().equalsIgnoreCase("LoginActivity"))
    		  timer.start();
    	super.onUserInteraction();
    	System.out.println("name:: " + ((Object)this).getClass().getSimpleName());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	// TODO Auto-generated method stub
    	super.onCreate(savedInstanceState);
    	if(stContext == null)
    		stContext = getApplicationContext();
    	if(!((Object)this).getClass().getSimpleName().equalsIgnoreCase("LoginActivity"))
    	timer.start();
    	setCurrentActivity(((Object)this).getClass().getSimpleName());
    }

    public LoginStatus verifyLogin(String user_name, String password) {
        LoginStatus loginStatus = new LoginStatus();
        try {
            // check network connection
            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            String res = null;

            if (networkInfo != null && networkInfo.isConnected()) {
                // fetch data
                try {
                    // Get the URL from the properties
                    // if (loginStatusParam==null)
                    String URL = LoginActivity.properties
                            .get("WEBAPP_BASE_URL").toString();
                    //System.out.println ("/Login?user=" + user_name + "&pwd="+ password + "&defrmint=" + defrmint);
                    AsyncTask<String, String, String> resr1 = new RequestTask()
                            .execute(URL + "/Login?user=" + user_name + "&pwd="
                                    + password + "&defrmint=" + defrmint);
                    try {
                        /*
                         * System.out.println("login url:"+ "/Login?user=" +
                         * user_name + "&pwd=" +
                         * password+"&defrmint="+defrmint);
                         */
                        res = resr1.get().trim().toString();
                        // System.out.println("login Result:"+res);
                        loginStatus.parseJSON(res);
                        if (res == null) {
                            noServerResponse();
                        }
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (NullPointerException e) {
                        // TODO Auto-generated catch block
                        noServerResponse();
                    }
                } catch (Exception e) {

                }
                LoginActivity.nauser = user_name;
            }
        } catch (Exception e) {
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(this,
                    "Problem connecting to Mobile App Server. Please contact STSBAC.("
                            + e.getMessage() + ")", duration);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
        return loginStatus;
    }
      
    private static String activity; 
    
    public void setCurrentActivity(String activity)
    {
    	SenateActivity.activity = activity;
    }
    public static String getCurrentActivity()
    {
    	return activity;
    }

    public int findEmployee(String employeeName, List<Employee> empList) {
        for (int x = 0; x < empList.size(); x++) {
            if (employeeName.equals(empList.get(x).getEmployeeName())) {
                return x;
            }
        }
        return -1;
    }

    protected boolean selectedEmployeeValid(String name, List<Employee> empList) {
        int index = -1;
        if (name.length() > 0) {
            index = findEmployee(name, empList);
        }
        return index > -1 ? true : false;
    }

    protected void displayInvalidEmployeeMessage(String name) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        if (name.length() > 0) {
            Toast toast = Toast.makeText(context,
                    "!!ERROR: No xref# found for employee", duration);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        } else {
            Toast toast = Toast
                    .makeText(
                            context,
                            "!!ERROR: You must first pick an employee name for the signature.",
                            3000);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    protected void displayNoSignatureMessage() {
        Context context = getApplicationContext();
        Toast toast = Toast.makeText(context,
                "!!ERROR: Employee must also sign within the Red box.",
                3000);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
    @Override
    public void onChangePasswordOKButtonClicked(boolean oldPasswordRequired, String oldPassword, String newPassword,
            String confirmPassword) {
        String username = LoginActivity.user_name.getText().toString();
        final String oldPasswordF = oldPassword;
        final String newPasswordF = newPassword;
        final String confirmPasswordF = confirmPassword;
        final boolean oldPasswordRequiredF = oldPasswordRequired;
        DialogInterface.OnClickListener onClickListener = new  DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                  if (which == dialog.BUTTON_POSITIVE) {
                      showChangePasswordDialog(oldPasswordF, newPasswordF, confirmPasswordF, oldPasswordRequiredF);
                  }
                
            }
            
        };

        
        if (username==null||username.trim().length()==0) {
            new Toasty(this, "!!ERROR: Username must first be entered.", Toast.LENGTH_SHORT).showMessage(); 
            return;
        }
        else if (oldPasswordRequired && (oldPassword==null||oldPassword.trim().length()==0) ) {
            new MsgAlert(this).showMessage("!!ERROR: Old Password must be entered", "Old Password must be entered.", onClickListener);                        
            //this.showChangePasswordDialog(oldPassword, newPassword, confirmPassword, oldPasswordRequired);           
            //new Toasty(this, "!!ERROR: Old password must be entered.", Toast.LENGTH_SHORT).showMessage();
            
            return;
        }
        
        if (oldPasswordRequired) {
            LoginStatus loginStatus = new LoginStatus();
            loginStatus = verifyLogin(username, oldPassword);
            //System.out.println("loginStatus:"+loginStatus.getNustatus());
            if (!loginStatus.isUsernamePasswordValid()) {
                if (loginStatus.getNustatus()==loginStatus.INVALID_USERNAME_OR_PASSWORD) {
                    //this.showChangePasswordDialog("", newPassword, confirmPassword, oldPasswordRequired);
                    //new Toasty(this, "!!ERROR: Invalid Old Password.", Toast.LENGTH_SHORT).showMessage();
                    onClickListener = new  DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                              if (which == dialog.BUTTON_POSITIVE) {
                                  showChangePasswordDialog("", newPasswordF, confirmPasswordF, oldPasswordRequiredF, ChangePasswordDialog.OLDPASSWORDFOCUS);
                              }
                            
                        }
                        
                    };                    
                    new MsgAlert(this).showMessage("!!ERROR: Invalid Old Password",  "Invalid Old Password.", onClickListener);                        
                }
                else {
                    new MsgAlert(this).showMessage("!!ERROR: "+loginStatus.getDestatus(),loginStatus.getDestatus(),  onClickListener);                        
                    //new Toasty(this, loginStatus.getDestatus(), Toast.LENGTH_SHORT).showMessage();
                }
                return;
            }
        }
        
        if (newPassword.isEmpty()) {
            
            new MsgAlert(this).showMessage("!!ERROR: New Password must be entered", "New Password must be entered.", onClickListener);            
            //this.showChangePasswordDialog(oldPassword, newPassword, confirmPassword, oldPasswordRequired);
            //new Toasty(this, "!!ERROR: New password must be entered.", Toast.LENGTH_SHORT).showMessage(); 
        }
        else if (newPassword.length()<8) {
            //this.showChangePasswordDialog(oldPassword, newPassword, confirmPassword, oldPasswordRequired);
            //new Toasty(this, "!!ERROR: New password must be at least 8 characters in length.", Toast.LENGTH_SHORT).showMessage();
            onClickListener = new  DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                      if (which == DialogInterface.BUTTON_POSITIVE) {
                          showChangePasswordDialog(oldPasswordF, newPasswordF, confirmPasswordF, oldPasswordRequiredF, ChangePasswordDialog.NEWPASSWORDFOCUS);
                      }
                    
                }
            };            
            new MsgAlert(this).showMessage("!!ERROR: New Password too short", "New password must be at least 8 characters in length.", onClickListener);            
        }        
        else if ((oldPasswordRequired && (newPassword.equalsIgnoreCase(oldPassword)) )||
                (!oldPasswordRequired && (newPassword.trim().equalsIgnoreCase(LoginActivity.password.getText().toString().trim())) )) {
            //this.showChangePasswordDialog(oldPassword, null, null, oldPasswordRequired, ChangePasswordDialog.NEWPASSWORDFOCUS);
            onClickListener = new  DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                      if (which == DialogInterface.BUTTON_POSITIVE) {
                          showChangePasswordDialog(oldPasswordF, null, null, oldPasswordRequiredF, ChangePasswordDialog.NEWPASSWORDFOCUS);
                      }
                }
                
            };            
            new MsgAlert(this).showMessage("!!ERROR: Password cannot be reused", "New Password cannot be the same as the Old Password.", onClickListener);            
            
            //new Toasty(this, "!!ERROR: New password cannot be the same as the old password.", Toast.LENGTH_SHORT).showMessage(); 
        }
        else if (confirmPassword.isEmpty()) {
            //this.showChangePasswordDialog(oldPassword, newPassword, confirmPassword, oldPasswordRequired);
            //new Toasty(this, "!!ERROR: Confirm password must be entered.", Toast.LENGTH_SHORT).showMessage(); 
            new MsgAlert(this).showMessage("!!ERROR: Confirm Password must be entered", "Confirm Password must be entered.", onClickListener);            
        }
        else if (confirmPassword.equals(newPassword)) {
            changePassword(LoginActivity.user_name.getText().toString(), newPassword, oldPasswordRequired, oldPassword);
        }

        else {
            //this.showChangePasswordDialog(oldPassword, newPassword, confirmPassword, oldPasswordRequired, ChangePasswordDialog.CONFIRMPASSWORDFOCUS);
            //new Toasty(this, "!!ERROR: New password and confirm password do not match.", Toast.LENGTH_SHORT).showMessage(); 
            onClickListener = new  DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                      if (which == DialogInterface.BUTTON_POSITIVE) {
                          showChangePasswordDialog(oldPasswordF, newPasswordF, confirmPasswordF, oldPasswordRequiredF,  ChangePasswordDialog.CONFIRMPASSWORDFOCUS);
                      }
                    
                }
                
            };            
            new MsgAlert(this).showMessage("!!ERROR: Passwords do not match", "New Password and Confirm Password do not match.", onClickListener);            
       }
    }

    @Override
    public void onChangePasswordCancelButtonClicked() {
    }    

    private void changePassword(String userName, String newPassword) {
        changePassword(userName, newPassword, false, null);
    }
        
    private void changePassword(String userName, String newPassword, boolean oldPasswordRequired, String oldPassword) {
        ArrayList<NameValuePair> postParams = new ArrayList<NameValuePair>();
        postParams.add( new BasicNameValuePair("user", userName));
        postParams.add( new BasicNameValuePair("newPassword", newPassword));
        AsyncTask<String, String, String> resr1;
        resr1 = new RequestTask(postParams).execute("/ChangePassword");
        String res = null;
        try {
            res = resr1.get().trim().toString();
//            final String userNameF = userName;         // Commented out since it is not needed (for now)
            final String oldPasswordF = oldPassword;
//            final String newPasswordF = newPassword;   // Commented out since it is not needed (for now)
            final boolean oldPasswordRequiredF = oldPasswordRequired;
            MsgAlert msgAlert = new MsgAlert(this);
            
            if (res.trim().equalsIgnoreCase("OK")) {
                new Toasty(this, "Password has been changed.", Toast.LENGTH_SHORT).showMessage();
                if (changePasswordOnLogin && currentLoginActivity!=null) {
                    currentLoginActivity.login(userName, newPassword);
                }
            }
            else if (res.contains("ORA-20003")||res.contains("ORA-20002")||res.contains("ORA-00988")||res.contains("ORA-28003")) {
                
                DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener () {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == Dialog.BUTTON_POSITIVE) {
                            showChangePasswordDialog(oldPasswordF, null, null, oldPasswordRequiredF, ChangePasswordDialog.NEWPASSWORDFOCUS);
                        }
                    }
                };  
                
                msgAlert.showMessage("!!ERROR: SFMS Password Policy Issue", res.trim(), onClickListener);
                //this.showChangePasswordDialog(oldPassword, null, null, oldPasswordRequired, ChangePasswordDialog.NEWPASSWORDFOCUS);
                //new Toasty(this, "!!ERROR: "+res.trim(), Toast.LENGTH_LONG).showMessage();
            }
            else {
                DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener () {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == Dialog.BUTTON_POSITIVE) {
                            showChangePasswordDialog(oldPasswordF, null, null, oldPasswordRequiredF, ChangePasswordDialog.NEWPASSWORDFOCUS);
                        }
                    }
                };  
                
                if (res.contains("ORA-28007")) {
                    msgAlert.showMessage("!!ERROR: Password cannot be reused", res.trim(), onClickListener);
                }
                else if (res.contains("ORA-28002")) {
                    msgAlert.showMessage("!!ERROR: Your password will expire soon", res.trim(), onClickListener);
                }
                else {
                    msgAlert.showMessage("!!ERROR: Change Password", res.trim(), onClickListener);
                }
                
                //new Toasty(this, "!!ERROR: "+res.trim(), Toast.LENGTH_LONG).showMessage(); 
            }
        } catch (InterruptedException e) {
            new Toasty(this, "!!ERROR: "+e.getMessage(), Toast.LENGTH_LONG).showMessage(); 
            e.printStackTrace();
        } catch (ExecutionException e) {
            new Toasty(this, "!!ERROR: "+e.getMessage(), Toast.LENGTH_LONG).showMessage(); 
            e.printStackTrace();
        }
    }    
    public void noServerResponse() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle(Html
                .fromHtml("<b><font color='#000055'>NO SERVER RESPONSE</font></b>"));

        // set dialog message
        alertDialogBuilder
                .setMessage(
                        Html.fromHtml("!!ERROR: There was <font color='RED'><b>NO SERVER RESPONSE</b></font>. <br/> Please contact STS/BAC."))
                .setCancelable(false)
                .setPositiveButton( Html.fromHtml("<b>Ok</b>"), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        Context context = getApplicationContext();

                        CharSequence text = "No action taken due to NO SERVER RESPONSE";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }    
    
}
