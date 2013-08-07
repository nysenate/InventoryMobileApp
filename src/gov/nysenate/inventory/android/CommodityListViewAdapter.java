package gov.nysenate.inventory.android;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CommodityListViewAdapter extends ArrayAdapter<Commodity> implements OnItemDoubleTapListener
{

    Context context;
    List<Commodity> items;
    int rowSelected = -1;

    public CommodityListViewAdapter(Context context, int resourceId,
            List<Commodity> items) {
        super(context, resourceId, items);
        this.context = context;
        this.items = items;
        //System.out.println("COMMODITY LIST ITEMS SIZE:" + items.size());
    }

    /* private view holder class */
    private class ViewHolder
    {
        RelativeLayout rlcomlist;
        TextView commodityListNucnt;
        TextView commodityListCdcommodity;
        TextView commodityListDecommodityf;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        Commodity rowItem = items.get(position);

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.commoditylist_row, null);
            holder = new ViewHolder();
            holder.rlcomlist = (RelativeLayout) convertView
                    .findViewById(R.id.rlcomlist);
            holder.commodityListNucnt = (TextView) convertView
                    .findViewById(R.id.commodityListNucnt);
            holder.commodityListCdcommodity = (TextView) convertView
                    .findViewById(R.id.commodityListCdcommodity);
            holder.commodityListDecommodityf = (TextView) convertView
                    .findViewById(R.id.commodityListDecommodityf);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.commodityListNucnt.setText(rowItem.getNucnt());
        holder.commodityListCdcommodity.setText(rowItem.getCdcommodty());
        holder.commodityListDecommodityf.setText(rowItem.getDecommodityf());        

        if (position==this.rowSelected) {
            holder.rlcomlist.setBackgroundColor(context.getResources().getColor(
                    R.color.yellow));
        } else if (position % 2 > 0) {
            holder.rlcomlist.setBackgroundColor(context.getResources().getColor(
                    R.color.white));
        } else {
            holder.rlcomlist.setBackgroundColor(context.getResources().getColor(
                    R.color.blueveryverylight));
        }

        OnClickListener l = new OnClickListener()
        {
            @Override
            public void onClick(View v) {
                 

            }
        };

        return convertView;
    }
    
    public Commodity getCommodityAt(int y) {
        return items.get(y);
    }

    public int removeCommodityCode(String cdcommodity) {
        int itemsRemoved = 0;
        this.setNotifyOnChange(true);
        if (this.items != null) {
            for (int x = this.items.size() - 1; x > -1; x--) {
                if (this.items.get(x).getCdcommodty().equals(cdcommodity)) {
                    this.items.remove(x);
                    itemsRemoved++;
                }
            }
        }
        if (itemsRemoved>0) {
            this.notifyDataSetChanged();
        }
        return itemsRemoved;
    }

   public int wordRowCount(String word) {
      int rowCount = 0;
      word = word.toUpperCase();
      for (int x=0;x< this.items.size();x++) {
          if (items.get(x).getDecommodityf().toUpperCase().indexOf(word)>-1) {
              rowCount++;
          }
      }
      return rowCount;
   }
   
   public void unselectRow() {
       setRowSelected(-1);
   }
   
   public void setRowSelected(int rowSelected) {
       this.rowSelected = rowSelected;
       this.notifyDataSetChanged();
   }
   
   public int getRowSelected() {
       return this.rowSelected;
   }
   
   @Override
   public void OnDoubleTap(AdapterView parent, View view, int position, long id) {
        System.out.println("Double Clicked on "+position+": "+items.get(position).getDecommodityf());
   }

   @Override
   public void OnSingleTap(AdapterView parent, View view, int position, long id) {
       // Do nothing on Single Tap (for now)
   }
  

}