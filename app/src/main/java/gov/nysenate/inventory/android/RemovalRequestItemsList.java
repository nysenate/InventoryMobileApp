package gov.nysenate.inventory.android;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import gov.nysenate.inventory.adapter.RemovalRequestItemListAdapter;
import gov.nysenate.inventory.model.Item;

public class RemovalRequestItemsList extends Fragment {
    private ListView removalList;
    private RemovalRequestItemListAdapter removalListAdapter;
    private List<Item> items = new ArrayList<Item>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_removal_item_list, container);
        removalList = (ListView) view.findViewById(R.id.removal_item_list);

        removalListAdapter = new RemovalRequestItemListAdapter(getActivity(), R.layout.removal_request_item_list_adapter, R.id.column1, items);
        removalList.setAdapter(removalListAdapter);

        return view;
    }

    public void addItem(Item item) {
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
