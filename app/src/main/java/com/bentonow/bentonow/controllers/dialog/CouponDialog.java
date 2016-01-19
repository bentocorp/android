package com.bentonow.bentonow.controllers.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.AndroidUtil;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.dao.IosCopyDao;
import com.bentonow.bentonow.listener.ListenerDialog;

public class CouponDialog extends Dialog implements View.OnClickListener {

    public static final String TAG = "CouponDialog";

    private EditText edit_coupon_input = null;
    private Button btn_coupon_accept = null;
    private Button btn_coupon_cancel = null;
    private ListenerDialog mDialogListener = null;

    public CouponDialog(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_input);
        setCancelable(true);

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        getBtnAccept().setText(IosCopyDao.get("complete-promo-button"));
        getBtnCancel().setText(IosCopyDao.get("complete-promo-cancel"));
        getEditCoupon().setHint(IosCopyDao.get("complete-promo-input-text"));

        getBtnAccept().setOnClickListener(this);
        getBtnCancel().setOnClickListener(this);


    }

    public void setCouponCode(String text) {
        try {
            getEditCoupon().setText(text);
        } catch (Exception e) {
            DebugUtils.logError(TAG, "setCouponCode: " + e);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_accept:
                if (mDialogListener != null)
                    mDialogListener.btnOkClick(getEditCoupon().getText().toString());
                dismiss();
                break;
            case R.id.btn_cancel:
                dismiss();
                break;
        }
    }

    @Override
    public void dismiss() {
        AndroidUtil.hideKeyboard(getBtnAccept());
        super.dismiss();
    }


    private Button getBtnAccept() {
        if (btn_coupon_accept == null)
            btn_coupon_accept = (Button) findViewById(R.id.button_accept);
        return btn_coupon_accept;
    }

    private Button getBtnCancel() {
        if (btn_coupon_cancel == null)
            btn_coupon_cancel = (Button) findViewById(R.id.btn_cancel);
        return btn_coupon_cancel;
    }

    private EditText getEditCoupon() {
        if (edit_coupon_input == null)
            edit_coupon_input = (EditText) findViewById(R.id.edit_coupon_input);
        return edit_coupon_input;
    }

    public void setmDialogListener(ListenerDialog mDialogListener) {
        this.mDialogListener = mDialogListener;
    }
}
