package gov.nysenate.inventory.adapter;

import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.android.R.color;
import gov.nysenate.inventory.android.R.id;
import gov.nysenate.inventory.android.R.layout;
import gov.nysenate.inventory.model.InvItem;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class InvSelListViewAdapter extends ArrayAdapter<InvItem>
{

    Context context;
    List<InvItem> items;

    public InvSelListViewAdapter(Context context, int resourceId,
            List<InvItem> items) {
        super(context, resourceId, items);
        this.context = context;
        this.items = items;
        this.setNotifyOnChange(true);
    }

    /* private view holder class */
    private class ViewHolder
    {
        RelativeLayout rslList;
        ImageView speech2Txt;
        TextView invSelListBarcode;
        TextView invSelListDescr;
        CheckBox invSelListSelected;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        final InvItem rowItem = items.get(position);

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.invlist_sel_item, null);
            holder = new ViewHolder();
            holder.rslList = (RelativeLayout) convertView
                    .findViewById(R.id.rsllist);
            holder.speech2Txt = (ImageView) convertView
                    .findViewById(R.id.invSelListSpeech);
            holder.invSelListBarcode = (TextView) convertView
                    .findViewById(R.id.invSelListBarcode);
            holder.invSelListDescr = (TextView) convertView
                    .findViewById(R.id.invSelListDescr);
            holder.invSelListSelected = (CheckBox) convertView
                    .findViewById(R.id.invSelListSelected);
            convertView.setTag(holder);
            holder.invSelListSelected.setTag(items.get(position));
        } else {
            holder = (ViewHolder) convertView.getTag();

        }

        holder.invSelListSelected.setChecked(rowItem.getSelected());

        if (position % 2 > 0) {
            holder.rslList.setBackgroundColor(context.getResources().getColor(
                    R.color.white));
        } else {
            holder.rslList.setBackgroundColor(context.getResources().getColor(
                    R.color.blueveryverylight));
        }

        holder.invSelListSelected.setSelected(rowItem.getSelected());

        if (rowItem.getType().equalsIgnoreCase("NEW")) {
            holder.invSelListBarcode.setText(rowItem.getNusenate());
            holder.invSelListBarcode.setTextColor(context.getResources()
                    .getColor(R.color.blue)); // blue
            String cdcommodity = rowItem.getCdcommodity();
            String decomments = rowItem.getDecomments();

            if (cdcommodity == null || cdcommodity.trim().length() == 0) {
                holder.invSelListBarcode.setText("*** NEW ITEM ***    "
                        + Html.escapeHtml(decomments));
            } else if (decomments == null || decomments.trim().length() == 0) {
                holder.invSelListBarcode.setText("*** NEW ITEM ***    CC:"
                        + cdcommodity);
            } else {
                holder.invSelListBarcode.setText("*** NEW ITEM ***    CC:"
                        + cdcommodity + ": " + Html.escapeHtml(decomments));
            }
            holder.invSelListDescr.setTextColor(context.getResources()
                    .getColor(R.color.blue)); // blue
        } else {
            holder.invSelListBarcode.setText(rowItem.getNusenate());
            holder.invSelListBarcode.setTextColor(context.getResources()
                    .getColor(R.color.black)); // blue
            holder.invSelListDescr.setText(rowItem.getDecommodityf());
            holder.invSelListDescr.setTextColor(context.getResources()
                    .getColor(R.color.black)); // black

        }
        final ViewHolder finalHolder = holder;
        final int pos = position;
        int id = Resources.getSystem().getIdentifier("btn_check_holo_light",
                "drawable", "android");
        holder.invSelListSelected.setButtonDrawable(id);
        holder.invSelListSelected.setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View arg0) {
                CheckBox cb = (CheckBox) arg0;
                // InvItem curInvItem = (InvItem) cb.getTag();�
                items.get(pos).setSelected(cb.isChecked());

                notifyDataSetChanged();
            }

        });

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

    public void setAllSelected(boolean selected) {
        if (items == null) {
            return;
        }
        for (int x = 0; x < items.size(); x++) {
            items.get(x).setSelected(selected);
        }
        notifyDataSetChanged();
    }

    public void setSelected(int row, boolean selected) {
        if (items == null || row > items.size() - 1) {
            return;
        }
        this.setNotifyOnChange(true);
        items.get(row).setSelected(selected);
        notifyDataSetChanged();
    }

    public ArrayList<InvItem> getSelectedItems(boolean selected) {
        ArrayList<InvItem> returnItems = new ArrayList<InvItem>();
        for (int x = 0; x < items.size(); x++) {
            if (items.get(x).getSelected() == selected) {
                returnItems.add(items.get(x));
            }
        }
        return returnItems;

    }

    public String getSelectedItems(String delimiter) {
        StringBuffer itemList = new StringBuffer();
        if (this.items.size() > 0) {
            for (InvItem selection : this.items) {
                itemList.append(selection.getNusenate() + delimiter);
            }
            // Delete the last delimiter.
            itemList.deleteCharAt(itemList.length() - 1);
        }
        return itemList.toString();
    }

    public String getSelectedItemsAsString(boolean selected, int field) {
        return getSelectedItemsAsString(selected, field, "|");
    }

    // TODO: only used for NUSENATE, do we need?
    public String getSelectedItemsAsString(boolean selected, int field,
            String delimeter) {

        StringBuffer returnItems = new StringBuffer();
        InvItem curInvItem = null;
        final int DECOMMODITYF = -101;
        final int TYPE = -102;
        final int NUSENATE = -103;
        final int CDCATEGORY = -104;
        final int SELECTED = -105;

        for (int x = 0; x < items.size(); x++) {
            curInvItem = items.get(x);
            if (curInvItem.getSelected() == selected) {
                switch (field) {
                case DECOMMODITYF:
                    if (x > 0) {
                        returnItems.append(delimeter);
                    }
                    returnItems.append(curInvItem.getDecommodityf());
                    break;
                case TYPE:
                    if (x > 0) {
                        returnItems.append(delimeter);
                    }
                    returnItems.append(curInvItem.getType());
                    break;
                case NUSENATE:
                    if (x > 0) {
                        returnItems.append(delimeter);
                    }
                    returnItems.append(curInvItem.getNusenate());
                    break;
                case CDCATEGORY:
                    if (x > 0) {
                        returnItems.append(delimeter);
                    }
                    returnItems.append(curInvItem.getCdcategory());
                    break;
                case SELECTED:
                    if (x > 0) {
                        returnItems.append(delimeter);
                    }
                    returnItems.append(curInvItem.getSelected());
                    break;
                default:
                    return "!!ERROR: InvItem Field Name not found.";
                }

            }

        }

        return returnItems.toString();

    }

    public ArrayList<InvItem> getAllItems() {
        return (ArrayList<InvItem>) items;
    }

    public String getAllItemsAsString(int field) {
        return getAllItemsAsString(field, "|");
    }

    public String getAllItemsAsString(int field, String delimeter) {
        StringBuffer returnItems = new StringBuffer();
        InvItem curInvItem = null;
        final int DECOMMODITYF = -101;
        final int TYPE = -102;
        final int NUSENATE = -103;
        final int CDCATEGORY = -104;
        final int SELECTED = -105;

        if (items == null) {
            Log.i("InvSelViewAdapter", "getAllItemsAsString items: null");
        } else {

        }

        for (int x = 0; x < items.size(); x++) {
            curInvItem = items.get(x);
            switch (field) {
            case DECOMMODITYF:
                if (x > 0) {
                    returnItems.append(delimeter);
                }
                returnItems.append(curInvItem.getDecommodityf());
                break;
            case TYPE:
                if (x > 0) {
                    returnItems.append(delimeter);
                }
                returnItems.append(curInvItem.getType());
                break;
            case NUSENATE:
                if (x > 0) {
                    returnItems.append(delimeter);
                }
                returnItems.append(curInvItem.getNusenate());
                break;
            case CDCATEGORY:
                if (x > 0) {
                    returnItems.append(delimeter);
                }
                returnItems.append(curInvItem.getCdcategory());
                break;
            case SELECTED:
                if (x > 0) {
                    returnItems.append(delimeter);
                }
                returnItems.append(curInvItem.getSelected());
                break;
            default:
                return "!!ERROR: InvItem Field Name not found.";
            }

        }
        return returnItems.toString();

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