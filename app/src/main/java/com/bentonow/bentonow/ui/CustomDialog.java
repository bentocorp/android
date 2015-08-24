package com.bentonow.bentonow.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.AndroidUtil;
import com.bentonow.bentonow.model.BackendText;

public class CustomDialog extends Dialog implements View.OnClickListener {

    EditText txt_input = null;
    Button btn_ok = null;
    Button btn_cancel = null;
    ProgressBar progressBar = null;

    public CustomDialog(Context context, String message, boolean progressbar) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_custom);
        setMessage(message);
        setCancelable(false);

        init();

        progressBar.setVisibility(progressbar ? View.VISIBLE : View.GONE);
    }

    public CustomDialog(Context context, boolean input) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(input ? R.layout.dialog_input : R.layout.dialog_custom);
        setCancelable(true);

        if (input) txt_input = (EditText) findViewById(R.id.txt_input);

        init();

        btn_ok.setText(BackendText.get("complete-promo-button"));
        btn_cancel.setText(BackendText.get("complete-promo-cancel"));
        txt_input.setHint(BackendText.get("complete-promo-input-text"));
        btn_ok.setVisibility(View.VISIBLE);
        btn_cancel.setVisibility(View.VISIBLE);
    }

    public CustomDialog(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_custom);
        setCancelable(true);

        init();

        progressBar.setVisibility(View.GONE);
    }

    public CustomDialog(Context context, String message, String acceptButton, String cancelButton) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_custom);
        setMessage(message);
        setCancelable(true);

        init();

        progressBar.setVisibility(View.GONE);

        btn_ok.setOnClickListener(this);

        btn_ok.setVisibility(acceptButton == null ? View.GONE : View.VISIBLE);

        if (acceptButton != null) btn_ok.setText(acceptButton);
        if (cancelButton == null) btn_ok.setBackgroundResource(R.drawable.bg_green_corner_bottom);

        btn_cancel = (Button) findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(this);

        btn_cancel.setVisibility(cancelButton == null ? View.GONE : View.VISIBLE);

        if (cancelButton != null) btn_cancel.setText(cancelButton);
        if (acceptButton == null)
            btn_cancel.setBackgroundResource(R.drawable.bg_green_corner_bottom);
    }

    void init() {
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        btn_ok = (Button) findViewById(R.id.btn_ok);
        if (btn_ok != null) btn_ok.setVisibility(View.GONE);

        btn_cancel = (Button) findViewById(R.id.btn_cancel);
        if (btn_cancel != null) btn_cancel.setVisibility(View.GONE);
    }

    public void setMessage(String message) {
        try {
            ((TextView) findViewById(R.id.txt_message)).setText(message);
        } catch (Exception ignored) {
        }
    }

    public void setText(String text) {
        try {
            txt_input.setText(text);
        } catch (Exception ignored) {
        }
    }

    public String getText() {
        try {
            return txt_input.getText().toString();
        } catch (Exception ignored) {
            return null;
        }
    }

    public EditText getEditText() {
        try {
            return txt_input;
        } catch (Exception ignored) {
            return null;
        }
    }

    public void setOnCancelPressed(final View.OnClickListener listener) {
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(v);
                dismiss();
            }
        });
    }

    public void setOnOkPressed(final View.OnClickListener listener) {
        if (txt_input != null) {
            txt_input.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (event != null) {
                        // if shift key is down, then we want to insert the '\n' char in the TextView;
                        // otherwise, the default action is to send the message.
                        if (!event.isShiftPressed()) {
                            listener.onClick(btn_ok);
                            return true;
                        }
                        return false;
                    }

                    listener.onClick(btn_ok);
                    return true;
                }
            });
        }
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(v);
                dismiss();
            }
        });
    }

    @Override
    public void onClick(View view) {
        dismiss();
    }

    @Override
    public void dismiss() {
        AndroidUtil.hideKeyboard(btn_ok);
        super.dismiss();
    }
}
