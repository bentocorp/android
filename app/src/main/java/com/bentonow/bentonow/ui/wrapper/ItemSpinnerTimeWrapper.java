/**
 * @author Kokusho Torres
 * 27/10/2015
 */
package com.bentonow.bentonow.ui.wrapper;

import android.view.View;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.ui.AutoFitTxtView;

public class ItemSpinnerTimeWrapper {

    private View view = null;

    private AutoFitTxtView txt_oa_time = null;

    public ItemSpinnerTimeWrapper(View base) {
        this.view = base;
    }

    public AutoFitTxtView getTxtOaTime() {
        if (txt_oa_time == null)
            txt_oa_time = (AutoFitTxtView) view.findViewById(R.id.txt_oa_time);
        return txt_oa_time;
    }
}
