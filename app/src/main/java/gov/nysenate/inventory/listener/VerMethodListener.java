package gov.nysenate.inventory.listener;

import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import gov.nysenate.inventory.android.ClearableAutoCompleteTextView;
import gov.nysenate.inventory.android.ClearableEditText;

public class VerMethodListener implements AdapterView.OnItemSelectedListener {

    private Spinner verMethod;
    private ClearableEditText remoteHelpReferenceNum;
    private ClearableAutoCompleteTextView remoteSigner;

    public VerMethodListener(Spinner verMethod, ClearableEditText helpRefNum, ClearableAutoCompleteTextView signer) {
        this.verMethod = verMethod;
        remoteHelpReferenceNum = helpRefNum;
        remoteSigner = signer;
    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        if (verMethod.getSelectedItem() != null) {
            if (verMethod.getSelectedItem().toString().equalsIgnoreCase("OSR Verified")) {
                remoteHelpReferenceNum.setVisibility(ClearableEditText.VISIBLE);
                remoteSigner.setHint("Name of Employee Verifying Remote Transaction");
            } else if (verMethod.getSelectedItem().toString().equalsIgnoreCase("Phone Verified")) {
                remoteSigner.setHint("Name of Employee Verifying Remote Transaction");
                remoteHelpReferenceNum.setVisibility(ClearableEditText.INVISIBLE);
            } else {
                remoteHelpReferenceNum.setVisibility(ClearableEditText.INVISIBLE);
                remoteSigner.setHint("Enter Remote Employee Signer");
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }
}
