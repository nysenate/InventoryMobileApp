package gov.nysenate.inventory.adapter.verification;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.model.InvItem;

import java.util.ArrayList;
import java.util.List;

public class EditVerificationAdapter extends ArrayAdapter<InvItem> {

    private List<InvItem> items;
    private List<InvItem> selectedItems;

    public EditVerificationAdapter(Context context, int resourceId, int textViewId, List<InvItem> items) {
        super(context, resourceId, textViewId, items);
        this.items = items;
        selectedItems = new ArrayList<>();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = super.getView(position, convertView, parent);

        TextView column1 = (TextView) row.findViewById(R.id.column1);
        TextView column2 = (TextView) row.findViewById(R.id.column2);
        final CheckBox checkbox = (CheckBox) row.findViewById(R.id.check_box);

        if (position % 2 > 0) {
            row.setBackgroundResource(R.drawable.selector_1);
        } else {
            row.setBackgroundResource(R.drawable.selector_2);
        }

        final InvItem rowItem = items.get(position);
        column1.setText(rowItem.getNusenate());
        column2.setText(rowItem.getDecommodityf());

        if (selectedItems.contains(rowItem)) {
            checkbox.setChecked(true);
        }
        else {
            checkbox.setChecked(false);
        }

        checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkbox.isChecked()) {
                    selectedItems.add(rowItem);
                }
                else {
                    selectedItems.remove(rowItem);
                }
            }
        });

        // Color code items by new/found/inactive etc status's
        // Keeping same colors/logic of InvListViewAdapter.java
        if (rowItem.getType().equalsIgnoreCase("NEW") || rowItem.getType().equalsIgnoreCase("INACTIVE")) {
            setTextViewsColor(row.getResources().getColor(R.color.red), column1, column2);
        }
        else if (rowItem.getType().equalsIgnoreCase("EXISTING")) {
            setTextViewsColor(row.getResources().getColor(R.color.black), column1, column2);
        }
        else if (rowItem.getType().equalsIgnoreCase("AT DESTINATION")) {
            setTextViewsColor(row.getResources().getColor(R.color.graydark), column1, column2);
        }
        else {
            setTextViewsColor(row.getResources().getColor(R.color.blue), column1, column2);
        }
        return row;
    }

    private void setTextViewsColor(int color, TextView... textViews) {
        for (TextView view : textViews) {
            view.setTextColor(color);
        }
    }

    public List<InvItem> getSelectedItems() {
        return selectedItems;
    }
}
