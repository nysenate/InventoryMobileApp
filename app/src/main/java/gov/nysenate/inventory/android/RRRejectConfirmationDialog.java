package gov.nysenate.inventory.android;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import gov.nysenate.inventory.model.RemovalRequest;
import gov.nysenate.inventory.util.Toasty;

public class RRRejectConfirmationDialog extends DialogFragment {
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
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setTitle("Confirmation");
        alertDialogBuilder.setMessage(Html.fromHtml("Are you sure you want to <b>Reject</b> this Removal Request?" +
                "<br><br> Please enter comments."));
        alertDialogBuilder.setPositiveButton(Html.fromHtml("<b>OK</b>"), null);
        alertDialogBuilder.setNegativeButton(Html.fromHtml("<b>No</b>"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handler.onRejectBtnNegativeClick();
            }
        });

        final AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button p = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                p.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (comments.getText().toString().isEmpty()) {
                            Toasty.displayCenteredMessage(getActivity(), "You must enter a reason for this rejection.", Toast.LENGTH_SHORT);
                        } else {
                            rr.setStatus("RJ");
                            rr.setInventoryControlComments(comments.getText().toString());
                            handler.onRejectBtnPositiveClick();
                        }
                    }
                });
            }
        });

        return alertDialog;
    }


    private void initializeUI(View view) {
        comments = (EditText) view.findViewById(R.id.reject_comments);
    }
}
