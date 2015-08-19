package com.bentonow.bentonow.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class BentoGridView extends GridView {

    public BentoGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BentoGridView(Context context) {
        super(context);
    }

    public BentoGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}