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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RemovalRequestItemSelectionAdapter extends ArrayAdapter<Item>
{
    public interface RemovalRequestItemSelectionAdapterI {
        public void itemCheckBoxPressed();
    }

    private List<Item> items;
    private RemovalRequestItemSelectionAdapterI handler;
    RemovalRequestComparer removalRequestComparer = new RemovalRequestComparer();

    public RemovalRequestItemSelectionAdapter(Context context, int resourceId, int textViewResource,
                                              List<Item> items, RemovalRequestItemSelectionAdapterI handler) {
        super(context, resourceId, textViewResource, items);
        this.items = items;
        Collections.sort(items, removalRequestComparer);
        this.handler = handler;
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

        final Item item = items.get(position);
        column1.setText(item.getBarcode());
        column2.setText(item.getCommodity().getDescription());

        if (item.getStatus().equals(ItemStatus.PENDING_REMOVAL)) {
            checkbox.setChecked(false);
        } else {
            checkbox.setChecked(true);
        }

        checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (item.getStatus().equals(ItemStatus.PENDING_REMOVAL)) {
                    item.setStatus(ItemStatus.INACTIVE);
                } else {
                    items.get(position).setStatus(ItemStatus.PENDING_REMOVAL);
                }

                handler.itemCheckBoxPressed();
            }
        });

        return row;
    }
    
    public class RemovalRequestComparer implements Comparator<Item> {
        @Override
        public int compare(Item item1, Item item2) {
            String description1 = "";
            
            try {
                description1 = item1.getCommodity().getDescription();
            }
            catch (Exception e) {
                
            }
            
            String description2 = "";
            try {
                description2 = item2.getCommodity().getDescription();
            }
            catch (Exception e) {
                
            }
            
            int value1 = description1.compareTo(description2);
            if (value1 == 0) {
                int serialNumber1 = 0;
                try {
                    serialNumber1 = Integer.parseInt(item1.getSerialNumber());
                }
                catch (Exception e) {
                    
                }
                
                int serialNumber2 = 0;
                try {
                    serialNumber2 = Integer.parseInt(item2.getSerialNumber());
                }
                catch (Exception e) {
                    
                }
                
                int value2 = serialNumber1 - serialNumber2;
                
                return value2;
            }
            return value1;
        }

      }    
}
