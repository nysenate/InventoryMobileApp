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
import android.view.View.OnFocusChangeListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import gov.nysenate.inventory.activity.SenateActivity;
import gov.nysenate.inventory.adapter.KeywordListViewAdapter;
import gov.nysenate.inventory.listener.OnKeywordChangeListener;

//...
@SuppressLint("ValidFragment")
public class KeywordDialog extends DialogFragment {

    SenateActivity senateActivity;
    public static ListView lvKeywords = null;
    public static Button btnAddKeyword = null;
    public static EditText etDummy = null;
    /*
     * Dummy Edit Text because Dialog
     * Fragment needs at least one
     * Navigable Edit Text on the main
     * Fragment (ListView Edit Texts
     * don't count) so the soft keyboard
     * will display in the foreground.
     */
    public String title = null;
    public String msg = null;
    public String keywordList = null;
    public String keywordListOrig = null;
    public String[] originalKeywordList = null;
    private KeywordListViewAdapter adapter = null;
    NewInvDialog newInvDialog = null;
    List<OnKeywordChangeListener> listeners = new ArrayList<OnKeywordChangeListener>();
    int position = -1;

    public KeywordDialog(SenateActivity senateActivity,
                         NewInvDialog newInvDialog, String title, String msg,
                         String keywordList) {
        this.senateActivity = senateActivity;
        this.title = title;
        this.msg = msg;
        this.keywordList = keywordList;
        this.keywordListOrig = keywordList;
        this.originalKeywordList = keywordList.split(",");
        this.newInvDialog = newInvDialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_keyword, null);
        btnAddKeyword = (Button) dialogView.findViewById(R.id.btnAddKeyword);
        lvKeywords = (ListView) dialogView.findViewById(R.id.lvKeywords);
        etDummy = (EditText) dialogView.findViewById(R.id.etDummy);
        adapter = new KeywordListViewAdapter(
                senateActivity.getApplicationContext(), newInvDialog,
                R.layout.row_keyword, arrayListFromString(this.keywordList));
        lvKeywords.setAdapter(adapter);

        etDummy.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (adapter.getCount() == 0) {
                        adapter.add(" ");
                        adapter.returnToSelectedRow();
                    } else {
                        adapter.returnToSelectedRow();
                    }
                }
            }
        });

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogView)
                .setTitle(
                        Html.fromHtml("<font color='#000055'>" + title
                                + "</font>"))
                .setMessage(Html.fromHtml(msg))
                .setPositiveButton(Html.fromHtml("<b>OK</b>"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        adapter.notifyDataSetChanged();
                        senateActivity.dialogKeywords = adapter.toString();
                        if (!senateActivity.dialogKeywords
                                .equalsIgnoreCase(keywordListOrig)) {
                            for (OnKeywordChangeListener onKeywordChangeListener : listeners) {
                                onKeywordChangeListener.OnKeywordChange(
                                        KeywordDialog.this, lvKeywords,
                                        senateActivity.dialogKeywords);
                            }
                        }

                        dismiss();
                    }
                })
                .setNegativeButton(Html.fromHtml("<b>Cancel</b>"),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dismiss();
                            }
                        });

        // Create the AlertDialog object and return it
        AlertDialog dialog = builder.create();
        dialog.getWindow().clearFlags(
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        dialog.setCanceledOnTouchOutside(false);
        // Create the AlertDialog object and return it
        return dialog;
    }

    public void clearListenters() {
        listeners = new ArrayList<OnKeywordChangeListener>();
    }

    public int originalKeyword(String keyword) {
        for (int x = 0; x < originalKeywordList.length; x++) {
            if (originalKeywordList[x].equalsIgnoreCase(keyword)) {
                return x;
            }
        }
        return -1;
    }

    public void addListener(OnKeywordChangeListener addListener) {
        listeners.add(addListener);
    }

    public boolean isListening(OnKeywordChangeListener addListener) {
        if (listeners == null || listeners.size() == 0) {
            return false;
        } else {
            for (int x = 0; x < listeners.size(); x++) {
                if (listeners.get(x) == addListener) {
                    return true;
                }
            }
            return false;
        }

    }

    public ArrayList<String> arrayListFromString(String values) {
        String[] valuesList = values.split(",");
        ArrayList<String> items = new ArrayList<String>();
        for (int x = 0; x < valuesList.length; x++) {
            String currentValue = valuesList[x].trim();
            if (currentValue.length() > 0) {
                items.add(currentValue);
            }
        }
        return items;
    }

    public KeywordListViewAdapter getAdapter() {
        return adapter;
    }

}
