package gov.nysenate.inventory.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.listener.OnItemDoubleTapListener;
import gov.nysenate.inventory.model.Commodity;

public class CommodityListViewAdapter extends ArrayAdapter<Commodity> implements
        OnItemDoubleTapListener {

    Context context;
    List<Commodity> items;
    String[] keywords = null;
    int rowSelected = -1;

    public CommodityListViewAdapter(Context context, int resourceId,
                                    List<Commodity> items, String[] keywords) {
        super(context, resourceId, items);
        this.context = context;
        this.keywords = keywords;
        this.items = formatCommodityList((ArrayList<Commodity>) items);
    }

    /* private view holder class */
    private class ViewHolder {
        RelativeLayout rlcomlist;
        TextView commodityListCdcommodity;
        TextView commodityListDecommodityf;
    }

    public ArrayList<Commodity> formatCommodityList(
            ArrayList<Commodity> commodityList) {

        for (int x = 0; x < commodityList.size(); x++) {
            commodityList.get(x).setDescription(
                    this.returnFormated(commodityList.get(x).getDescription(),
                            keywords));
        }
        return commodityList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        Commodity rowItem = null;
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = mInflater.inflate(R.layout.commoditylist_row, null);
            holder = new ViewHolder();
            holder.rlcomlist = (RelativeLayout) convertView
                    .findViewById(R.id.rlcomlist);
            holder.commodityListCdcommodity = (TextView) convertView
                    .findViewById(R.id.commodityListCdcommodity);
            holder.commodityListDecommodityf = (TextView) convertView
                    .findViewById(R.id.commodityListDecommodityf);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (position > -1 && items != null && position < items.size()) {
            rowItem = items.get(position);

            holder.commodityListCdcommodity.setText(rowItem.getCode());
            holder.commodityListDecommodityf.setText(Html.fromHtml(rowItem
                    .getDescription()));
        } else {
            holder.commodityListCdcommodity.setText("");
            holder.commodityListDecommodityf.setText("");
        }

        if (position == this.rowSelected) {
            holder.rlcomlist.setBackgroundColor(context.getResources()
                    .getColor(R.color.yellow));
        } else if (position % 2 > 0) {
            holder.rlcomlist.setBackgroundColor(context.getResources()
                    .getColor(R.color.white));
        } else {
            holder.rlcomlist.setBackgroundColor(context.getResources()
                    .getColor(R.color.blueveryverylight));
        }

        return convertView;
    }

    public String returnFormated(String inValue, String[] currentList) {
        String formattedResults = inValue;
        if (currentList == null) { // Return Early to avoid NullPointerException
            return formattedResults;
        }

        StringBuffer sb = null;
        for (int x = 0; x < currentList.length; x++) {
            String currentValue = currentList[x].trim().toUpperCase();
            // Log.i("returnFormated", "currentValue:"+currentValue);
            int startPos = formattedResults.toUpperCase().indexOf(currentValue);
            int endPos = startPos + currentValue.length();
            // Log.i("returnFormated",
            // "startPos:"+startPos+", endPos:"+endPos+" formatedResults Length:"+formattedResults.length());
            if (startPos > -1) {
                sb = new StringBuffer();
                if (startPos > 0) {
                    sb.append(formattedResults.substring(0, startPos));
                }
                sb.append("<b>");
                sb.append(formattedResults.substring(startPos, endPos));
                sb.append("</b>");
                if (endPos < formattedResults.length()) {
                    sb.append(formattedResults.substring(endPos));
                }
                formattedResults = sb.toString();
            }
        }
        return formattedResults;
    }

    public Commodity getCommodityAt(int y) {
        return items.get(y);
    }

    public int removeCommodityCode(String cdcommodity) {
        int itemsRemoved = 0;
        this.setNotifyOnChange(true);
        if (this.items != null) {
            for (int x = this.items.size() - 1; x > -1; x--) {
                if (this.items.get(x).getCode().equals(cdcommodity)) {
                    this.items.remove(x);
                    itemsRemoved++;
                }
            }
        }
        if (itemsRemoved > 0) {
            this.notifyDataSetChanged();
        }
        return itemsRemoved;
    }

    public int wordRowCount(String word) {
        int rowCount = 0;
        word = word.toUpperCase();
        for (int x = 0; x < this.items.size(); x++) {
            if (items.get(x).getDescription().toUpperCase().indexOf(word) > -1) {
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

    public void clearData() {
        items = new ArrayList<Commodity>();
        keywords = null;
        rowSelected = -1;
        this.notifyDataSetChanged();
    }

    @Override
    public void OnDoubleTap(AdapterView parent, View view, int position, long id) {
        System.out.println("Double Clicked on " + position + ": "
                + items.get(position).getDescription());
    }

    @Override
    public void OnSingleTap(AdapterView parent, View view, int position, long id) {
        // Do nothing on Single Tap (for now)
    }

}