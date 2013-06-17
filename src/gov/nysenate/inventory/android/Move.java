package gov.nysenate.inventory.android;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Move extends SenateActivity implements OnItemClickListener {

    private String res = null;
    public String URL = null;
    TextView pickupDeliveryStats;

	private ListView mList;
    
	public static final String[] titles = new String[] { "Pickup",
			"Delivery", "Main Menu"};/*, "Pickup/Deliver Log"*/

	public static final String[] descriptions = new String[] {
			"Pickup items from initial location",
			"Deliver items that have been picked up to a new location",
			"Return to the Main Menu"}; /*,
			"Show History on Pickup/Delivered items"*/

	public static final Integer[] images = { R.drawable.pickup3,
			R.drawable.delivery2, R.drawable.mainmenu}; /*, R.drawable.log*/

	ListView listView;
	List<RowItem> rowItems;    
	
	public static ProgressBar progBarMove;
    
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_move);
		progBarMove = (ProgressBar) this.findViewById(R.id.progBarMove);
		rowItems = new ArrayList<RowItem>();
		for (int i = 0; i < titles.length; i++) {
			RowItem item = new RowItem(images[i], titles[i]);
			rowItems.add(item);
		}

		listView = (ListView) findViewById(R.id.moveMenu2);
		CustomListViewAdapter adapter = new CustomListViewAdapter(this,
				R.layout.list_item, rowItems);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		RowItem curRow = rowItems.get(position);
        progBarMove.setVisibility(ProgressBar.VISIBLE);
		if (curRow.getTitle().equalsIgnoreCase("Pickup")) {
			this.pickup(view);
		} else if (curRow.getTitle().equalsIgnoreCase("Delivery")) {
			this.delivery(view);
		} else if (curRow.getTitle().equalsIgnoreCase("Pickup/Deliver Log")) {
				// TODO
		} else if (curRow.getTitle().equalsIgnoreCase("Main Menu")) {
			this.onBackPressed();
	        progBarMove.setVisibility(ProgressBar.INVISIBLE);
		}

		/*
		 * Toast toast = Toast.makeText(getApplicationContext(), "TEST Item " +
		 * (position + 1) + ": " + rowItems.get(position), Toast.LENGTH_SHORT);
		 * toast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
		 * toast.show();
		 */
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_move, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_move_useraction:
			Toast toast = Toast.makeText(getApplicationContext(), "Show User Log",
					Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			Intent intent = new Intent(this, UserActionActivity.class);
			startActivity(intent);
            overridePendingTransition(R.anim.in_up, R.anim.out_up);
			//NavUtils.navigateUpFromSameTask(this);

			//overridePendingTransition(R.anim.in_left, R.anim.out_right);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void pickup(View view) {

		Intent intent = new Intent(this, Pickup1.class);
		startActivity(intent);
		overridePendingTransition(R.anim.in_right, R.anim.out_left);
		

	}

	public void delivery(View view) {

		Intent intent = new Intent(this, Delivery1.class);
		startActivity(intent);
		overridePendingTransition(R.anim.in_right, R.anim.out_left);

	}
	
	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this, MenuActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.in_left, R.anim.out_right);
		
	}	

}
