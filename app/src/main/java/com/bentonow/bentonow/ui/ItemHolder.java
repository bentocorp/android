package com.bentonow.bentonow.ui;

import android.app.Activity;
import android.media.Image;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.ImageUtils;
import com.bentonow.bentonow.model.BackendText;
import com.bentonow.bentonow.model.Item;

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

    public Item item;
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

    public void setData(Item item, boolean countCurrent) {
        this.item = item;

        updateUI(countCurrent);
    }

    public void updateUI(boolean countCurrent) {
        if (item == null) {
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
            if (title != null) title.setText(item.name);
            if (titleContainer != null) titleContainer.setVisibility(View.VISIBLE);
            if (label != null) label.setVisibility(View.GONE);

            if (description != null) {
                description.setVisibility(View.GONE);
                description.setText(item.description);
            }

            if (soldOut != null) {
                if (item.isSoldOut(countCurrent)) {
                    soldOut.setVisibility(View.VISIBLE);
                } else {
                    soldOut.setVisibility(View.GONE);
                }
            }

            try {

                if (image.getTag() == null || !image.getTag().equals(item.image1)) {
                    if (item.type.equals("main"))
                        ImageUtils.initImageLoader().displayImage(item.image1, image, ImageUtils.dishMainImageOptions());
                    else
                        ImageUtils.initImageLoader().displayImage(item.image1, image, ImageUtils.dishSideImageOptions());
                    image.setTag(item.image1);
                }
            } catch (Exception ex) {
                DebugUtils.logError("LoadImage", ex);
                image.setImageResource(R.drawable.menu_placeholder);
            }

            if (btn_add_to_bento != null) {
                if (item.isSoldOut(countCurrent)) btn_add_to_bento.setText("Sold Out");
                else if (!item.canBeAdded()) btn_add_to_bento.setText("Reached to max");
                else btn_add_to_bento.setText(BackendText.get("build-main-add-button-1"));

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
