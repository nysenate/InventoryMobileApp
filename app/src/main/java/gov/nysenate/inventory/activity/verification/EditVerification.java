package gov.nysenate.inventory.activity.verification;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import gov.nysenate.inventory.activity.SenateActivity;
import gov.nysenate.inventory.adapter.verification.EditVerificationAdapter;
import gov.nysenate.inventory.android.CancelBtnFragment;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.model.InvItem;


public class EditVerification extends SenateActivity implements CancelBtnFragment.CancelBtnOnClick {

    private List<InvItem> scannedItems;
    private EditVerificationAdapter editListAdapter;
    private ListView scannedItemsList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_verification);
        registerBaseActivityReceiver();

        TextView directions = (TextView) findViewById(R.id.directions);
        scannedItemsList = (ListView) findViewById(R.id.editVerificationList);
        scannedItems = VerScanActivity.allScannedItems;

        directions.setText(Html.fromHtml("Check the items you wish to <b>remove</b> from the verification."));

        editListAdapter = new EditVerificationAdapter(this, R.layout.two_column_and_checkbox_adapter,
                R.id.column1, scannedItems);
        scannedItemsList.setAdapter(editListAdapter);
    }

    public void onSaveBtnClick(View view) {
        if (editListAdapter.getSelectedItems().size() > 0) {
            confirmEditsDialog();
        } else {
            noEditsMadeDialog();
        }
    }

    private void saveChanges() {
        for (InvItem deleteItem : editListAdapter.getSelectedItems()) {
            if (deleteItem.getType().equalsIgnoreCase("NEW") ||
                    deleteItem.getType().equalsIgnoreCase("DIFFERENT LOCATION") ||
                    deleteItem.getType().equalsIgnoreCase("INACTIVE")) {
                VerScanActivity.newItems.remove(deleteItem);
                scannedItems.remove(deleteItem);
                VerScanActivity.invList.remove(deleteItem);
            } else {
                scannedItems.remove(deleteItem);
                VerScanActivity.unscannedItems.add(deleteItem);
                VerScanActivity.invList.add(deleteItem);
            }
        }

        onBackPressed();
    }

    @Override
    public void cancelBtnOnClick(View view) {
        onBackPressed();
    }

    private void confirmEditsDialog() {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Confirmation")
                .setMessage(Html.fromHtml("Are you sure you wish to remove <b>" + editListAdapter.getSelectedItems().size() +
                        "</b> item(s) from this verification?"))
                .setNegativeButton(Html.fromHtml("<b>Cancel</b>"), null)
                .setPositiveButton(Html.fromHtml("<b>Ok</b>"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveChanges();
                    }
                })
                .show();
    }

    private void noEditsMadeDialog() {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Confirmation")
                .setMessage(Html.fromHtml("No items are selected to be removed from this verification. <br>" +
                        "Continuing will not make any changes."))
                .setNegativeButton(Html.fromHtml("<b>Cancel</b>"), null)
                .setPositiveButton(Html.fromHtml("<b>Ok</b>"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveChanges();
                    }
                })
                .show();
    }
}
