/**
 * @author Kokusho Torres
 * 27/10/2015
 */
package com.bentonow.bentonow.ui.wrapper;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.ui.BackendButton;

public class ItemChildOrderWrapper {

    private View view = null;

    public TextView txt_name;
    public TextView txt_price;
    public ImageView btn_edit;
    public ImageButton btn_remove;

    public ItemChildOrderWrapper(View base) {
        this.view = base;
    }

    public TextView getTxtName() {
        if (txt_name == null)
            txt_name = (TextView) view.findViewById(R.id.txt_name);
        return txt_name;
    }

    public TextView getTxtPrice() {
        if (txt_price == null)
            txt_price = (TextView) view.findViewById(R.id.txt_price);
        return txt_price;
    }

    public ImageView getBtnEdit() {
        if (btn_edit == null)
            btn_edit = (ImageView) view.findViewById(R.id.btn_edit);
        return btn_edit;
    }

    public ImageButton getBtnRemove() {
        if (btn_remove == null)
            btn_remove = (ImageButton) view.findViewById(R.id.btn_remove);
        return btn_remove;
    }
}
