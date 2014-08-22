package gov.nysenate.inventory.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import gov.nysenate.inventory.adapter.CustomListViewAdapter;
import gov.nysenate.inventory.android.InvApplication;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.model.RowItem;
import gov.nysenate.inventory.util.Toasty;

import java.util.ArrayList;
import java.util.List;

public class InventoryRemovalMenu extends SenateActivity implements OnItemClickListener
{

    private ProgressBar progressBar;
    private List<RowItem> menuItems = new ArrayList<RowItem>();

    // TODO: more effective implemetation of security features
    private static final String[] TITLES_SEC_STATUS_0 = new String[]
            {"New Request", "Edit/Submit Request", "Main Menu"};
    private static final String[] TITLES_SEC_STATUS_1 = new String[]
            {"New Request", "Edit/Submit Request", "Verify Request", "Main Menu"};

    private static final Integer[] IMAGES_SEC_STATUS_0 =
            {R.drawable.removalrequest, R.drawable.editremovalrequest, R.drawable.mainmenu};
    private static final Integer[] IMAGES_SEC_STATUS_1 =
            {R.drawable.removalrequest, R.drawable.editremovalrequest, R.drawable.arrow, R.drawable.mainmenu};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_removal_menu);
        registerBaseActivityReceiver();

        int securityLevel = ((InvApplication)getApplicationContext()).getCdseclevel();
        menuItems = populateMenuBySecurityStatus(securityLevel);

        ListView menu = (ListView) findViewById(R.id.removal_menu);
        CustomListViewAdapter adapter = new CustomListViewAdapter(this, R.layout.list_item, menuItems);
        menu.setAdapter(adapter);
        menu.setOnItemClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkServerResponse(true);
    }

    private List<RowItem> populateMenuBySecurityStatus(int securityLevel) {
        List<RowItem> items = new ArrayList<RowItem>();
        String[] titles = null;
        Integer[] images = null;
        if (securityLevel == 0) {
            titles = TITLES_SEC_STATUS_0;
            images = IMAGES_SEC_STATUS_0;
        } else if (securityLevel == 1) {
            titles = TITLES_SEC_STATUS_1;
            images = IMAGES_SEC_STATUS_1;
        }

        for (int i = 0; i < titles.length; i++) {
            RowItem item = new RowItem(images[i], titles[i]);
            items.add(item);
        }

        return items;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (checkServerResponse() != OK) {
            return;
        }
        Intent intent;
        String title = menuItems.get(position).getTitle();
        if (title.equalsIgnoreCase("New Request")) {
            intent = new Intent(this, EnterRemovalRequestActivity.class);
        } else if (title.equalsIgnoreCase("Edit/Submit Request")) {
            intent = new Intent(this, EditRemovalRequestSelection.class);
        } else if (title.equalsIgnoreCase("Verify Request")) {
            intent = new Intent(this, ApproveRemovalRequestSelection.class);
        } else {
            onBackPressed();
            return;
        }

        startActivity(intent);
        overridePendingTransition(R.anim.in_right, R.anim.out_left);
    }
}