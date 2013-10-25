package gov.nysenate.inventory.android;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;

public class ClearableAutoCompleteTextView extends AutoCompleteTextView
{

    public String defaultValue = "";
    final Drawable imgX = getResources().getDrawable(
            android.R.drawable.ic_delete); // X image
    boolean showClearMsg = false;
    Context context = null;
    boolean clearField = true;
    private String clearMsg = "Do you want to clear this field?";
    List<ClearButtonListener> listeners = new ArrayList<ClearButtonListener>();

    
    public ClearableAutoCompleteTextView(Context context) {
        super(context);

        this.context = context;

        init();
    }

    public ClearableAutoCompleteTextView(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);

        this.context = context;

        init();
    }

    public ClearableAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;

        init();
    }

    void init() {

        // Set bounds of our X button
        imgX.setBounds(0, 0, imgX.getIntrinsicWidth(),
                imgX.getIntrinsicHeight());

        // There may be initial text in the field, so we may need to display the
        // button
        manageClearButton();

        this.setOnTouchListener(new OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                final ClearableAutoCompleteTextView et = ClearableAutoCompleteTextView.this;

                // Is there an X showing?
                if (et.getCompoundDrawables()[2] == null) {
                    clearField = false;
                    return false;
                }
                // Only do this for up touches
                if (event.getAction() != MotionEvent.ACTION_UP)
                    return false;
                // Is touch on our clear button?
                if (event.getX() > et.getWidth() - et.getPaddingRight()
                        - imgX.getIntrinsicWidth()) {
                    clearField = true;
                    if (showClearMsg) {
                        clearField = false;
                        // super.getContext()
                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                context);
                        // Add the buttons
                        builder.setMessage(clearMsg)
                                .setPositiveButton(Html.fromHtml(getResources().getString(R.string.ok_button)),
                                        new DialogInterface.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int id) {
                                                // User clicked OK button
                                                clearField = true;
                                                et.setText("");
                                                ClearableAutoCompleteTextView.this
                                                        .removeClearButton();

                                            }
                                        })
                                .setNegativeButton(Html.fromHtml(getResources().getString(R.string.cancel_button)),
                                        new DialogInterface.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int id) {
                                                // User cancelled the dialog
                                                clearField = false;
                                            }
                                        });

                        // Create the AlertDialog
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                    if (clearField) {
                        et.setText("");
                        ClearableAutoCompleteTextView.this.removeClearButton();
                        for (ClearButtonListener clearButtonListener : listeners)
                            clearButtonListener.onClearButtonPressed((AdapterView) v, v);
                    }

                } else {
                    clearField = false;
                }

                return false;
            }
        });

        this.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                    int count) {

                ClearableAutoCompleteTextView.this.manageClearButton();
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
            }
        });
    }

    public void showClearMsg(boolean showClearMsg) {
        this.showClearMsg = showClearMsg;
    }

    public boolean getShowClearMsg() {
        return showClearMsg;
    }

    public void setClearMsg(String clearMsg) {
        this.clearMsg = clearMsg;
    }

    public String getClearMsg() {
        return this.clearMsg;
    }

    void manageClearButton() {
        if (this.getText().toString().equals(""))
            removeClearButton();
        else
            addClearButton();
    }

    void addClearButton() {
        this.setCompoundDrawables(this.getCompoundDrawables()[0],
                this.getCompoundDrawables()[1], imgX,
                this.getCompoundDrawables()[3]);
    }

    void removeClearButton() {
        this.setCompoundDrawables(this.getCompoundDrawables()[0],
                this.getCompoundDrawables()[1], null,
                this.getCompoundDrawables()[3]);
    }

    public void addClearButtonListener(ClearButtonListener listener) {
        listeners.add(listener);
    }
    
    
}