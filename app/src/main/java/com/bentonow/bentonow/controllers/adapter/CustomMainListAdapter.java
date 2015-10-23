/**
 *
 */
package com.bentonow.bentonow.controllers.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.listener.ListenerCustomDish;
import com.bentonow.bentonow.model.Item;
import com.bentonow.bentonow.ui.ItemHolder;

/**
 * @author José Torres Fuentes
 */
public class CustomMainListAdapter extends ArrayAdapter<Item> {

    private Activity mActivity;
    private ListenerCustomDish mListener;
    private Item mCurrentAdded;
    private Item mCurrentSelected;

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

        final ItemHolder viewHolder;
        final Item mDish = getItem(position);

        if (convertView == null) {
            convertView = mActivity.getLayoutInflater().inflate(R.layout.list_item_dish_main_custom, parent, false);
            viewHolder = new ItemHolder(mActivity, convertView, R.id.img, R.id.txt_title, R.id.txt_description, R.id.img_sold_out, R.id.img_gradient, R.id.btn_add_to_bento, R.id.btn_added);
            convertView.setTag(viewHolder);
        } else
            viewHolder = (ItemHolder) convertView.getTag();

        viewHolder.btn_add_to_bento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onAddToBentoClick(position);
                DebugUtils.logDebug("onAddToBentoClick()", position);
            }
        });
        viewHolder.btn_added.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onAddedClick(position);
                DebugUtils.logDebug("onAddedClick()", position);
            }
        });

        viewHolder.setData(mDish, true);
        viewHolder.added = (getCurrentAdded() != null && getCurrentAdded().itemId == viewHolder.item.itemId);
        viewHolder.selected = (getCurrentSelected() != null && getCurrentSelected().itemId == viewHolder.item.itemId);
        viewHolder.updateUI(!viewHolder.added);

        return convertView;
    }

    public Item getCurrentAdded() {
        return mCurrentAdded;
    }

    public void setCurrentAdded(Item mCurrentAdded) {
        this.mCurrentAdded = mCurrentAdded;
    }

    public Item getCurrentSelected() {
        return mCurrentSelected;
    }

    public void setCurrentSelected(Item mCurrentSelected) {
        this.mCurrentSelected = mCurrentSelected;
    }
}
