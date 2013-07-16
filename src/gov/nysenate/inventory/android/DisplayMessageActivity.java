package gov.nysenate.inventory.android;

import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

public class DisplayMessageActivity extends Activity
{

    String res = null;

    @SuppressWarnings("static-access")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);

        String status = null;

        // Get the message from the intent
        Intent intent = getIntent();
        String user_name = intent.getStringExtra(LoginActivity.u_name_intent);
        String password = intent.getStringExtra(LoginActivity.pwd_intent);
        String res = null;

        try {
            // check network connection
            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                // fetch data
                status = "yes";
                try {
                    // Get the URL from the properties
                    String URL = LoginActivity.properties.get("WEBAPP_BASE_URL")
                            .toString();
                    Log.i("Login test", URL + "/Login?user=" + user_name
                            + "&pwd=" + password);
                    AsyncTask<String, String, String> resr1 = new RequestTask()
                            .execute(URL + "/Login?user=" + user_name + "&pwd="
                                    + password);
                    try {
                        res = null;
                        res = resr1.get().trim().toString();
                        if (res==null) {
                            noServerResponse();
                            Intent intent2 = new Intent(this, LoginActivity.class);
                            intent2.setFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent2);
                            overridePendingTransition(R.anim.slide_in_left,
                                    R.anim.slide_out_left);
                            finish();
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
                        Intent intent2 = new Intent(this, LoginActivity.class);
                        intent2.setFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent2);
                        overridePendingTransition(R.anim.slide_in_left,
                                R.anim.slide_out_left);
                        finish();
                        }
                } catch (Exception e) {

                    Intent intent2 = new Intent(this, LoginActivity.class);
                    intent2.setFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent2);
                    overridePendingTransition(R.anim.slide_in_left,
                            R.anim.slide_out_left);
                    finish();
                }
                status = "yes1";
                if (user_name == null || user_name.trim().length() == 0) { // TESTING
                                                                           // PURPOSE
                                                                           // ONLY!!!
                    user_name = "height";
                }
                LoginActivity.nauser = user_name;
                System.out.println("NAUSER NOW SET TO " + user_name);
            } else {
                // display error
                status = "no";
                LoginActivity.nauser = null;
                System.out.println("NAUSER NULL!!");
            }

            // Create the text view
            TextView textView = new TextView(this);
            textView.setTextSize(40);

            // calling the menu activity after validation
            System.out.println("RES:" + res);
            if (res.equals("VALID")) {
                Intent intent2 = new Intent(this, MenuActivity.class);
                startActivity(intent2);
                overridePendingTransition(R.anim.slide_in_left,
                        R.anim.slide_out_left);
            } else if (res.trim().startsWith("!!ERROR :")) {
                Intent intent2 = new Intent(this, LoginActivity.class);
                intent2.setFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent2);
                overridePendingTransition(R.anim.slide_in_left,
                        R.anim.slide_out_left);
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(this, res.trim(), duration);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();

            } else {
                Intent intent2 = new Intent(this, LoginActivity.class);
                intent2.setFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent2);
                overridePendingTransition(R.anim.slide_in_left,
                        R.anim.slide_out_left);
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(this,
                        "!!ERROR: Invalid Username and/or Password.", duration);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();

                finish();
            }
        } catch (Exception e) {
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(this,
                    "Problem connecting to Mobile App Server. Please contact STSBAC.("
                            + e.getMessage() + ")", duration);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();

            Intent intent2 = new Intent(this, LoginActivity.class);
            intent2.setFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent2);
            finish();

        }
    }

    public void noServerResponse() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle("NO SERVER RESPONSE");

        // set dialog message
        alertDialogBuilder
                .setMessage(
                        Html.fromHtml("!!ERROR: There was <font color='RED'><b>NO SERVER RESPONSE</b></font>. <br/> Please contact STS/BAC."))
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener()
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


                        dialog.dismiss();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }             
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.in_left, R.anim.out_right);
    }

}
