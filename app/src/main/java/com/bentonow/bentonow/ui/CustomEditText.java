package com.bentonow.bentonow.ui;

import android.content.Context;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.widget.EditText;

import com.bentonow.bentonow.Utils.AndroidUtil;

/**
 * Created by kokusho on 2/11/16.
 */
public class CustomEditText extends EditText {
    public CustomEditText(Context context) {
        super(context);
        init();
    }

    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setFilters(new InputFilter[]{AndroidUtil.getInputFilterEmoji()});
    }

}