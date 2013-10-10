package gov.nysenate.inventory.util;

import java.util.List;

import gov.nysenate.inventory.android.EditPickup1Activity.SearchByParam;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.model.Pickup;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PickupSearchList extends ArrayAdapter<Pickup> {

    List<Pickup> pickups;
    SearchByParam searchParam;

    public PickupSearchList(Context context, int resource, List<Pickup> pickups, SearchByParam param) {
        super(context, resource, pickups);
        this.pickups = pickups;
        this.searchParam = param;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        
        if (view == null) {
            LayoutInflater inflator;
            inflator = LayoutInflater.from(getContext());
            view = inflator.inflate(R.layout.pickup_search_list , null);
        }
        
        Pickup pickup = pickups.get(position);
        
        if (pickup != null) {
            
            RelativeLayout rlPickupGrpRow = (RelativeLayout) view.findViewById(R.id.relative_layout);
            TextView column1 = (TextView) view.findViewById(R.id.pickup_search_colum1);
            TextView column2 = (TextView) view.findViewById(R.id.pickup_search_colum2);
            TextView column3 = (TextView) view.findViewById(R.id.pickup_search_colum3);
            TextView column4 = (TextView) view.findViewById(R.id.pickup_search_colum4);

            if (position % 2 > 0) {
                rlPickupGrpRow.setBackgroundResource(R.drawable.selector_1);
            } else {
                rlPickupGrpRow.setBackgroundResource(R.drawable.selector_2);
            }

            switch(searchParam) {
            
            case PICKUPLOC:
                column1.setText(pickup.getDate().split("-")[0]);
                column2.setText(pickup.getNaPickupBy());
                column3.setText(pickup.getDestinationSummaryString());
                column4.setText(Integer.toString(pickup.getCount()));
                break;
            case DELIVERYLOC:
                column1.setText(pickup.getDate().split("-")[0]);
                column2.setText(pickup.getNaPickupBy());
                column3.setText(pickup.getOriginSummaryString());
                column4.setText(Integer.toString(pickup.getCount()));
                break;
            case NAPICKUPBY:
                column1.setText(pickup.getDate().split("-")[0]);
                column2.setText(pickup.getOriginSummaryString());
                column3.setText(pickup.getDestinationSummaryString());
                column4.setText(Integer.toString(pickup.getCount()));
                break;
            case DATE:
                column1.setText(pickup.getNaPickupBy());
                column2.setText(pickup.getOriginSummaryString());
                column3.setText(pickup.getDestinationSummaryString());
                column4.setText(Integer.toString(pickup.getCount()));
                break;
            }
        }
        
        return view;
    }

}
