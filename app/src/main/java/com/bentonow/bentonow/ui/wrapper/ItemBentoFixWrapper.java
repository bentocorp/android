/**
 * @author Kokusho Torres
 * 27/10/2015
 */
package com.bentonow.bentonow.ui.wrapper;

import android.view.View;
import android.widget.ImageView;

import com.bentonow.bentonow.R;

import me.grantland.widget.AutofitTextView;

public class ItemBentoFixWrapper {

    private View base = null;

    private ImageView img_dish_fixed = null;
    private ImageView img_sold_out = null;
    private AutofitTextView txt_title = null;
    private AutofitTextView btn_add_to_bento = null;

    public ItemBentoFixWrapper(View base) {
        this.base = base;
    }

    public AutofitTextView getBtnAddToBento() {
        if (btn_add_to_bento == null)
            btn_add_to_bento = (AutofitTextView) base.findViewById(R.id.btn_add_to_bento);
        return btn_add_to_bento;
    }

    public ImageView getImageDish() {
        if (img_dish_fixed == null)
            img_dish_fixed = (ImageView) base.findViewById(R.id.img_dish_fixed);
        return img_dish_fixed;
    }

    public ImageView getImageSoldOut() {
        if (img_sold_out == null)
            img_sold_out = (ImageView) base.findViewById(R.id.img_sold_out);
        return img_sold_out;
    }

    public AutofitTextView getTxtTitle() {
        if (txt_title == null)
            txt_title = (AutofitTextView) base.findViewById(R.id.txt_title);
        return txt_title;
    }

}
