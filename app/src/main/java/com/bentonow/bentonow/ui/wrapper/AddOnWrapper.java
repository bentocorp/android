/**
 * @author Kokusho Torres
 * 27/10/2015
 */
package com.bentonow.bentonow.ui.wrapper;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.ui.AutoFitTxtView;
import com.bentonow.bentonow.ui.BackendAutoFitTextView;

public class AddOnWrapper extends RecyclerView.ViewHolder {

    public View view = null;

    private ImageView img_dish = null;
    private ImageView img_sold_out = null;
    private ImageView img_gradient = null;
    private ImageView img_add_dish = null;
    private ImageView img_remove_dish = null;
    private AutoFitTxtView txt_add_on_title = null;
    private AutoFitTxtView txt_add_on_price = null;
    private AutoFitTxtView txt_add_on_description = null;
    private BackendAutoFitTextView txt_oa_label;
    private TextView txt_number_add_on = null;


    public AddOnWrapper(View base) {
        super(base);
        this.view = base;
    }

    public ImageView getImgDish() {
        if (img_dish == null)
            img_dish = (ImageView) view.findViewById(R.id.img_dish);
        return img_dish;
    }

    public ImageView getImgSoldOut() {
        if (img_sold_out == null)
            img_sold_out = (ImageView) view.findViewById(R.id.img_sold_out);
        return img_sold_out;
    }


    public ImageView getImgGradient() {
        if (img_gradient == null)
            img_gradient = (ImageView) view.findViewById(R.id.img_gradient);
        return img_gradient;
    }

    public ImageView getImgRemoveDish() {
        if (img_remove_dish == null)
            img_remove_dish = (ImageView) view.findViewById(R.id.img_remove_dish);
        return img_remove_dish;
    }

    public ImageView getImgAddDish() {
        if (img_add_dish == null)
            img_add_dish = (ImageView) view.findViewById(R.id.img_add_dish);
        return img_add_dish;
    }

    public AutoFitTxtView getTxtTitle() {
        if (txt_add_on_title == null)
            txt_add_on_title = (AutoFitTxtView) view.findViewById(R.id.txt_add_on_title);
        return txt_add_on_title;
    }

    public AutoFitTxtView getTxtPrice() {
        if (txt_add_on_price == null)
            txt_add_on_price = (AutoFitTxtView) view.findViewById(R.id.txt_add_on_price);
        return txt_add_on_price;
    }

    public AutoFitTxtView getTxtDescription() {
        if (txt_add_on_description == null)
            txt_add_on_description = (AutoFitTxtView) view.findViewById(R.id.txt_add_on_description);
        return txt_add_on_description;
    }

    public BackendAutoFitTextView getTxtOaLabel() {
        if (txt_oa_label == null)
            txt_oa_label = (BackendAutoFitTextView) view.findViewById(R.id.txt_oa_label);
        return txt_oa_label;
    }

    public TextView getTxtNumberAddOn() {
        if (txt_number_add_on == null)
            txt_number_add_on = (TextView) view.findViewById(R.id.txt_number_add_on);
        return txt_number_add_on;
    }

}
