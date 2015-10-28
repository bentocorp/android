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

public class ItemBentoFixWrapper {

    private View view = null;

    private ImageView img_dish_fixed = null;
    private ImageView img_sold_out = null;
    private AutoFitTxtView txt_title = null;
    private AutoFitTxtView btn_add_price = null;
    private AutoFitTxtView btn_add_to_bento = null;
    private LinearLayout wrapper_add_price = null;
    private View view_line_divider = null;

    public ItemBentoFixWrapper(View base) {
        this.view = base;
    }

    public AutoFitTxtView getBtnAddToBento() {
        if (btn_add_to_bento == null)
            btn_add_to_bento = (AutoFitTxtView) view.findViewById(R.id.btn_add_to_bento);
        return btn_add_to_bento;
    }

    public ImageView getImageDish() {
        if (img_dish_fixed == null)
            img_dish_fixed = (ImageView) view.findViewById(R.id.img_dish_fixed);
        return img_dish_fixed;
    }

    public ImageView getImageSoldOut() {
        if (img_sold_out == null)
            img_sold_out = (ImageView) view.findViewById(R.id.img_sold_out);
        return img_sold_out;
    }

    public AutoFitTxtView getTxtTitle() {
        if (txt_title == null)
            txt_title = (AutoFitTxtView) view.findViewById(R.id.txt_title);
        return txt_title;
    }

    public LinearLayout getWrapperAddPrice() {
        if (wrapper_add_price == null)
            wrapper_add_price = (LinearLayout) view.findViewById(R.id.wrapper_add_price);
        return wrapper_add_price;
    }


    public AutoFitTxtView getTxtAddPrice() {
        if (btn_add_price == null)
            btn_add_price = (AutoFitTxtView) view.findViewById(R.id.btn_add_price);
        return btn_add_price;
    }


    public View getViewLineDivider() {
        if (view_line_divider == null)
            view_line_divider = view.findViewById(R.id.view_line_divider);
        return view_line_divider;
    }


}
