package gov.nysenate.inventory.android;

import gov.nysenate.inventory.activity.SenateActivity;
import gov.nysenate.inventory.adapter.CommodityListViewAdapter;
import gov.nysenate.inventory.listener.ClearButtonListener;
import gov.nysenate.inventory.listener.CommodityDialogListener;
import gov.nysenate.inventory.listener.OnKeywordChangeListener;
import gov.nysenate.inventory.model.Commodity;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

//...G
@SuppressLint("ValidFragment")
public class NewInvDialog extends DialogFragment implements
        OnKeywordChangeListener, ClearButtonListener
{

    public static ClearableTextView tvKeywordsToBlock;
    public static ClearableEditText etNewItemComments;
    public static ProgressBar progBarNewInvItem;
    public static ImageView btnKeywordSpeech;
    public static TextView tvMsg;
    private int msgGravity = -1;
    SenateActivity senateActivity;
    public static ListView commodityList = null;
    public String title = null;
    public String msg = null;
    public final int MODE_KEYWORD_SEARCH = -200;
    public final int MODE_COMMODITY_PICKED = -201;

    GestureDetector gestureDectector = null;
    List<CommodityDialogListener> listeners = new ArrayList<CommodityDialogListener>();
    Commodity currentCommodity = null;
    public CommodityListViewAdapter adapter = null;
    int position = -1;
    String nusenate = null;
    public int currentMode = MODE_KEYWORD_SEARCH;

    @SuppressLint("ValidFragment")
    public NewInvDialog(SenateActivity senateActivity, String title, String msg) {
        this.senateActivity = senateActivity;
        this.title = title;
        this.msg = msg;
    }

    @SuppressLint("ValidFragment")
    public NewInvDialog(SenateActivity senateActivity, String title, String msg, int msgGravity) {
        this.senateActivity = senateActivity;
        this.title = title;
        this.msg = msg;
        this.msgGravity = msgGravity;
    }
    
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_newinvitem, null);
        tvKeywordsToBlock = (ClearableTextView) dialogView
                .findViewById(R.id.tvKeywordsToBlock);
        commodityList = (ListView) dialogView.findViewById(R.id.searchResults);
        commodityList.setOnItemClickListener(new OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                if (position < 0) {
                    return;
                }

                adapter = (CommodityListViewAdapter) commodityList.getAdapter();

                currentCommodity = adapter.getCommodityAt(position);
                adapter.setRowSelected(position);
                flipMode();

            }

        });

        etNewItemComments = (ClearableEditText) dialogView
                .findViewById(R.id.etNewItemComments);
        progBarNewInvItem = (ProgressBar) dialogView
                .findViewById(R.id.progBarNewInvItem);
        btnKeywordSpeech = (ImageView) dialogView
                .findViewById(R.id.btnKeywordSpeech);
        tvMsg = (TextView) dialogView
                .findViewById(R.id.tvMsg);

        if (senateActivity.dialogKeywords != null) {
            tvKeywordsToBlock.setText(senateActivity.dialogKeywords);
            senateActivity.getDialogDataFromServer();
            checkKeywordsFound();
            senateActivity.dialogKeywords = null;
        }
        if (senateActivity.dialogComments != null) {
            etNewItemComments.setText(senateActivity.dialogComments);
            senateActivity.dialogComments = null;
        }
        adapter = (CommodityListViewAdapter) commodityList.getAdapter();

        tvMsg.setText(Html.fromHtml(msg));
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogView)
                .setTitle(
                        Html.fromHtml("<font color='#000055'>" + title
                                + "</font>"))
                .setCancelable(false)
                .setPositiveButton( Html.fromHtml("<b>Save Tag# Info</b>"), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Don't write any code here.. but coded here for now
                        if (adapter != null && adapter.getRowSelected() > -1
                                && adapter.getCount() > 0) {
                            currentCommodity.setDecomments(etNewItemComments
                                    .getText().toString());
                            for (CommodityDialogListener commodityDialogListener : listeners)
                                commodityDialogListener.commoditySelected(
                                        position, currentCommodity);
                            dialog.dismiss();
                        } else if (etNewItemComments.getText().toString()
                                .trim().length() > 0) {
                            currentCommodity = new Commodity();
                            currentCommodity.setDecomments(etNewItemComments
                                    .getText().toString());
                            for (CommodityDialogListener commodityDialogListener : listeners)
                                commodityDialogListener.commoditySelected(
                                        position, currentCommodity);
                            dialog.dismiss();
                        } else {
                            int duration = Toast.LENGTH_SHORT;

                            Toast toast = Toast.makeText(
                                    senateActivity.getApplicationContext(),
                                    "!!ERRROR: No Commodity Code selected or Comments entered.",
                                    duration);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        }

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

        /*
         * gestureDectector = new GestureDetector(senateActivity, new
         * GestureListener()); commodityList.setOnTouchListener(new
         * OnTouchListener() {
         * 
         * @Override public boolean onTouch(View v, MotionEvent event) {
         * if(position<0) { return false; }
         * 
         * adapter = (CommodityListViewAdapter)commodityList.getAdapter();
         * 
         * currentCommodity = adapter.getCommodityAt(position);
         * adapter.setRowSelected(position); flipMode(); return false; }
         * 
         * });
         */

        this.setStyle(STYLE_NO_FRAME, android.R.style.Theme_Holo);
        tvKeywordsToBlock.addClearButtonListener(this);

        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        
        if (msgGravity!=-1) {
            tvMsg.setGravity(msgGravity);
        }

        // Create the AlertDialog object and return it
        return dialog;
    }

    public void setNusenate(String nusenate) {
        this.nusenate = nusenate;
    }

    public String getNusenate() {
        return this.nusenate;
    }

    public void addListener(CommodityDialogListener addListener) {
        listeners.add(addListener);
    }

    public void checkKeywordsFound() {
        String keywords = tvKeywordsToBlock.getText().toString();
        String[] keywordList = keywords.split(",");
        StringBuilder s = new StringBuilder();
        adapter = (CommodityListViewAdapter) commodityList.getAdapter();
        int currentWordCount = 0;
        for (int x = 0; x < keywordList.length; x++) {
            currentWordCount = adapter.wordRowCount(keywordList[x]);

            if (currentWordCount > 0) {
                s.append("<font color='#005500'><b>");
            } else {
                s.append("<font color='red'>");
            }

            s.append(keywordList[x]);

            if (currentWordCount > 0) {
                s.append("</b></font>");
            } else {
                s.append("</font>");
            }

            if (x != keywordList.length - 1) {
                s.append(",");
            }

        }
        tvKeywordsToBlock.setText(Html.fromHtml(s.toString()));
        if (senateActivity.dialogSelectedRow > -1) {
            adapter.setRowSelected(senateActivity.dialogSelectedRow);
            currentCommodity = adapter
                    .getCommodityAt(senateActivity.dialogSelectedRow);
            senateActivity.dialogSelectedRow = -1;
        }
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

                        if (adapter == null) {
                            senateActivity.dialogSelectedRow = -1;
                        } else {
                            senateActivity.dialogSelectedRow = adapter.getRowSelected();
                        }
                        senateActivity.dialogComments = etNewItemComments
                                .getText().toString();
                        senateActivity.dialogKeywords = tvKeywordsToBlock
                                .getText().toString();

                        senateActivity.reOpenNewInvDialog();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    /*
     * public class GestureListener extends
     * GestureDetector.SimpleOnGestureListener {
     * 
     * public boolean onDown(MotionEvent e) { return true; } public boolean
     * onSingeTap(MotionEvent e) { // Notify everybody that may be interested.
     * return true; }
     * 
     * public boolean onDoubleTap(MotionEvent e) { position =
     * commodityList.pointToPosition((int)e.getX(), (int)e.getY());
     * 
     * if(position<0) { return false; }
     * 
     * adapter = (CommodityListViewAdapter)commodityList.getAdapter();
     * 
     * currentCommodity = adapter.getCommodityAt(position);
     * adapter.setRowSelected(position); flipMode(); return true; } }
     */

    public void flipMode() {
        if (currentMode == MODE_KEYWORD_SEARCH) {
            setMode(MODE_COMMODITY_PICKED);
        } else {
            setMode(MODE_KEYWORD_SEARCH);
        }
    }

    public void setMode(int setMode) {
        currentMode = setMode;
        if (currentMode == MODE_KEYWORD_SEARCH) {
            tvKeywordsToBlock.setText("");
            tvKeywordsToBlock.setBackgroundColor(0);
            adapter.clearData();
            btnKeywordSpeech.setVisibility(View.VISIBLE);
        } else {
            Commodity currentCommodity = adapter.getCommodityAt(adapter
                    .getRowSelected());
            tvKeywordsToBlock.setText(Html.fromHtml(currentCommodity
                    .getCdcommodty()
                    + " &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
                    + currentCommodity.getDecommodityf()));
            tvKeywordsToBlock.setBackgroundColor(senateActivity
                    .getApplicationContext().getResources()
                    .getColor(R.color.blueveryverylight));
            btnKeywordSpeech.setVisibility(View.INVISIBLE);
        }
        setDisplayBasedonCurrentMode();
    }

    public void setDisplayBasedonCurrentMode() {
        if (currentMode == MODE_COMMODITY_PICKED) {
            commodityList.setVisibility(View.INVISIBLE);
        } else {
            commodityList.setVisibility(View.VISIBLE);
        }
        View v = this.getDialog().findViewById(R.id.dgNewInvItem);
        v.invalidate();
    }

    @Override
    public void OnKeywordChange(KeywordDialog keywordDialog,
            ListView lvKeywords, String keywords) {
        // Log.i("OnKeywordChange",
        // "keywords:"+keywords+"    senateActivity.dialogKeywords:"+senateActivity.dialogKeywords
        // );
        tvKeywordsToBlock.setText(senateActivity.dialogKeywords);
        senateActivity.getDialogDataFromServer();
        checkKeywordsFound();
        senateActivity.dialogKeywords = null;

    }

    @Override
    public void onClearButtonPressed(AdapterView parent, View view) {
        if (view == tvKeywordsToBlock) {
            this.flipMode();
        }
    }

    class OKButtonListener implements View.OnClickListener
    {
        private final Dialog dialog;

        public OKButtonListener(Dialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void onClick(View v) {
            if (adapter != null && adapter.getRowSelected() > -1
                    && adapter.getCount() > 0) {
                currentCommodity.setDecomments(etNewItemComments.getText()
                        .toString());
                for (CommodityDialogListener commodityDialogListener : listeners)
                    commodityDialogListener.commoditySelected(position,
                            currentCommodity);
                dialog.dismiss();
            } else if (etNewItemComments.getText().toString().trim().length() > 0) {
                currentCommodity = new Commodity();
                currentCommodity.setDecomments(etNewItemComments.getText()
                        .toString());
                for (CommodityDialogListener commodityDialogListener : listeners)
                    commodityDialogListener.commoditySelected(position,
                            currentCommodity);
                dialog.dismiss();
            } else {
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast
                        .makeText(
                                senateActivity.getApplicationContext(),
                                "!!ERRROR: No Commodity Code selected or Comments entered.",
                                duration);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }

        }
    }

}