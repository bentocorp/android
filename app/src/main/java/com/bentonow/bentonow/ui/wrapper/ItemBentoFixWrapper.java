/**
 * @author Kokusho Torres
 * 08/10/2014
 */
package com.bentonow.bentonow.ui.wrapper;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bentonow.bentonow.R;

public class ItemBentoFixWrapper {

    private View base = null;

    private ImageView img_dish_fixed = null;
    private ImageView img_sold_out = null;
    private ImageView img_action_star = null;
    private TextView txt_article_name = null;

    public ItemBentoFixWrapper(View base) {
        this.base = base;
    }

    public TextView getTxtArticleName() {
        if (img_dish_fixed == null)
            img_dish_fixed = (TextView) base.findViewById(R.id.img_dish_fixed);
        return img_dish_fixed;
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

    public ImageView getImageStar() {
        if (img_action_star == null)
            img_action_star = (ImageView) base.findViewById(R.id.img_action_star);
        return img_action_star;
    }

}
