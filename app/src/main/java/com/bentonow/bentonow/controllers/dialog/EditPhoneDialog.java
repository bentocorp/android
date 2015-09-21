package com.bentonow.bentonow.controllers.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.AndroidUtil;
import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.BentoRestClient;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.listener.ListenerDialog;
import com.bentonow.bentonow.model.BackendText;
import com.bentonow.bentonow.model.Settings;
import com.bentonow.bentonow.model.User;
import com.google.gson.Gson;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;

public class EditPhoneDialog extends DialogFragment implements Animation.AnimationListener, View.OnClickListener, View.OnFocusChangeListener {

    public static final String TAG = "EditPhoneDialog";

    private View rootView;
    private FrameLayout mainLayout;
    private RelativeLayout layoutAlertError;
    private EditText editTextPhoneNumber = null;
    private EditText editTextConfirmPhone = null;
    private Button btnChange = null;
    private Button btnCancel = null;
    private TextView txtAlertError = null;

    private Animation anim;
    private Animation animOut;

    private ListenerDialog mListenerDialog;

    private int numBacks = 0;
    private boolean bChangePhone;

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

        rootView = inflater.inflate(R.layout.dialog_edit_phone, container, false);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);

        getBtnChange().setOnClickListener(this);
        getBtnCancel().setOnClickListener(this);

        getEditTextPhoneNumber().setOnFocusChangeListener(this);
        getEditTextConfirmPhone().setOnFocusChangeListener(this);

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
                getEditTextPhoneNumber().setTextColor(getResources().getColor(R.color.gray));
                String phone = getEditTextPhoneNumber().getText().toString().replaceAll("[^0-9]", "");

                if (oldLength != phone.length()) {
                    oldLength = phone.length();

                    getEditTextPhoneNumber().setText(BentoNowUtils.getPhoneFromNumber(getEditTextPhoneNumber().getText().toString()));
                    getEditTextPhoneNumber().setSelection(getEditTextPhoneNumber().getText().length());

                    validate(getEditTextPhoneNumber());
                }
            }
        });

        getEditTextConfirmPhone().addTextChangedListener(new TextWatcher() {
            int oldLength;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                getEditTextConfirmPhone().setTextColor(getResources().getColor(R.color.gray));
                String phone = getEditTextConfirmPhone().getText().toString().replaceAll("[^0-9]", "");

                if (oldLength != phone.length()) {
                    oldLength = phone.length();

                    getEditTextConfirmPhone().setText(BentoNowUtils.getPhoneFromNumber(getEditTextConfirmPhone().getText().toString()));
                    getEditTextConfirmPhone().setSelection(getEditTextConfirmPhone().getText().length());

                    validate(getEditTextConfirmPhone());
                }
            }
        });

        anim = AnimationUtils.loadAnimation(getActivity(), R.anim.fadein);
        animOut = AnimationUtils.loadAnimation(getActivity(), R.anim.fadeout);
        animOut.setAnimationListener(this);
    }

    private void onDismissDialog() {
        if (numBacks == 0) {
            getLayoutMain().startAnimation(animOut);
            numBacks++;
        }
    }

    private void validate(EditText view) {
        if (BentoNowUtils.validPhoneNumber(view.getText().toString())) {
            getLayoutAlertError().setVisibility(View.INVISIBLE);
            view.setTextColor(getResources().getColor(R.color.gray));
            validateMatchPhones();
        } else {
            getTxtAlertError().setText(getResources().getText(R.string.alert_error_enter_valid_number));
            getLayoutAlertError().setVisibility(View.VISIBLE);
            view.setTextColor(getResources().getColor(R.color.orange));
        }
    }

    private void validateMatchPhones() {
        if (BentoNowUtils.validPhoneNumber(getEditTextPhoneNumber().getText().toString()) && BentoNowUtils.validPhoneNumber(getEditTextConfirmPhone().getText().toString()))
            if (getEditTextPhoneNumber().getText().toString().equals(getEditTextConfirmPhone().getText().toString())) {
                getLayoutAlertError().setVisibility(View.INVISIBLE);
                bChangePhone = true;
            } else {
                getTxtAlertError().setText(getResources().getText(R.string.alert_error_mismatch));
                getLayoutAlertError().setVisibility(View.VISIBLE);
                bChangePhone = false;
            }
    }

    @Override
    public void onStart() {
        getLayoutMain().setAnimation(anim);
        numBacks = 0;
        super.onStart();
    }


    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.edit_text_phone_number:
                getEditTextPhoneNumber().setTextSize(TypedValue.COMPLEX_UNIT_SP, hasFocus ? 18 : 14);
                break;
            case R.id.edit_text_confirm_phone:
                getEditTextConfirmPhone().setTextSize(TypedValue.COMPLEX_UNIT_SP, hasFocus ? 18 : 14);
                break;
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancel:
                onDismissDialog();
                break;
            case R.id.btn_ok:
                if (bChangePhone && mListenerDialog != null){
                    mListenerDialog.btnOkClick(getEditTextPhoneNumber().getText().toString());
                    onDismissDialog();
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

    public FrameLayout getLayoutMain() {
        if (mainLayout == null)
            mainLayout = (FrameLayout) rootView.findViewById(R.id.main_layout);
        return mainLayout;
    }

    public RelativeLayout getLayoutAlertError() {
        if (layoutAlertError == null)
            layoutAlertError = (RelativeLayout) rootView.findViewById(R.id.layout_alert_error);
        return layoutAlertError;
    }

    public Button getBtnChange() {
        if (btnChange == null)
            btnChange = (Button) rootView.findViewById(R.id.btn_ok);
        return btnChange;
    }

    public Button getBtnCancel() {
        if (btnCancel == null)
            btnCancel = (Button) rootView.findViewById(R.id.btn_cancel);
        return btnCancel;
    }

    public EditText getEditTextConfirmPhone() {
        if (editTextConfirmPhone == null)
            editTextConfirmPhone = (EditText) rootView.findViewById(R.id.edit_text_confirm_phone);
        return editTextConfirmPhone;
    }

    private EditText getEditTextPhoneNumber() {
        if (editTextPhoneNumber == null)
            editTextPhoneNumber = (EditText) rootView.findViewById(R.id.edit_text_phone_number);
        return editTextPhoneNumber;
    }


    private TextView getTxtAlertError() {
        if (txtAlertError == null)
            txtAlertError = (TextView) rootView.findViewById(R.id.alert_error_phone);
        return txtAlertError;
    }

}
