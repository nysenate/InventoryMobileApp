package gov.nysenate.inventory.android;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;

public class UserActionActivity extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_action);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.user_action, menu);
        return true;
    }
    
    public void close(View view) {
    	this.finish();
        overridePendingTransition(R.anim.in_down, R.anim.out_down);    	
    }

}
