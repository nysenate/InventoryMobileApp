package gov.nysenate.inventory.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;
import gov.nysenate.inventory.adapter.InvListViewAdapter;
import gov.nysenate.inventory.adapter.NothingSelectedSpinnerAdapter;
import gov.nysenate.inventory.android.ClearableAutoCompleteTextView;
import gov.nysenate.inventory.android.ClearableEditText;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.android.SignatureView;
import gov.nysenate.inventory.model.Employee;
import gov.nysenate.inventory.model.InvItem;
import gov.nysenate.inventory.model.Transaction;
import gov.nysenate.inventory.util.AppProperties;
import gov.nysenate.inventory.util.EmployeeParser;
import gov.nysenate.inventory.util.Toasty;
import gov.nysenate.inventory.util.TransactionParser;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.util.*;

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
    private String URL;
    static Button continueBtn;
    static Button cancelBtn;
    static Button btnPickup3ClrSig;
    private ListView ListViewTab1;

    private TextView pickupCountTV;
    private TextView tvOriginPickup3;
    private TextView tvDestinationPickup3;

    public static ProgressBar progBarPickup3;
    public final int CONTINUEBUTTON_TIMEOUT = 101,
            POSITIVEDIALOG_TIMEOUT = 102, KEEPALIVE_TIMEOUT = 103,
            EMPLOYEELIST_TIMEOUT = 104;
    public String timeoutFrom = "pickup3";
    private Transaction pickup;
    private CheckBox remoteBox;
    private Spinner remoteShipType;
    ArrayList<NameValuePair> postParams = new ArrayList<NameValuePair>();

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

        ListViewTab1 = (ListView) findViewById(R.id.listView1);

        pickup = TransactionParser.parseTransaction(getIntent().getStringExtra(
                "pickup"));
        pickup.setNapickupby(LoginActivity.nauser);

        scannedBarcodeNumbers = pickup.getPickupItems();
        tvOriginPickup3 = (TextView) findViewById(R.id.tv_origin_pickup3);
        tvDestinationPickup3 = (TextView) findViewById(R.id.tv_destination_pickup3);
        tvOriginPickup3.setText(pickup.getOriginAddressLine1());
        tvDestinationPickup3.setText(pickup.getDestinationAddressLine1());
        pickupCountTV = (TextView) findViewById(R.id.tv_count_pickup3);
        pickupCountTV.setText(Integer.toString(pickup.getPickupItems().size()));
        remoteBox = (CheckBox) findViewById(R.id.remote_checkbox);
        remoteShipType = (Spinner) findViewById(R.id.remote_ship_type);
        remoteShipType.setVisibility(View.INVISIBLE);

        // Display a "hint" in the spinner.
        ArrayAdapter<CharSequence> spinAdapter = ArrayAdapter
                .createFromResource(this, R.array.remote_ship_types,
                        android.R.layout.simple_spinner_item);
        spinAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        remoteShipType.setAdapter(new NothingSelectedSpinnerAdapter(
                spinAdapter, R.layout.spinner_nothing_selected, this));
        remoteShipType.setOnItemSelectedListener(new OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {
                if (remoteShipType.getSelectedItem() != null) {
                    pickup.setShipType(remoteShipType.getSelectedItem()
                            .toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

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

        progBarPickup3 = (ProgressBar) findViewById(R.id.progBarPickup3);

        getEmployeeList();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        if (checkServerResponse(true) != OK) {
            return;
        }

        if (isRemoteOptionVisible()) {
            if (remoteShipType.getSelectedItem() == null) {
                Toasty.displayCenteredMessage(this,
                        "You must pick a remote shipping option.",
                        Toast.LENGTH_SHORT);
                return;
            }
        }

        String emp = "";
        if (!pickup.isRemote() || pickup.isRemoteDelivery()) {
            emp = employeeNamesView.getEditableText().toString();
            if (!selectedEmployeeValid(emp, employeeHiddenList)) {
                displayInvalidEmployeeMessage(employeeNamesView
                        .getEditableText().toString().trim());
                return;
            }
            nuxrefem = employeeHiddenList.get(
                    findEmployee(emp, employeeHiddenList)).getNuxrefem();

            if (!sign.isSigned()) {
                displayNoSignatureMessage();
                return;
            }
        }

        pickup.setPickupComments(commentsEditText.getText().toString());
        pickup.setNareleaseby(emp);
        displayPickupConfirmationDialog();
    }

    private void displayPickupConfirmationDialog() {
        AlertDialog.Builder confirmDialog = new AlertDialog.Builder(this)
                .setTitle(
                        Html.fromHtml("<font color='#000055'>Pickup Confirmation</font>"))
                .setMessage(
                        "Are you sure you want to pickup these "
                                + scannedBarcodeNumbers.size() + " items?")
                .setPositiveButton(Html.fromHtml("<b>Yes</b>"),
                        new DialogInterface.OnClickListener()
                        {
                            private boolean positiveButtonPressed = false;

                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                if (!positiveButtonPressed) {
                                    positiveButtonPressed = true;
                                    positiveDialog();
                                }
                            }
                        }).setNegativeButton(Html.fromHtml("<b>No</b>"), null);

        AlertDialog dialog = confirmDialog.create();
        dialog.show();
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
                    HttpPost httpPost = new HttpPost(uri[0]);

                    response = httpclient.execute(new HttpPost(uri[0]));
                    // response = httpclient.execute(new HttpGet(uri[0]));
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
        if (checkServerResponse(true) != OK) {
            return;
        }
        continueBtn.getBackground().setAlpha(45);
        String URL = LoginActivity.properties.get("WEBAPP_BASE_URL").toString();
        if (!URL.endsWith("/")) {
            URL += "/";
        }

        new ProcessPickupTask().execute(URL + "ImgUpload?nauser="
                + LoginActivity.nauser + "&nuxrefem=" + nuxrefem, URL
                + "/Pickup");
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

    public void getEmployeeList() {
        new GetEmployeeListTask().execute();
    }

    private class ProcessPickupTask extends AsyncTask<String, Void, String>
    {

        @Override
        protected void onPreExecute() {
            progBarPickup3.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... uri) {
            String NUXRRELSIGN = "";
            String responseString = "";

            if (!pickup.isRemotePickup()) {
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
                    HttpResponse response = httpClient.execute(httpPost,
                            localContext);
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(response.getEntity()
                                    .getContent(), "UTF-8"));
                    responseString = reader.readLine();
                    /*
                     * System.out.println("***Image Server response:\n'" +
                     * responseString + "'");
                     */
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
                pickup.setNuxrrelsign(NUXRRELSIGN);
            }

            pickup.setPickupDate(new Date());

            HttpClient httpclient = LoginActivity.httpClient;
            HttpResponse response;
            responseString = null;
            try {
                String pickupURL = uri[1];
                postParams = new ArrayList<NameValuePair>();
                postParams
                        .add(new BasicNameValuePair("pickup", pickup.toJson()));
                postParams.add(new BasicNameValuePair("userFallback",
                        LoginActivity.nauser));

                HttpPost httpPost = new HttpPost(pickupURL);
                httpPost.setEntity(new UrlEncodedFormEntity(postParams));

                // System.out.println("pickupURL:" + pickupURL);
                // response = httpclient.execute(new HttpGet(pickupURL));
                response = httpclient.execute(httpPost);
                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    responseString = out.toString();
                    /*
                     * System.out.println("***Pickup Server response:\n'" +
                     * responseString + "'");
                     */

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
            progBarPickup3.setVisibility(View.INVISIBLE);

            if (res == null) {
                noServerResponse();
                return;
            } else if (res.indexOf("Session timed out") > -1) {
                startTimeout(POSITIVEDIALOG_TIMEOUT);
                return;
            } else if (res.startsWith("***WARNING:")
                    || res.startsWith("!!ERROR:")) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        Pickup3.this);

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
                                    public void onClick(DialogInterface dialog,
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
                                    public void onClick(DialogInterface dialog,
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
                noServerResponse();
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

    private class GetEmployeeListTask extends AsyncTask<Void, Void, String>
    {

        @Override
        protected void onPreExecute() {
            progBarPickup3.setVisibility(View.VISIBLE);
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

            if (responseString == null
                    || responseString.indexOf("Session timed out") != -1) {
                return responseString;
            }

            List<Employee> currentEmployees = EmployeeParser
                    .parseMultipleEmployees(responseString);
            employeeHiddenList.addAll(currentEmployees);
            for (Employee emp : currentEmployees) {
                employeeNameList.add(emp.getFullName());
            }

            Collections.sort(employeeNameList);

            return responseString;
        }

        @Override
        protected void onPostExecute(String response) {
            progBarPickup3.setVisibility(View.INVISIBLE);

            if (response == null) {
                noServerResponse();
                return;
            } else if (response.indexOf("Session timed out") > -1) {
                startTimeout(EMPLOYEELIST_TIMEOUT);
                return;
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    Pickup3.this, android.R.layout.simple_dropdown_item_1line,
                    employeeNameList);

            employeeNamesView.setThreshold(1);
            employeeNamesView.setAdapter(adapter);
        }
    }

    public void remoteBoxClicked(View view) {
        if (!pickup.getOrigin().isRemote()
                && !pickup.getDestination().isRemote()) {
            AlertDialog.Builder errorMsg = new AlertDialog.Builder(this)
                    .setTitle("INVALID REMOTE PICKUP")
                    .setMessage(
                            "!!ERROR: Albany to Albany PICKUPs cannot be processed as Remote.")
                    .setCancelable(false)
                    .setNeutralButton(Html.fromHtml("<b>Ok</b>"), null);

            errorMsg.show();
            remoteBox.setChecked(false);
            return;
        }
        if (((CheckBox) view).isChecked()) {
            remoteShipType.setVisibility(View.VISIBLE);
            if (isOriginLocationRemote()) {
                hideNaReleaseByInfo();
                expandItemList();
            }
        } else {
            remoteShipType.setVisibility(View.INVISIBLE);
            pickup.setShipType("");
            showNaReleaseByInfo();
            resetItemList();
        }
    }

    private void resetItemList() {
        ViewGroup.LayoutParams params = ListViewTab1.getLayoutParams();
        params.height = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 260, getResources()
                        .getDisplayMetrics());
        ListViewTab1.setLayoutParams(params);
        ListViewTab1.requestLayout();
    }

    private void expandItemList() {
        ViewGroup.LayoutParams params = ListViewTab1.getLayoutParams();
        params.height = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 400, getResources()
                        .getDisplayMetrics());
        ListViewTab1.setLayoutParams(params);
        ListViewTab1.requestLayout();
    }

    private boolean isOriginLocationRemote() {
        return !pickup.getOrigin().getAdcity().equalsIgnoreCase("Albany");
    }

    private void hideNaReleaseByInfo() {
        btnPickup3ClrSig.setVisibility(View.INVISIBLE);
        sign.setVisibility(View.INVISIBLE);
        employeeNamesView.setVisibility(View.INVISIBLE);
    }

    private void showNaReleaseByInfo() {
        btnPickup3ClrSig.setVisibility(View.VISIBLE);
        sign.setVisibility(View.VISIBLE);
        employeeNamesView.setVisibility(View.VISIBLE);
    }

    private boolean isRemoteOptionVisible() {
        return (remoteShipType.getVisibility() == View.VISIBLE) ? true : false;
    }
}
