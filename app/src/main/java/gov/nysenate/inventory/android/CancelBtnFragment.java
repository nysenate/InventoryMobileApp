package gov.nysenate.inventory.android;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class CancelBtnFragment extends Fragment implements View.OnClickListener
{
    private CancelBtnOnClick clickHandler;

    public interface CancelBtnOnClick {
        public void cancelBtnOnClick(View v);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof CancelBtnOnClick) {
            clickHandler = (CancelBtnOnClick) activity;
        } else {
            throw new ClassCastException(activity.toString() + " must implement CancelBtnFragment.CancelBtnOnClick.");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cancel_btn_fragment, container);
        Button cancelBtn = (Button) view.findViewById(R.id.cancel_btn);
        cancelBtn.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        clickHandler.cancelBtnOnClick(v);
    }
}
