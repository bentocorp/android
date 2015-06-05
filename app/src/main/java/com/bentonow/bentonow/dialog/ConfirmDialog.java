package com.bentonow.bentonow.dialog;

import android.view.View;

/**
 * Created by normanpaniagua on 6/5/15.
 */
public class ConfirmDialog {
    View overlay;
    View btnCancel;
    View btnOk;

    public ConfirmDialog (View overlay, View btnCancel, View btnOk) {
        this.overlay = overlay;
        this.btnCancel = btnCancel;
        this.btnOk = btnOk;
    }

    public void setContext (final ConfirmDialogInterface context) {
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.onAccept();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.onCancel();
            }
        });
    }

    public void hide () {
        overlay.setVisibility(View.GONE);
    }

    public void show () {
        overlay.setVisibility(View.VISIBLE);
    }
}
