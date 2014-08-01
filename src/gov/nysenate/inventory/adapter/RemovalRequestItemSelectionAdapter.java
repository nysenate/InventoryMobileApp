package gov.nysenate.inventory.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.model.Item;
import gov.nysenate.inventory.model.ItemStatus;

import java.util.List;

public class RemovalRequestItemSelectionAdapter extends ArrayAdapter<Item>
{
    private List<Item> items;

    public RemovalRequestItemSelectionAdapter(Context context, int resourceId, int textViewResource, List<Item> items) {
        super(context, resourceId, textViewResource, items);
        this.items = items;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = super.getView(position, convertView, parent);

        TextView column1 = (TextView) row.findViewById(R.id.column1);
        TextView column2 = (TextView) row.findViewById(R.id.column2);
        CheckBox checkbox = (CheckBox) row.findViewById(R.id.check_box);

        if (position % 2 > 0) {
            row.setBackgroundResource(R.drawable.selector_1);
        } else {
            row.setBackgroundResource(R.drawable.selector_2);
        }

        Item item = items.get(position);
        column1.setText(item.getBarcode());
        column2.setText(item.getCommodity().getDescription());

        checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox)v).isChecked()) {
                    items.get(position).setStatus(ItemStatus.INACTIVE);
                } else {
                    items.get(position).setStatus(ItemStatus.ACTIVE);
                }
            }
        });

        return row;
    }
}
