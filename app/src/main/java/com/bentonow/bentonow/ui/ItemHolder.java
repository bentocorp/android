package com.bentonow.bentonow.ui;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.model.BackendText;
import com.bentonow.bentonow.model.Item;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Callback;

public class ItemHolder implements Callback {

    public ImageView image;
    public TextView title;
    public TextView description;
    public View soldOut;
    public View label;
    public View titleContainer;
    public View img_gradient;
    public Button btn;
    public View btn_added;

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

    void setup (Activity context, View view, int imageId, int titleId, int descriptionId, int soldOutId, int labelId, int titleContainerId, int imgGradientId, int btnId, int btnAddedId) {
        this.context = context;

        if (view != null) {
            image = (ImageView) view.findViewById(imageId);
            title = (TextView) view.findViewById(titleId);
            description = (TextView) view.findViewById(descriptionId);
            soldOut = view.findViewById(soldOutId);
            label = view.findViewById(labelId);
            titleContainer = view.findViewById(titleContainerId);
            img_gradient = view.findViewById(imgGradientId);
            btn = (Button) view.findViewById(btnId);
            btn_added = view.findViewById(btnAddedId);
        } else {
            image = (ImageView) context.findViewById(imageId);
            title = (TextView) context.findViewById(titleId);
            description = (TextView) context.findViewById(descriptionId);
            soldOut = context.findViewById(soldOutId);
            label = context.findViewById(labelId);
            titleContainer = context.findViewById(titleContainerId);
            img_gradient = context.findViewById(imgGradientId);
            btn = (Button) context.findViewById(btnId);
            btn_added = context.findViewById(btnAddedId);
        }

        setData(null, true);
    }

    public void setData (Item item, boolean countCurrent) {
        this.item = item;

        updateUI(countCurrent);
    }

    public void updateUI (boolean countCurrent) {
        if (item == null) {
            image.setImageBitmap(null);
            if (title != null) title.setText("");
            if (description != null) description.setText("");
            if (label != null) label.setVisibility(View.VISIBLE);
            if (titleContainer != null) titleContainer.setVisibility(View.GONE);
            if (img_gradient != null) img_gradient.setVisibility(View.GONE);
            if (btn != null) btn.setVisibility(View.GONE);
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
                if (item.image1.isEmpty()) {
                    image.setImageResource(R.drawable.menu_placeholder);
                } else {
                    Picasso.with(context.getApplicationContext())
                            .load(item.image1)
                            .into(image, this);
                }
            } catch (Exception ignored) {}

            if (btn != null) {
                if (item.isSoldOut(countCurrent)) btn.setText("Sold Out");
                else if (!item.canBeAdded()) btn.setText("Reached to max");
                else btn.setText(BackendText.get("build-main-add-button-1"));

            }

            if (img_gradient != null) img_gradient.setVisibility(selected ? View.VISIBLE : View.GONE);
            if (btn_added != null) btn_added.setVisibility(selected && added ? View.VISIBLE : View.GONE);
            if (btn != null) btn.setVisibility(selected && !added ? View.VISIBLE : View.GONE);
            if (description != null) description.setVisibility(selected ? View.VISIBLE : View.GONE);
        }
    }

    //region Picasso Callback

    @Override
    public void onSuccess() {

    }

    @Override
    public void onError() {
        if (image == null) return;
        image.setImageResource(R.drawable.menu_placeholder);
    }

    //endregion
}
