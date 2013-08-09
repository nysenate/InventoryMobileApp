package gov.nysenate.inventory.android;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ListView;
import android.text.Html;
import android.util.Log;

//...
@SuppressLint("ValidFragment")
public class KeywordDialog extends DialogFragment  {

 SenateActivity senateActivity;
 public static ListView lvKeywords = null;
 public static Button btnAddKeyword = null;
 public static EditText etDummy = null; /* Dummy Edit Text because Dialog Fragment needs at least one
                                         * Navigable Edit Text on the main Fragment (ListView Edit Texts don't count)
                                         * so the soft keyboard will display in the foreground. 
                                         */
 public String title = null;
 public String msg = null;
 public String keywordList = null;
 public String keywordListOrig = null;
 KeywordListViewAdapter adapter = null;
 List<OnKeywordChangeListener> listeners = new ArrayList<OnKeywordChangeListener>();
 int position = -1;
 
 public KeywordDialog(SenateActivity senateActivity, String title, String msg, String keywordList) {
     this.senateActivity = senateActivity;
     this.title = title;
     this.msg = msg;
     this.keywordList = keywordList;
     this.keywordListOrig = keywordList;
     Log.i("KeywordDialog", "START KEYWORDLIST:"+keywordList);
 }
 
 @Override
 public Dialog onCreateDialog(Bundle savedInstanceState) {
      
     Log.i("onCreateDialog", "START KEYWORDLIST:"+keywordList);
     LayoutInflater inflater = getActivity().getLayoutInflater();
     View dialogView = inflater.inflate(R.layout.dialog_keyword, null);
     btnAddKeyword = (Button) dialogView.findViewById(R.id.btnAddKeyword);
     lvKeywords = (ListView) dialogView.findViewById(R.id.lvKeywords);
     etDummy = (EditText) dialogView.findViewById(R.id.etDummy);
     adapter = new KeywordListViewAdapter(senateActivity.getApplicationContext(), R.layout.row_keyword, arrayListFromString(this.keywordList));
     lvKeywords.setAdapter(adapter);
     //adapter.unselectRow();
     
     etDummy.setOnFocusChangeListener(new OnFocusChangeListener() {          

         public void onFocusChange(View v, boolean hasFocus) {
             if(hasFocus) {
                 adapter.returnToSelectedRow();
             }
         }
     });     
     

     // Use the Builder class for convenient dialog construction
     AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
     builder.setView(dialogView)
            .setTitle(title)
            .setMessage(Html.fromHtml(msg))
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    senateActivity.dialogKeywords =  adapter.toString();
                    if (senateActivity.dialogKeywords.equalsIgnoreCase(keywordListOrig)) {
                        for (OnKeywordChangeListener onKeywordChangeListener : listeners)
                            onKeywordChangeListener.OnKeywordChange(KeywordDialog.this, lvKeywords, senateActivity.dialogKeywords);  
                    }

                    dismiss();
                }
            })            
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dismiss();
                }
            });
   
     // Create the AlertDialog object and return it
     AlertDialog dialog = builder.create();
     dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
     
     return dialog;
 }
 
 public void addListener(OnKeywordChangeListener addListener) {
     listeners.add(addListener);
 } 
 


 
 public ArrayList<String> arrayListFromString(String values) {
     String[] valuesList = values.split(",");
     ArrayList<String> items = new ArrayList<String>();
     for (int x=0;x<valuesList.length;x++) {
         String currentValue = valuesList[x].trim();
         if (currentValue.length()>0) {
             items.add(currentValue);
         }
     }
     return items;
 }  
 
 
 
}
