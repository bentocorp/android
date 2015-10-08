package com.bentonow.bentonow.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Jose Torres on 10/7/15.
 */
public class RotateLayout extends TextView {


    public RotateLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        //Save the current matrix
        canvas.save();
        //Rotate this View at its center
        canvas.rotate(45, getWidth() / 2, getHeight() / 2);
        //Draw it
        super.onDraw(canvas);
        //Restore to the previous matrix
        canvas.restore();
    }

}