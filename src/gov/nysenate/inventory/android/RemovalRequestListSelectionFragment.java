package gov.nysenate.inventory.android;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import gov.nysenate.inventory.adapter.RRApprovalListAdapter;
import gov.nysenate.inventory.model.Item;

import java.util.List;

/**
 * Contains a List view with Removal Request information along with a check box.
 * Used to verify items in a Removal Request.
 */

public class RemovalRequestListSelectionFragment extends Fragment {

    public interface RRListSelectionFragmentI {
        public List<Item> getItemsReference();
    }

    ListView list;
    RRApprovalListAdapter adapter;
    RRListSelectionFragmentI handler;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof RRListSelectionFragmentI) {
            this.handler = (RRListSelectionFragmentI) activity;
        } else {
            throw new ClassCastException(activity.toString() + " must implement RRListSelectionFragmentI");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.removal_request_list_selection_fragment, container);
        list = (ListView) view.findViewById(R.id.removal_request_item_selection_list);

        adapter = new RRApprovalListAdapter(getActivity(), R.layout.rr_approval_list_adapter, R.id.column1, handler.getItemsReference());
        list.setAdapter(adapter);

        return view;
    }

    public List<Item> getApprovedItems() {
        return adapter.getApprovedItems();
    }

    public void approveItem(Item i) {
        adapter.approveItem(i);
    }

    public void approveAll() {
        adapter.approveAll();
    }

    public void disapproveAll() {
        adapter.disapproveAll();
    }

    public void refresh() {
        adapter.notifyDataSetChanged();
    }
}