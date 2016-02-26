/**
 * @author Kokusho Torres
 * 27/10/2015
 */
package com.bentonow.bentonow.ui.wrapper;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bentonow.bentonow.R;

public class ItemChildOrderHistoryWrapper {

    public TextView txtOrderHistoryTitle;
    public TextView txtOrderHistoryPrice;
    public ImageView imgOpenOrder;
    private View view = null;

    public ItemChildOrderHistoryWrapper(View base) {
        this.view = base;
    }

    public TextView getTxtOrderHistoryTitle() {
        if (txtOrderHistoryTitle == null)
            txtOrderHistoryTitle = (TextView) view.findViewById(R.id.txt_order_history_title);
        return txtOrderHistoryTitle;
    }

    public TextView getTxtOrderHistoryPrice() {
        if (txtOrderHistoryPrice == null)
            txtOrderHistoryPrice = (TextView) view.findViewById(R.id.txt_order_history_price);
        return txtOrderHistoryPrice;
    }

    public ImageView getImgOpenOrder() {
        if (imgOpenOrder == null)
            imgOpenOrder = (ImageView) view.findViewById(R.id.img_open_order);
        return imgOpenOrder;
    }

}
