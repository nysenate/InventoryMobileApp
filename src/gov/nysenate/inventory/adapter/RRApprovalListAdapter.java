package gov.nysenate.inventory.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RRApprovalListAdapter extends ArrayAdapter<Item>
{
    List<Item> items;
    HashMap<Item, CheckBox> itemsSelectionStatus = new HashMap<Item, CheckBox>();

    public RRApprovalListAdapter(Context context, int resource, int textViewResource, List<Item> items) {
        super(context, resource, textViewResource, items);
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = super.getView(position, convertView, parent);

        TextView column1 = (TextView) row.findViewById(R.id.column1);
        TextView column2 = (TextView) row.findViewById(R.id.column2);
        CheckBox checkbox = (CheckBox) row.findViewById(R.id.checkbox);

        if (position % 2 > 0) {
            row.setBackgroundResource(R.drawable.selector_1);
        } else {
            row.setBackgroundResource(R.drawable.selector_2);
        }

        Item item = items.get(position);

        column1.setText(item.getBarcode());
        column2.setText(item.getCommodity().getDescription());
        checkbox.setEnabled(false);

        itemsSelectionStatus.put(item, checkbox);

        return row;
    }

    public List<Item> getApprovedItems() {
        List<Item> items = new ArrayList<Item>();
        for (Map.Entry<Item, CheckBox> entry : itemsSelectionStatus.entrySet()) {
            if (entry.getValue().isChecked()) {
                items.add(entry.getKey());
            }
        }

        return items;
    }

    public void approveItem(Item i) {
        itemsSelectionStatus.get(i).setChecked(true);
    }

    public void approveAll() {
        for (Item i : itemsSelectionStatus.keySet()) {
            itemsSelectionStatus.get(i).setChecked(true);
        }
    }

    public void disapproveAll() {
        for (Item i : itemsSelectionStatus.keySet()) {
            itemsSelectionStatus.get(i).setChecked(false);
        }
    }
}
