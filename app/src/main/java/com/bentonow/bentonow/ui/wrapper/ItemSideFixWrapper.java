/**
 * @author Kokusho Torres
 * 27/10/2015
 */
package com.bentonow.bentonow.ui.wrapper;

import android.view.View;
import android.widget.ImageView;

import com.bentonow.bentonow.R;

import com.bentonow.bentonow.ui.AutoFitTxtView;

public class ItemSideFixWrapper {

    private View base = null;

    private ImageView img_dish_side_fix = null;
    private AutoFitTxtView txt_title = null;
    private AutoFitTxtView txt_description = null;

    public ItemSideFixWrapper(View base) {
        this.base = base;
    }


    public ImageView getImageDish() {
        if (img_dish_side_fix == null)
            img_dish_side_fix = (ImageView) base.findViewById(R.id.img_dish_side_fix);
        return img_dish_side_fix;
    }

    public AutoFitTxtView getTxtDescription() {
        if (txt_description == null)
            txt_description = (AutoFitTxtView) base.findViewById(R.id.txt_description);
        return txt_description;
    }

    public AutoFitTxtView getTxtTitle() {
        if (txt_title == null)
            txt_title = (AutoFitTxtView) base.findViewById(R.id.txt_title);
        return txt_title;
    }

}
