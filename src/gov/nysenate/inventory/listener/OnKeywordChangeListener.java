package gov.nysenate.inventory.listener;

import android.widget.ListView;
import gov.nysenate.inventory.android.KeywordDialog;

public interface OnKeywordChangeListener
{
    public void OnKeywordChange(KeywordDialog keywordDialog,
            ListView lvKeywords, String keywords);
}
