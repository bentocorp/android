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
import com.bentonow.bentonow.model.menu.TimesModel;
import com.bentonow.bentonow.ui.wrapper.ItemSpinnerTimeSelectedWrapper;
import com.bentonow.bentonow.ui.wrapper.ItemSpinnerTimeWrapper;

/**
 * @author José Torres Fuentes
 */

public class SpinnerOATimeListAdapter extends ArrayAdapter<TimesModel> {

    private Activity mActivity;
    public int iSelectedPosition = 0;

    /**
     * @param context
     */
    public SpinnerOATimeListAdapter(Activity context) {
        super(context, 0);
        this.mActivity = context;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        return getCustomViewSelector(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomViewSelector(int position, View convertView, ViewGroup parent) {

        final ItemSpinnerTimeWrapper viewHolder;
        final TimesModel mTime = getItem(position);

        if (convertView == null) {
            LayoutInflater mInflater = mActivity.getLayoutInflater();
            convertView = mInflater.inflate(R.layout.spinner_item_time, parent, false);
            viewHolder = new ItemSpinnerTimeWrapper(convertView);
            convertView.setTag(viewHolder);
        } else
            viewHolder = (ItemSpinnerTimeWrapper) convertView.getTag();

        viewHolder.getTxtOaTime().setText(BentoNowUtils.getDateHuman(mTime.start, false) + "-" + BentoNowUtils.getDateHuman(mTime.end, true));

        return convertView;
    }

    public View getCustomView(final int position, View convertView, ViewGroup parent) {

        final ItemSpinnerTimeSelectedWrapper viewHolder;
        final TimesModel mTime = getItem(position);

        if (convertView == null) {
            LayoutInflater mInflater = mActivity.getLayoutInflater();
            convertView = mInflater.inflate(R.layout.spinner_item_time_selected, parent, false);
            viewHolder = new ItemSpinnerTimeSelectedWrapper(convertView);
            convertView.setTag(viewHolder);
        } else
            viewHolder = (ItemSpinnerTimeSelectedWrapper) convertView.getTag();

        viewHolder.getTxtOaTime().setText(BentoNowUtils.getDateHuman(mTime.start, false) + "-" + BentoNowUtils.getDateHuman(mTime.end, true));
        viewHolder.getImgSelector().setVisibility(iSelectedPosition == position ? View.VISIBLE : View.GONE);

        return convertView;
    }
}
