package com.bentonow.bentonow.ui;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.ImageUtils;
import com.bentonow.bentonow.dao.DishDao;
import com.bentonow.bentonow.model.BackendText;
import com.bentonow.bentonow.model.DishModel;

public class ItemHolder {

    public ImageView image;
    public TextView title;
    public TextView description;
    public View soldOut;
    public View label;
    public View titleContainer;
    public View img_gradient;
    public BackendAutoFitTextView btn_add_to_bento;
    public BackendAutoFitTextView btn_added;

    public DishModel dishModel;
    public DishDao dishDao;
    public boolean added;
    public boolean selected;

    Activity context;

    public ItemHolder(Activity context, View view, int imageId, int titleId, int descriptionId, int soldOutId, int labelId, int titleContainerId) {
        setup(context, view, imageId, titleId, descriptionId, soldOutId, labelId, titleContainerId, 0, 0, 0);
    }

    public ItemHolder(Activity context, View view, int imageId, int titleId, int descriptionId, int soldOutId, int imgGradientId, int btnId, int btnAddedId) {
        setup(context, view, imageId, titleId, descriptionId, soldOutId, 0, 0, imgGradientId, btnId, btnAddedId);
    }

    void setup(Activity context, View view, int imageId, int titleId, int descriptionId, int soldOutId, int labelId, int titleContainerId, int imgGradientId, int btnId, int btnAddedId) {
        this.context = context;

        if (view != null) {
            image = (ImageView) view.findViewById(imageId);
            title = (TextView) view.findViewById(titleId);
            description = (TextView) view.findViewById(descriptionId);
            soldOut = view.findViewById(soldOutId);
            label = view.findViewById(labelId);
            titleContainer = view.findViewById(titleContainerId);
            img_gradient = view.findViewById(imgGradientId);
            btn_add_to_bento = (BackendAutoFitTextView) view.findViewById(btnId);
            btn_added = (BackendAutoFitTextView) view.findViewById(btnAddedId);
        } else {
            image = (ImageView) context.findViewById(imageId);
            title = (TextView) context.findViewById(titleId);
            description = (TextView) context.findViewById(descriptionId);
            soldOut = context.findViewById(soldOutId);
            label = context.findViewById(labelId);
            titleContainer = context.findViewById(titleContainerId);
            img_gradient = context.findViewById(imgGradientId);
            btn_add_to_bento = (BackendAutoFitTextView) context.findViewById(btnId);
            btn_added = (BackendAutoFitTextView) context.findViewById(btnAddedId);
        }

        setData(null, true);
    }

    public void setData(DishModel dishModel, boolean countCurrent) {
        this.dishModel = dishModel;
        dishDao = new DishDao();
        updateUI(countCurrent);
    }

    public void updateUI(boolean countCurrent) {
        if (dishModel == null || dishModel.name.isEmpty()) {
            image.setImageBitmap(null);
            if (title != null) title.setText("");
            if (description != null) description.setText("");
            if (label != null) label.setVisibility(View.VISIBLE);
            if (titleContainer != null) titleContainer.setVisibility(View.GONE);
            if (img_gradient != null) img_gradient.setVisibility(View.GONE);
            if (btn_add_to_bento != null) btn_add_to_bento.setVisibility(View.GONE);
            if (btn_added != null) btn_added.setVisibility(View.GONE);

            if (soldOut != null) {
                soldOut.setVisibility(View.GONE);
            }
        } else {
            if (title != null) title.setText(dishModel.name);
            if (titleContainer != null) titleContainer.setVisibility(View.VISIBLE);
            if (label != null) label.setVisibility(View.GONE);

            if (description != null) {
                description.setVisibility(View.GONE);
                description.setText(dishModel.description);
            }

            try {

                if (image.getTag() == null || !image.getTag().equals(dishModel.image1)) {
                    if (dishModel.type.equals("main"))
                        ImageUtils.initImageLoader().displayImage(dishModel.image1, image, ImageUtils.dishMainImageOptions());
                    else
                        ImageUtils.initImageLoader().displayImage(dishModel.image1, image, ImageUtils.dishSideImageOptions());
                    image.setTag(dishModel.image1);
                }
            } catch (Exception ex) {
                DebugUtils.logError("LoadImage", ex);
                image.setImageResource(R.drawable.menu_placeholder);
            }


            if (btn_add_to_bento != null) {
                if (!dishDao.canBeAdded(dishModel)) {
                    btn_add_to_bento.setText(BackendText.get("reached-max-button"));
                } else if (DishDao.isSoldOut(dishModel, countCurrent)) {
                    btn_add_to_bento.setText("Sold Out");

                    if (soldOut != null)
                        if (DishDao.isSoldOut(dishModel, countCurrent))
                            soldOut.setVisibility(View.VISIBLE);
                        else
                            soldOut.setVisibility(View.GONE);

                } else
                    btn_add_to_bento.setText(BackendText.get("build-main-add-button-1"));
            }

            if (img_gradient != null)
                img_gradient.setVisibility(selected ? View.VISIBLE : View.GONE);
            if (btn_added != null)
                btn_added.setVisibility(selected && added ? View.VISIBLE : View.GONE);
            if (btn_add_to_bento != null)
                btn_add_to_bento.setVisibility(selected && !added ? View.VISIBLE : View.GONE);
            if (description != null) description.setVisibility(selected ? View.VISIBLE : View.GONE);
        }
    }
}
