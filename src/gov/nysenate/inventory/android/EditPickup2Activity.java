package gov.nysenate.inventory.android;

import gov.nysenate.inventory.android.EditPickup1Activity.SearchByParam;
import gov.nysenate.inventory.model.Pickup;
import gov.nysenate.inventory.util.PickupSearchList;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class EditPickup2Activity extends SenateActivity
{
    private List<Pickup> avaliablePickups;
    private ListView pickupSearchResults;
    private ProgressBar progBarEditPickup2;
    private Button btnEditPickupActivity2Cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editpickup2);
        registerBaseActivityReceiver();
        
        TextView pageTitle = (TextView) findViewById(R.id.title);
        pickupSearchResults = (ListView) findViewById(R.id.listView1);
        progBarEditPickup2 = (ProgressBar) findViewById(R.id.progBarEditPickup2);
        TextView column1label = (TextView) findViewById(R.id.column1label);
        TextView column2label = (TextView) findViewById(R.id.column2label);
        TextView column3label = (TextView) findViewById(R.id.column3label);
        TextView column4label = (TextView) findViewById(R.id.column4label);
        btnEditPickupActivity2Cancel = (Button) findViewById(R.id.btnEditPickup2Cancel);

        String searchText;
        final List<Pickup> filteredPickups = new ArrayList<Pickup>();

        SearchByParam searchParameter = setSearchParam(getIntent().getStringExtra("searchParam"));
        searchText = getIntent().getStringExtra("searchText");
        avaliablePickups = getIntent().getParcelableArrayListExtra("pickups");

        setStaticText(searchParameter, searchText, pageTitle, column1label, column2label, column3label, column4label, filteredPickups);

        PickupSearchList adapter = new PickupSearchList(this, R.layout.pickup_group_row, filteredPickups, searchParameter);
        pickupSearchResults.setAdapter(adapter);
        pickupSearchResults.setTextFilterEnabled(true);
        pickupSearchResults.setOnItemClickListener(new OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                if (checkServerResponse(true) == OK) {
                    Pickup selectedPickup = filteredPickups.get(position);
                    String nuxrpd = Integer.toString(selectedPickup.getNuxrpd());

                    Intent intent = new Intent(EditPickup2Activity.this, EditPickupMenu.class);
                    intent.putExtra("nuxrpd", nuxrpd);
                    intent.putExtra("date", selectedPickup.getDate());
                    startActivity(intent);
                    overridePendingTransition(R.anim.in_right, R.anim.out_left);
                }
            }

        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        btnEditPickupActivity2Cancel.getBackground().setAlpha(255);
        progBarEditPickup2.getBackground().setAlpha(255);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_editpickup2, menu);
        return true;
    }

    public void cancelButton(View view) {
        if (checkServerResponse(true) == OK) {
            btnEditPickupActivity2Cancel.getBackground().setAlpha(45);
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

    private SearchByParam setSearchParam(String parameter) {
        if (parameter.equals("Pickup Location")) {
            return SearchByParam.PICKUPLOC;
        } else if (parameter.equals("Delivery Location")) {
            return SearchByParam.DELIVERYLOC;
        } else if (parameter.equals("Picked Up By")) {
            return SearchByParam.NAPICKUPBY;
        } else if (parameter.equals("Date")) {
            return SearchByParam.DATE;
        } else {
            // TODO: should never occur, should we test/handle it?
            return null;
        }
    }

    private void setStaticText(SearchByParam searchParam, String searchText, TextView title,
            TextView row1label, TextView row2label, TextView row3label, TextView row4label, List<Pickup> filteredPickups) {
        
        String titleBuffer = "Please select a pickup to edit.";

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
    
    private List<Pickup> pickupsWithPickupLoc(String loc) {
        List<Pickup> pickups = new ArrayList<Pickup>();
        String locCode = loc.split("-")[0];
        for(Pickup pickup: avaliablePickups) {
            if (pickup.getOriginCdLoc().equalsIgnoreCase(locCode)) {
                pickups.add(pickup);
            }
        }
        return pickups;
    }
    
    private List<Pickup> pickupsWithDestLoc(String loc) {
        List<Pickup> pickups = new ArrayList<Pickup>();
        String locCode = loc.split("-")[0];
        for(Pickup pickup: avaliablePickups) {
            if (pickup.getDestinationCdLoc().equalsIgnoreCase(locCode)) {
                pickups.add(pickup);
            }
        }
        return pickups;
    }
    
    private List<Pickup> pickupsWithNaPickupBy(String name) {
        List<Pickup> pickups = new ArrayList<Pickup>();
        for(Pickup pickup: avaliablePickups) {
            if (pickup.getNaPickupBy().equalsIgnoreCase(name)) {
                pickups.add(pickup);
            }
        }
        return pickups;
    }

    private List<Pickup> pickupsOnDate(String date) {
        List<Pickup> pickups = new ArrayList<Pickup>();
        for(Pickup pickup: avaliablePickups) {
            if (pickup.getDateWithoutTime().equalsIgnoreCase(date)) {
                pickups.add(pickup);
            }
        }
        return pickups;
    }
}
