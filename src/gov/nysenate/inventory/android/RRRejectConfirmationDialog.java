package gov.nysenate.inventory.android;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import gov.nysenate.inventory.model.RemovalRequest;

public class RRRejectConfirmationDialog extends DialogFragment
{
    public interface RRRejectConfirmationDialogI {
        public void onRejectBtnPositiveClick();
        public void onRejectBtnNegativeClick();
    }

    private RemovalRequest rr;
    private RRRejectConfirmationDialogI handler;
    private EditText comments;

    public static RRRejectConfirmationDialog newInstance(RemovalRequest rr, RRRejectConfirmationDialogI handler) {
        RRRejectConfirmationDialog dialog = new RRRejectConfirmationDialog();
        dialog.rr = rr;
        dialog.handler = handler;
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.rr_reject_confirmation_dialog, null);

        initializeUI(view);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setTitle("Confirmation");
        alertDialogBuilder.setMessage(Html.fromHtml("Are you sure you want to <b>Reject</b> this Removal Request?" +
                "<br><br> Enter any comments."));
        alertDialogBuilder.setPositiveButton(Html.fromHtml("<b>OK</b>"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                    rr.setStatus("RJ");
                    rr.setInventoryControlComments(comments.getText().toString());
                    handler.onRejectBtnPositiveClick();
            }
        });
        alertDialogBuilder.setNegativeButton(Html.fromHtml("<b>No</b>"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();

        return alertDialog;
    }

    private void initializeUI(View view) {
        comments = (EditText) view.findViewById(R.id.reject_comments);
    }
}
