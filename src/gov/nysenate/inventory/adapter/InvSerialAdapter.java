package gov.nysenate.inventory.adapter;

import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.model.InvSerialNumber;
import gov.nysenate.inventory.util.Toasty;

import java.util.ArrayList;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class InvSerialAdapter extends ArrayAdapter
{
    private final String MY_DEBUG_TAG = "CustomerAdapter";
    private ArrayList<InvSerialNumber> items;
    private ArrayList<InvSerialNumber> itemsAll;
    public ArrayList<InvSerialNumber> suggestions;
    public AutoCompleteTextView acView;
    private boolean setTextColor = false;
    private int viewResourceId;
    Context context;     

    public InvSerialAdapter(Context context, AutoCompleteTextView acView,  int viewResourceId, ArrayList<InvSerialNumber> items) {
        super(context, viewResourceId, items);
        this.context = context;
        this.acView = acView;
        
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
    
    
    public void setTextColor(boolean setTextColor) {
        this.setTextColor = setTextColor;
    }
    
    public boolean getTextColor() {
        return setTextColor;
    }
        
    public int getFilteredCount(String s) {
        int cnt = 0;
        String st = s.toUpperCase();
        if (suggestions!=null) {
            for (int x=0;x<suggestions.size();x++) {
                try {
                if (suggestions.get(x).getNuserial().startsWith(st)) {
                    cnt++;
                }
                }
                catch (IndexOutOfBoundsException iobe) {
                    break;
                }
            }
        }
        //System.out.println ("getFilteredCount:"+s+" = "+cnt);
        return cnt;
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
                    if (acView!=null && setTextColor) {
                        if (filterResults.count==0) {
                            acView.setTextColor(context.getResources().getColor(R.color.redlight));
                        }
                        else {
                            acView.setTextColor(context.getResources().getColor(R.color.black));
                        }
                    }
                }
                return filterResults;
            } else {
                return new FilterResults();
            }
        }
        
     public int getCount(CharSequence constraint) {
         return performFiltering(constraint).count;
     }
        
   @Override
        protected synchronized void publishResults(CharSequence constraint, FilterResults results) {
       try {
            ArrayList<InvSerialNumber> filteredList = ((ArrayList<InvSerialNumber>) results.values);
            //filteredList = (ArrayList<InvSerialNumber>)Collections.synchronizedList(filteredList);
            
            //int listSize = filteredList.size();
            if(results != null && results.count > 0) {
                clear();
                    //InvSerialNumber invSerialNumber;
//                    for (int x=0;x<listSize;x++) {
                      for (InvSerialNumber invSerialNumber: filteredList) {
                        try {
                            //invSerialNumber = filteredList.get(x);
                            add(invSerialNumber);
                        }
                        catch (Exception e) {
                                
                        }
                    }

                notifyDataSetChanged();
            }
       }
       catch (Exception e) {
           Toasty toasty = new Toasty(context, "An unexpected occured on publishing Serial# results:"+e.getMessage()+": "+e.getStackTrace()[0].toString()+" PLEASE CONTACT STSBAC", Toast.LENGTH_LONG);
           toasty.showMessage();
           e.printStackTrace();
           
       }
        }

    };
    
}