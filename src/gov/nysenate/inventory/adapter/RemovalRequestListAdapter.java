package gov.nysenate.inventory.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.model.InvItem;
import gov.nysenate.inventory.removalrequest.RemovalRequest;

import java.util.List;

public class RemovalRequestListAdapter extends ArrayAdapter<InvItem>
{

    private List<InvItem> items;
    private int resource;

    public RemovalRequestListAdapter(Context context, int resource, int textViewResource, List<InvItem> items) {
        super(context, resource, textViewResource, items);
        this.resource = resource;
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = super.getView(position, convertView, parent);

        TextView column1 = (TextView) row.findViewById(R.id.column1);
        TextView column2 = (TextView) row.findViewById(R.id.column2);

        if (position % 2 > 0) {
            row.setBackgroundResource(R.drawable.selector_1);
        } else {
            row.setBackgroundResource(R.drawable.selector_2);
        }

        InvItem item = items.get(position);

        column1.setText(item.getNusenate());
        column2.setText(item.getDecommodityf());

        return row;
    }
}
