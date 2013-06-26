package gov.nysenate.inventory.android;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class InvListViewAdapter extends ArrayAdapter<InvItem>
{

    Context context;
    List<InvItem> items;

    public InvListViewAdapter(Context context, int resourceId,
            List<InvItem> items) {
        super(context, resourceId, items);
        this.context = context;
        this.items = items;
        System.out.println("INV LIST ITEMS SIZE:" + items.size());
    }

    /* private view holder class */
    private class ViewHolder
    {
        RelativeLayout rlList;
        ImageView speech2Txt;
        TextView invListBarcode;
        TextView invListDescr;
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
            holder.rlList = (RelativeLayout) convertView
                    .findViewById(R.id.rllist);
            holder.speech2Txt = (ImageView) convertView
                    .findViewById(R.id.invListSpeech);
            holder.invListBarcode = (TextView) convertView
                    .findViewById(R.id.invListBarcode);
            holder.invListDescr = (TextView) convertView
                    .findViewById(R.id.invListDescr);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (position % 2 > 0) {
            holder.rlList.setBackgroundColor(context.getResources().getColor(
                    R.color.white));
        } else {
            holder.rlList.setBackgroundColor(context.getResources().getColor(
                    R.color.blueveryverylight));
        }

        if (rowItem.getType().equalsIgnoreCase("NEW")) {
            holder.invListBarcode.setText(rowItem.getNusenate());
            holder.invListBarcode.setTextColor(context.getResources().getColor(
                    R.color.red)); // blue
            holder.invListDescr.setText(rowItem.getDecommodityf());
            holder.invListDescr.setTextColor(context.getResources().getColor(
                    R.color.red)); // blue
        } else if (rowItem.getType().equalsIgnoreCase("EXISTING")) {
            holder.invListBarcode.setText(rowItem.getNusenate());
            holder.invListBarcode.setTextColor(context.getResources().getColor(
                    R.color.black)); // blue
            holder.invListDescr.setText(rowItem.getDecommodityf());
            holder.invListDescr.setTextColor(context.getResources().getColor(
                    R.color.black)); // black
        } else if (rowItem.getType().equalsIgnoreCase("AT DESTINATION")) {
            holder.invListBarcode.setText(rowItem.getNusenate());
            holder.invListBarcode.setTextColor(context.getResources().getColor(
                    R.color.graydark)); // blue
            holder.invListDescr.setText(rowItem.getDecommodityf());
            holder.invListDescr.setTextColor(context.getResources().getColor(
                    R.color.graydark)); // black
        
        } else {
            holder.invListBarcode.setText(rowItem.getNusenate());
            holder.invListBarcode.setTextColor(context.getResources().getColor(
                    R.color.blue)); // blue
            holder.invListDescr.setText(rowItem.getDecommodityf());
            holder.invListDescr.setTextColor(context.getResources().getColor(
                    R.color.blue)); // blue            

        }
        OnClickListener l = new OnClickListener()
        {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.invListSpeech) {

                }

            }
        };

        holder.speech2Txt.setOnClickListener(l);
        return convertView;
    }

    public int removeBarCode(String barcode) {
        int itemsRemoved = 0;
        this.setNotifyOnChange(true);
        if (this.items != null) {
            for (int x = this.items.size() - 1; x > -1; x--) {
                if (this.items.get(x).getNusenate().equals(barcode)) {
                    this.items.remove(x);
                    itemsRemoved++;
                }
            }
        }
        return itemsRemoved;
    }

    public int findTypePos(String type) {
        return findTypePos(type, 0);
    }

    public int findTypePos(String type, int startAt) {
        for (int x = startAt; x < this.items.size(); x++) {
            if (this.items.get(x).getType().equals(type)) {
                return x;
            }
        }
        return -1;
    }
}