package gov.nysenate.inventory.model;

import gov.nysenate.inventory.android.R;

import java.util.ArrayList;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class InvSerialAdapter extends ArrayAdapter
{
    private final String MY_DEBUG_TAG = "CustomerAdapter";
    private ArrayList<InvSerialNumber> items;
    private ArrayList<InvSerialNumber> itemsAll;
    private ArrayList<InvSerialNumber> suggestions;
    private int viewResourceId;
    Context context;

    public InvSerialAdapter(Context context, int viewResourceId, ArrayList<InvSerialNumber> items) {
        super(context, viewResourceId, items);
        this.context = context;
        this.items = items;
        this.itemsAll = (ArrayList<InvSerialNumber>) items.clone();
        this.suggestions = new ArrayList<InvSerialNumber>();
        this.viewResourceId = viewResourceId;
    }

    class ViewHolder
    {
        RelativeLayout rlSerialRow;
        // TextView commodityListNucnt;
        TextView tvNuserial;
        TextView tvNusenate;
        TextView tvDecommodityf;
    }         
    
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        ViewHolder holder = null;
        InvSerialNumber rowItem = null;
        if (convertView == null) {
            final LayoutInflater mInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = mInflater.inflate(R.layout.row_serialitem, null);
            holder = new ViewHolder();
            holder.rlSerialRow = (RelativeLayout) convertView
                    .findViewById(R.id.rlSerialRow);
            //
            // holder.commodityListNucnt = (TextView) convertView
            // .findViewById(R.id.commodityListNucnt);
            //
            holder.tvNuserial = (TextView) convertView
                    .findViewById(R.id.tvNuserial);
            holder.tvNusenate = (TextView) convertView
                    .findViewById(R.id.tvNusenate);
            holder.tvDecommodityf = (TextView) convertView
                    .findViewById(R.id.tvDecommodityf);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        
        if (position > -1 && items != null && position < items.size()) {
            rowItem = items.get(position);
            // holder.commodityListNucnt.setText(rowItem.getNucnt());
            holder.tvNusenate.setText(Html.fromHtml("<b>T: "+rowItem.getNusenate()+"</b>"));
            holder.tvDecommodityf.setText(Html.fromHtml(rowItem.getDecommodityf()));
            holder.tvNuserial.setText(Html.fromHtml("<b>S: "+rowItem.getNuserial()+"</b>"));
            holder.tvNusenate.setTextColor(context.getResources()
                    .getColor(R.color.black));
            holder.tvDecommodityf.setTextColor(context.getResources()
                    .getColor(R.color.black));
            holder.tvNuserial.setTextColor(context.getResources()
                    .getColor(R.color.black));
            
        } else {
            // holder.commodityListNucnt.setText("");
            holder.tvNuserial.setText("");
            holder.tvNusenate.setText("");
            holder.tvDecommodityf.setText("");                }

        if (position % 2 > 0) {
            holder.rlSerialRow.setBackgroundColor(context.getResources()
                    .getColor(R.color.white));
        } else {
            holder.rlSerialRow.setBackgroundColor(context.getResources()
                    .getColor(R.color.blueveryverylight));
        }

        return convertView;
    }

    @Override
    public Filter getFilter() {
        return nameFilter;
    }

    Filter nameFilter = new Filter() {
        public String convertResultToString(Object resultValue) {
            String str = ((InvSerialNumber)(resultValue)).toString(); 
            return str;
        }
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if(constraint != null) {
                suggestions.clear();
                for (InvSerialNumber invSerialNumber : itemsAll) {
                    if(invSerialNumber.toString().toLowerCase().startsWith(constraint.toString().toLowerCase())){
                        suggestions.add(invSerialNumber);
                    }
                }
                FilterResults filterResults = new FilterResults();
                synchronized(this) {
                    filterResults.values = suggestions;
                    filterResults.count = suggestions.size();
                }
                return filterResults;
            } else {
                return new FilterResults();
            }
        }
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            ArrayList<InvSerialNumber> filteredList = (ArrayList<InvSerialNumber>) results.values;
            if(results != null && results.count > 0) {
                clear();
                synchronized(this) {                
                    for (InvSerialNumber c : filteredList) {
                    add(c);
                    }
                }
                notifyDataSetChanged();
            }
        }
    };

}
