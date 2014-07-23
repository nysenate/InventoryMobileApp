package gov.nysenate.inventory.listener;

import gov.nysenate.inventory.android.ClearableTextView;
import android.view.View;
import android.widget.AdapterView;

public interface ClearButtonListener
{
    public void onClearButtonPressed(AdapterView parent, View view);

    public void onClearButtonPressed(ClearableTextView parent, View view);

}
