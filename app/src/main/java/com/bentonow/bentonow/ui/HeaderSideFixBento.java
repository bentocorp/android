/**
 * @author Kokusho Torres
 */
package com.bentonow.bentonow.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bentonow.bentonow.R;

import org.w3c.dom.Text;

import me.grantland.widget.AutofitTextView;

public class HeaderSideFixBento extends LinearLayout {

    private Context context;
    private View rootView;
    private ImageView img_main_side_fix;
    private ImageView img_sold_out;
    private TextView txt_title;
    private AutofitTextView txt_description;

    public HeaderSideFixBento(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public void init() {
        getRootView();
    }

    public View getRootView() {
        if (rootView == null)
            rootView = LayoutInflater.from(context).inflate(R.layout.header_side_fix_bento, this, true);
        return rootView;
    }

    public ImageView getImgMainSide() {
        if (img_main_side_fix == null)
            img_main_side_fix = (ImageView) getRootView().findViewById(R.id.img_main_side_fix);
        return img_main_side_fix;
    }

    public ImageView getImgIsSoldOut() {
        if (img_sold_out == null)
            img_sold_out = (ImageView) getRootView().findViewById(R.id.img_sold_out);
        return img_sold_out;
    }

    public TextView getTxtTitle() {
        if (txt_title == null)
            txt_title = (TextView) getRootView().findViewById(R.id.txt_title);
        return txt_title;
    }

    public AutofitTextView getTxtDescription() {
        if (txt_description == null)
            txt_description = (AutofitTextView) getRootView().findViewById(R.id.txt_description);
        return txt_description;
    }
}
