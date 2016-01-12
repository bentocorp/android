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
import com.bentonow.bentonow.model.DishModel;
import com.bentonow.bentonow.ui.wrapper.ItemMainNextWrapper;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

/**
 * @author José Torres Fuentes
 */

public class NextDayMainListAdapter extends ArrayAdapter<DishModel> {

    private Activity mActivity;
    private DishModel mCurrentSelected;

    /**
     * @param context
     */
    public NextDayMainListAdapter(Activity context) {
        super(context, 0);
        this.mActivity = context;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final ItemMainNextWrapper viewHolder;
        final DishModel mDish = getItem(position);
        final boolean selected;

        if (convertView == null) {
            convertView = mActivity.getLayoutInflater().inflate(R.layout.list_item_dish_next_main, parent, false);
            viewHolder = new ItemMainNextWrapper(convertView);
            convertView.setTag(viewHolder);
        } else
            viewHolder = (ItemMainNextWrapper) convertView.getTag();

        selected = (mCurrentSelected != null && mCurrentSelected.itemId == mDish.itemId);

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


        if (selected) {
            viewHolder.getTxtDescription().setText(mDish.description);
            viewHolder.getTxtDescription().setVisibility(View.VISIBLE);
        } else
            viewHolder.getTxtDescription().setVisibility(View.GONE);


        return convertView;
    }

    public void setCurrentSelected(DishModel mCurrentSelected) {
        this.mCurrentSelected = mCurrentSelected;
    }

}
