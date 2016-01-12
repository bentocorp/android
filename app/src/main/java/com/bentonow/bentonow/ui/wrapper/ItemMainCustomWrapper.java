/**
 * @author Kokusho Torres
 * 27/10/2015
 */
package com.bentonow.bentonow.ui.wrapper;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.ui.AutoFitTxtView;
import com.bentonow.bentonow.ui.material.ProgressBarCircularIndeterminate;


public class ItemMainCustomWrapper {

    private View view = null;

    private ImageView img_dish_main_custom = null;
    private ImageView img_gradient = null;
    private ImageView img_sold_out = null;
    private AutoFitTxtView txt_title = null;
    private AutoFitTxtView txt_description = null;
    private AutoFitTxtView btn_add_price = null;
    private AutoFitTxtView btn_dish_price = null;
    private AutoFitTxtView btn_add_to_bento;
    private AutoFitTxtView btn_added;
    private LinearLayout wrapper_add_price = null;
    private View view_line_divider = null;
    private ProgressBarCircularIndeterminate mProgressLoading;

    public ItemMainCustomWrapper(View base) {
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

    public AutoFitTxtView getTxtAddPrice() {
        if (btn_add_price == null)
            btn_add_price = (AutoFitTxtView) view.findViewById(R.id.btn_add_price);
        return btn_add_price;
    }

    public AutoFitTxtView getTxtDishPrice() {
        if (btn_dish_price == null)
            btn_dish_price = (AutoFitTxtView) view.findViewById(R.id.btn_dish_price);
        return btn_dish_price;
    }


    public AutoFitTxtView getBtnAddToBento() {
        if (btn_add_to_bento == null)
            btn_add_to_bento = (AutoFitTxtView) view.findViewById(R.id.btn_add_to_bento);
        return btn_add_to_bento;
    }

    public AutoFitTxtView getBtnAdded() {
        if (btn_added == null)
            btn_added = (AutoFitTxtView) view.findViewById(R.id.btn_added);
        return btn_added;
    }

    public LinearLayout getWrapperAddPrice() {
        if (wrapper_add_price == null)
            wrapper_add_price = (LinearLayout) view.findViewById(R.id.wrapper_add_price);
        return wrapper_add_price;
    }

    public View getViewLineDivider() {
        if (view_line_divider == null)
            view_line_divider = view.findViewById(R.id.view_line_divider);
        return view_line_divider;
    }

    public ProgressBarCircularIndeterminate getProgressLoading() {
        if (mProgressLoading == null)
            mProgressLoading = (ProgressBarCircularIndeterminate) view.findViewById(R.id.progress_loading);
        return mProgressLoading;
    }

}
