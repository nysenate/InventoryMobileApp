package gov.nysenate.inventory.android;


import gov.nysenate.inventory.activity.SenateActivity;
import gov.nysenate.inventory.listener.ChangePasswordDialogListener;

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
public class ChangePasswordDialog extends DialogFragment
{

    public static ClearableEditText oldPassword;
    public static ClearableEditText newPassword;
    public static ClearableEditText confirmPassword;
    public static ProgressBar progBarNewInvItem;
    SenateActivity senateActivity;
    public static ListView commodityList = null;
    public String title = null;
    public String msg = null;
    List<ChangePasswordDialogListener> listeners = new ArrayList<ChangePasswordDialogListener>();

    public ChangePasswordDialog(SenateActivity senateActivity, String title,
            String msg) {
        this.senateActivity = senateActivity;
        this.title = title;
        this.msg = msg;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_change_passord, null);
        /*oldPassword = (ClearableEditText) dialogView
                .findViewById(R.id.oldPassword);*/
        newPassword = (ClearableEditText) dialogView
                .findViewById(R.id.newPassword);
        confirmPassword = (ClearableEditText) dialogView
                .findViewById(R.id.confirmPassword);
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogView)
                .setTitle(
                        Html.fromHtml("<font color='#000055'>" + title
                                + "</font>"))
                .setMessage(Html.fromHtml(msg))
                .setCancelable(false)
                .setPositiveButton(Html.fromHtml("<b>Continue</b>"), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        for (ChangePasswordDialogListener ChangePasswordDialogListener : listeners)
                            ChangePasswordDialogListener
                                    .onContinueButtonClicked(oldPassword
                                            .getText().toString(), newPassword
                                            .getText().toString(), confirmPassword
                                            .getText().toString());
                        dismiss();
                    }
                })
                .setNegativeButton(Html.fromHtml("<b>Cancel</b>"), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // cancelMsg();
                        dismiss();
                    }
                });

       /* if (senateActivity.dialogComments != null) {
            etComments.setText(senateActivity.dialogComments);
            senateActivity.dialogComments = null;
        }*/

        this.setStyle(STYLE_NO_FRAME, android.R.style.Theme_Holo);

        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        // Create the AlertDialog object and return it
        return dialog;
    }

  
    public void addListener(ChangePasswordDialogListener addListener) {
        listeners.add(addListener);
    }

}