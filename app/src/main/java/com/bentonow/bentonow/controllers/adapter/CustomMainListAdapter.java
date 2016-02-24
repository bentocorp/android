/**
 *
 */
package com.bentonow.bentonow.controllers.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.ImageUtils;
import com.bentonow.bentonow.dao.DishDao;
import com.bentonow.bentonow.dao.IosCopyDao;
import com.bentonow.bentonow.listener.ListenerCustomDish;
import com.bentonow.bentonow.model.DishModel;
import com.bentonow.bentonow.ui.wrapper.ItemMainCustomWrapper;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

/**
 * @author José Torres Fuentes
 */
public class CustomMainListAdapter extends ArrayAdapter<DishModel> {

    private Activity mActivity;
    private ListenerCustomDish mListener;
    private DishModel mCurrentAdded;
    private DishModel mCurrentSelected;
    private DishDao mDishDao;
    private boolean bIsMenuOD;

    /**
     * @param context
     */
    public CustomMainListAdapter(Activity context, boolean bIsMenuOD, ListenerCustomDish mListener) {
        super(context, 0);
        this.mActivity = context;
        this.mListener = mListener;
        this.bIsMenuOD = bIsMenuOD;
        mDishDao = new DishDao();
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
                    ImageUtils.initImageLoader().displayImage(mDish.image1, viewHolder.getImgDish(), ImageUtils.dishMainImageOptions(), new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri, View view) {
                            viewHolder.getProgressLoading().setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                            viewHolder.getProgressLoading().setVisibility(View.GONE);
                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            viewHolder.getProgressLoading().setVisibility(View.GONE);
                        }
                    });
                else
                    ImageUtils.initImageLoader().displayImage(mDish.image1, viewHolder.getImgDish(), ImageUtils.dishSideImageOptions(), new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri, View view) {
                            viewHolder.getProgressLoading().setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                            viewHolder.getProgressLoading().setVisibility(View.GONE);
                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            viewHolder.getProgressLoading().setVisibility(View.GONE);
                        }
                    });
                viewHolder.getImgDish().setTag(mDish.image1);
            }

            //DebugUtils.logDebug("Dish Price: " + mDish.price);
        } catch (Exception ex) {
            DebugUtils.logError("LoadImage", ex);
            viewHolder.getImgDish().setImageResource(R.drawable.menu_placeholder);
        }

        viewHolder.getTxtTitle().setText(mDish.name);

        viewHolder.getImgGradient().setVisibility(selected ? View.VISIBLE : View.GONE);

        viewHolder.getBtnAdded().setVisibility(added ? View.VISIBLE : View.GONE);

        viewHolder.getTxtDishPrice().setText(String.format(mActivity.getString(R.string.money_main_format), BentoNowUtils.getDefaultPriceBento(mDish.price)));
        viewHolder.getTxtDishPrice().setVisibility(selected ? View.GONE : View.VISIBLE);

        viewHolder.getTxtDescription().setText(selected ? mDish.description : "");

        viewHolder.getTxtAddPrice().setText(String.format(mActivity.getString(R.string.money_main_format), BentoNowUtils.getDefaultPriceBento(mDish.price)));
        viewHolder.getTxtAddPrice().setTextColor(added ? mActivity.getResources().getColor(R.color.black) : mActivity.getResources().getColor(R.color.white));

        viewHolder.getWrapperAddPrice().setVisibility(selected ? View.VISIBLE : View.GONE);
        viewHolder.getWrapperAddPrice().setBackgroundDrawable(!added ? mActivity.getResources().getDrawable(R.drawable.btn_border_lineal_white) : mActivity.getResources().getDrawable(R.drawable.btn_white));

        viewHolder.getViewLineDivider().setBackgroundColor(added ? mActivity.getResources().getColor(R.color.black) : mActivity.getResources().getColor(R.color.white));

        viewHolder.getBtnAddToBento().setVisibility(!added ? View.VISIBLE : View.GONE);

        viewHolder.getBtnAddToBento().setOnClickListener(mDish.is_oa_only == 1 ? null : new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onAddToBentoClick(position);
                DebugUtils.logDebug("onAddToBentoClick()", position);
            }
        });

        viewHolder.getBtnAdded().setOnClickListener(mDish.is_oa_only == 1 ? null : new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onAddedClick(position);
                DebugUtils.logDebug("onAddedClick()", position);
            }
        });

        if (mDish.is_oa_only == 1) {
            viewHolder.getBtnAddToBento().setText(IosCopyDao.get("oa-only-od-btn"));
            viewHolder.getImgSoldOut().setVisibility(View.GONE);
            getItem(position).can_be_added = 0;
        } else if (!mDishDao.canBeAdded(mDish)) {
            viewHolder.getBtnAddToBento().setText(IosCopyDao.get("reached-max-button"));
            viewHolder.getImgSoldOut().setVisibility(View.GONE);
            getItem(position).can_be_added = 0;
        } else if (mDishDao.isSoldOut(mDish, true, bIsMenuOD)) {
            viewHolder.getBtnAddToBento().setText("Sold Out");
            viewHolder.getImgSoldOut().setVisibility(View.VISIBLE);
            getItem(position).can_be_added = 0;
        } else {
            viewHolder.getBtnAddToBento().setText(IosCopyDao.get("build-main-add-button-1"));
            viewHolder.getImgSoldOut().setVisibility(View.GONE);
            getItem(position).can_be_added = 1;
        }

        viewHolder.getTxtOaLabel().setVisibility(mDish.is_oa_only == 0 || selected ? View.GONE : View.VISIBLE);

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
