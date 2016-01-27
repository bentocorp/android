/**
 * @author Kokusho Torres
 * 27/10/2015
 */
package com.bentonow.bentonow.ui.wrapper;

import android.view.View;
import android.widget.ImageView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.ui.AutoFitTxtView;

public class ItemSpinnerTimeSelectedWrapper {

    private View view = null;

    private AutoFitTxtView txt_oa_time = null;
    private ImageView img_selector = null;

    public ItemSpinnerTimeSelectedWrapper(View base) {
        this.view = base;
    }

    public AutoFitTxtView getTxtOaTime() {
        if (txt_oa_time == null)
            txt_oa_time = (AutoFitTxtView) view.findViewById(R.id.txt_oa_time);
        return txt_oa_time;
    }

    public ImageView getImgSelector() {
        if (img_selector == null)
            img_selector = (ImageView) view.findViewById(R.id.img_selector);
        return img_selector;
    }
}
