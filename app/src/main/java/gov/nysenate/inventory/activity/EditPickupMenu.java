package gov.nysenate.inventory.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import gov.nysenate.inventory.adapter.CustomListViewAdapter;
import gov.nysenate.inventory.android.AppSingleton;
import gov.nysenate.inventory.android.InvApplication;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.android.StringInvRequest;
import gov.nysenate.inventory.model.RowItem;
import gov.nysenate.inventory.model.Transaction;
import gov.nysenate.inventory.util.AppProperties;
import gov.nysenate.inventory.util.Serializer;

public class EditPickupMenu extends SenateActivity implements OnItemClickListener {
    public static Transaction pickup;
    private ProgressBar progressBar;
    private List<RowItem> menuRowItems;
    private static final String[] titles = {"Cancel Pickup", "Change Delivery Location", "Change Pickup Location",
            "Remove Items", "Add/Change Remote Info", "Move Menu"};
    private static final Integer[] images = {R.drawable.cancelpickup, R.drawable.editlocdelivery, R.drawable.editlocpickup,
            R.drawable.removeitems, R.drawable.edit_remote, R.drawable.mainmenu};
    private TextView oldPickupLocation;
    private TextView oldDeliveryLocation;
    private TextView oldPickupBy;
    private TextView oldCount;
    private TextView oldDate;
    ListView menuRowsListView = null;

    Response.Listener<String> responseListener = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            pickup = Serializer.deserialize(response, Transaction.class).get(0);
            progressBar.setVisibility(ProgressBar.INVISIBLE);

            if (pickup.isRemote()) {
                oldPickupLocation.setText(Html.fromHtml(pickup.getOrigin().getLocationSummaryStringRemoteAppended()));
                oldDeliveryLocation.setText(Html.fromHtml(pickup.getDestination().getLocationSummaryStringRemoteAppended()));
            } else {
                oldPickupLocation.setText(pickup.getOrigin().getLocationSummaryString());
                oldDeliveryLocation.setText(pickup.getDestination().getLocationSummaryString());
            }

            oldPickupBy.setText(pickup.getNapickupby());
            oldCount.setText(Integer.toString(pickup.getPickupItems().size()));
            SimpleDateFormat sdf = ((InvApplication) getApplicationContext()).getDateTimeFormat();
            oldDate.setText(sdf.format(pickup.getPickupDate()));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_pickup_menu);
        registerBaseActivityReceiver();
        progressBar = (ProgressBar) findViewById(R.id.edit_pickup_menu_progress_bar);
        menuRowsListView = (ListView) findViewById(R.id.edit_pickup_menu_list);
        oldPickupLocation = (TextView) findViewById(R.id.old_pickup_location);
        oldDeliveryLocation = (TextView) findViewById(R.id.old_delivery_location);
        oldPickupBy = (TextView) findViewById(R.id.pickup_by);
        oldCount = (TextView) findViewById(R.id.pickup_count);
        oldDate = (TextView) findViewById(R.id.pickup_date);

        //pickup = new Transaction();

        String url = AppProperties.getBaseUrl();
        url += "GetPickup?nuxrpd=" + getIntent().getStringExtra("nuxrpd");
        url += "&userFallback=" + LoginActivity.nauser;

        StringInvRequest stringInvRequest = new StringInvRequest(Request.Method.GET, url, null, responseListener);

        /* Add your Requests to the RequestQueue to execute */
        AppSingleton.getInstance(InvApplication.getAppContext()).addToRequestQueue(stringInvRequest);

        menuRowItems = new ArrayList<RowItem>();
        for (int i = 0; i < titles.length; i++) {
            menuRowItems.add(new RowItem(images[i], titles[i]));
        }

        CustomListViewAdapter adapter = new CustomListViewAdapter(this, R.layout.edit_pickup_menu, menuRowItems);
        menuRowsListView.setAdapter(adapter);
        menuRowsListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        RowItem selected = menuRowItems.get(position);
        //       if (checkServerResponse(true) == OK) {
        if (selected.getTitle().equalsIgnoreCase(titles[0])) {
            startActivity(CancelPickup.class);

        } else if (selected.getTitle().equalsIgnoreCase(titles[1])) {
            startActivity(ChangePickupDestination.class);

        } else if (selected.getTitle().equalsIgnoreCase(titles[2])) {
            startActivity(ChangePickupOrigin.class);

        } else if (selected.getTitle().equalsIgnoreCase(titles[3])) {
            startActivity(RemovePickupItems.class);

        } else if (selected.getTitle().equalsIgnoreCase(titles[4])) {
            startActivity(EditRemoteStatus.class);
        } else if (selected.getTitle().equalsIgnoreCase(titles[5])) {
            startActivity(Move.class, R.anim.in_left);
        }
        //     }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_menu, menu);
        return true;
    }

    private void startActivity(Class<?> activity) {
        startActivity(activity, R.anim.in_right);
    }

    private void startActivity(Class<?> activity, int inTransition) {
        int outTransition = -1;
        switch (inTransition) {
            case R.anim.in_right:
                outTransition = R.anim.out_left;
                break;
            case R.anim.in_left:
                outTransition = R.anim.out_right;
                break;
            case R.anim.in_down:
                outTransition = R.anim.out_down;
                break;
            case R.anim.in_up:
                outTransition = R.anim.out_up;
                break;
            default:
                inTransition = R.anim.in_right;
                outTransition = R.anim.in_left;
        }
        Intent intent = new Intent(this, activity);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(inTransition, outTransition);
    }

}
