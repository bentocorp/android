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
import com.bentonow.bentonow.Utils.ImageUtils;
import com.bentonow.bentonow.model.DishModel;
import com.bentonow.bentonow.ui.wrapper.ItemSideFixWrapper;

/**
 * @author José Torres Fuentes
 */
public class DishFixGridListAdapter extends ArrayAdapter<DishModel> {

    private Activity mActivity;

    private int iDishSelected = -1;

    /**
     * @param context
     */
    public DishFixGridListAdapter(Activity context) {
        super(context, 0);
        this.mActivity = context;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ItemSideFixWrapper viewHolder;
        final DishModel mDishDishModel = getItem(position);

        if (convertView == null) {
            LayoutInflater mInflater = mActivity.getLayoutInflater();
            convertView = mInflater.inflate(R.layout.list_item_dish_side_fix, parent, false);
            viewHolder = new ItemSideFixWrapper(convertView);
            convertView.setTag(viewHolder);
        } else
            viewHolder = (ItemSideFixWrapper) convertView.getTag();

        viewHolder.getTxtTitle().setText(mDishDishModel.name);
        viewHolder.getTxtDescription().setText(mDishDishModel.description);
        viewHolder.getTxtDescription().setVisibility(iDishSelected == position ? View.VISIBLE : View.GONE);

        if (viewHolder.getImageDish().getTag() == null || !viewHolder.getImageDish().getTag().equals(mDishDishModel.image1)) {
            ImageUtils.initImageLoader().displayImage(mDishDishModel.image1, viewHolder.getImageDish(), ImageUtils.dishSideImageOptions());
            viewHolder.getImageDish().setTag(mDishDishModel.image1);
        }

        //if (mDishItem.isSoldOut(countCurrent)) btn_add_to_bento.setText("Sold Out");
        //else if (!mDishItem.canBeAdded()) btn_add_to_bento.setText("Reached to max");

        return convertView;
    }


    public void setItemSelected(int iDishSelected) {
        this.iDishSelected = iDishSelected;
    }

}
