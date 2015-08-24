package com.bentonow.bentonow.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Button;

public class FontAwesomeButton extends Button {
    static final String TAG = "ui.SSSocialCircle";
    static Typeface tf = null;

    public FontAwesomeButton(Context context) {
        super(context);
        setup(context);
    }

    public FontAwesomeButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(context);
    }

    public FontAwesomeButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup(context);
    }

    void setup (Context context) {
        Log.i(TAG, "setup");
        if (tf == null) tf = Typeface.createFromAsset(context.getAssets(), "fonts/fontawesome-webfont.ttf");
        setTypeface(tf);
    }
}
