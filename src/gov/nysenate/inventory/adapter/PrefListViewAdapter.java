package gov.nysenate.inventory.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.model.InvItem;

import java.util.List;

public class PrefListViewAdapter extends ArrayAdapter<InvItem>
{

    Context context;
    List<InvItem> items;

    public PrefListViewAdapter(Context context, int resourceId,
            List<InvItem> items) {
        super(context, resourceId, items);
        this.context = context;
        this.items = items;
    }

    /* private view holder class */
    private class ViewHolder
    {
        TextView decommodityf;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        InvItem rowItem = items.get(position);

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.invlist_item, null);
            holder = new ViewHolder();
            holder.decommodityf = (TextView) convertView
                    .findViewById(R.id.invListDescr);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (position % 2 > 0) {
            holder.decommodityf.setBackgroundColor(context.getResources()
                    .getColor(R.color.white));
        } else {
            holder.decommodityf.setBackgroundColor(context.getResources()
                    .getColor(R.color.blueveryverylight));
        }

        holder.decommodityf.setText(rowItem.getDecommodityf());
        if (rowItem.getType().equalsIgnoreCase("NEW")) {
            holder.decommodityf.setTextColor(context.getResources().getColor(
                    R.color.blue)); // blue
        } else {
            holder.decommodityf.setTextColor(context.getResources().getColor(
                    R.color.black)); // black

        }

        return convertView;
    }
}