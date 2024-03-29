package com.bentonow.bentonow.controllers.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.ui.material.ProgressBarCircularIndeterminate;

/**
 * Created by Jose Torres on 10/2/15.
 */
public class ProgressDialog extends android.app.Dialog {

    Context context;
    View view;
    View backView;
    String title;
    TextView titleTextView;

    int progressColor = -1;

    private boolean bCancelable;

    public ProgressDialog(Context context, String title, boolean bCancelable) {
        super(context, android.R.style.Theme_Translucent);
        this.title = title;
        this.context = context;
        this.bCancelable = bCancelable;
    }

    public ProgressDialog(Context context, int idTitle, boolean bCancelable) {
        super(context, android.R.style.Theme_Translucent);
        this.title = context.getResources().getString(idTitle);
        this.context = context;
        this.bCancelable = bCancelable;
    }

    public ProgressDialog(Context context, String title, int progressColor) {
        super(context, android.R.style.Theme_Translucent);
        this.title = title;
        this.progressColor = progressColor;
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.progress_dialog);

        view = (RelativeLayout) findViewById(R.id.contentDialog);
        backView = (RelativeLayout) findViewById(R.id.dialog_rootView);

        setCancelable(bCancelable);

        if (bCancelable)
            backView.setOnTouchListener(new OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getX() < view.getLeft()
                            || event.getX() > view.getRight()
                            || event.getY() > view.getBottom()
                            || event.getY() < view.getTop()) {
                        dismiss();
                    }
                    return false;
                }
            });

        this.titleTextView = (TextView) findViewById(R.id.title);
        setTitle(title);
        if (progressColor != -1) {
            ProgressBarCircularIndeterminate progressBarCircularIndeterminate = (ProgressBarCircularIndeterminate) findViewById(R.id.progressBarCircularIndetermininate);
            progressBarCircularIndeterminate.setBackgroundColor(progressColor);
        }


    }

    @Override
    public void show() {
        super.show();
    }

    // GETERS & SETTERS

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        if (title == null)
            titleTextView.setVisibility(View.GONE);
        else {
            titleTextView.setVisibility(View.VISIBLE);
            titleTextView.setText(title);
        }
    }

    public TextView getTitleTextView() {
        return titleTextView;
    }

    public void setTitleTextView(TextView titleTextView) {
        this.titleTextView = titleTextView;
    }

}