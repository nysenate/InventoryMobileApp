package gov.nysenate.inventory.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import gov.nysenate.inventory.adapter.InvSelListViewAdapter;
import gov.nysenate.inventory.android.AppSingleton;
import gov.nysenate.inventory.android.ClearableAutoCompleteTextView;
import gov.nysenate.inventory.android.ClearableEditText;
import gov.nysenate.inventory.android.InvApplication;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.android.RemoteConfirmationDialog;
import gov.nysenate.inventory.android.SignatureView;
import gov.nysenate.inventory.android.StringInvRequest;
import gov.nysenate.inventory.model.Employee;
import gov.nysenate.inventory.model.InvItem;
import gov.nysenate.inventory.model.Transaction;
import gov.nysenate.inventory.util.AppProperties;
import gov.nysenate.inventory.util.Serializer;
import gov.nysenate.inventory.util.Toasty;

public class Delivery3 extends SenateActivity {
    public TextView loc_details;
    String location = "";
    String nuxrpd = "";
    String URL = "";
    String res = "";
    ListView listview;
    String nuxrAcceptSign = "";
    String naDeliverby = "";
    String naAcceptby = "";
    private SignatureView sign;
    private byte[] imageInByte = {};
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
    ClearableEditText commentsEditText;
    int nuxrefem = -1;
    public ArrayList<Employee> employeeHiddenList = new ArrayList<Employee>();
    public ArrayList<String> employeeNameList = new ArrayList<String>();
    TextView tvItemCount;
    ClearableAutoCompleteTextView naemployeeView;
    String deliveryRequestTaskType = "";
    private String deliveryComments = null;
    Button btnDeliv3ClrSig;
    Button btnDelivery3Back;
    Button btnDelivery3Cont;
    InvSelListViewAdapter invAdapter;
    public static ProgressBar progBarDelivery3;
    Activity currentActivity;
    String timeoutFrom = "delivery1";
    public final int DELIVERYDETAILS_TIMEOUT = 101,
            POSITIVEDIALOG_TIMEOUT = 102, KEEPALIVE_TIMEOUT = 103;

    public ArrayList<InvItem> invList = new ArrayList<InvItem>();
    private Transaction delivery;

    Response.Listener employeeListResponseListener = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {

            naemployeeView.setThreshold(1);

            nuxrAcceptSign = "1111";
            naDeliverby = "XX";

            naAcceptby = "Abc,xyz";// note : we need to have comma in name (query is
            // formated that way)

            // Get the results for the Employee List and now do the actual setting
            // of the Signing Employee
            // Dropdown.

            employeeHiddenList = new ArrayList<Employee>();
            employeeNameList = new ArrayList<String>();

            List<Employee> currentEmployees = Serializer.deserialize(response, Employee.class);
            employeeHiddenList.addAll(currentEmployees);
            for (Employee emp : currentEmployees) {
                employeeNameList.add(emp.getFullName());
            }

            Collections.sort(employeeNameList);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(Delivery3.this,
                    android.R.layout.simple_dropdown_item_1line, employeeNameList);

            naemployeeView.setAdapter(adapter);
        }
    };

    Response.Listener deliveryListResponseListener = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            delivery = Serializer.deserialize(response, Transaction.class).get(0);
            invList = delivery.getPickupItems();

            // Display the pickup data
            listview = (ListView) findViewById(R.id.listView1);
            listview.setItemsCanFocus(false);
            invAdapter = new InvSelListViewAdapter(getApplicationContext(),
                    R.layout.invlist_sel_item, invList);
            listview.setAdapter(invAdapter);
            // set everything as checked
            invAdapter.setAllSelected(true);
            invAdapter.setNotifyOnChange(true);
            try {
                tvItemCount.setText("Item Count:  " + invList.size());
            } catch (Exception e) {
                try {
                    tvItemCount.setText("Item Count:   N/A");
                } catch (Exception e2) {
                    e.printStackTrace();
                }
            }

            location = delivery.getDestinationSummaryString();
            // Append [R] to location if remote.
            if (delivery.isRemote()) {
                location += " [" + "<font color='#ff0000'>R</font>" + "]";
            }
            loc_details.setText(Html.fromHtml(location));

            if (delivery.isRemoteDelivery()) {
                naemployeeView.setVisibility(TextView.INVISIBLE);
                sign.setVisibility(SignatureView.INVISIBLE);
                btnDeliv3ClrSig.setVisibility(Button.INVISIBLE);

                ViewGroup.LayoutParams params = listview.getLayoutParams();
                params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 460, getResources().getDisplayMetrics());
                listview.setLayoutParams(params);
                listview.requestLayout();
            }

        }
    };


    Response.Listener imageUploadResponseListener = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                delivery.setNuxrrelsign(jsonObject.getString("nuxrrelsign"));

                if (jsonObject.has("Error")) {
                    new Toasty(Delivery3.this).showMessage((String) jsonObject.get("Error"), Toast.LENGTH_LONG);
                }

                processDeliveryPart2();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    Response.Listener deliveryResponseListener = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            // No Errors, display toast and return to main menu.
            // Display Toster
            if (response.length() == 0) {
                noServerResponse("");
                return;
            }

            int duration = Toast.LENGTH_SHORT;
            new Toasty(InvApplication.getAppContext()).showMessage(response, duration);
            returnToMoveMenu();
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery3);
        registerBaseActivityReceiver();
        currentActivity = this;
        // leaving as delivery1 for now but probably should be delivery3
        AppSingleton.getInstance(this).timeoutFrom = "delivery1";

        // Get the data from previous activity
        Intent intent = getIntent();
        nuxrpd = intent.getStringExtra("nuxrpd");

        // Set the location in textview
        loc_details = (TextView) findViewById(R.id.textView2);
        tvItemCount = (TextView) findViewById(R.id.tvItemCount);
        // Get the barcode numbers from the server and set it to the listview

        // Configure Image Buttons
        btnDeliv3ClrSig = (Button) findViewById(R.id.btnDeliv3ClrSig);
        btnDelivery3Back = (Button) findViewById(R.id.btnDelivery3Back);
        btnDelivery3Cont = (Button) findViewById(R.id.btnDelivery3Cont);

        // Setup ProgressBar
        progBarDelivery3 = (ProgressBar) findViewById(R.id.progBarDelivery3);

        // Setup the Signature Field (sign) and Delivery Comments
        // (commentsEditText)
        sign = (SignatureView) findViewById(R.id.sv_accept_sign_delivery3);
        sign.setMinDimensions(200, 100);
        commentsEditText = (ClearableEditText) findViewById(R.id.deliveryCommentsEditText);
        commentsEditText
                .setClearMsg("Do you want to clear the Delivery Comments?");
        commentsEditText.showClearMsg(true);

        naemployeeView = (ClearableAutoCompleteTextView) findViewById(R.id.naemployee);
        naemployeeView
                .setClearMsg("Do you want to clear the name of the signer?");
        naemployeeView.showClearMsg(true);
        naemployeeView
                .setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(
                                naemployeeView.getWindowToken(), 0);
                    }
                });
        delivery = new Transaction();
        getEmployeeList();
        getDeliveryDetails();
    }

    @Override
    public void startTimeout(int timeoutType) {
        Intent intentTimeout = new Intent(this, LoginActivity.class);
        intentTimeout.putExtra("TIMEOUTFROM", timeoutFrom);
        startActivityForResult(intentTimeout, timeoutType);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setNotBusy();
    }

    private void setNotBusy() {
        if (this.btnDeliv3ClrSig == null) {
            this.btnDeliv3ClrSig = (Button) this
                    .findViewById(R.id.btnDeliv3ClrSig);
        }
        if (this.btnDelivery3Back == null) {
            this.btnDelivery3Back = (Button) this
                    .findViewById(R.id.btnDelivery3Back);
        }
        if (this.btnDelivery3Cont == null) {
            this.btnDelivery3Cont = (Button) this
                    .findViewById(R.id.btnDelivery3Cont);
        }
        if (Delivery3.progBarDelivery3 == null) {
            Delivery3.progBarDelivery3 = (ProgressBar) this
                    .findViewById(R.id.progBarDelivery3);
        }
        this.btnDeliv3ClrSig.getBackground().setAlpha(255);
        this.btnDelivery3Back.getBackground().setAlpha(255);
        this.btnDelivery3Cont.getBackground().setAlpha(255);
        Delivery3.progBarDelivery3.setVisibility(View.INVISIBLE);
    }

    public int getEmployeeId(String name) {
        for (Employee emp : employeeHiddenList) {
            if (emp.getFullName().equalsIgnoreCase(name)) {
                return emp.getNuxrefem();
            }
        }
        return 0;
    }

    @Override
    public void onVolleyInvError() {
        new Toasty(this).showMessage("On Volley Error DELIVERY Activity");
        setNotBusy();
    }

    public void continueButton(View view) {

        if (invAdapter == null || invAdapter.getSelectedItems(true) == null || invAdapter.getSelectedItems(true).size() < 1) {
            displayNoItemsSelectedMessage();
            return;
        }

        if (employeeHiddenList == null || employeeHiddenList.size() == 0) {
            new Toasty(this).showMessage("!!ERROR: Employee list is empty. This could be caused by a loss of connection. Please contact STSBAC.", Toast.LENGTH_LONG);
            return;
        }

        String emp = "";
        if (delivery == null || !delivery.isRemoteDelivery()) {
            emp = naemployeeView.getEditableText().toString().trim();
            if (!selectedEmployeeValid(emp, employeeHiddenList)) {
                displayInvalidEmployeeMessage(naemployeeView.getEditableText().toString().trim());
                return;
            }

            nuxrefem = employeeHiddenList.get(findEmployee(emp, employeeHiddenList)).getNuxrefem();

            if (!sign.isSigned()) {
                displayNoSignatureMessage();
                return;
            }
        }

        displayDeliveryConfirmationDialog();
    }

    private void displayDeliveryConfirmationDialog() {
        AlertDialog.Builder confirmDialog = new AlertDialog.Builder(this);
        confirmDialog.setTitle(Html.fromHtml("<font color='#000055'>Delivery Confirmation</font>"));
        confirmDialog.setMessage("Are you sure you want to deliver these "
                + invAdapter.getSelectedItems(true).size() + " items?");
        confirmDialog.setCancelable(false);
        confirmDialog.setPositiveButton(Html.fromHtml("<b>Yes</b>"),
                new DialogInterface.OnClickListener() {
                    boolean positiveButtonPressed = false;

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!positiveButtonPressed) {
                            positiveButtonPressed = true;

                            if (delivery.isRemote()) {
                                // Show fragment to getInstance Remote info, calls positiveDialog() on completion.
                                DialogFragment newFragment = RemoteConfirmationDialog.newInstance(employeeNameList, delivery);
                                newFragment.setCancelable(false);
                                newFragment.show(getFragmentManager(), "dialog");
                            } else {
                                positiveDialog();
                            }
                        }
                    }
                });

        confirmDialog.setNegativeButton(Html.fromHtml("<b>No</b>"), null);

        AlertDialog dialog = confirmDialog.create();
        dialog.show();
    }

    private void displayNoItemsSelectedMessage() {
        CharSequence text = "!!ERROR: You must select an item to deliver.";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(this, text, duration);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public void positiveDialog() {
        progBarDelivery3.setVisibility(View.VISIBLE);
        this.btnDelivery3Cont.getBackground().setAlpha(45);

        naAcceptby = naemployeeView.getText().toString();
        deliveryComments = commentsEditText.getText().toString();

        if (this.invAdapter == null) {
            this.invAdapter = (InvSelListViewAdapter) this.listview
                    .getAdapter();
        }
        String URL = AppProperties.getBaseUrl();

        this.deliveryRequestTaskType = "Delivery";
        String deliveryURL = URL
                + "DeliveryConfirmation?";


        if (!delivery.isRemoteDelivery()) {
            // When remote delivery, name set in RemoteConfirmationDialog.
            delivery.setNaacceptby(naAcceptby);
        }

        delivery.setNadeliverby(LoginActivity.nauser);
        delivery.setNuxrpd(Integer.valueOf(nuxrpd));
        delivery.setDeliveryComments(deliveryComments);
        delivery.setPickupItems(invList);
        delivery.setCheckedItems(this.invAdapter.getSelectedItems(true));

        this.processDelivery(URL
                + "ImgUpload?nauser=" + LoginActivity.nauser
                + "&nuxrefem=" + nuxrefem, deliveryURL);
    }

    public void returnToMoveMenu() {
        Intent intent = new Intent(this, Move.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.in_right, R.anim.out_left);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                Toast toast = Toast.makeText(getApplicationContext(), "Going Back",
                        Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                this.onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_delivery3, menu);
        return true;
    }

    public void clearSignatureButton(View view) {
        Bitmap clearedSignature = BitmapFactory.decodeResource(getResources(),
                R.drawable.simplethinborder);
        if (clearedSignature == null) {
            Log.i("ClearSig", "Signature drawable was NULL");
        } else {
            Log.i("ClearSig", "Signature size:" + clearedSignature.getWidth()
                    + " x " + clearedSignature.getHeight());
        }
        sign.clearSignature();
    }

    /**
     * Fire an intent to start the speech recognition activity.
     */
    public void startCommentsSpeech(View view) {
        if (view.getId() == R.id.deliveryCommentsSpeechButton) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                    "Delivery Comments Speech");
            startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
        }
    }

    /**
     * Handle the results from the recognition activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case DELIVERYDETAILS_TIMEOUT:
                if (resultCode == RESULT_OK) {
                    getDeliveryDetails();
                    break;
                }
            case POSITIVEDIALOG_TIMEOUT:
                // positiveDialog();
                break;
            case KEEPALIVE_TIMEOUT:
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(
                                naemployeeView.getWindowToken(), 0);
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
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void backButton(View view) {
        super.onBackPressed();
    }

    public byte[] getByteArray(Bitmap bitmap) {
        return getByteArray(bitmap, 0, 0);
    }

    public byte[] getByteArray(Bitmap bitmap, int width, int height) {
        byte[] byteArray = null;

        ByteArrayOutputStream bs = new ByteArrayOutputStream();

        if (width <= 0) {
            if (bitmap != null) {
                width = bitmap.getWidth();
            } else {
                width = 200; // default value if all else fails
            }
        }

        if (height <= 0) {
            if (bitmap != null) {
                height = bitmap.getHeight();
            } else {
                height = 40; // default value if all else fails
            }
        }

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, width,
                height, true);

        for (int x = 0; x < scaledBitmap.getWidth(); x++) {
            for (int y = 0; y < scaledBitmap.getHeight(); y++) {
                String strColor = String.format("#%06X",
                        0xFFFFFF & scaledBitmap.getPixel(x, y));
                if (strColor.equals("#000000")
                        || scaledBitmap.getPixel(x, y) == Color.TRANSPARENT) {
                    scaledBitmap.setPixel(x, y, Color.WHITE);
                }
            }
        }
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bs);

        byteArray = bs.toByteArray();

        return byteArray;
    }

    public void processDelivery(String imageUploadURL, String deliveryURL) {

        if (delivery.isRemoteDelivery()) {
            Log.i(this.getClass().getName(), "Remote Delivery: Process without a signature");
            processDeliveryPart2();
        } else {
            Log.i(this.getClass().getName(), "Not a Remote Delivery: Process signature");

            String imageString = Arrays.toString(getByteArray(sign.getImage(), 200, 40));

            Map<String, String> params = new HashMap<String, String>();

            if (LoginActivity.nauser != null) {
                params.put("userFallback",
                        LoginActivity.nauser);
            }

            params.put("nuxrefem", String.valueOf(nuxrefem));
            params.put("signature", imageString);

            StringInvRequest stringInvRequest = new StringInvRequest(Request.Method.POST,
                    imageUploadURL, params, imageUploadResponseListener);

            InvApplication.timeoutType = DELIVERYDETAILS_TIMEOUT;

            /* Add your Requests to the RequestQueue to execute */
            AppSingleton.getInstance(InvApplication.getAppContext()).addToRequestQueue(stringInvRequest);
        }
    }

    public void processDeliveryPart2() {
        try {
            delivery.setDeliveryDate(new Date());

            Map<String, String> params = new HashMap<String, String>();
            params.put("Delivery", Serializer.serialize(delivery));
            params.put("userFallback", LoginActivity.nauser);

            String pickupURL = AppProperties.getBaseUrl()
                    + "DeliveryConfirmation";

            StringInvRequest stringInvRequest = new StringInvRequest(Request.Method.POST,
                    pickupURL, params, deliveryResponseListener);

            InvApplication.timeoutType = DELIVERYDETAILS_TIMEOUT;
            /* Add your Requests to the RequestQueue to execute */
            AppSingleton.getInstance(InvApplication.getAppContext()).addToRequestQueue(stringInvRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getDeliveryDetails() {

        String url = AppProperties.getBaseUrl();
        url += URL + "GetPickup?nuxrpd=" + nuxrpd;

        Log.i(this.getClass().getName(), url);

        InvApplication.timeoutType = DELIVERYDETAILS_TIMEOUT;

        StringInvRequest stringInvRequest = new StringInvRequest(Request.Method.GET, url, null, deliveryListResponseListener);

        /* Add your Requests to the RequestQueue to execute */
        AppSingleton.getInstance(InvApplication.getAppContext()).addToRequestQueue(stringInvRequest);
    }


    public void getEmployeeList() {
        String url = AppProperties.getBaseUrl();
        url += "EmployeeList";
        //     url += "userFallback=" + LoginActivity.nauser;

        Log.i(this.getClass().getName(), url);

        StringInvRequest stringInvRequest = new StringInvRequest(Request.Method.GET, url, null, employeeListResponseListener);

        /* Add your Requests to the RequestQueue to execute */
        AppSingleton.getInstance(InvApplication.getAppContext()).addToRequestQueue(stringInvRequest);
    }

    public Transaction getDelivery() {
        return delivery;
    }

    protected void onPostExecute(Boolean result) {
        LoginActivity.activeAsyncTask = null;
        // execution of result of Long time consuming operation
    }

}
