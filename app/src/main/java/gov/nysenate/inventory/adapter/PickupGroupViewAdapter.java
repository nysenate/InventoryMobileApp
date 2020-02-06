package gov.nysenate.inventory.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.model.PickupGroup;

import java.util.List;

public class PickupGroupViewAdapter extends ArrayAdapter<PickupGroup>
{

    Context context;
    List<PickupGroup> pickupGroups;

    public PickupGroupViewAdapter(Context context, int resourceId,
            List<PickupGroup> pickupGroups) {
        super(context, resourceId, pickupGroups);
        this.context = context;
        this.pickupGroups = pickupGroups;
    }

    /* private view holder class */
    private class ViewHolder
    {
        RelativeLayout rlPickupGrpRow;
        TextView pickupDateTime;
        TextView pickupLocat;
        TextView pickupBy;
        TextView pickupRelBy;
        TextView pickupCount;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        PickupGroup currentPickupGroup = pickupGroups.get(position);

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.pickup_group_row, null);
            holder = new ViewHolder();
            holder.rlPickupGrpRow = (RelativeLayout) convertView
                    .findViewById(R.id.rlPickupGrpRow);
            holder.pickupDateTime = (TextView) convertView
                    .findViewById(R.id.pickupDateTime);
            holder.pickupLocat = (TextView) convertView
                    .findViewById(R.id.pickupLocat);
            holder.pickupBy = (TextView) convertView
                    .findViewById(R.id.pickupFrom);
            // holder.pickupRelBy = (TextView)
            // convertView.findViewById(R.id.pickupRelBy);
            holder.pickupCount = (TextView) convertView
                    .findViewById(R.id.pickupCnt);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        try {

            if (position % 2 > 0) {
                // holder.rlPickupGrpRow.setBackgroundColor(context.getResources().getColor(R.color.white));
                holder.rlPickupGrpRow
                        .setBackgroundResource(R.drawable.selector_1);
            } else {
                // holder.rlPickupGrpRow.setBackgroundColor(context.getResources().getColor(R.color.blueveryverylight));
                holder.rlPickupGrpRow
                        .setBackgroundResource(R.drawable.selector_2);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            String address = currentPickupGroup.getPickupLocat()
                    + " - " + currentPickupGroup.getPickupAdstreet1() + ", "
                    + currentPickupGroup.getPickupAdcity() + ", "
                    + currentPickupGroup.getPickupAdstate() + " "
                    + currentPickupGroup.getPickupAdzipcode();
            holder.pickupLocat.setText(Html.fromHtml(address));
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*
         * try {
         * holder.pickupRelBy.setText(currentPickupGroup.getPickupRelBy()); }
         * catch (Exception e) { e.printStackTrace(); }
         */
        try {
            holder.pickupBy.setText(currentPickupGroup.getPickupFrom());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            holder.pickupCount.setText(Integer.toString(currentPickupGroup
                    .getPickupItemCount()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            holder.pickupDateTime.setText(currentPickupGroup
                    .getPickupDateTime());
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*
         * OnClickListener l = new OnClickListener() {
         * 
         * @Override public void onClick(View v) { if
         * (v.getId()==R.id.invListSpeech){
         * 
         * }
         * 
         * } };
         * 
         * holder.speech2Txt.setOnClickListener(l);
         */
        return convertView;
    }

}