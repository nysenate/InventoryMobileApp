package gov.nysenate.inventory.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

import gov.nysenate.inventory.android.Nvl;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.model.Location;

public class LocHistAdapter extends ArrayAdapter<Location> {

    Context context;
    List<Location> locations;
    SimpleDateFormat simpleDateFormat  = new SimpleDateFormat("MM/dd/yyyy");

    public LocHistAdapter(Context context, int resourceId,
                          List<Location> locations) {
        super(context, resourceId, locations);
        this.context = context;
        this.locations = locations;
    }

    /* private view holder class */
    private class ViewHolder {
        TextView entryDate;
        TextView locCode;
        TextView department;
        TextView locAddr;
        TextView locCityZip;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        Location rowLocation = locations.get(position);

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.row_location_history, null);
            holder =  new ViewHolder();
            holder.entryDate = (TextView) convertView
                    .findViewById(R.id.tvLHEntryDate);
            holder.locCode = (TextView) convertView
                    .findViewById(R.id.tvLHLocCode);
            holder.locAddr = (TextView) convertView
                    .findViewById(R.id.tvLHLocAddr);
            holder.department = (TextView) convertView
                    .findViewById(R.id.tvLHDepartment);
            holder.locCityZip = (TextView) convertView
                    .findViewById(R.id.tvLHLocCityZip);
            convertView.setTag(holder);
        } else {
            holder = (LocHistAdapter.ViewHolder) convertView.getTag();
        }

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.row_location_history, null);
            holder = new ViewHolder();
            holder.entryDate = (TextView) convertView
                    .findViewById(R.id.tvLHEntryDate);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        int currentBaground;

        if (position % 2 > 0) {
            currentBaground = context.getResources()
                    .getColor(R.color.white);

        } else {
            currentBaground = context.getResources()
                    .getColor(R.color.blueveryverylight);
        }

        try {
            holder.entryDate.setText(simpleDateFormat.format(rowLocation.getEntryDate()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        holder.entryDate.setBackgroundColor(currentBaground);

        holder.locCode.setBackgroundColor(currentBaground);
        holder.locCode.setText(rowLocation.getCdlocat()+(rowLocation.getCdloctype()==null ? "" :"-")+rowLocation.getCdloctype());

        holder.department.setText(Nvl.staticValue(rowLocation.getDepartment(), ""));
        holder.department.setBackgroundColor(currentBaground);


        holder.locAddr.setText(rowLocation.getAdstreet1());
        holder.locAddr.setBackgroundColor(currentBaground);

        StringBuilder cityStateZip = new StringBuilder();

        cityStateZip.append(rowLocation.getAdcity()==null ? "" : rowLocation.getAdcity());
        cityStateZip.append(rowLocation.getAdcity()!=null && (rowLocation.getAdstate() != null || rowLocation.getAdzipcode() == null) ? ", " : "");
        cityStateZip.append(Nvl.staticValue(rowLocation.getAdstate(), "") + " "+ Nvl.staticValue(rowLocation.getAdzipcode(), ""));

        holder.locCityZip.setText(cityStateZip.toString());

        convertView.setBackgroundColor(currentBaground);

        return convertView;
    }
}