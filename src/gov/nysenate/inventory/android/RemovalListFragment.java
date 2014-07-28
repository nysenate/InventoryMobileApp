package gov.nysenate.inventory.android;

import android.app.Fragment;
import android.os.Bundle;
import android.view.*;
import android.widget.ListView;
import gov.nysenate.inventory.adapter.RemovalRequestListAdapter;
import gov.nysenate.inventory.model.InvItem;
import gov.nysenate.inventory.model.Item;

import java.util.ArrayList;
import java.util.List;

public class RemovalListFragment extends Fragment
{
    private ListView removalList;
    private RemovalRequestListAdapter removalListAdapter;
    private List<Item> items = new ArrayList<Item>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_removal_list, container);
        removalList = (ListView) view.findViewById(R.id.removal_list);

        removalListAdapter = new RemovalRequestListAdapter(getActivity(), R.layout.removal_request_list, R.id.column1, items);
        removalList.setAdapter(removalListAdapter);

        return view;
    }

    public void add(Item item) {
        items.add(item);
        removalListAdapter.notifyDataSetChanged();
    }

    public void setItems(List<Item> removalItems) {
        items.clear();
        items.addAll(removalItems);
        removalListAdapter.notifyDataSetChanged();
    }

    public List<Item> getItems() {
        return items;
    }

}
