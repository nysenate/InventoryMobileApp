package gov.nysenate.inventory.listener;

import android.view.View;
import android.widget.AdapterView;
import gov.nysenate.inventory.android.ClearableTextView;

public interface ClearButtonListener
{
    public void onClearButtonPressed(AdapterView parent, View view);

    public void onClearButtonPressed(ClearableTextView parent, View view);

}
