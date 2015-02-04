package gov.nysenate.inventory.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import gov.nysenate.inventory.adapter.CustomListViewAdapter;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.model.RowItem;

import java.util.ArrayList;
import java.util.List;

public class Move extends SenateActivity implements OnItemClickListener {
    private static final String[] titles = new String[]{"New Pickup", "Edit Existing Pickup", "Delivery", "Enter Remote Info",
            "Main Menu"};
    private static final Integer[] images = {R.drawable.pickup, R.drawable.editpickup,
            R.drawable.delivery2, R.drawable.enterremote, R.drawable.mainmenu};

    private List<RowItem> rowItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_move);
        registerBaseActivityReceiver();

        rowItems = new ArrayList<RowItem>();
        for (int i = 0; i < titles.length; i++) {
            RowItem item = new RowItem(images[i], titles[i]);
            rowItems.add(item);
        }

        ListView listView = (ListView) findViewById(R.id.moveMenu2);
        CustomListViewAdapter adapter = new CustomListViewAdapter(this, R.layout.list_item, rowItems);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        if (Delivery3.progBarDelivery3 != null) {
            Delivery3.progBarDelivery3.setVisibility(View.INVISIBLE);
        }
        if (Pickup3.progBarPickup3 != null) {
            Pickup3.progBarPickup3.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Intent intent;
        RowItem curRow = rowItems.get(position);
        if (curRow.getTitle().equalsIgnoreCase("New Pickup")) {
            intent = pickup();
        } else if (curRow.getTitle().equalsIgnoreCase("Delivery")) {
            intent = delivery();
        } else if (curRow.getTitle().equalsIgnoreCase("Edit Existing Pickup")) {
            intent = editPickup();
        } else if (curRow.getTitle().equalsIgnoreCase("Enter Remote Info")) {
            intent = enterRemote();
        } else {
            onBackPressed();
            return;
        }

        startActivity(intent);
        overridePendingTransition(R.anim.in_right, R.anim.out_left);
    }

    public Intent pickup() {
        return new Intent(this, Pickup1.class);
    }

    public Intent delivery() {
        return new Intent(this, Delivery1.class);
    }

    public Intent editPickup() {
        return new Intent(this, EditPickup1Activity.class);
    }

    private Intent enterRemote() {
        return new Intent(this, EnterRemote1.class);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_move, menu);
        return true;
    }

}
