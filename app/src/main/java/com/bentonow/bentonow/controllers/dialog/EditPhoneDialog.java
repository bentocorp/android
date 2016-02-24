package com.bentonow.bentonow.controllers.dialog;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.AndroidUtil;
import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.listener.ListenerDialog;
import com.bentonow.bentonow.ui.material.ButtonFlat;
import com.rengwuxian.materialedittext.MaterialEditText;

public class EditPhoneDialog extends DialogFragment implements Animation.AnimationListener, View.OnClickListener {

    public static final String TAG = "EditPhoneDialog";

    private View rootView;
    private RelativeLayout mainLayout;
    private MaterialEditText editTextPhoneNumber = null;
    private ButtonFlat btnChange = null;
    private ButtonFlat btnCancel = null;

    private Animation anim;

    private ListenerDialog mListenerDialog;

    private int numBacks = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.Theme_Dialog_Transparent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {

                if (keyCode == KeyEvent.KEYCODE_BACK && numBacks == 0) {
                    onDismissDialog();

                    return true;
                }
                return false;
            }
        });

        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        rootView = inflater.inflate(R.layout.dialog_edit_phone, container, false);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);

        getBtnChange().setOnClickListener(this);
        getBtnCancel().setOnClickListener(this);

        getEditTextPhoneNumber().addTextChangedListener(new TextWatcher() {
            int oldLength;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String phone = getEditTextPhoneNumber().getText().toString().replaceAll("[^0-9]", "");

                if (oldLength != phone.length()) {
                    oldLength = phone.length();

                    getEditTextPhoneNumber().setText(BentoNowUtils.getPhoneFromNumber(getEditTextPhoneNumber().getText().toString()));
                    getEditTextPhoneNumber().setSelection(getEditTextPhoneNumber().getText().length());
                }

            }
        });


        anim = AnimationUtils.loadAnimation(getActivity(), R.anim.fadein);
    }

    private void onDismissDialog() {
        if (numBacks == 0) {
            numBacks++;
        }
    }

    private boolean isValidPhoneNumber() {
        if (BentoNowUtils.validPhoneNumber(getEditTextPhoneNumber().getText().toString())) {
            return true;
        } else {
            getEditTextPhoneNumber().setError(getResources().getString(R.string.alert_error_enter_valid_number));
            return false;
        }
    }

    @Override
    public void onStart() {
        getLayoutMain().setAnimation(anim);
        numBacks = 0;
        super.onStart();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_cancel:
                dismiss();
                break;
            case R.id.button_change:
                if (isValidPhoneNumber() && mListenerDialog != null) {
                    mListenerDialog.btnOkClick(getEditTextPhoneNumber().getText().toString());
                    dismiss();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        getDialog().dismiss();
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
    }

    @Override
    public void onAnimationStart(Animation animation) {
    }

    @Override
    public void dismiss() {
        AndroidUtil.hideKeyboard(getLayoutMain());
        super.dismiss();
    }

    public void setmListenerDialog(ListenerDialog mListenerDialog) {
        this.mListenerDialog = mListenerDialog;
    }

    public RelativeLayout getLayoutMain() {
        if (mainLayout == null)
            mainLayout = (RelativeLayout) rootView.findViewById(R.id.dialog_rootView);
        return mainLayout;
    }

    public ButtonFlat getBtnChange() {
        if (btnChange == null)
            btnChange = (ButtonFlat) rootView.findViewById(R.id.button_change);
        return btnChange;
    }

    public ButtonFlat getBtnCancel() {
        if (btnCancel == null)
            btnCancel = (ButtonFlat) rootView.findViewById(R.id.button_cancel);
        return btnCancel;
    }

    private MaterialEditText getEditTextPhoneNumber() {
        if (editTextPhoneNumber == null)
            editTextPhoneNumber = (MaterialEditText) rootView.findViewById(R.id.edit_text_phone);
        return editTextPhoneNumber;
    }

}
