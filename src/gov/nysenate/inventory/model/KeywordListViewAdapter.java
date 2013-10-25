package gov.nysenate.inventory.model;

import gov.nysenate.inventory.android.NewInvDialog;
import gov.nysenate.inventory.android.OnItemDoubleTapListener;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.util.ClearableEditText;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class KeywordListViewAdapter extends ArrayAdapter<String> implements
        OnItemDoubleTapListener
{

    Context context;
    List<String> items;
    List<String> originalKeywordList = null;
    ClearableEditText etKeywordCurrent = null;
    public List<ClearableEditText> etKeywordFields = new ArrayList<ClearableEditText>();
    NewInvDialog newInvDialog = null;
    int rowSelected = -1;
    boolean replaceSpace = false;
    InputFilter keywordFilter = new InputFilter() {   
        @Override  
        public CharSequence filter(CharSequence arg0, int arg1, int arg2, Spanned arg3, int arg4, int arg5)  
            {  
                 for (int k = arg1; k < arg2; k++) {
                     if (k>0  && arg0.charAt(k) == ',' && etKeywordCurrent.getText().toString().endsWith(",") ) {
                         return "";   
                     }
                    /* else if (Character.isSpaceChar(arg0.charAt(k))) {
                         return ",";   
                     }*/
                     else if (!Character.isLetterOrDigit(arg0.charAt(k)) && arg0.charAt(k) != '-' && arg0.charAt(k) != '/' && arg0.charAt(k) != '\\'/* && arg0.charAt(k) != '.' && arg0.charAt(k) != ','*/) {   
                         return "";   
                     }   
                 }   
             return null;   
            }   
    };       

    public KeywordListViewAdapter(Context context, NewInvDialog newInvDialog,
            int resourceId, List<String> items) {
        super(context, resourceId, items);
        this.context = context;
        this.items = items;
        originalKeywordList = new ArrayList<String>();
        for (int x = 0; x < items.size(); x++) {
            this.originalKeywordList.add(items.get(x));
        }
        this.newInvDialog = newInvDialog;
        // System.out.println("COMMODITY LIST ITEMS SIZE:" + items.size());
    }

    /* private view holder class */
    private class ViewHolder
    {
        LinearLayout rlkeywordlistrow;
        ClearableEditText etKeyword;
        TextView tvKeywordCnt;
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
            holder.etKeyword = (ClearableEditText) convertView
                    .findViewById(R.id.etKeyword);
            holder.tvKeywordCnt = (TextView) convertView
                    .findViewById(R.id.tvKeywordCnt);
            holder.btnDeleteKeyword = (Button) convertView
                    .findViewById(R.id.btnDeleteKeyword);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.etKeyword.setText(rowItem);
        if (this.originalKeyword(rowItem) > -1) {
            int wordRowCount = newInvDialog.adapter.wordRowCount(rowItem);
            if (wordRowCount == 0) {
                holder.tvKeywordCnt.setText(Html
                        .fromHtml("<font color='red'><b>(" + wordRowCount
                                + ")</b></font>"));
            } else {
                holder.tvKeywordCnt.setText(Html
                        .fromHtml("<font color='#005500'><b>(" + wordRowCount
                                + ")</b></font>"));
            }

        } else {
            holder.tvKeywordCnt.setText("(***)");
        }

        final EditText currentEtKeyword = holder.etKeyword;
        final TextView currentTvKeywordCnt = holder.tvKeywordCnt;
        final int currentPosition = position;

        OnClickListener l = new OnClickListener()
        {
            @Override
            public void onClick(View v) {
                // Log.i("DELETEKEYWORD", rowItem);
                // removeKeyword(rowItem);
                removeRow(currentPosition);
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
                currentTvKeywordCnt.setText("(***)");
                items.set(currentPosition, currentEtKeyword.getText()
                        .toString());
                rowSelected = currentPosition;
               /* String result = s.toString().replaceAll(" ", ",");
                if (result.indexOf(",,")>-1) {
                    result = result.replaceAll(",,",",");
                }
                if (!s.toString().equals(result)) {
                    currentEtKeyword.setText(result);
                    currentEtKeyword.setSelection(result.length());
                    // alert the user
               }*/
                // notifyDataSetChanged();
            }
        };

        if (currentEtKeyword != null) {
            currentEtKeyword.addTextChangedListener(filterTextWatcher);
        }

        if (holder.btnDeleteKeyword != null) {
            holder.btnDeleteKeyword.setOnClickListener(l);
        }

        holder.etKeyword.setFilters(new InputFilter[]{ keywordFilter});
        etKeywordCurrent = holder.etKeyword;

        if (etKeywordFields.size() - 1 < position) {
            etKeywordFields.add(holder.etKeyword);
        } else {
            etKeywordFields.set(position, holder.etKeyword);
        }
      

        return convertView;
    }

    public String getKeywordAt(int y) {
        return items.get(y);
    }

    public boolean removeRow(int row) {
        boolean rowRemoved = false;

        if (items != null && items.size() > 1 && row < items.size()) {
            this.items.remove(row);
            this.setNotifyOnChange(true);
            this.notifyDataSetChanged();
            rowRemoved = true;
        } else if (items != null && items.size() == 1) {
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context,
                    "At least one Keyword row must exist.", duration);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }

        return rowRemoved;
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
        if (itemsRemoved > 0) {
            this.notifyDataSetChanged();
        }
        return itemsRemoved;
    }

    public int originalKeyword(String keyword) {
        for (int x = 0; x < originalKeywordList.size(); x++) {
            if (originalKeywordList.get(x).equalsIgnoreCase(keyword)) {
                return x;
            }
        }
        return -1;
    }

    public int findBlankKeyword() {
        for (int x = 0; x < this.items.size(); x++) {
            if (this.items.get(x).trim().length() == 0) {
                return x;
            }
        }
        return -1;
    }

    public int getCurPosition(EditText etKeyword) {
        for (int x = 0; x < etKeywordFields.size(); x++) {
            if (etKeyword == etKeywordFields.get(x)) {
                return x;
            }
        }

        return -1;
    }

    public int addRow() {
        int blankRow = findBlankKeyword();
        if (blankRow == -1) {
            this.items.add(""); // Add a Row with a blank value
            this.notifyDataSetChanged();
            return this.items.size() - 1;
        } else {

            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context,
                    "A blank keyword row was already added.", duration);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return blankRow;
        }
    }
   
    public ArrayList<String> arrayListFromString(String values) {
        String[] valuesList = values.split(",");
        ArrayList<String> items = new ArrayList<String>();
        for (int x = 0; x < valuesList.length; x++) {
            String currentValue = valuesList[x].trim();
            if (currentValue.length() > 0) {
                items.add(currentValue);
            }
        }
        return items;
    }    

    public String stringFromArrayList(ArrayList<String> values) {
        StringBuffer sb = new StringBuffer();
        ArrayList<String> items = new ArrayList<String>();
        for (int x = 0; x < values.size(); x++) {
            String currentValue = values.get(x).trim();
            if (currentValue!=null && currentValue.length()>0) {
                if (x>0) {
                    sb.append(",");
                }
                sb.append(currentValue);
            }
        }
        return sb.toString();
    }    
    
    
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        boolean keywordsAdded = false;
        for (int x = 0; x < items.size(); x++) {
            if (items.get(x).trim().length() > 0) {
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
        for (int x = 0; x < keywordsList.length; x++) {
            String currentKeyword = keywordsList[x].trim();
            if (currentKeyword.length() > 0) {
                items.add(currentKeyword);
            }
        }
        this.notifyDataSetChanged();
    }

    @Override
    public void OnDoubleTap(AdapterView parent, View view, int position, long id) {
        System.out.println("Double Clicked on " + position + ": "
                + items.get(position));
    }

    @Override
    public void OnSingleTap(AdapterView parent, View view, int position, long id) {
        // Do nothing on Single Tap (for now)
    }

    public void returnToSelectedRow() {
        if (rowSelected == -1) {
            rowSelected = 0;
        }
        goRow(rowSelected);
    }

    public void goRow(int row) {
        if (etKeywordFields == null || etKeywordFields.size() == 0) {
            return;
        } else if (etKeywordFields.size() - 1 < row) {
            etKeywordFields.get(etKeywordFields.size() - 1).requestFocus();
        } else {
            etKeywordFields.get(row).requestFocus();
        }
    }

}