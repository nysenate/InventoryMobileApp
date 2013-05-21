package gov.nysenate.inventory.android;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
 
public class PickupGroupViewAdapter extends ArrayAdapter<PickupGroup> {
 
    Context context;
    List<PickupGroup> pickupGroups;
 
    public PickupGroupViewAdapter(Context context, int resourceId,
            List<PickupGroup> pickupGroups) {
        super(context, resourceId, pickupGroups);
        this.context = context;
        this.pickupGroups = pickupGroups;
    }
 
    /*private view holder class*/
    private class ViewHolder {
     	RelativeLayout rlPickupGrpRow;    	
       	TextView pickupDateTime;    	    	
        TextView pickupLocat;    	
    	TextView pickupFrom;    	    	
    	TextView pickupRelBy;    	
    	TextView pickupCount;    	    	
    }
 
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        PickupGroup currentPickupGroup = pickupGroups.get(position);
        Log.i("Pickup GetView", "currentPickupGroup#"+position);
        
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.pickup_group_row, null);
            holder = new ViewHolder();
            holder.rlPickupGrpRow = (RelativeLayout) convertView.findViewById(R.id.rlPickupGrpRow);
            holder.pickupDateTime  = (TextView) convertView.findViewById(R.id.pickupDateTime);
            holder.pickupLocat = (TextView) convertView.findViewById(R.id.pickupLocat);
            holder.pickupFrom = (TextView) convertView.findViewById(R.id.pickupFrom);                      
//            holder.pickupRelBy = (TextView) convertView.findViewById(R.id.pickupRelBy);
            holder.pickupCount = (TextView) convertView.findViewById(R.id.pickupCnt);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            }
     
        try {
        if (position%2>0) {
        	holder.rlPickupGrpRow.setBackgroundColor(context.getResources().getColor(R.color.white));
            }
        else {
        	holder.rlPickupGrpRow.setBackgroundColor(context.getResources().getColor(R.color.blueveryverylight));
           }
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
    	
        try {
        	holder.pickupLocat.setText(currentPickupGroup.getPickupLocat()+ " - "+currentPickupGroup.getPickupAdstreet1()+", "+currentPickupGroup.getPickupAdcity()+", "+currentPickupGroup.getPickupAdstate()+" "+currentPickupGroup.getPickupAdzipcode());
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
        
       /* try {
        	holder.pickupRelBy.setText(currentPickupGroup.getPickupRelBy());
        }
        catch (Exception e) {
        	e.printStackTrace();
        }*/
        try {
        	holder.pickupFrom.setText(currentPickupGroup.getPickupFrom());    	
        }
        catch (Exception e) {
        	e.printStackTrace();
        }        
        try {
        	holder.pickupCount.setText(Integer.toString(currentPickupGroup.getPickupItemCount()));    	
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
        try {
        	holder.pickupDateTime.setText(currentPickupGroup.getPickupDateTime());
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
 
       /* OnClickListener l = new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (v.getId()==R.id.invListSpeech){
					
				}
				
			}
        };
        
		holder.speech2Txt.setOnClickListener(l); */
        return convertView;
    }
    
    
}