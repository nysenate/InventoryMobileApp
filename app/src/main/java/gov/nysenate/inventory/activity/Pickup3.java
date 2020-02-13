package gov.nysenate.inventory.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Html;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;

import org.apache.http.NameValuePair;
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

import gov.nysenate.inventory.adapter.InvListViewAdapter;
import gov.nysenate.inventory.adapter.NothingSelectedSpinnerAdapter;
import gov.nysenate.inventory.android.AppSingleton;
import gov.nysenate.inventory.android.ClearableAutoCompleteTextView;
import gov.nysenate.inventory.android.ClearableEditText;
import gov.nysenate.inventory.android.InvApplication;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.android.SignatureView;
import gov.nysenate.inventory.android.StringInvRequest;
import gov.nysenate.inventory.model.Employee;
import gov.nysenate.inventory.model.InvItem;
import gov.nysenate.inventory.model.Transaction;
import gov.nysenate.inventory.util.AppProperties;
import gov.nysenate.inventory.util.Serializer;
import gov.nysenate.inventory.util.Toasty;

public class Pickup3 extends SenateActivity {
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

    Response.Listener employeeListResponseListener = new Response.Listener<String>() {

        @Override
        public void onResponse(String response) {

            if (response == null
                    || response.indexOf("Session timed out") != -1) {
                if (response.indexOf("Session timed out") != -1) {
                    startTimeout(EMPLOYEELIST_TIMEOUT);
                }

                return;
            }

            List<Employee> currentEmployees = Serializer.deserialize(response, Employee.class);

            employeeHiddenList.addAll(currentEmployees);

            for (Employee emp : currentEmployees) {
                employeeNameList.add(emp.getFullName());
            }

            Collections.sort(employeeNameList);

            setProgBarVisibility(View.INVISIBLE);

            if (response == null) {
                noServerResponse("Pickup3:3: response:null");
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
    };

    Response.Listener imageUploadResponseListener = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                pickup.setNuxrrelsign(jsonObject.getString("nuxrrelsign"));
                setProgBarVisibility(View.INVISIBLE);

                if (jsonObject.has("Error")) {
                    new Toasty(Pickup3.this).showMessage((String) jsonObject.get("Error"), Toast.LENGTH_LONG);
                }

                processPickupPart2();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    Response.Listener pickupResponseListener = new Response.Listener<String>() {
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
        setContentView(R.layout.activity_pickup3);
        registerBaseActivityReceiver();
        AppSingleton.getInstance(this).timeoutFrom = "pickup3";

        sign = (SignatureView) findViewById(R.id.blsignImageView);
        sign.setMinDimensions(200, 100);
        commentsEditText = (ClearableEditText) findViewById(R.id.pickupCommentsEditText);
        commentsEditText
                .setClearMsg("Do you want to clear the Pickup Comments?");
        commentsEditText.showClearMsg(true);

        ListViewTab1 = (ListView) findViewById(R.id.listView1);

        pickup = Serializer.deserialize(getIntent().getStringExtra("pickup"), Transaction.class).get(0);
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
        remoteShipType.setOnItemSelectedListener(new OnItemSelectedListener() {
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

        Pickup2.continueBtn.getBackground().setAlpha(255);

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
                .setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(
                                employeeNamesView.getWindowToken(), 0);
                    }
                });

        progBarPickup3 = (ProgressBar) findViewById(R.id.progBarPickup3);

        Map<String, String> arguments = new HashMap<>();
        arguments.put("oncreate", "Y");

        getEmployeeList(arguments);
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

    public void setProgBarVisibility(int visibility) {
        if (progBarPickup3 == null) {
            progBarPickup3 = (ProgressBar) this
                    .findViewById(R.id.progBarPickup3);
        }

        progBarPickup3.setVisibility(visibility);
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

        switch (requestCode) {
            case EMPLOYEELIST_TIMEOUT:
                if (resultCode == RESULT_OK) {
                    getEmployeeList();
                }
                break;
            case POSITIVEDIALOG_TIMEOUT:
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(
                                employeeNamesView.getWindowToken(), 0);
                    }
                }, 50);

                if (progBarPickup3 != null) {
                    progBarPickup3.setVisibility(View.INVISIBLE);
                }
                break;
            case KEEPALIVE_TIMEOUT:
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        // "KEEPALIVE_TIMEOUT Hide Keyboard");
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(
                                employeeNamesView.getWindowToken(), 0);
                    }
                }, 50);
                if (progBarPickup3 != null) {
                    progBarPickup3.setVisibility(View.INVISIBLE);
                }
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

        /*
            12/11/19   If it is a remote Pickup and Delivery, do not continue since one must not be remote (Albany)
                       If this is wrong, then get rid of the check but processing pickups also needs to be fixed
                       bacause it will error (due to isRemotePickup and isRemoteDelivery in Transaction assuming that
                       one location is Albany).
         */

        if (pickup.isRemote()) {
            if (pickup.getOrigin().isRemote()
                    && pickup.getDestination().isRemote()) {
                AlertDialog.Builder errorMsg = new AlertDialog.Builder(this)
                        .setTitle("INVALID REMOTE PICKUP")
                        .setMessage(
                                "!!ERROR: Either Pickup or Delivery Location must be Albany.")
                        .setCancelable(false)
                        .setNeutralButton(Html.fromHtml("<b>Ok</b>"), null);

                errorMsg.show();
                return;
            }
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
                        new DialogInterface.OnClickListener() {
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
        super.onBackPressed();
        /*
         * float alpha = 0.45f; AlphaAnimation alphaUp = new
         * AlphaAnimation(alpha, alpha); alphaUp.setFillAfter(true);
         * btnPickup3Back.startAnimation(alphaUp); Intent intent = new
         * Intent(this, Pickup2.class); startActivity(intent);
         * overridePendingTransition(R.anim.in_left, R.anim.out_right);
         */
    }

    public void clearSignatureButton(View view) {
        btnPickup3ClrSig.getBackground().setAlpha(45);
        Bitmap clearedSignature = BitmapFactory.decodeResource(getResources(),
                R.drawable.simplethinborder);
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

    /**
     * positiveDialog dummy proceedure to allow proceedure to be called by layout objects which automatically pass
     * a view parameter.
     *
     * @param view
     */
    public void positiveDialog(View view) {
        positiveDialog();
    }

    private void positiveDialog() {
        continueBtn.getBackground().setAlpha(45);
        String URL = AppProperties.getBaseUrl();

        this.processPickup(URL + "ImgUpload?nauser="
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
        getEmployeeList(null);
    }

    public void getEmployeeList(Map<String, String> arguments) {

        String url = AppProperties.getBaseUrl(Pickup3.this);
        url += "EmployeeList?";
        url += "&userFallback=" + LoginActivity.nauser;

        setProgBarVisibility(View.VISIBLE);

        InvApplication.timeoutType = EMPLOYEELIST_TIMEOUT;

        StringInvRequest stringInvRequest = new StringInvRequest(Request.Method.GET, url, null, employeeListResponseListener);

        /* Add your Requests to the RequestQueue to execute */
        AppSingleton.getInstance(InvApplication.getAppContext()).addToRequestQueue(stringInvRequest);
    }

    public byte[] getByteArray(Bitmap bitmap) {
        return getByteArray(bitmap, 0, 0);
    }

    public byte[] getByteArray(Bitmap bitmap, int width, int height) {
        byte[] byteArray = null;

        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        //Bitmap bitmap = sign.getImage();

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

    public void processPickup(String imageUploadURL, String pickupURL) {

        if (pickup.isRemotePickup()) {
            processPickupPart2();
        } else {

            String imageString = Arrays.toString(getByteArray(sign.getImage(), 200, 40));

            Map<String, String> params = new HashMap<String, String>();

            if (LoginActivity.nauser != null) {
                params.put("userFallback",
                        LoginActivity.nauser);
            }

            params.put("nuxrefem", String.valueOf(nuxrefem));
            params.put("signature", imageString);

            setProgBarVisibility(View.VISIBLE);

            InvApplication.timeoutType = -1;

            StringInvRequest stringInvRequest = new StringInvRequest(Request.Method.POST,
                    imageUploadURL, params, imageUploadResponseListener);

            /* Add your Requests to the RequestQueue to execute */
            AppSingleton.getInstance(InvApplication.getAppContext()).addToRequestQueue(stringInvRequest);
        }

    }

    public void processPickupPart2() {
        try {
            pickup.setPickupDate(new Date());

            Map<String, String> params = new HashMap<String, String>();
            params.put("pickup", Serializer.serialize(pickup));
            params.put("userFallback", LoginActivity.nauser);

            String pickupURL = AppProperties.getBaseUrl()
                    + "Pickup";

            setProgBarVisibility(View.VISIBLE);

            InvApplication.timeoutType = this.POSITIVEDIALOG_TIMEOUT;

            StringInvRequest stringInvRequest = new StringInvRequest(Request.Method.POST,
                    pickupURL, params, pickupResponseListener);
            /* Add your Requests to the RequestQueue to execute */
            AppSingleton.getInstance(InvApplication.getAppContext()).addToRequestQueue(stringInvRequest);

        } catch (Exception e) {
            e.printStackTrace();
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

        /*
            12/11/19     If Remote is selected, then make sure that either Pickup or Destination
                       Location is Albany (is not remote). If this is incorrect, we will need to
                       fix Transaction.isRemoteDelivery and isRemotePickuo.
         */

        if (pickup.getOrigin().isRemote()
                && pickup.getDestination().isRemote()) {
            AlertDialog.Builder errorMsg = new AlertDialog.Builder(this)
                    .setTitle("INVALID REMOTE PICKUP")
                    .setMessage(
                            "!!ERROR: Either Pickup or Delivery Location must be Albany.")
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