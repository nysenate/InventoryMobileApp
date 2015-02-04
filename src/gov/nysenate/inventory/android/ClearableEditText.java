package gov.nysenate.inventory.android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import gov.nysenate.inventory.activity.LoginActivity;
import gov.nysenate.inventory.activity.SenateActivity;
import gov.nysenate.inventory.listener.ClearButtonListener;

import java.util.ArrayList;
import java.util.List;

public class ClearableEditText extends EditText
{

    public String defaultValue = "";
    final Drawable imgX = getResources().getDrawable(
            android.R.drawable.ic_delete); // X image
    private boolean showClearMsg = false;
    Context context = null;
    public boolean clearField = true;
    private boolean suppressEnter = false;    
    private String clearMsg = "Do you want to clear this field?";
    List<ClearButtonListener> listeners = new ArrayList<ClearButtonListener>();    
    CountDownTimer timer = new CountDownTimer(1 *60 * 1000, 1000) {
		
		@Override
		public void onTick(long millisUntilFinished) {
			// TODO Auto-generated method stub
			System.out.println(this.getClass().getSimpleName());
			System.out.println(millisUntilFinished/1000);
		}
		
		@Override
		public void onFinish() {
			// TODO Auto-generated method stub
			if(!this.getClass().getSimpleName().equalsIgnoreCase("LoginActivity"))
			{
				Intent intentTimeout = new Intent(context, LoginActivity.class);
				intentTimeout.addFlags(200);
				context.startActivity(intentTimeout);
			}
	        
			
		}
		
	};
	
    OnKeyListener suppressEnterTab = new OnKeyListener() {

        @Override
        public boolean onKey (View v, int keyCode, KeyEvent event) {
            // TODO Auto-generated method stub
            if (event.getAction() == KeyEvent.ACTION_DOWN
                    && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER || event.getKeyCode() == KeyEvent.KEYCODE_TAB) ) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                        Log.i("event", "ENTER captured");
                }
                if (event.getKeyCode() == KeyEvent.KEYCODE_TAB) {
                    Log.i("event", "TAB captured");
            }

                return false;
            } 
            return false;
        }
    };    

    public ClearableEditText(Context context) {
        super(context);

        this.context = context;

        init();
    }

    public ClearableEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        this.context = context;

        init();
    }

    public ClearableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;

        init();
    }

    void init() {
        this.setOnKeyListener(suppressEnterTab);
        suppressEnter = true;
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

                final ClearableEditText et = ClearableEditText.this;

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
                                                ClearableEditText.this
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
                        ClearableEditText.this.removeClearButton();
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

                ClearableEditText.this.manageClearButton();
                SenateActivity.timer.cancel();
                if(!SenateActivity.getCurrentActivity().equalsIgnoreCase("LoginActivity"))
                SenateActivity.timer.start();
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

    void manageClearButton() {
        if (this.getText().toString().equals("")) {
            removeClearButton();
        } else {
            addClearButton();
        }
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

    public void addClearButton() {
        this.setCompoundDrawables(this.getCompoundDrawables()[0],
                this.getCompoundDrawables()[1], imgX,
                this.getCompoundDrawables()[3]);
    }

    public void removeClearButton() {
        this.setCompoundDrawables(this.getCompoundDrawables()[0],
                this.getCompoundDrawables()[1], null,
                this.getCompoundDrawables()[3]);
    }

    public void addClearButtonListener(ClearButtonListener listener) {
        listeners.add(listener);
    }

    public boolean isEnterTabSuppressed() {
        return suppressEnter;
        
    }
    
    public void suppressEnterTabKey() {
        this.setOnKeyListener(suppressEnterTab);
        suppressEnter = true;
    }
    
    public void allowEnterTabKey() {
        this.setOnKeyListener(null);
        suppressEnter = false;
    }      
}