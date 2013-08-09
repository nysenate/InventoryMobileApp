package gov.nysenate.inventory.android;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public interface OnKeywordChangeListener
{
       public void OnKeywordChange(KeywordDialog keywordDialog, ListView lvKeywords, String keywords);
}
