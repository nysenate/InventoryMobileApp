package gov.nysenate.inventory.android;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class KeywordListViewAdapter extends ArrayAdapter<String> implements OnItemDoubleTapListener
{

    Context context;
    List<String> items;
    public List<EditText> etKeywordFields = new ArrayList<EditText>();
    int rowSelected = -1;

    
    public KeywordListViewAdapter(Context context, int resourceId,
            List<String> items) {
        super(context, resourceId, items);
        this.context = context;
        this.items = items;
        //System.out.println("COMMODITY LIST ITEMS SIZE:" + items.size());
    }
    
    
    /* private view holder class */
    private class ViewHolder
    {
        LinearLayout rlkeywordlistrow;
        EditText etKeyword;
        Button btnDeleteKeyword;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        final String rowItem = items.get(position);

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.row_keyword, null);
            holder = new ViewHolder();
            holder.rlkeywordlistrow = (LinearLayout) convertView
                    .findViewById(R.id.rlkeywordlistrow);
            holder.etKeyword = (EditText) convertView
                    .findViewById(R.id.etKeyword);
            holder.btnDeleteKeyword = (Button)convertView.findViewById(R.id.btnDeleteKeyword);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        holder.etKeyword.setText(rowItem);
        
        final EditText currentEtKeyword = holder.etKeyword;
        final int currentPosition = position;
        
        OnClickListener l = new OnClickListener()
        {
            @Override
            public void onClick(View v) {
                 Log.i("DELETEKEYWORD", rowItem);
                 removeKeyword(rowItem);
            }
        };
        
        TextWatcher filterTextWatcher = new TextWatcher()
        {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                    int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                items.set(currentPosition, currentEtKeyword.getText().toString());
                rowSelected = currentPosition;
                //notifyDataSetChanged();
            }
        };
        
        
        if (currentEtKeyword!=null) {
            currentEtKeyword.addTextChangedListener(filterTextWatcher);
        }
        
        if (holder.btnDeleteKeyword!=null) {
            holder.btnDeleteKeyword.setOnClickListener(l);
        }

        
        
        if (etKeywordFields.size()-1<position) {
            etKeywordFields.add(holder.etKeyword);
        }
        else {
            etKeywordFields.set(position, holder.etKeyword);
        }
        
        return convertView;
    }
    
    public String getKeywordAt(int y) {
        return items.get(y);
    }

    public int removeKeyword(String keyword) {
        int itemsRemoved = 0;
        this.setNotifyOnChange(true);
        if (this.items != null) {
            for (int x = this.items.size() - 1; x > -1; x--) {
                if (this.items.get(x).equals(keyword)) {
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
    
   public int addRow() {
       this.items.add(""); // Add a Row with a blank value
       this.notifyDataSetChanged();
       return this.items.size()-1;
   }
    
   public String toString() {
       StringBuffer sb = new StringBuffer();
       boolean keywordsAdded = false;
       for (int x=0;x<items.size();x++) {
           if (items.get(x).trim().length()>0) {
               if (keywordsAdded) {
                   sb.append(",");
               }
               sb.append(items.get(x).trim());
               keywordsAdded = true;
           }
      }
       return sb.toString();
   }
   
   public void fromString(String keywords) {
       String[] keywordsList = keywords.split(",");
       items = new ArrayList<String>();
       for (int x=0;x<keywordsList.length;x++) {
           String currentKeyword = keywordsList[x].trim();
           if (currentKeyword.length()>0) {
               items.add(currentKeyword);
           }
       }
       this.notifyDataSetChanged();
   }
  
   @Override
   public void OnDoubleTap(AdapterView parent, View view, int position, long id) {
        System.out.println("Double Clicked on "+position+": "+items.get(position));
   }

   @Override
   public void OnSingleTap(AdapterView parent, View view, int position, long id) {
       // Do nothing on Single Tap (for now)
   }
  
   
   public void returnToSelectedRow() {
       if (rowSelected==-1) {
           rowSelected = 0;
       }
       goRow(rowSelected);       
   }
   
   public void goRow(int row) {
       if (etKeywordFields==null||etKeywordFields.size()==0) {
           return;
       }
       else if (etKeywordFields.size()-1<row) {
           etKeywordFields.get(etKeywordFields.size()-1).requestFocus();
       }
       else {
           etKeywordFields.get(row).requestFocus();
       }
   }

}