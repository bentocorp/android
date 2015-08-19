package com.bentonow.bentonow.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.model.BackendText;

public class BackendTextView extends TextView {

    public BackendTextView(Context context) {
        super(context);
    }

    public BackendTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public BackendTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BackendTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    void init (Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.BackendTextView,
                0, 0);

        try {
            String key = a.getString(R.styleable.BackendTextView_key);
            setText(BackendText.get(key));
        } finally {
            a.recycle();
        }
    }
}
