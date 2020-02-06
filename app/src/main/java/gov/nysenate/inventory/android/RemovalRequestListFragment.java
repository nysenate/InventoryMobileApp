package gov.nysenate.inventory.android;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import gov.nysenate.inventory.model.RemovalRequest;

public class RemovalRequestListFragment extends Fragment {

    public interface RemovalRequestListFragmentI {
        public List<RemovalRequest> getListReference();

        public void nextActivity(int position);
    }

    private RemovalRequestListAdapter adapter;
    private RemovalRequestListFragmentI handler;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof RemovalRequestListFragmentI) {
            handler = (RemovalRequestListFragmentI) activity;
        } else {
            throw new ClassCastException(activity.toString() + " must implement RemovalRequestListFragment.RemovalRequestListFragmentI");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.removal_request_list_fragment, container);

        ListView list = (ListView) view.findViewById(R.id.removal_request_list);

        adapter = new RemovalRequestListAdapter(getActivity(),
                R.layout.removal_request_approval_list_adapter, R.id.column1, handler.getListReference());

        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                handler.nextActivity(position);
            }
        });

        return view;
    }

    public void refreshDisplay() {
        adapter.notifyDataSetChanged();
    }
}