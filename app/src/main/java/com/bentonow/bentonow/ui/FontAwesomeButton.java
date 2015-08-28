package com.bentonow.bentonow.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Button;

public class FontAwesomeButton extends Button {
    final String TAG = getClass().getSimpleName();

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

    public void setup (Context context) {
        Log.i(TAG, "setup");
        Typeface tf = Typeface.createFromAsset(context.getAssets(), "fonts/fontawesome-webfont.ttf");
        setTypeface(tf);
    }
}
