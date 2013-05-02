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
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Move extends SenateActivity {

    private String res = null;
    public String URL = null;
    TextView pickupDeliveryStats;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_move);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_move, menu);
  /*      URL = MainActivity.properties.get("WEBAPP_BASE_URL").toString();
        String sDtstart = "";
        Date d = new Date();
        try {
            sDtstart = new SimpleDateFormat("MMddyy").format(d);
            System.out.println("sDtstart:"+sDtstart);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        AsyncTask<String, String, String> resr1 = null;        
        pickupDeliveryStats = (TextView) findViewById(R.id.pickupDeliveryStats);
      try {
    System.out.println (URL + "/PickupDeliveryStats?NAUSER="+MainActivity.nauser+"&DTSTART="+sDtstart);
    resr1 = new RequestTask()
    .execute(URL + "/PickupDeliveryStats?NAUSER="+MainActivity.nauser+"&DTSTART="+sDtstart);
    res = resr1.get().trim().toString();
    pickupDeliveryStats.setText(res);
}
catch (Exception e) {
//    pickupDeliveryStats.setText("Pickup/Delivery Stats could not be loaded.");
}
    // code for JSON

        
	    try {
	        res = resr1.get().trim().toString();
	    }
	    catch (Exception e) {
	        e.printStackTrace();
	    }*/
		return true;
	}

/*	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			Toast toast = Toast.makeText(getApplicationContext(), "Going Back",
					Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			NavUtils.navigateUpFromSameTask(this);

			overridePendingTransition(R.anim.in_left, R.anim.out_right);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}*/

	public void pickup(View view) {

		Intent intent = new Intent(this, Pickup1.class);
		startActivity(intent);

	}

	public void delivery(View view) {

		Intent intent = new Intent(this, Delivery1.class);
		startActivity(intent);

	}

}
