package gov.nysenate.inventory.android;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
 
public class InvListViewAdapter extends ArrayAdapter<InvItem> {
 
    Context context;
    List<InvItem> items;
 
    public InvListViewAdapter(Context context, int resourceId,
            List<InvItem> items) {
        super(context, resourceId, items);
        this.context = context;
        this.items = items;
        System.out.println("INV LIST ITEMS SIZE:"+items.size());
    }
 
    /*private view holder class*/
    private class ViewHolder {
    	
    	TextView decommodityf;
    }
 
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        InvItem rowItem = items.get(position);
        
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.invlist_item, null);
            holder = new ViewHolder();
            holder.decommodityf = (TextView) convertView.findViewById(R.id.invListItem);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            }
        
        if (position%2>0) {
        	holder.decommodityf.setBackgroundColor(context.getResources().getColor(R.color.white));
        }
        else {
        	holder.decommodityf.setBackgroundColor(context.getResources().getColor(R.color.blueveryverylight));
        }
 
        if (rowItem.getType().equalsIgnoreCase("NEW")) {
            holder.decommodityf.setText(rowItem.getNusenate()+" "+rowItem.getDecommodityf());
        	holder.decommodityf.setTextColor( context.getResources().getColor(R.color.blue) ); //blue
        }
        else {
            holder.decommodityf.setText(rowItem.getNusenate()+" "+rowItem.getDecommodityf());
        	holder.decommodityf.setTextColor( context.getResources().getColor(R.color.black) ); //black
        	
        }
 
        return convertView;
    }
}