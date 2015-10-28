/**
 *
 */
package com.bentonow.bentonow.controllers.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.ImageUtils;
import com.bentonow.bentonow.dao.DishDao;
import com.bentonow.bentonow.listener.ListenerMainDishFix;
import com.bentonow.bentonow.model.DishModel;
import com.bentonow.bentonow.ui.wrapper.ItemBentoFixWrapper;

/**
 * @author José Torres Fuentes
 */

public class BuildBentoFixListAdapter extends ArrayAdapter<DishModel> {

    private Activity mActivity;
    private ListenerMainDishFix mListener;

    /**
     * @param context
     */
    public BuildBentoFixListAdapter(Activity context, ListenerMainDishFix mListener) {
        super(context, 0);
        this.mActivity = context;
        this.mListener = mListener;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final ItemBentoFixWrapper viewHolder;
        final DishModel mDish = getItem(position);

        if (convertView == null) {
            LayoutInflater mInflater = mActivity.getLayoutInflater();
            convertView = mInflater.inflate(R.layout.list_item_build_fixed, parent, false);
            viewHolder = new ItemBentoFixWrapper(convertView);
            convertView.setTag(viewHolder);
        } else
            viewHolder = (ItemBentoFixWrapper) convertView.getTag();

        viewHolder.getTxtTitle().setText(mDish.name);
        viewHolder.getTxtAddPrice().setText(String.format(mActivity.getString(R.string.money_main_format), BentoNowUtils.getDefaultPriceBento(mDish.price)));

        if (viewHolder.getImageDish().getTag() == null || !viewHolder.getImageDish().getTag().equals(mDish.image1)) {
            ImageUtils.initImageLoader().displayImage(mDish.image1, viewHolder.getImageDish(), ImageUtils.dishMainImageOptions());
            viewHolder.getImageDish().setTag(mDish.image1);
        }

        viewHolder.getImageDish().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onDishClick(position);
                DebugUtils.logDebug("onDishClick()", position);
            }
        });


        if (DishDao.isSoldOut(mDish, true)) {
            viewHolder.getImageSoldOut().setVisibility(View.VISIBLE);
            viewHolder.getBtnAddToBento().setOnClickListener(null);
            viewHolder.getWrapperAddPrice().setBackground(getContext().getResources().getDrawable(R.drawable.btn_dark_gray));
        } else {
            viewHolder.getImageSoldOut().setVisibility(View.INVISIBLE);
            viewHolder.getWrapperAddPrice().setBackground(getContext().getResources().getDrawable(R.drawable.btn_rounded_green));
            viewHolder.getBtnAddToBento().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onAddToBentoClick(position);
                    DebugUtils.logDebug("onAddToBentoClick()", position);
                }
            });
        }

        return convertView;
    }


}
