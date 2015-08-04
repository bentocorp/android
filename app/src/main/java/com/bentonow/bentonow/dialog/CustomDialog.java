package com.bentonow.bentonow.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.bentonow.bentonow.R;

public class CustomDialog extends Dialog implements View.OnClickListener {

    public CustomDialog (Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_custom);
        setCancelable(true);

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        View btn;

        btn = findViewById(R.id.btn_ok);
        if (btn != null) btn.setOnClickListener(this);

        btn = findViewById(R.id.btn_cancel);
        if (btn != null) btn.setOnClickListener(this);
    }

    public CustomDialog (Context context, String message, String acceptButton, String cancelButton) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_custom);
        setMessage(message);
        setCancelable(false);

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        View btn;

        btn = findViewById(R.id.btn_ok);
        btn.setOnClickListener(this);

        if (acceptButton == null) btn.setVisibility(View.GONE);
        else if (cancelButton == null) btn.setBackgroundResource(R.drawable.bg_green_corner_bottom);

        btn = findViewById(R.id.btn_cancel);
        btn.setOnClickListener(this);

        if (cancelButton == null) btn.setVisibility(View.GONE);
        else if (acceptButton == null) btn.setBackgroundResource(R.drawable.bg_green_corner_bottom);
    }

    public void setMessage (String message) {
        try {
            ((TextView) findViewById(R.id.txt_message)).setText(message);
        } catch (Exception ignored) {}
    }

    public void setText (String text) {
        try {
            ((EditText)findViewById(R.id.txt_input)).setText(text);
        } catch (Exception ignored) {}
    }

    public String getText () {
        try {
            return ((EditText)findViewById(R.id.txt_input)).getText().toString();
        } catch (Exception ignored) {
            return null;
        }
    }

    public EditText getEditText () {
        try {
            return (EditText)findViewById(R.id.txt_input);
        } catch (Exception ignored) {
            return null;
        }
    }

    public void setOnCancelPressed (View.OnClickListener listener) {
        findViewById(R.id.btn_cancel).setOnClickListener(listener);
    }

    public void setOnOkPressed (View.OnClickListener listener) {
        findViewById(R.id.btn_ok).setOnClickListener(listener);
    }

    @Override
    public void onClick(View view) {
        dismiss();
    }
}
