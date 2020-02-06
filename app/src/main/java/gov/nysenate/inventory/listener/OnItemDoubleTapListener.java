package gov.nysenate.inventory.listener;

import android.view.View;
import android.widget.AdapterView;

public interface OnItemDoubleTapListener {
    public void OnDoubleTap(AdapterView parent, View view, int position, long id);

    public void OnSingleTap(AdapterView parent, View view, int position, long id);
}