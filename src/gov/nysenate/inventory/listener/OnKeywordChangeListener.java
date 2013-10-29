package gov.nysenate.inventory.listener;

import gov.nysenate.inventory.android.KeywordDialog;
import android.widget.ListView;

public interface OnKeywordChangeListener
{
    public void OnKeywordChange(KeywordDialog keywordDialog,
            ListView lvKeywords, String keywords);
}
