package gov.nysenate.inventory.android;

import android.app.Fragment;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.*;
import android.widget.AbsListView;
import android.widget.ListView;
import gov.nysenate.inventory.adapter.RemovalRequestListAdapter;
import gov.nysenate.inventory.model.InvItem;

import java.util.ArrayList;
import java.util.List;

public class RemovalListFragment extends Fragment
{
    private ListView removalList;
    private RemovalRequestListAdapter removalListAdapter;
    private List<InvItem> items = new ArrayList<InvItem>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_removal_list, container);
        removalList = (ListView) view.findViewById(R.id.removal_list);

        removalListAdapter = new RemovalRequestListAdapter(getActivity(), R.layout.removal_request_list, R.id.column1, items);
        removalList.setAdapter(removalListAdapter);

        setupActionMode();

        return view;
    }

    private void setupActionMode() {
        removalList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        removalList.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.context_menu, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete:
                        deleteSelection();
                        mode.finish();
                        return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        });
    }

    // TODO: update the activity.removalReqeust with the items deleted.... and Server!
    private void deleteSelection() {
        SparseBooleanArray boolArray = removalList.getCheckedItemPositions();
        for (int i = 0; i < boolArray.size(); i ++) {
            if (boolArray.get(i) == true) {
                items.remove(i);
            }
        }
        removalListAdapter.notifyDataSetChanged();
    }

    public void add(InvItem item) {
        items.add(item);
        removalListAdapter.notifyDataSetChanged();
    }

    public void setItems(List<InvItem> removalItems) {
        items.clear();
        items.addAll(removalItems);
        removalListAdapter.notifyDataSetChanged();
    }

    public List<InvItem> getItems() {
        return items;
    }

}
