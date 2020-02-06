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

import gov.nysenate.inventory.adapter.RRListStatusAdapter;
import gov.nysenate.inventory.model.RemovalRequest;

public class RRListStatusFragment extends Fragment {

    public interface RRListStatusFragmentI {
        public List<RemovalRequest> getListReference();

        public void nextActivity(int position);
    }

    private RRListStatusAdapter adapter;
    private RRListStatusFragmentI handler;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof RRListStatusFragmentI) {
            handler = (RRListStatusFragmentI) activity;
        } else {
            throw new ClassCastException(activity.toString() + " must implement "
                    + RRListStatusFragment.class.getName() + "." + RRListStatusFragmentI.class.getName());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.rr_list_status_fragment, container);

        ListView list = (ListView) view.findViewById(R.id.removal_request_list);

        adapter = new RRListStatusAdapter(getActivity(),
                R.layout.rr_list_status_adapter, R.id.column1, handler.getListReference());

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
