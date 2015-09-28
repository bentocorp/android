/**
 * @author Kokusho Torres
 * 27/10/2015
 */
package com.bentonow.bentonow.ui.wrapper;

import android.view.View;
import android.widget.ImageView;

import com.bentonow.bentonow.R;

import me.grantland.widget.AutofitTextView;

public class ItemSideFixWrapper {

    private View base = null;

    private ImageView img_dish_side_fix = null;
    private AutofitTextView txt_title = null;
    private AutofitTextView txt_description = null;

    public ItemSideFixWrapper(View base) {
        this.base = base;
    }


    public ImageView getImageDish() {
        if (img_dish_side_fix == null)
            img_dish_side_fix = (ImageView) base.findViewById(R.id.img_dish_side_fix);
        return img_dish_side_fix;
    }

    public AutofitTextView getTxtDescription() {
        if (txt_description == null)
            txt_description = (AutofitTextView) base.findViewById(R.id.txt_description);
        return txt_description;
    }

    public AutofitTextView getTxtTitle() {
        if (txt_title == null)
            txt_title = (AutofitTextView) base.findViewById(R.id.txt_title);
        return txt_title;
    }

}
