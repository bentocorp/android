/**
 * @author Kokusho Torres
 * 27/10/2015
 */
package com.bentonow.bentonow.ui.wrapper;

import android.view.View;
import android.widget.ImageView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.ui.BackendAutoFitTextView;
import com.bentonow.bentonow.ui.material.ProgressBarCircularIndeterminate;

import me.grantland.widget.AutofitTextView;

public class ItemSideCustomWrapper {

    private View view = null;

    private ImageView img_dish_main_custom = null;
    private ImageView img_gradient = null;
    private ImageView img_sold_out = null;
    private AutofitTextView txt_title = null;
    private AutofitTextView txt_description = null;
    private BackendAutoFitTextView btn_add_to_bento;
    private BackendAutoFitTextView btn_added;
    private BackendAutoFitTextView txt_oa_label;
    private ProgressBarCircularIndeterminate mProgressLoading;

    public ItemSideCustomWrapper(View base) {
        this.view = base;
    }

    public ImageView getImgDish() {
        if (img_dish_main_custom == null)
            img_dish_main_custom = (ImageView) view.findViewById(R.id.img_dish_main_custom);
        return img_dish_main_custom;
    }

    public ImageView getImgGradient() {
        if (img_gradient == null)
            img_gradient = (ImageView) view.findViewById(R.id.img_gradient);
        return img_gradient;
    }

    public ImageView getImgSoldOut() {
        if (img_sold_out == null)
            img_sold_out = (ImageView) view.findViewById(R.id.img_sold_out);
        return img_sold_out;
    }

    public AutofitTextView getTxtDescription() {
        if (txt_description == null)
            txt_description = (AutofitTextView) view.findViewById(R.id.txt_description);
        return txt_description;
    }

    public AutofitTextView getTxtTitle() {
        if (txt_title == null)
            txt_title = (AutofitTextView) view.findViewById(R.id.txt_title);
        return txt_title;
    }

    public BackendAutoFitTextView getBtnAddToBento() {
        if (btn_add_to_bento == null)
            btn_add_to_bento = (BackendAutoFitTextView) view.findViewById(R.id.btn_add_to_bento);
        return btn_add_to_bento;
    }

    public BackendAutoFitTextView getBtnAdded() {
        if (btn_added == null)
            btn_added = (BackendAutoFitTextView) view.findViewById(R.id.btn_added);
        return btn_added;
    }

    public BackendAutoFitTextView getTxtOaLabel() {
        if (txt_oa_label == null)
            txt_oa_label = (BackendAutoFitTextView) view.findViewById(R.id.txt_oa_label);
        return txt_oa_label;
    }

    public ProgressBarCircularIndeterminate getProgressLoading() {
        if (mProgressLoading == null)
            mProgressLoading = (ProgressBarCircularIndeterminate) view.findViewById(R.id.progress_loading);
        return mProgressLoading;
    }

}
