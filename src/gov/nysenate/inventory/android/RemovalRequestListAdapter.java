package gov.nysenate.inventory.android;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import gov.nysenate.inventory.model.RemovalRequest;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class RemovalRequestListAdapter extends ArrayAdapter<RemovalRequest>
{

    private List<RemovalRequest> rrs;
    private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy hh:mm:ssa", Locale.US);

    public RemovalRequestListAdapter(Context context, int resource, int textViewResource, List<RemovalRequest> rrs) {
        super(context, resource, textViewResource, rrs);
        this.rrs = rrs;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = super.getView(position, convertView, parent);

        TextView column1 = (TextView) row.findViewById(R.id.column1);
        TextView column2 = (TextView) row.findViewById(R.id.column2);
        TextView column3 = (TextView) row.findViewById(R.id.column3);

        if (position % 2 > 0) {
            row.setBackgroundResource(R.drawable.selector_1);
        } else {
            row.setBackgroundResource(R.drawable.selector_2);
        }

        RemovalRequest rr = rrs.get(position);

        column1.setText(String.valueOf(rr.getTransactionNum()));
        column2.setText(sdf.format(rr.getDate()));
        column3.setText(String.valueOf(rr.getItems().size()));

        return row;
    }
}
