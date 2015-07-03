package com.bentonow.bentonow.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.bentonow.bentonow.R;

public class CustomDialog extends Dialog implements View.OnClickListener {

    public CustomDialog (Context context, String message, String acceptButton, String cancelButton) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_custom);
        setText(message);
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

    public void setText (String message) {
        ((TextView)findViewById(R.id.text)).setText(message);
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
