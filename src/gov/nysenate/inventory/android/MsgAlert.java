package gov.nysenate.inventory.android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.text.Html;

public class MsgAlert
{
    Context context;
    AlertDialog dialog;

    public MsgAlert(Context context) {
        this.context = context;
    }

    public MsgAlert(Context context, final String title, final String message,
            OnClickListener onClickListener) {
        this.context = context;
        showMessage(title, message, onClickListener);
    }

    public MsgAlert(Context context, final String title, final String message) {
        this.context = context;
        showMessage(title, message);
    }

    public void showMessage(String title, String message) {
        showMessage(title, message, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // if this button is clicked, just close
                // the dialog box and do nothing
                dialog.dismiss();
            }
        });
    }

    public void showMessage(String title, String message,
            OnClickListener onClickListener) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        // set title
        alertDialogBuilder.setTitle(Html.fromHtml("<font color='#000055'>"
                + title + "</font>"));

        // set dialog message
        alertDialogBuilder.setMessage(Html.fromHtml(message))
                .setCancelable(false).setPositiveButton(Html.fromHtml("<b>Ok</b>"), onClickListener);
        // create alert dialog
        dialog = alertDialogBuilder.create();

        // show it
        dialog.show();
    }
    
    public AlertDialog getDialog() {
        return dialog;
    }

}
