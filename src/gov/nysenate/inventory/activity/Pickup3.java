package gov.nysenate.inventory.activity;

import gov.nysenate.inventory.adapter.InvListViewAdapter;
import gov.nysenate.inventory.android.ClearableAutoCompleteTextView;
import gov.nysenate.inventory.android.ClearableEditText;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.android.SignatureView;
import gov.nysenate.inventory.android.R.drawable;
import gov.nysenate.inventory.android.R.id;
import gov.nysenate.inventory.android.R.layout;
import gov.nysenate.inventory.android.R.menu;
import gov.nysenate.inventory.model.Employee;
import gov.nysenate.inventory.model.InvItem;
import gov.nysenate.inventory.model.Transaction;
import gov.nysenate.inventory.util.AppProperties;
import gov.nysenate.inventory.util.Formatter;
import gov.nysenate.inventory.util.TransactionParser;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Pickup3 extends SenateActivity
{
    private ArrayList<InvItem> scannedBarcodeNumbers = new ArrayList<InvItem>();
    private String res = null;
    private SignatureView sign;
    private byte[] imageInByte = {};
    private ArrayList<Employee> employeeHiddenList = new ArrayList<Employee>();
    private ArrayList<String> employeeNameList = new ArrayList<String>();
    private ClearableAutoCompleteTextView employeeNamesView;
    private int nuxrefem = -1;
    private String pickupRequestTaskType = "";
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
    private ClearableEditText commentsEditText;
    private String DECOMMENTS = null;
    private String URL;
    static Button continueBtn;
    static Button cancelBtn;
    static Button btnPickup3ClrSig;

    private TextView pickupCountTV;
    private TextView tvOriginPickup3;
    private TextView tvDestinationPickup3;

    public static ProgressBar progBarPickup3;
    private boolean positiveButtonPressed = false;
    public final int CONTINUEBUTTON_TIMEOUT = 101,
            POSITIVEDIALOG_TIMEOUT = 102, KEEPALIVE_TIMEOUT = 103,
            EMPLOYEELIST_TIMEOUT = 104;
    public String timeoutFrom = "pickup3";
    private Transaction transaction;
    private CheckBox remoteBox;
    private CheckBox paperworkBox;
    private Spinner remoteShipType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pickup3);
        registerBaseActivityReceiver();

        sign = (SignatureView) findViewById(R.id.blsignImageView);
        sign.setMinDimensions(200, 100);
        commentsEditText = (ClearableEditText) findViewById(R.id.pickupCommentsEditText);
        commentsEditText
                .setClearMsg("Do you want to clear the Pickup Comments?");
        commentsEditText.showClearMsg(true);

        ListView ListViewTab1 = (ListView) findViewById(R.id.listView1);

        try {
            transaction = TransactionParser.parseTransaction(getIntent().getStringExtra("pickup"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        scannedBarcodeNumbers = transaction.getPickupItems();
        tvOriginPickup3 = (TextView) findViewById(R.id.tv_origin_pickup3);
        tvDestinationPickup3 = (TextView) findViewById(R.id.tv_destination_pickup3);
        tvOriginPickup3.setText(transaction.getOriginAddressLine1());
        tvDestinationPickup3.setText(transaction.getDestinationAddressLine1());
        pickupCountTV = (TextView) findViewById(R.id.tv_count_pickup3);
        pickupCountTV.setText(Integer.toString(transaction.getPickupItems().size()));
        remoteBox = (CheckBox) findViewById(R.id.remote_checkbox);
        paperworkBox = (CheckBox) findViewById(R.id.paperwork_checkbox);
        remoteShipType = (Spinner) findViewById(R.id.remote_ship_type);

        remoteShipType.setVisibility(Spinner.INVISIBLE);

        Adapter listAdapter1 = new InvListViewAdapter(this,
                R.layout.invlist_item, scannedBarcodeNumbers);
        ListViewTab1.setAdapter((ListAdapter) listAdapter1);

        // Brian code starts
        Pickup2Activity.continueBtn.getBackground().setAlpha(255);

        continueBtn = (Button) findViewById(R.id.btnPickup3Cont);
        continueBtn.getBackground().setAlpha(255);
        cancelBtn = (Button) findViewById(R.id.btnPickup3Back);
        cancelBtn.getBackground().setAlpha(255);
        btnPickup3ClrSig = (Button) findViewById(R.id.btnPickup3ClrSig);
        btnPickup3ClrSig.getBackground().setAlpha(255);
        employeeNamesView = (ClearableAutoCompleteTextView) findViewById(R.id.naemployee);
        employeeNamesView
                .setClearMsg("Do you want to clear the name of the signer?");
        employeeNamesView.showClearMsg(true);
        employeeNamesView
                .setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(
                                employeeNamesView.getWindowToken(), 0);
                    }
                });

        // Setup ProgressBar
        progBarPickup3 = (ProgressBar) findViewById(R.id.progBarPickup3);

        // TODO: Is this what takes so long in loading Pickup3?
        getEmployeeList();

        // code for textwatcher
        // for origin location code
        // loc_code = (EditText) findViewById(R.id.editText1);
        // loc_code.addTextChangedListener(filterTextWatcher);

        // Commented out by Brian Heitner, found by Kevin Caseiras
        // I believe this code is left over code that doesn't ever fire..
        // Leaving commented code in just in case I am wrong.
        // /

        /*
         * naemployeeView.setOnItemSelectedListener(new OnItemSelectedListener()
         * {
         * 
         * @Override public void onItemSelected(AdapterView<?> arg0, View arg1,
         * int arg2, long arg3) { String employeeSelected =
         * naemployeeView.getText().toString(); int employeeFoundAt =
         * findEmployee(employeeSelected);
         * System.out.println("EMPLOYEE SELECTED:" + employeeSelected +
         * " FOUND AT:" + employeeFoundAt); if (employeeSelected == null ||
         * employeeSelected.length() == 0) { nuxrefem = -1; Context context =
         * getApplicationContext(); int duration = Toast.LENGTH_SHORT;
         * 
         * Toast toast = Toast.makeText(context, "No Employee entered.", 3000);
         * toast.setGravity(Gravity.CENTER, 0, 0); toast.show(); } else if
         * (employeeFoundAt == -1) { nuxrefem = -1; Context context =
         * getApplicationContext(); int duration = Toast.LENGTH_SHORT;
         * 
         * Toast toast = Toast.makeText(context, "Employee not found.", 3000);
         * toast.setGravity(Gravity.CENTER, 0, 0); toast.show(); } else {
         * nuxrefem = employeeHiddenList.get(employeeFoundAt)
         * .getEmployeeXref(); Context context = getApplicationContext(); int
         * duration = Toast.LENGTH_SHORT; Toast toast = Toast.makeText(context,
         * "Employee xref#:" + nuxrefem + " Name:" +
         * employeeHiddenList.get(employeeFoundAt) .getEmployeeName(), 3000);
         * toast.setGravity(Gravity.CENTER, 0, 0); toast.show(); } }
         * 
         * @Override public void onNothingSelected(AdapterView<?> arg0) {
         * nuxrefem = -1; } });
         */
    }

    @Override
    protected void onResume() {
        super.onResume();
        positiveButtonPressed = false;
        continueBtn = (Button) findViewById(R.id.btnPickup3Cont);
        continueBtn.getBackground().setAlpha(255);
        cancelBtn = (Button) findViewById(R.id.btnPickup3Back);
        cancelBtn.getBackground().setAlpha(255);
        btnPickup3ClrSig = (Button) findViewById(R.id.btnPickup3ClrSig);
        btnPickup3ClrSig.getBackground().setAlpha(255);
        if (progBarPickup3 == null) {
            progBarPickup3 = (ProgressBar) this
                    .findViewById(R.id.progBarPickup3);
        }
        progBarPickup3.getBackground().setAlpha(255);
    }

    public int findEmployee(String employeeName) {
        for (int x = 0; x < employeeHiddenList.size(); x++) {
            if (employeeName
                    .equals(employeeHiddenList.get(x).getEmployeeName())) {
                return x;
            }
        }
        return -1;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_pickup3, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
        case android.R.id.home:
            Toast toast = Toast.makeText(getApplicationContext(), "Going Back",
                    Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            backButton(this.getCurrentFocus());
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Fire an intent to start the speech recognition activity.
     */
    public void startCommentsSpeech(View view) {
        if (view.getId() == R.id.pickupCommentsSpeechButton) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                    "Pickup Comments Speech");
            startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
        }
    }

    /**
     * Handle the results from the recognition activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("onActivityResult", "requestCode:" + requestCode + " resultCode:"
                + resultCode);

        switch (requestCode) {
        case EMPLOYEELIST_TIMEOUT:
            if (resultCode == RESULT_OK) {
                getEmployeeList();
            }
            break;
        case POSITIVEDIALOG_TIMEOUT:
            new Timer().schedule(new TimerTask()
            {
                @Override
                public void run() {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(
                            employeeNamesView.getWindowToken(), 0);
                }
            }, 50);
            break;
        case KEEPALIVE_TIMEOUT:
            // Log.i("onActivityResult", "KEEPALIVE_TIMEOUT");
            new Timer().schedule(new TimerTask()
            {
                @Override
                public void run() {
                    // Log.i("onActivityResult",
                    // "KEEPALIVE_TIMEOUT Hide Keyboard");
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(
                            employeeNamesView.getWindowToken(), 0);
                }
            }, 50);
            break;

        case VOICE_RECOGNITION_REQUEST_CODE:
            if (resultCode == RESULT_OK) {
                // Fill the list view with the strings the recognizer thought it
                // could have heard
                ArrayList<String> matches = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                commentsEditText.setText(matches.get(0));
            }
            break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void continueButton(View view) {
        if (checkServerResponse(true) == OK) {

            String employeePicked = employeeNamesView.getEditableText()
                    .toString();
            if (employeePicked.trim().length() > 0) {
                int foundEmployee = this.findEmployee(employeePicked);

                if (foundEmployee < 0) {
                    nuxrefem = -1;
                } else {
                    nuxrefem = this.employeeHiddenList.get(foundEmployee)
                            .getEmployeeXref();
                }
            } else {
                nuxrefem = -1;
            }

            if (nuxrefem < 0) {
                Context context = getApplicationContext();
                int duration = Toast.LENGTH_SHORT;
                if (employeeNamesView.getEditableText().toString().trim()
                        .length() > 0) {
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

                return;

            }

            if (!sign.isSigned()) {
                Context context = getApplicationContext();
                Toast toast = Toast.makeText(context,
                        "!!ERROR: Employee must also sign within the Red box.",
                        3000);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return;
            }

            Log.i("continueButton",
                    "Check for Session by using KeepSessionAlive");

            if (!keepAlive()) {
                return;
            }

            AlertDialog.Builder confirmDialog = new AlertDialog.Builder(this);
            confirmDialog
                    .setTitle(Html
                            .fromHtml("<font color='#000055'>Pickup Confirmation</font>"));
            confirmDialog.setMessage("Are you sure you want to pickup these "
                    + scannedBarcodeNumbers.size() + " items?");
            confirmDialog.setPositiveButton(Html.fromHtml("<b>Yes</b>"),
                    new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            /*
                             * Prevent Multiple clicks on button, which will
                             * cause issues witn the database inserting multiple
                             * nuxrpds for the same pickup.
                             */

                            if (positiveButtonPressed) {
                                /*
                                 * Context context = getApplicationContext();
                                 * int duration = Toast.LENGTH_SHORT;
                                 * 
                                 * Toast toast = Toast.makeText(context,
                                 * "Button was already been pressed.",
                                 * Toast.LENGTH_SHORT);
                                 * toast.setGravity(Gravity.CENTER, 0, 0);
                                 * toast.show();
                                 */

                            } else {
                                positiveButtonPressed = true;
                                positiveDialog();
                            }
                        }
                    });

            confirmDialog.setNegativeButton(Html.fromHtml("<b>No</b>"),
                    new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Continue in same activity.
                        }
                    });

            AlertDialog dialog = confirmDialog.create();
            dialog.show();

        }
    }


    public void backButton(View view) {
        if (checkServerResponse(true) == OK) {
            super.onBackPressed();
        }
        /*
         * float alpha = 0.45f; AlphaAnimation alphaUp = new
         * AlphaAnimation(alpha, alpha); alphaUp.setFillAfter(true);
         * btnPickup3Back.startAnimation(alphaUp); Intent intent = new
         * Intent(this, Pickup2Activity.class); startActivity(intent);
         * overridePendingTransition(R.anim.in_left, R.anim.out_right);
         */
    }

    public void clearSignatureButton(View view) {
        btnPickup3ClrSig.getBackground().setAlpha(45);
        Bitmap clearedSignature = BitmapFactory.decodeResource(getResources(),
                R.drawable.simplethinborder);
        if (clearedSignature == null) {
            Log.i("ClearSig", "Signature drawable was NULL");
        } else {
            Log.i("ClearSig", "Signature size:" + clearedSignature.getWidth()
                    + " x " + clearedSignature.getHeight());
        }
        sign.clearSignature();
        btnPickup3ClrSig.getBackground().setAlpha(255);
    }

    public ArrayList<String> getJSONArrayList(ArrayList<InvItem> invList) {
        ArrayList<String> returnArray = new ArrayList<String>();
        if (invList != null) {
            for (int x = 0; x < invList.size(); x++) {
                returnArray.add(invList.get(x).toJSON());
            }
        }

        return returnArray;
    }

    public ArrayList<InvItem> getInvItemArrayList(ArrayList<String> invList) {
        ArrayList<InvItem> returnArray = new ArrayList<InvItem>();
        if (invList != null) {
            for (int x = 0; x < invList.size(); x++) {
                String curInvJson = invList.get(x);
                InvItem currentInvItem = new InvItem();
                currentInvItem.parseJSON(curInvJson);
                returnArray.add(currentInvItem);
            }
        }

        return returnArray;
    }

    public Bitmap setBackgroundColor(Bitmap image, int backgroundColor) {
        Bitmap newBitmap = Bitmap.createBitmap(image.getWidth(),
                image.getHeight(), image.getConfig());
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(backgroundColor);
        canvas.drawBitmap(image, 0, 0, null);
        return newBitmap;
    }

    class pickupRequestTask extends AsyncTask<String, String, String>
    {

        @Override
        protected String doInBackground(String... uri) {
            // First Upload the Signature and get the nuxsign from the Server

            if (pickupRequestTaskType.equalsIgnoreCase("KeepAlive")) {
                HttpClient httpclient = LoginActivity.httpClient;
                HttpResponse response;
                String responseString = null;
                try {
                    response = httpclient.execute(new HttpGet(uri[0]));
                    StatusLine statusLine = response.getStatusLine();
                    if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        response.getEntity().writeTo(out);
                        out.close();
                        responseString = out.toString();
                    } else {
                        // Closes the connection.
                        response.getEntity().getContent().close();
                        throw new IOException(statusLine.getReasonPhrase());
                    }
                } catch (ConnectTimeoutException e) {
                    return "***WARNING: Server Connection timeout";
                    // Toast.makeText(getApplicationContext(),
                    // "Server Connection timeout", Toast.LENGTH_LONG).show();
                    // Log.e("CONN TIMEOUT", e.toString());
                } catch (SocketTimeoutException e) {
                    return "***WARNING: Server Socket timeout";
                    // Toast.makeText(getApplicationContext(), "Server timeout",
                    // Toast.LENGTH_LONG).show();
                    // Log.e("SOCK TIMEOUT", e.toString());
                } catch (ClientProtocolException e) {
                    // TODO Handle problems..
                } catch (IOException e) {
                    // TODO Handle problems..
                }
                res = responseString;
                return responseString;

            } else {
                System.out.println("!!ERROR: Invalid requestTypeTask:"
                        + pickupRequestTaskType);
                return "!!ERROR: Invalid requestTypeTask:"
                        + pickupRequestTaskType;
            }
            
        }
    }

    private void positiveDialog() {
        continueBtn.getBackground().setAlpha(45);
        // new VersummaryActivity().sendJsonString(scannedBarcodeNumbers);
        // String jsonString = null;
        // JSONArray jsArray = new JSONArray(scannedBarcodeNumbers);

        // String barcodeNum = "";

        // for (int i = 0; i < scannedBarcodeNumbers.size(); i++) {
        // barcodeNum += scannedBarcodeNumbers.get(i).getNusenate() + ",";
        // }

        // Create a JSON string from the arraylist
        /*
         * WORK ON IT LATER (SENDING THE STRING AS JSON) JSONObject jo=new
         * JSONObject();// =jsArray.toJSONObject("number"); try {
         * 
         * //jo.putOpt("barcodes",scannedBarcodeNumbers.toString());
         * jsonString=jsArray.toString(); } catch (Exception e) { // TODO
         * Auto-generated catch block e.printStackTrace(); }
         */

        // call the servlet image upload and return the nuxrsign

        String NAPICKUPBY = LoginActivity.nauser;
        String NARELEASEBY = null;
        DECOMMENTS = null;

        try {
            NARELEASEBY = URLEncoder.encode(this.employeeNamesView.getText().toString(), "UTF-8");
            DECOMMENTS = URLEncoder.encode(this.commentsEditText.getText().toString(), "UTF-8");

        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        String URL = LoginActivity.properties.get("WEBAPP_BASE_URL")
                .toString();

        new ProcessPickupTask().execute(
                URL + "/ImgUpload?nauser=" + LoginActivity.nauser
                + "&nuxrefem=" + nuxrefem,
                URL
                + "/Pickup?originLocation="
                + transaction.getOriginCdLoc()
                + "&destinationLocation="
                + transaction.getDestinationCdLoc()
                + Formatter.generateGetArray("barcode[]", scannedBarcodeNumbers)
                + "&NAPICKUPBY=" + NAPICKUPBY
                + "&NARELEASEBY=" + NARELEASEBY
                + "&cdloctypeto="
                + transaction.getDestinationCdLocType()
                + "&cdloctypefrm="
                + transaction.getOriginCdLocType());
    }


    public void returnToMoveMenu() {
        Intent intent = new Intent(this, Move.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void startTimeout(int timeoutType) {
        Intent intentTimeout = new Intent(this, LoginActivity.class);
        intentTimeout.putExtra("TIMEOUTFROM", timeoutFrom);
        startActivityForResult(intentTimeout, timeoutType);
    }

    public boolean keepAlive() {
        // check network connection
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // fetch data

            AsyncTask<String, String, String> resr1;
            try {
                // Get the URL from the properties
                String URL = LoginActivity.properties.get("WEBAPP_BASE_URL")
                        .toString();
                this.pickupRequestTaskType = "KeepAlive";
                resr1 = new pickupRequestTask().execute(URL
                        + "/KeepSessionAlive");

                try {
                    res = null;
                    res = resr1.get().trim().toString();
                    if (res == null) {
                        noServerResponseMsg();
                        return false;
                    } else if (res.indexOf("Session timed out") > -1) {
                        startTimeout(this.KEEPALIVE_TIMEOUT);
                        return false;
                    }

                } catch (NullPointerException e) {
                    noServerResponseMsg();
                    return false;
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        } else {
            // display error
        }
        return true;

    }

    public void getEmployeeList() {
            new GetEmployeeListTask().execute();
    }

    private class ProcessPickupTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            progBarPickup3.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected String doInBackground(String... uri) {
            // Scale the Image

            String NUXRRELSIGN = "";

            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            Bitmap bitmap = sign.getImage();
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 200,
                    40, true);

            for (int x = 0; x < scaledBitmap.getWidth(); x++) {
                for (int y = 0; y < scaledBitmap.getHeight(); y++) {
                    String strColor = String.format("#%06X",
                            0xFFFFFF & scaledBitmap.getPixel(x, y));
                    if (strColor.equals("#000000")
                            || scaledBitmap.getPixel(x, y) == Color.TRANSPARENT) {
                        // System.out.println("********"+x+" x "+y+" SETTING COLOR TO WHITE");
                        scaledBitmap.setPixel(x, y, Color.WHITE);
                    }
                }
            }
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bs);

            scaledBitmap = setBackgroundColor(scaledBitmap, Color.WHITE);
            imageInByte = bs.toByteArray();
            String responseString = "";
            try {
                // Post the Image to the Web Server

                StringBuilder urls = new StringBuilder();
                urls.append(uri[0].trim());
                if (uri[0].indexOf("?") > -1) {
                    if (!uri[0].trim().endsWith("?")) {
                        urls.append("&");
                    }
                } else {
                    urls.append("?");
                }
                urls.append("userFallback=");
                urls.append(LoginActivity.nauser);

                HttpClient httpClient = LoginActivity.httpClient;

                if (httpClient == null) {
                    Log.i(pickupRequestTask.class.getName(),
                            "MainActivity.httpClient was null so it is being reset");
                    LoginActivity.httpClient = new DefaultHttpClient();
                    httpClient = LoginActivity.httpClient;
                }

                HttpContext localContext = new BasicHttpContext();
                MultipartEntity entity = new MultipartEntity(
                        HttpMultipartMode.BROWSER_COMPATIBLE);

                HttpPost httpPost = new HttpPost(urls.toString());
                entity.addPart("Signature", new ByteArrayBody(imageInByte,
                        "temp.jpg"));
                httpPost.setEntity(entity);

                /*
                 * HttpURLConnection conn = (HttpURLConnection) url
                 * .openConnection(); // Set connection parameters.
                 * conn.setDoInput(true); conn.setDoOutput(true);
                 * conn.setUseCaches(false);
                 * 
                 * // Set content type to PNG
                 * conn.setRequestProperty("Content-Type", "image/jpg");
                 * OutputStream outputStream = conn.getOutputStream();
                 * OutputStream out = outputStream; // Write out the bytes
                 * of the content string to the stream.
                 * out.write(imageInByte); out.flush(); out.close(); // Read
                 * response from the input stream. BufferedReader in = new
                 * BufferedReader( new
                 * InputStreamReader(conn.getInputStream())); String temp;
                 * while ((temp = in.readLine()) != null) { responseString
                 * += temp + "\n"; } temp = null; in.close();
                 */

                // Get Server Response to the posted Image

                HttpResponse response = httpClient.execute(httpPost,
                        localContext);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(response.getEntity()
                                .getContent(), "UTF-8"));
                responseString = reader.readLine();
                System.out.println("***Image Server response:\n'"
                        + responseString + "'");
                int nuxrsignLoc = responseString.indexOf("NUXRSIGN:");
                if (nuxrsignLoc > -1) {
                    NUXRRELSIGN = responseString.substring(nuxrsignLoc + 9)
                            .replaceAll("\r", "").replaceAll("\n", "");
                } else {
                    NUXRRELSIGN = responseString.replaceAll("\r", "")
                            .replaceAll("\n", "");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            // Then post the rest of the information along with the NUXRSIGN

            HttpClient httpclient = LoginActivity.httpClient;
            HttpResponse response;
            responseString = null;
            try {

                String pickupURL = uri[1] + "&NUXRRELSIGN=" + NUXRRELSIGN
                        + "&DECOMMENTS=" + DECOMMENTS + "&userFallback="
                        + LoginActivity.nauser;
                System.out.println("pickupURL:" + pickupURL);
                response = httpclient.execute(new HttpGet(pickupURL));
                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    responseString = out.toString();
                } else {
                    // Closes the connection.
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            } catch (ClientProtocolException e) {
                // TODO Handle problems..
            } catch (ConnectTimeoutException e) {
                return "***WARNING: Server Connection timeout";
                // Toast.makeText(getApplicationContext(),
                // "Server Connection timeout", Toast.LENGTH_LONG).show();
                // Log.e("CONN TIMEOUT", e.toString());
            } catch (SocketTimeoutException e) {
                return "***WARNING: Server Socket timeout";
                // Toast.makeText(getApplicationContext(), "Server timeout",
                // Toast.LENGTH_LONG).show();
                // Log.e("SOCK TIMEOUT", e.toString());
            } catch (IOException e) {
                // TODO Handle problems..
            }
            res = responseString;
            return responseString;
        }

        @Override
        protected void onPostExecute(String response) {
            progBarPickup3.setVisibility(ProgressBar.INVISIBLE);

            if (res == null) {
                noServerResponseMsg();
                return;
            } else if (res.indexOf("Session timed out") > -1) {
                startTimeout(POSITIVEDIALOG_TIMEOUT);
                return;
            } else if (res.startsWith("***WARNING:")
                    || res.startsWith("!!ERROR:")) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Pickup3.this);

                // set title
                alertDialogBuilder.setTitle(Html
                        .fromHtml("<font color='#000055'>" + res.trim()
                                + "</font>"));

                // set dialog message
                alertDialogBuilder
                .setMessage(
                        Html.fromHtml(res.trim()
                                + "<br/> Continue (Y/N)?"))
                                .setCancelable(false)
                                .setPositiveButton(Html.fromHtml("<b>Yes</b>"),
                                        new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(
                                            DialogInterface dialog,
                                            int id) {
                                        // if this button is clicked,
                                        // just close
                                        // the dialog box and do nothing
                                        returnToMoveMenu();
                                        dialog.dismiss();
                                    }
                                })
                                .setPositiveButton(Html.fromHtml("<b>No</b>"),
                                        new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(
                                            DialogInterface dialog,
                                            int id) {
                                        // if this button is clicked,
                                        // just close
                                        // the dialog box and do nothing
                                        dialog.dismiss();
                                    }
                                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
            }

            // Display Toster
            Context context = getApplicationContext();
            CharSequence text = res.trim();
            if (res.length() == 0) {
                noServerResponseMsg();
                return;
            }

            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();

            // ===================ends
            // Intent intent = new Intent(this, MenuActivity.class);
            returnToMoveMenu();
        }
    }

    private class GetEmployeeListTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            progBarPickup3.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... arg) {
            String url = AppProperties.getBaseUrl(Pickup3.this);
            url += "EmployeeList?";
            url += "&userFallback=" + LoginActivity.nauser;

            HttpClient httpclient = LoginActivity.httpClient;
            HttpResponse response = null;
            String responseString = null;
            try {
                response = httpclient.execute(new HttpGet(url));
                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    responseString = out.toString();
                } else {
                    // Closes the connection.
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            } catch (ClientProtocolException e) {
                // TODO Handle problems..
            } catch (ConnectTimeoutException e) {
                return "***WARNING: Server Connection timeout";
            } catch (SocketTimeoutException e) {
                return "***WARNING: Server Socket timeout";
            } catch (IOException e) {
                // TODO Handle problems..
            }

            if (responseString == null || responseString.indexOf("Session timed out") != -1) {
                return responseString;
            }

            JSONArray jsonArray;
            try {
                jsonArray = new JSONArray(responseString);

                for (int x = 0; x < jsonArray.length(); x++) {
                    JSONObject jo = new JSONObject();
                    jo = jsonArray.getJSONObject(x);
                    Employee currentEmployee = new Employee();
                    currentEmployee.setEmployeeData(jo.getInt("nuxrefem"),
                            jo.getString("naemployee"));
                    employeeHiddenList.add(currentEmployee);
                    employeeNameList.add(jo.getString("naemployee"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Collections.sort(employeeNameList);

            return responseString;
        }

        @Override
        protected void onPostExecute(String response) {
            progBarPickup3.setVisibility(ProgressBar.INVISIBLE);

            if (response == null) {
                noServerResponseMsg();
                return;
            } else if (response.indexOf("Session timed out") > -1) {
                startTimeout(EMPLOYEELIST_TIMEOUT);
                return;
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(Pickup3.this,
                    android.R.layout.simple_dropdown_item_1line,
                    employeeNameList);

            employeeNamesView.setThreshold(1);
            employeeNamesView.setAdapter(adapter);
        }
    }

    public void remoteBoxClicked(View view) {
        // If checked
        if (((CheckBox) view).isChecked()) {
            remoteShipType.setVisibility(Spinner.VISIBLE);
        } else {
            remoteShipType.setVisibility(Spinner.INVISIBLE);
        }
    }

    public void paperworkRequestedClick(View view) {

    }
}
