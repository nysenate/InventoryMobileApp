package gov.nysenate.inventory.adapter;

import java.util.List;

import gov.nysenate.inventory.activity.EditPickup1Activity.SearchByParam;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.model.Transaction;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PickupSearchList extends ArrayAdapter<Transaction> {

    List<Transaction> transactions;
    SearchByParam searchParam;

    public PickupSearchList(Context context, int resource, List<Transaction> trans, SearchByParam param) {
        super(context, resource, trans);
        this.transactions = trans;
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

        Transaction tran = transactions.get(position);

        if (tran != null) {

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
                column1.setText(tran.getPickupDate().split("-")[0]);
                column2.setText(tran.getNapickupby());
                column3.setText(tran.getDestinationSummaryString());
                column4.setText(Integer.toString(tran.getCount()));
                break;
            case DELIVERYLOC:
                column1.setText(tran.getPickupDate().split("-")[0]);
                column2.setText(tran.getNapickupby());
                column3.setText(tran.getOriginSummaryString());
                column4.setText(Integer.toString(tran.getCount()));
                break;
            case NAPICKUPBY:
                column1.setText(tran.getPickupDate().split("-")[0]);
                column2.setText(tran.getOriginSummaryString());
                column3.setText(tran.getDestinationSummaryString());
                column4.setText(Integer.toString(tran.getCount()));
                break;
            case DATE:
                column1.setText(tran.getNapickupby());
                column2.setText(tran.getOriginSummaryString());
                column3.setText(tran.getDestinationSummaryString());
                column4.setText(Integer.toString(tran.getCount()));
                break;
            }
        }
        
        return view;
    }

}
