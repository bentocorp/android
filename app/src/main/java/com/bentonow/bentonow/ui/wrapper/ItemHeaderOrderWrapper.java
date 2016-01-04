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
import com.bentonow.bentonow.ui.BackendButton;

public class ItemHeaderOrderWrapper {

    private View view = null;

    private BackendButton btn_order_name = null;
    private BackendButton btn_delete = null;

    public ItemHeaderOrderWrapper(View base) {
        this.view = base;
    }

    public BackendButton getBtnOrderName() {
        if (btn_order_name == null)
            btn_order_name = (BackendButton) view.findViewById(R.id.btn_order_name);
        return btn_order_name;
    }

    public BackendButton getBtnDelete() {
        if (btn_delete == null)
            btn_delete = (BackendButton) view.findViewById(R.id.btn_delete);
        return btn_delete;
    }

}
