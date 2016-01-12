/**
 * @author Kokusho Torres
 * 27/10/2015
 */
package com.bentonow.bentonow.ui.wrapper;

import android.view.View;
import android.widget.ImageView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.ui.AutoFitTxtView;
import com.bentonow.bentonow.ui.material.ProgressBarCircularIndeterminate;


public class ItemMainNextWrapper {

    private View view = null;

    private ImageView img_dish_next_main = null;
    private ImageView img_gradient = null;
    private AutoFitTxtView txt_title = null;
    private AutoFitTxtView txt_description = null;
    private ProgressBarCircularIndeterminate mProgressLoading;

    public ItemMainNextWrapper(View base) {
        this.view = base;
    }

    public ImageView getImgDish() {
        if (img_dish_next_main == null)
            img_dish_next_main = (ImageView) view.findViewById(R.id.img_dish_next_main);
        return img_dish_next_main;
    }

    public ImageView getImgGradient() {
        if (img_gradient == null)
            img_gradient = (ImageView) view.findViewById(R.id.img_gradient);
        return img_gradient;
    }

    public AutoFitTxtView getTxtDescription() {
        if (txt_description == null)
            txt_description = (AutoFitTxtView) view.findViewById(R.id.txt_description);
        return txt_description;
    }

    public AutoFitTxtView getTxtTitle() {
        if (txt_title == null)
            txt_title = (AutoFitTxtView) view.findViewById(R.id.txt_title);
        return txt_title;
    }

    public ProgressBarCircularIndeterminate getProgressLoading() {
        if (mProgressLoading == null)
            mProgressLoading = (ProgressBarCircularIndeterminate) view.findViewById(R.id.progress_loading);
        return mProgressLoading;
    }

}
