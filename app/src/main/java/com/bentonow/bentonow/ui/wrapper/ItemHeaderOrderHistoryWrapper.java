/**
 * @author Kokusho Torres
 * 27/10/2015
 */
package com.bentonow.bentonow.ui.wrapper;

import android.view.View;
import android.widget.TextView;

import com.bentonow.bentonow.R;

public class ItemHeaderOrderHistoryWrapper {

    private View view = null;

    private TextView txtOhHeader = null;

    public ItemHeaderOrderHistoryWrapper(View base) {
        this.view = base;
    }

    public TextView getTxtOhHeader() {
        if (txtOhHeader == null)
            txtOhHeader = (TextView) view.findViewById(R.id.txt_oh_header);
        return txtOhHeader;
    }

}
