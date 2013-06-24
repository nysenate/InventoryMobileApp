package gov.nysenate.inventory.android;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class DeliveryItemViewAdapter extends ArrayAdapter<DeliveryItem>
{
    Context context;
    List<DeliveryItem> items;
    ViewHolder holder;

    public DeliveryItemViewAdapter(Context context, int textViewResourceId,
            List<DeliveryItem> items) {
        // super(context, textViewResourceId);
        super(context, textViewResourceId, items);
        // this super method calls getView method and passes the list to view

        this.context = context;
        this.items = items;
        System.out.println("DELIVERY LIST ITEMS SIZE:" + items.size());
        Log.i("inside constructor", "array adaptor const");
    }

    /* private view holder class */
    private class ViewHolder
    {

        TextView textView;
        CheckBox checkBox;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        DeliveryItem rowItem = items.get(position);

        Log.i("getView() start", "start At Position:" + position);

        // LayoutInflater mInflater = (LayoutInflater) context
        // .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            Log.i("getView() convertview==null ", "At Position:" + position);
            convertView = mInflater.inflate(R.layout.delivery_row, null);
            holder = new ViewHolder();
            holder.checkBox = (CheckBox) convertView
                    .findViewById(R.id.checkBox1);

            holder.textView = (TextView) convertView
                    .findViewById(R.id.textView1);
            convertView.setTag(holder);
        } else {
            Log.i("getView() convertview != null ", "At Position:" + position);
            holder = (ViewHolder) convertView.getTag();
        }

        /*
         * if (position % 2 > 0) {
         * holder.textView.setBackgroundColor(context.getResources().getColor(
         * R.color.white)); } else {
         * 
         * holder.textView.setBackgroundColor(context.getResources().getColor(
         * R.color.blueveryverylight));
         * 
         * }
         */

        holder.textView.setText(rowItem.text);
        holder.checkBox.setChecked(rowItem.isChecked);

        OnClickListener l = new OnClickListener()
        {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.checkBox1) {
                    Log.i("getView() onClick() ", "At Position:s");
                    holder.checkBox.setChecked(false);
                }

            }
        };
        // holder.speech2Txt.setOnClickListener(l);
        return convertView;
    }

}
