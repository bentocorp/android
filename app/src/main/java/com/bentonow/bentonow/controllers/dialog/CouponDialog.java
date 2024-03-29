package com.bentonow.bentonow.controllers.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.AndroidUtil;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.dao.IosCopyDao;
import com.bentonow.bentonow.listener.ListenerDialog;
import com.bentonow.bentonow.ui.material.ButtonFlat;
import com.rengwuxian.materialedittext.MaterialEditText;

public class CouponDialog extends Dialog implements View.OnClickListener {

    public static final String TAG = "CouponDialog";

    private MaterialEditText edit_coupon_input = null;
    private ButtonFlat btn_coupon_accept = null;
    private ButtonFlat btn_coupon_cancel = null;
    private ListenerDialog mDialogListener = null;

    public CouponDialog(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_coupon);
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
                    if (!getEditCoupon().getText().toString().trim().isEmpty())
                        mDialogListener.btnOkClick(getEditCoupon().getText().toString());
                    else
                        AndroidUtil.hideKeyboard(view);
                else
                    dismiss();
                break;
            case R.id.button_cancel:
                dismiss();
                break;
        }
    }

    @Override
    public void dismiss() {
        AndroidUtil.hideKeyboard(getBtnAccept());
        super.dismiss();
    }


    private ButtonFlat getBtnAccept() {
        if (btn_coupon_accept == null)
            btn_coupon_accept = (ButtonFlat) findViewById(R.id.button_accept);
        return btn_coupon_accept;
    }

    private ButtonFlat getBtnCancel() {
        if (btn_coupon_cancel == null)
            btn_coupon_cancel = (ButtonFlat) findViewById(R.id.button_cancel);
        return btn_coupon_cancel;
    }

    private MaterialEditText getEditCoupon() {
        if (edit_coupon_input == null)
            edit_coupon_input = (MaterialEditText) findViewById(R.id.edit_coupon_input);
        return edit_coupon_input;
    }

    public void setmDialogListener(ListenerDialog mDialogListener) {
        this.mDialogListener = mDialogListener;
    }
}
