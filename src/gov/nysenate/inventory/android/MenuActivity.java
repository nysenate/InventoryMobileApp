package gov.nysenate.inventory.android;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MenuActivity extends Activity implements OnItemClickListener {
	public static final String[] titles = new String[] { "Search",
			"Verification", "Move Items", "Logout" };

	public static final String[] descriptions = new String[] {
			"Scan an item and show information",
			"Perform Inventory Verification for a Senate Location",
			"Move Items from one location to another", "Logout of this UserID" };

	public static final Integer[] images = { R.drawable.ssearch,
			R.drawable.sverify, R.drawable.smove, R.drawable.slogout };

	ListView listView;
	List<RowItem> rowItems;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);
		rowItems = new ArrayList<RowItem>();
		for (int i = 0; i < titles.length; i++) {
			RowItem item = new RowItem(images[i], titles[i]);
			rowItems.add(item);
		}

		listView = (ListView) findViewById(R.id.list);
		CustomListViewAdapter adapter = new CustomListViewAdapter(this,
				R.layout.list_item, rowItems);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		RowItem curRow = rowItems.get(position);
		if (curRow.getTitle().equalsIgnoreCase("Search")) {
			this.search(view);
		} else if (curRow.getTitle().equalsIgnoreCase("Verification")) {
			this.verify(view);
		} else if (curRow.getTitle().equalsIgnoreCase("Move Items")) {
			this.addItem(view);
		} else if (curRow.getTitle().equalsIgnoreCase("Logout")) {
			this.logout(view);
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
		getMenuInflater().inflate(R.menu.activity_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			final Activity currentActivity = this;
			// 1. Instantiate an AlertDialog.Builder with its constructor
			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			// 2. Chain together various setter methods to set the dialog
			// characteristics
			builder.setMessage("Do you really want to log out?").setTitle(
					"Log out");
			// Add the buttons
			builder.setPositiveButton("Yes",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							backToParent();
						}
					});
			builder.setNegativeButton("No",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							// User cancelled the dialog
						}
					});

			// 3. Get the AlertDialog from create()
			AlertDialog dialog = builder.create();
			dialog.show();

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void backToParent() {
		Toast toast = Toast.makeText(getApplicationContext(), "Logging Out",
				Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
		NavUtils.navigateUpFromSameTask(this);
		finish();

		overridePendingTransition(R.anim.in_left, R.anim.out_right);
	}

	public void backButtonPressed() {
		super.onBackPressed();
		finish();
		overridePendingTransition(R.anim.in_left, R.anim.out_right);
	}

	public void search(View view) {

		Intent intent = new Intent(this, SearchActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.in_right, R.anim.out_left);
		// overridePendingTransition(R.anim.slide_in_left,
		// R.anim.slide_out_left);

	}

	public void addItem(View view) {
		Intent intent = new Intent(this, Move.class);
		startActivity(intent);
		overridePendingTransition(R.anim.in_right, R.anim.out_left);

		// overridePendingTransition(R.anim.slide_in_left,
		// R.anim.slide_out_left);
	}

	public void verify(View view) {
		Intent intent = new Intent(this, Verification.class);
		startActivity(intent);
		overridePendingTransition(R.anim.in_right, R.anim.out_left);
		// overridePendingTransition(R.anim.slide_in_left,
		// R.anim.slide_out_left);

	}

	public void location(View view) {
		Intent intent = new Intent(this, LocationActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.in_right, R.anim.out_left);

		// overridePendingTransition(R.anim.slide_in_left,
		// R.anim.slide_out_left);
		// we are passing the intent to the activity again ,
		// see instead if we can restart the activity to
		// avoid the data being passed to the login activity
	}

	public void logout(View view) {
		final Intent intent = new Intent(this, MainActivity.class);

		// 1. Instantiate an AlertDialog.Builder with its constructor
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		// 2. Chain together various setter methods to set the dialog
		// characteristics
		builder.setMessage("Do you really want to log out?")
				.setTitle("Log out");
		// Add the buttons
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				intent.setFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
				overridePendingTransition(R.anim.in_left, R.anim.out_right);
				Toast toast = Toast.makeText(getApplicationContext(),
						"logging out", Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			}
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// User cancelled the dialog
			}
		});

		// 3. Get the AlertDialog from create()
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	@Override
	public void onBackPressed() {
		final Activity currentActivity = this;
		// 1. Instantiate an AlertDialog.Builder with its constructor
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		// 2. Chain together various setter methods to set the dialog
		// characteristics
		builder.setMessage("Do you really want to log out?")
				.setTitle("Log out");
		// Add the buttons
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				backToParent();
			}
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// User cancelled the dialog
			}
		});

		// 3. Get the AlertDialog from create()
		AlertDialog dialog = builder.create();
		dialog.show();

	}

}