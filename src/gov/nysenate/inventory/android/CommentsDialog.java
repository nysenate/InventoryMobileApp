package gov.nysenate.inventory.android;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

//...G
@SuppressLint("ValidFragment")
public class CommentsDialog extends DialogFragment
{

    public static ClearableEditText etComments;
    public static ProgressBar progBarNewInvItem;
    SenateActivity senateActivity;
    public static ListView commodityList = null;
    public String title = null;
    public String msg = null;
    List<CommentsDialogListener> listeners = new ArrayList<CommentsDialogListener>();

    public CommentsDialog(SenateActivity senateActivity, String title,
            String msg) {
        this.senateActivity = senateActivity;
        this.title = title;
        this.msg = msg;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_comments, null);
        etComments = (ClearableEditText) dialogView
                .findViewById(R.id.etComments);

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogView)
                .setTitle(
                        Html.fromHtml("<font color='#000055'>" + title
                                + "</font>"))
                .setMessage(Html.fromHtml(msg))
                .setCancelable(false)
                .setPositiveButton(Html.fromHtml("<b>Yes</b>"), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        for (CommentsDialogListener commentsDialogListener : listeners)
                            commentsDialogListener
                                    .onCommentOKButtonClicked(etComments
                                            .getText().toString());
                        dismiss();
                    }
                })
                .setNegativeButton(Html.fromHtml("<b>No</b>"), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // cancelMsg();
                        dismiss();
                    }
                });

        if (senateActivity.dialogComments != null) {
            etComments.setText(senateActivity.dialogComments);
            senateActivity.dialogComments = null;
        }

        this.setStyle(STYLE_NO_FRAME, android.R.style.Theme_Holo);

        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        // Create the AlertDialog object and return it
        return dialog;
    }

    public void cancelMsg() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                senateActivity);

        String cancelTitle = "<font color='#000055'>Cancel</font>";

        String cancelMsg = "***WARNING: You have chosen not to add this Senate Tag#. Continue?";

        // set title
        alertDialogBuilder.setTitle(Html.fromHtml(cancelTitle));

        // set dialog message
        alertDialogBuilder.setMessage(Html.fromHtml(cancelMsg))
                .setCancelable(false)
                .setPositiveButton(Html.fromHtml("<b>Yes</b>"), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dismiss();
                    }
                })
                .setNegativeButton(Html.fromHtml("<b>No</b>"), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, we need to reopen the
                        // original Dialog
                        senateActivity.dialogTitle = title;
                        senateActivity.dialogMsg = msg;
                        senateActivity.dialogComments = etComments.getText()
                                .toString();
                        senateActivity.reOpenCommentsDialog();
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