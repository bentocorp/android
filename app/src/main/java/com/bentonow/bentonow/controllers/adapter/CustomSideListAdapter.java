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
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.ImageUtils;
import com.bentonow.bentonow.dao.DishDao;
import com.bentonow.bentonow.dao.IosCopyDao;
import com.bentonow.bentonow.listener.ListenerCustomDish;
import com.bentonow.bentonow.model.DishModel;
import com.bentonow.bentonow.ui.wrapper.ItemSideCustomWrapper;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

/**
 * @author José Torres Fuentes
 */
public class CustomSideListAdapter extends ArrayAdapter<DishModel> {

    private Activity mActivity;
    private ListenerCustomDish mListener;
    private DishModel mCurrentAdded;
    private DishModel mCurrentSelected;
    private DishDao dishDao;
    private boolean bIsMenuOD;

    /**
     * @param context
     */
    public CustomSideListAdapter(Activity context, boolean bIsMenuOD, ListenerCustomDish mListener) {
        super(context, 0);
        this.mActivity = context;
        this.mListener = mListener;
        this.bIsMenuOD = bIsMenuOD;
        dishDao = new DishDao();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final ItemSideCustomWrapper viewHolder;
        final DishModel mDish = getItem(position);
        final boolean added;
        final boolean selected;

        if (convertView == null) {
            convertView = mActivity.getLayoutInflater().inflate(R.layout.list_item_dish_side_custom, parent, false);
            viewHolder = new ItemSideCustomWrapper(convertView);
            convertView.setTag(viewHolder);
        } else
            viewHolder = (ItemSideCustomWrapper) convertView.getTag();

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
        } catch (Exception ex) {
            DebugUtils.logError("LoadImage", ex);
            viewHolder.getImgDish().setImageResource(R.drawable.menu_placeholder);
        }

        viewHolder.getTxtTitle().setText(mDish.name);
        viewHolder.getImgGradient().setVisibility(selected ? View.VISIBLE : View.GONE);
        viewHolder.getBtnAdded().setVisibility(selected && added ? View.VISIBLE : View.GONE);
        viewHolder.getBtnAddToBento().setVisibility(selected && !added ? View.VISIBLE : View.GONE);

        viewHolder.getBtnAddToBento().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onAddToBentoClick(position);
                DebugUtils.logDebug("onAddToBentoClick()", position);
            }
        });

        if (selected) {
            viewHolder.getTxtDescription().setText(mDish.description);
            viewHolder.getTxtDescription().setVisibility(View.VISIBLE);
        } else
            viewHolder.getTxtDescription().setVisibility(View.GONE);

        if (!dishDao.canBeAdded(mDish)) {
            viewHolder.getBtnAddToBento().setText(IosCopyDao.get("reached-max-button"));
            viewHolder.getImgSoldOut().setVisibility(View.GONE);
            getItem(position).can_be_added = 0;
        } else if (dishDao.isSoldOut(mDish, true, bIsMenuOD)) {
            viewHolder.getBtnAddToBento().setText("Sold Out");
            viewHolder.getImgSoldOut().setVisibility(View.VISIBLE);
            getItem(position).can_be_added = 0;
        } else {
            viewHolder.getBtnAddToBento().setText(IosCopyDao.get("build-main-add-button-1"));
            viewHolder.getImgSoldOut().setVisibility(View.GONE);
            getItem(position).can_be_added = 1;
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
