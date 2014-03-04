package gov.nysenate.inventory.android;

import java.util.ArrayList;

import android.os.AsyncTask;
import android.os.Build;
import gov.nysenate.inventory.activity.Delivery3;
import gov.nysenate.inventory.activity.LoginActivity;
import gov.nysenate.inventory.activity.SenateActivity;
import gov.nysenate.inventory.adapter.NothingSelectedSpinnerAdapter;
import gov.nysenate.inventory.model.Transaction;
import gov.nysenate.inventory.util.Toasty;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

public class RemoteConfirmationDialog extends DialogFragment {

    private ArrayList<String> employeeNameList;
    private Transaction delivery;
    private Spinner verMethod = null;
    private ClearableAutoCompleteTextView remoteSigner = null;
    private ClearableEditText remoteComment;
    private ClearableEditText remoteHelpReferenceNum;

    public static RemoteConfirmationDialog newInstance(ArrayList<String> employeeNameList, Transaction delivery) {
        RemoteConfirmationDialog frag = new RemoteConfirmationDialog();
        frag.employeeNameList = employeeNameList;
        frag.delivery = delivery;
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater li = LayoutInflater.from(getActivity());
        View promptView = li.inflate(R.layout.prompt_dialog_remote, null);
        initializeUi(promptView);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(promptView);
        alertDialogBuilder.setTitle("Remote Information");
        setPositiveButton(alertDialogBuilder);
        setNeutralButton(alertDialogBuilder);
        setNegativeButton(alertDialogBuilder);

        final AlertDialog alertDialog = alertDialogBuilder.create();

        // Use our own View.onClickListener so the dialog is not automatically dismissed
        // when the positive button is pressed.
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {

                Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (completelyFilledOut()) {
                            alertDialog.dismiss();
                            updateDeliveryInfo();
                            updateDeliveryUsers(delivery, remoteSigner.getText().toString());
                            returnToDelivery3();
                        }
                    }
                });
            }
        });
        return alertDialog;
    }

    private void initializeUi(View view) {
        verMethod = (Spinner) view.findViewById(R.id.remote_method);
        remoteSigner = (ClearableAutoCompleteTextView) view.findViewById(R.id.remote_employee_signer);
        remoteComment = (ClearableEditText) view.findViewById(R.id.remote_comments);
        remoteHelpReferenceNum = (ClearableEditText) view.findViewById(R.id.remote_helprefnum);
        remoteHelpReferenceNum.setVisibility(ClearableEditText.INVISIBLE);
        verMethod.setOnItemSelectedListener(onlyDisplayOSRTextBoxWhenSelected);

        ArrayAdapter<CharSequence> spinAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.remote_ver_method, android.R.layout.simple_spinner_item);
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        verMethod.setAdapter(new NothingSelectedSpinnerAdapter(spinAdapter, R.layout.spinner_nothing_selected, getActivity()));

        ArrayAdapter<String> empAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, employeeNameList);
        remoteSigner.setAdapter(empAdapter);
        remoteSigner.setThreshold(1);
        remoteSigner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(
                        remoteSigner.getWindowToken(), 0);
            }
        });

        checkForPreviousEntry();
    }

    private void checkForPreviousEntry() {
        OrigRemoteTask task = new OrigRemoteTask(getActivity(), delivery, verMethod, remoteSigner, remoteComment, remoteHelpReferenceNum);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            task.execute();
        }
    }

    private void setPositiveButton(AlertDialog.Builder alertDialogBuilder) {
        alertDialogBuilder.setPositiveButton(Html.fromHtml("<b>OK</b>"), null);
    }

    private void setNeutralButton(AlertDialog.Builder alertDialogBuilder) {
        alertDialogBuilder.setNeutralButton(Html.fromHtml("<b>Later</b>"), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                updateDeliveryUsers(delivery, "");
                ((Delivery3) getActivity()).positiveDialog();
            }
        });
    }

    private void setNegativeButton(AlertDialog.Builder alertDialogBuilder) {
        alertDialogBuilder.setNegativeButton(Html.fromHtml("<b>Cancel</b>"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
    }

    private boolean completelyFilledOut() {
        if (verMethod.getSelectedItem() != null) {
            String methodSelected = verMethod.getSelectedItem().toString();
            boolean isEmployeeEntered = ((SenateActivity)getActivity())
                    .findEmployee(remoteSigner.getText().toString(), ((Delivery3)getActivity()).employeeHiddenList) > -1;
            boolean isOsrNumEntered = !remoteHelpReferenceNum.getText().toString().isEmpty();

            if (methodSelected.equals("Paperwork")) {
                if (!isEmployeeEntered) {
                    Toasty.displayCenteredMessage(getActivity(), "Please enter an employee name", Toast.LENGTH_SHORT);
                    return false;
                } else {
                    return true;
                }
            } else if (methodSelected.equals("Phone Verified")) {
                if (!isEmployeeEntered) {
                    Toasty.displayCenteredMessage(getActivity(), "Please enter an employee name", Toast.LENGTH_SHORT);
                    return false;
                } else {
                    return true;
                }
            } else if (methodSelected.equals("OSR Verified")) {
                if (!isEmployeeEntered) {
                    Toasty.displayCenteredMessage(getActivity(), "Please enter an employee name", Toast.LENGTH_SHORT);
                    return false;
                }
                if (!isOsrNumEntered) {
                    Toasty.displayCenteredMessage(getActivity(), "Please enter an OSR Reference Number", Toast.LENGTH_SHORT);
                    return false;
                } else {
                    return true;
                }
            }
        }
        Toasty.displayCenteredMessage(getActivity(), "Please select a verification method.", Toast.LENGTH_SHORT);
        return false;
    }

    private void updateDeliveryInfo() {
        delivery.setVerificationMethod(verMethod.getSelectedItem().toString().split(" ")[0]);
        delivery.setVerificationComments(remoteComment.getText().toString());
        int nuxrefem = ((Delivery3) getActivity()).getEmployeeId(remoteSigner.getText().toString());
        delivery.setEmployeeId(nuxrefem);
        delivery.setHelpReferenceNum(remoteHelpReferenceNum.getText().toString());
    }

    private void returnToDelivery3() {
        ((Delivery3) getActivity()).positiveDialog();
    }

    private void updateDeliveryUsers(Transaction trans, String remoteUser) {
        if (trans.isRemoteDelivery()) {
            trans.setNaacceptby(remoteUser);
        } else {
            trans.setNareleaseby(remoteUser);
        }
    }

    private OnItemSelectedListener onlyDisplayOSRTextBoxWhenSelected = new OnItemSelectedListener(){
        @Override
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            if (verMethod.getSelectedItem() != null) {
                if (verMethod.getSelectedItem().toString().equalsIgnoreCase("OSR Verified")) {
                    remoteHelpReferenceNum.setVisibility(ClearableEditText.VISIBLE);
                } else {
                    remoteHelpReferenceNum.setVisibility(ClearableEditText.INVISIBLE);
                }
            }
        }
        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    };

}
