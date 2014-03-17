package gov.nysenate.inventory.activity;

import android.os.AsyncTask;
import android.os.Build;
import android.widget.*;
import gov.nysenate.inventory.adapter.PickupSearchList;
import gov.nysenate.inventory.android.GetAllPickupsListener;
import gov.nysenate.inventory.android.GetAllPickupsTask;
import gov.nysenate.inventory.android.InvApplication;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.model.Transaction;
import gov.nysenate.inventory.util.Toasty;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.View;
import org.apache.http.HttpStatus;

public abstract class SelectDelivery2 extends SenateActivity implements GetAllPickupsListener {

    private List<Transaction> avaliablePickups;
    private ListView searchResults;
    private ProgressBar progressBar;
    private Button cancelButton;
    private List<Transaction> filteredPickups;

    private TextView pageTitle;
    private TextView column1label;
    private TextView column2label;
    private TextView column3label;
    private TextView column4label;

    protected abstract String getPageTitle();

    protected abstract Class getNextActivity();

    protected abstract String getPickupsUrl();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_delivery_2);
        registerBaseActivityReceiver();

        pageTitle = (TextView) findViewById(R.id.title);
        searchResults = (ListView) findViewById(R.id.listView1);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        column1label = (TextView) findViewById(R.id.column1label);
        column2label = (TextView) findViewById(R.id.column2label);
        column3label = (TextView) findViewById(R.id.column3label);
        column4label = (TextView) findViewById(R.id.column4label);
        cancelButton = (Button) findViewById(R.id.cancelButton);
    }

    @Override
    protected void onResume() {
        super.onResume();
        avaliablePickups = new ArrayList<Transaction>();
        filteredPickups = new ArrayList<Transaction>();

        GetAllPickupsTask task = new GetAllPickupsTask(this, getPickupsUrl(), avaliablePickups);
        task.setProgressBar(progressBar);
        if (checkServerResponse(true) == OK) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                task.execute();
            }
        }
    }

    @Override
    public void onResponseExecute(Integer res) {
        SelectDelivery1.SearchByParam searchParameter = setSearchParam(getIntent().getStringExtra("searchParam"));
        String searchText = getIntent().getStringExtra("searchText");

        setStaticText(searchParameter, searchText, pageTitle, column1label, column2label, column3label, column4label, filteredPickups);
        putInDecendingOrder(filteredPickups);

        PickupSearchList adapter = new PickupSearchList(SelectDelivery2.this, R.layout.pickup_group_row, filteredPickups, searchParameter);
        searchResults.setAdapter(adapter);
        searchResults.setTextFilterEnabled(true);
        searchResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (checkServerResponse(true) == OK) {
                    continueToNextActivity(position);
                }
            }

        });

        if (res == HttpStatus.SC_OK) {
            return;
        } else if (res == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            Toasty.displayCenteredMessage(SelectDelivery2.this, "!!ERROR: Database Error while trying to get pickup info.", Toast.LENGTH_SHORT);
        } else {
            Toasty.displayCenteredMessage(SelectDelivery2.this, "!!ERROR: Unknown Error occured pickup data may be inaccurate.", Toast.LENGTH_SHORT);
        }
    }

    private void continueToNextActivity(int position) {
        Transaction selectedPickup = filteredPickups.get(position);
        String nuxrpd = Integer.toString(selectedPickup.getNuxrpd());

        Intent intent = new Intent(this, getNextActivity());
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra("nuxrpd", nuxrpd);
        startActivity(intent);
        overridePendingTransition(R.anim.in_right, R.anim.out_left);
    }

    private void putInDecendingOrder(List<Transaction> pickups) {
        Collections.sort(pickups, Collections.reverseOrder(new Comparator<Transaction>() {
            @Override
            public int compare(Transaction lhs, Transaction rhs) {
                return lhs.getPickupDate().compareTo(rhs.getPickupDate());
            }
        }));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_editpickup2, menu);
        return true;
    }

    public void cancelButton(View view) {
        if (checkServerResponse(true) == OK) {
            cancelButton.getBackground().setAlpha(45);
            finish();
            overridePendingTransition(R.anim.in_left, R.anim.out_right);
        }
    }

    @Override
    public void startTimeout(int timeoutType) {
        Intent intentTimeout = new Intent(this, LoginActivity.class);
        intentTimeout.putExtra("TIMEOUTFROM", timeoutFrom);
        startActivityForResult(intentTimeout, timeoutType);
    }

    private SelectDelivery1.SearchByParam setSearchParam(String parameter) {
        if (parameter.equals("Pickup Location")) {
            return SelectDelivery1.SearchByParam.PICKUPLOC;
        } else if (parameter.equals("Delivery Location")) {
            return SelectDelivery1.SearchByParam.DELIVERYLOC;
        } else if (parameter.equals("Picked Up By")) {
            return SelectDelivery1.SearchByParam.NAPICKUPBY;
        } else if (parameter.equals("Date")) {
            return SelectDelivery1.SearchByParam.DATE;
        } else {
            return null;
        }
    }

    private void setStaticText(SelectDelivery1.SearchByParam searchParam, String searchText, TextView title,
                               TextView row1label, TextView row2label, TextView row3label, TextView row4label, List<Transaction> filteredPickups) {
        String titleBuffer = getPageTitle();
        switch(searchParam) {
            case PICKUPLOC:
                titleBuffer += Html.fromHtml("<br>Pickup Location: <b>" + searchText + "</b>");
                row1label.setText("Date");
                row2label.setText("Pickup By");
                row3label.setText("Delivery Location");
                filteredPickups.addAll(pickupsWithPickupLoc(searchText));
                break;

            case DELIVERYLOC:
                titleBuffer += Html.fromHtml("<br>Delivery Location: <b>" + searchText + "</b>");
                row1label.setText("Date");
                row2label.setText("Pickup By");
                row3label.setText("Pickup Location");
                filteredPickups.addAll(pickupsWithDestLoc(searchText));
                break;

            case NAPICKUPBY:
                titleBuffer += Html.fromHtml("<br>Picked Up By: <b>" + searchText + "</b>");
                row1label.setText("Date");
                row2label.setText("Pickup Location");
                row3label.setText("Delivery Location");
                filteredPickups.addAll(pickupsWithNaPickupBy(searchText));
                break;

            case DATE:
                titleBuffer += Html.fromHtml("<br>Pickup Date: <b>" + searchText + "</b>");
                row1label.setText("Pickup By");
                row2label.setText("Pickup Location");
                row3label.setText("Delivery Location");
                filteredPickups.addAll(pickupsOnDate(searchText));
                break;
        }

        title.setText(titleBuffer);
        row4label.setText("Count");
    }

    private List<Transaction> pickupsWithPickupLoc(String loc) {
        List<Transaction> pickups = new ArrayList<Transaction>();
        String locCode = loc.split("-")[0];
        for(Transaction pickup: avaliablePickups) {
            if (pickup.getOriginCdLoc().equalsIgnoreCase(locCode)) {
                pickups.add(pickup);
            }
        }
        return pickups;
    }

    private List<Transaction> pickupsWithDestLoc(String loc) {
        List<Transaction> pickups = new ArrayList<Transaction>();
        String locCode = loc.split("-")[0];
        for(Transaction pickup: avaliablePickups) {
            if (pickup.getDestinationCdLoc().equalsIgnoreCase(locCode)) {
                pickups.add(pickup);
            }
        }
        return pickups;
    }

    private List<Transaction> pickupsWithNaPickupBy(String name) {
        List<Transaction> pickups = new ArrayList<Transaction>();
        for(Transaction pickup: avaliablePickups) {
            if (pickup.getNapickupby().equalsIgnoreCase(name)) {
                pickups.add(pickup);
            }
        }
        return pickups;
    }

    private List<Transaction> pickupsOnDate(String date) {
        SimpleDateFormat sdf = ((InvApplication)getApplicationContext()).getLongDayFormat();
        List<Transaction> pickups = new ArrayList<Transaction>();
        for(Transaction pickup: avaliablePickups) {
            if (sdf.format(pickup.getPickupDate()).equalsIgnoreCase(date)) {
                pickups.add(pickup);
            }
        }
        return pickups;
    }

}