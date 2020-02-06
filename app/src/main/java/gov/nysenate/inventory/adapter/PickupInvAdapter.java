package gov.nysenate.inventory.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.model.InvItem;

public class PickupInvAdapter extends ArrayAdapter<InvItem> {

    Context context;
    List<InvItem> items;

    public PickupInvAdapter(Context context, int resourceId, List<InvItem> items) {
        super(context, resourceId, items);
        this.context = context;
        this.items = items;
    }

    /* private view holder class */
    private class ViewHolder {
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

        if (rowItem.getType().toUpperCase().startsWith("LOCATED AT:")) {
            holder.invListBarcode.setText(rowItem.getNusenate());
            holder.invListBarcode.setTextColor(context.getResources().getColor(
                    R.color.blue)); // blue
            holder.invListDescr.setText(rowItem.getDecommodityf());
            holder.invListDescr.setTextColor(context.getResources().getColor(
                    R.color.blue)); // blue
        } else if (rowItem.getType().equalsIgnoreCase("NOT IN SFMS")) {
            holder.invListBarcode.setText(rowItem.getNusenate());
            holder.invListBarcode.setTextColor(context.getResources().getColor(
                    R.color.red)); // red
            holder.invListDescr.setText(rowItem.getDecommodityf());
            holder.invListDescr.setTextColor(context.getResources().getColor(
                    R.color.red)); // red
        } else if (rowItem.getType().equalsIgnoreCase("AT DESTINATION")) {
            holder.invListBarcode.setText(rowItem.getNusenate());
            holder.invListBarcode.setTextColor(context.getResources().getColor(
                    R.color.green)); // green
            holder.invListDescr.setText(rowItem.getDecommodityf());
            holder.invListDescr.setTextColor(context.getResources().getColor(
                    R.color.green)); // green
        } else {
            holder.invListBarcode.setText(rowItem.getNusenate());
            holder.invListBarcode.setTextColor(context.getResources().getColor(
                    R.color.black)); // blue
            holder.invListDescr.setText(rowItem.getDecommodityf());
            holder.invListDescr.setTextColor(context.getResources().getColor(
                    R.color.black)); // black

        }
        OnClickListener l = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.invListSpeech) {

                }

            }
        };

        holder.speech2Txt.setOnClickListener(l);
        return convertView;
    }

}