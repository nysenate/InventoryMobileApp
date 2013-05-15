package gov.nysenate.inventory.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public abstract class SenateActivity extends Activity {

    @Override
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
    }    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.in_left, R.anim.out_right);
    }

    
}