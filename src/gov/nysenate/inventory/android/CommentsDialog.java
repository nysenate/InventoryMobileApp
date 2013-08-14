package gov.nysenate.inventory.android;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ListView;
import android.text.Html;
import android.text.Spanned;

//...G
@SuppressLint("ValidFragment")
public class CommentsDialog extends DialogFragment   {

 public static ClearableEditText etComments;
 public static ProgressBar progBarNewInvItem;
 SenateActivity senateActivity;
 public static ListView commodityList = null;
 public String title = null;
 public String msg = null;
 List<CommentsDialogListener> listeners = new ArrayList<CommentsDialogListener>();
 
 public CommentsDialog(SenateActivity senateActivity, String title, String msg) {
     this.senateActivity = senateActivity;
     this.title = title;
     this.msg = msg;
 }

 @Override
 public Dialog onCreateDialog(Bundle savedInstanceState) {
      
     LayoutInflater inflater = getActivity().getLayoutInflater();
     View dialogView = inflater.inflate(R.layout.dialog_comments, null);
     etComments = (ClearableEditText)dialogView.findViewById(R.id.etComments);
     
     // Use the Builder class for convenient dialog construction
     AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
     builder.setView(dialogView)
            .setTitle(title)
            .setMessage(Html.fromHtml(msg))
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {                 
                    for (CommentsDialogListener commentsDialogListener : listeners)
                        commentsDialogListener.onCommentOKButtonClicked (etComments.getText().toString());                         dismiss();
                }
            })            
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    cancelMsg();
                }
            });
     
   
     // Create the AlertDialog object and return it
     return builder.create();
 }
   
 public void cancelMsg() {  
    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(senateActivity);

         String cancelTitle =  "Cancel";

         String  cancelMsg = "***WARNING: Senate Tag# will not be added if you cancel. Do you want to cancel?";

         // set title
         alertDialogBuilder.setTitle(cancelTitle);

         // set dialog message
         alertDialogBuilder.setMessage(Html.fromHtml(cancelMsg))
                 .setCancelable(false)
                 .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                 {
                     @Override
                     public void onClick(DialogInterface dialog, int id) {
                         // if this button is clicked, just close
                         // the dialog box and do nothing
                         dismiss();
                     }
                 })
                 .setNegativeButton("No", new DialogInterface.OnClickListener()
                 {
                     @Override
                     public void onClick(DialogInterface dialog, int id) {
                         // if this button is clicked, we need to reopen the original Dialog
                         senateActivity.dialogTitle = title;
                         senateActivity.dialogMsg = msg;
                         senateActivity.dialogComments = etComments.getText().toString();
                     }
                 });

         // create alert dialog
         AlertDialog alertDialog = alertDialogBuilder.create();

         // show it
         alertDialog.show();
 }

 public void addListener(CommentsDialogListener addListener) {
     listeners.add(addListener);
 } 
   
}