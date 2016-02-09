package com.bentonow.bentonow.ui.material;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Spinner;

/**
 * Created by kokusho on 2/8/16.
 */
public class SpinnerMaterial extends Spinner {
    Context context = null;

    public SpinnerMaterial(Context context) {
        super(context);
        this.context = context;
    }

    public SpinnerMaterial(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SpinnerMaterial(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

}
