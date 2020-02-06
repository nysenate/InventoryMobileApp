package gov.nysenate.inventory.android;

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

import java.util.ArrayList;
import java.util.List;

import gov.nysenate.inventory.activity.SenateActivity;
import gov.nysenate.inventory.listener.ChangePasswordDialogListener;

//...G
@SuppressLint("ValidFragment")
public class ChangePasswordDialog extends DialogFragment {

    public static ClearableEditText oldPassword;
    public static ClearableEditText newPassword;
    public static ClearableEditText confirmPassword;
    public static ProgressBar progBarNewInvItem;
    SenateActivity senateActivity;
    public static ListView commodityList = null;
    public String title = null;
    public String msg = null;
    private String oldPasswordSet = null;
    private String newPasswordSet = null;
    private String confirmPasswordSet = null;
    private boolean oldPasswordRequired = true;
    List<ChangePasswordDialogListener> listeners = new ArrayList<ChangePasswordDialogListener>();
    public static final int DEFAULTFOCUS = 100;
    public static final int OLDPASSWORDFOCUS = 101;
    public static final int NEWPASSWORDFOCUS = 102;
    public static final int CONFIRMPASSWORDFOCUS = 103;
    private int initialFocus = DEFAULTFOCUS;

    public ChangePasswordDialog(SenateActivity senateActivity, String title,
                                String msg) {
        this.senateActivity = senateActivity;
        this.title = title;
        this.msg = msg;
        this.oldPasswordRequired = true;
        this.oldPasswordSet = null;
        this.newPasswordSet = null;
        this.confirmPasswordSet = null;
    }

    public ChangePasswordDialog(SenateActivity senateActivity, String title,
                                String msg, int initialFocus) {
        this.senateActivity = senateActivity;
        this.title = title;
        this.msg = msg;
        this.oldPasswordRequired = true;
        this.oldPasswordSet = null;
        this.newPasswordSet = null;
        this.confirmPasswordSet = null;
        this.initialFocus = initialFocus;
    }

    public ChangePasswordDialog(SenateActivity senateActivity, String title,
                                String msg, String oldPasswordSet, String newPasswordSet,
                                String confirmPasswordSet) {
        this.senateActivity = senateActivity;
        this.title = title;
        this.msg = msg;
        this.oldPasswordRequired = true;
        this.oldPasswordSet = oldPasswordSet;
        this.newPasswordSet = newPasswordSet;
        this.confirmPasswordSet = confirmPasswordSet;
    }

    public ChangePasswordDialog(SenateActivity senateActivity, String title,
                                String msg, String oldPasswordSet, String newPasswordSet,
                                String confirmPasswordSet, int initialFocus) {
        this.senateActivity = senateActivity;
        this.title = title;
        this.msg = msg;
        this.oldPasswordRequired = true;
        this.oldPasswordSet = oldPasswordSet;
        this.newPasswordSet = newPasswordSet;
        this.confirmPasswordSet = confirmPasswordSet;
        this.initialFocus = initialFocus;
    }

    public ChangePasswordDialog(SenateActivity senateActivity, String title,
                                String msg, boolean oldPasswordRequired) {
        this.senateActivity = senateActivity;
        this.title = title;
        this.msg = msg;
        this.oldPasswordRequired = oldPasswordRequired;
        this.oldPasswordSet = null;
        this.newPasswordSet = null;
        this.confirmPasswordSet = null;
    }

    public ChangePasswordDialog(SenateActivity senateActivity, String title,
                                String msg, boolean oldPasswordRequired, int initialFocus) {
        this.senateActivity = senateActivity;
        this.title = title;
        this.msg = msg;
        this.oldPasswordRequired = oldPasswordRequired;
        this.oldPasswordSet = null;
        this.newPasswordSet = null;
        this.confirmPasswordSet = null;
        this.initialFocus = initialFocus;
    }

    public ChangePasswordDialog(SenateActivity senateActivity, String title,
                                String msg, boolean oldPasswordRequired, String oldPasswordSet,
                                String newPasswordSet, String confirmPasswordSet) {
        this.senateActivity = senateActivity;
        this.title = title;
        this.msg = msg;
        this.oldPasswordRequired = oldPasswordRequired;
        this.oldPasswordSet = oldPasswordSet;
        this.newPasswordSet = newPasswordSet;
        this.confirmPasswordSet = confirmPasswordSet;
    }

    public ChangePasswordDialog(SenateActivity senateActivity, String title,
                                String msg, boolean oldPasswordRequired, String oldPasswordSet,
                                String newPasswordSet, String confirmPasswordSet, int initialFocus) {
        this.senateActivity = senateActivity;
        this.title = title;
        this.msg = msg;
        this.oldPasswordRequired = oldPasswordRequired;
        this.oldPasswordSet = oldPasswordSet;
        this.newPasswordSet = newPasswordSet;
        this.confirmPasswordSet = confirmPasswordSet;
        this.initialFocus = initialFocus;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater
                .inflate(R.layout.dialog_change_passord, null);

        oldPassword = (ClearableEditText) dialogView
                .findViewById(R.id.oldPassword);
        newPassword = (ClearableEditText) dialogView
                .findViewById(R.id.newPassword);
        confirmPassword = (ClearableEditText) dialogView
                .findViewById(R.id.confirmPassword);

        if (this.oldPasswordSet != null) {
            oldPassword.setText(this.oldPasswordSet);
        }

        if (this.newPasswordSet != null) {
            newPassword.setText(this.newPasswordSet);
        }

        if (this.confirmPasswordSet != null) {
            confirmPassword.setText(this.confirmPasswordSet);
        }

        switch (this.initialFocus) {
            case DEFAULTFOCUS:
                if (this.oldPasswordRequired) {
                    if (oldPassword.getText().toString().trim().length() == 0) {
                        oldPassword.requestFocus();
                        oldPassword.setSelection(oldPassword.getText().length());
                    } else if (oldPassword.getText().toString().trim().length() > 0
                            && newPassword.getText().toString().trim().length() == 0) {
                        newPassword.requestFocus();
                        newPassword.setSelection(newPassword.getText().length());
                    } else if (oldPassword.getText().toString().trim().length() > 0
                            && newPassword.getText().toString().trim().length() > 0
                            && confirmPassword.getText().toString().trim().length() == 0) {
                        confirmPassword.requestFocus();
                        confirmPassword.setSelection(confirmPassword.getText().length());
                    }
                } else {
                    oldPassword.setVisibility(View.GONE);
                    if (newPassword.getText().toString().trim().length() > 0
                            && confirmPassword.getText().toString().trim().length() == 0) {
                        confirmPassword.requestFocus();
                        confirmPassword.setSelection(confirmPassword.getText().length());
                    }
                }
                break;
            case OLDPASSWORDFOCUS:
                if (this.oldPasswordRequired) {
                    oldPassword.requestFocus();
                    oldPassword.setSelection(oldPassword.getText().length());
                } else {
                    newPassword.requestFocus();
                    newPassword.setSelection(newPassword.getText().length());
                }
                break;
            case NEWPASSWORDFOCUS:
                newPassword.requestFocus();
                newPassword.setSelection(newPassword.getText().length());
                break;
            case CONFIRMPASSWORDFOCUS:
                confirmPassword.requestFocus();
                confirmPassword.setSelection(confirmPassword.getText().length());
                break;
            default:
                System.out
                        .println("Invalid Focus Value in ChangePassword Dialog. Any coding needed to handle this scenario should be coded here.");
        }
        if (!this.oldPasswordRequired) {
            oldPassword.setVisibility(View.GONE);
        }

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogView)
                .setTitle(
                        Html.fromHtml("<font color='#000055'>" + title
                                + "</font>"))
                .setMessage(Html.fromHtml(msg))
                .setCancelable(false)
                .setPositiveButton(Html.fromHtml("<b>Continue</b>"),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                for (ChangePasswordDialogListener ChangePasswordDialogListener : listeners)
                                    ChangePasswordDialogListener
                                            .onChangePasswordOKButtonClicked(
                                                    oldPasswordRequired,
                                                    oldPassword.getText()
                                                            .toString(),
                                                    newPassword.getText()
                                                            .toString(),
                                                    confirmPassword.getText()
                                                            .toString());
                                dismiss();
                            }
                        })
                .setNegativeButton(Html.fromHtml("<b>Cancel</b>"),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                // cancelMsg();
                                for (ChangePasswordDialogListener ChangePasswordDialogListener : listeners)
                                    ChangePasswordDialogListener
                                            .onChangePasswordCancelButtonClicked();
                                dismiss();
                            }
                        });

        /*
         * if (senateActivity.dialogComments != null) {
         * etComments.setText(senateActivity.dialogComments);
         * senateActivity.dialogComments = null; }
         */

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