/**
 *
 */
package com.bentonow.bentonow.controllers.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.ImageUtils;
import com.bentonow.bentonow.dao.DishDao;
import com.bentonow.bentonow.listener.ListenerCustomDish;
import com.bentonow.bentonow.model.BackendText;
import com.bentonow.bentonow.model.DishModel;
import com.bentonow.bentonow.ui.wrapper.ItemMainCustomWrapper;

/**
 * @author José Torres Fuentes
 */
public class CustomMainListAdapter extends ArrayAdapter<DishModel> {

    private Activity mActivity;
    private ListenerCustomDish mListener;
    private DishModel mCurrentAdded;
    private DishModel mCurrentSelected;

    /**
     * @param context
     */
    public CustomMainListAdapter(Activity context, ListenerCustomDish mListener) {
        super(context, 0);
        this.mActivity = context;
        this.mListener = mListener;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final ItemMainCustomWrapper viewHolder;
        final DishModel mDish = getItem(position);
        final boolean added;
        final boolean selected;

        if (convertView == null) {
            convertView = mActivity.getLayoutInflater().inflate(R.layout.list_item_dish_main_custom, parent, false);
            viewHolder = new ItemMainCustomWrapper(convertView);
            convertView.setTag(viewHolder);
        } else
            viewHolder = (ItemMainCustomWrapper) convertView.getTag();

        added = (getCurrentAdded() != null && getCurrentAdded().itemId == mDish.itemId);
        selected = (getCurrentSelected() != null && getCurrentSelected().itemId == mDish.itemId);

        try {
            if (viewHolder.getImgDish().getTag() == null || !viewHolder.getImgDish().getTag().equals(mDish.image1)) {
                if (mDish.type.equals("main"))
                    ImageUtils.initImageLoader().displayImage(mDish.image1, viewHolder.getImgDish(), ImageUtils.dishMainImageOptions());
                else
                    ImageUtils.initImageLoader().displayImage(mDish.image1, viewHolder.getImgDish(), ImageUtils.dishSideImageOptions());
                viewHolder.getImgDish().setTag(mDish.image1);
            }

            DebugUtils.logDebug("Dish Price: " + mDish.price);
        } catch (Exception ex) {
            DebugUtils.logError("LoadImage", ex);
            viewHolder.getImgDish().setImageResource(R.drawable.menu_placeholder);
        }

        viewHolder.getTxtTitle().setText(mDish.name);
        viewHolder.getImgGradient().setVisibility(selected ? View.VISIBLE : View.GONE);
        viewHolder.getBtnAdded().setVisibility(selected && added ? View.VISIBLE : View.GONE);

        viewHolder.getTxtDishPrice().setText(String.format(mActivity.getString(R.string.money_main_format), BentoNowUtils.getDefaultPriceBento(mDish.price)));
        viewHolder.getTxtDishPrice().setVisibility(selected ? View.GONE : View.VISIBLE);

        viewHolder.getTxtDescription().setText(selected ? mDish.description : "");
       // viewHolder.getTxtDescription().setVisibility(selected ? View.VISIBLE : View.INVISIBLE);

        viewHolder.getTxtAddPrice().setText(String.format(mActivity.getString(R.string.money_main_format), BentoNowUtils.getDefaultPriceBento(mDish.price)));

        viewHolder.getWrapperAddPrice().setVisibility(selected && !added ? View.VISIBLE : View.GONE);

        viewHolder.getBtnAddToBento().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onAddToBentoClick(position);
                DebugUtils.logDebug("onAddToBentoClick()", position);
            }
        });
        viewHolder.getBtnAdded().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onAddedClick(position);
                DebugUtils.logDebug("onAddedClick()", position);
            }
        });

        if (!DishDao.canBeAdded(mDish)) {
            viewHolder.getBtnAddToBento().setText(BackendText.get("reached-max-button"));
            viewHolder.getImgSoldOut().setVisibility(View.GONE);
        } else if (DishDao.isSoldOut(mDish, true)) {
            viewHolder.getBtnAddToBento().setText("Sold Out");
            viewHolder.getImgSoldOut().setVisibility(View.VISIBLE);
        } else {
            viewHolder.getBtnAddToBento().setText(BackendText.get("build-main-add-button-1"));
            viewHolder.getImgSoldOut().setVisibility(View.GONE);
        }


        return convertView;
    }

    public DishModel getCurrentAdded() {
        return mCurrentAdded;
    }

    public void setCurrentAdded(DishModel mCurrentAdded) {
        this.mCurrentAdded = mCurrentAdded;
    }

    public DishModel getCurrentSelected() {
        return mCurrentSelected;
    }

    public void setCurrentSelected(DishModel mCurrentSelected) {
        this.mCurrentSelected = mCurrentSelected;
    }
}
