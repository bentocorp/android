package com.bentonow.bentonow.controllers.adapter;

import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.listener.ListenerOrderHistory;
import com.bentonow.bentonow.model.order.history.OrderHistoryItemModel;
import com.bentonow.bentonow.model.order.history.OrderHistoryItemSectionModel;
import com.bentonow.bentonow.ui.wrapper.ItemChildOrderHistoryWrapper;
import com.bentonow.bentonow.ui.wrapper.ItemHeaderOrderHistoryWrapper;

import java.util.ArrayList;

/**
 * Created by kokusho on 1/4/16.
 */
public class ExpandableListOrderHistoryAdapter extends BaseExpandableListAdapter {

    public static String TAG = "ExpandableListOrderHistoryAdapter";

    private FragmentActivity mActivity;
    private ArrayList<OrderHistoryItemSectionModel> listHistorySection = new ArrayList<>();
    private ListenerOrderHistory mListener;

    public ExpandableListOrderHistoryAdapter(FragmentActivity mActivity, ListenerOrderHistory mListener) {
        this.mActivity = mActivity;
        this.mListener = mListener;
    }

    public void setListHistory(ArrayList<OrderHistoryItemSectionModel> listHistorySection) {
        this.listHistorySection = listHistorySection;
        notifyDataSetChanged();
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return listHistorySection.get(groupPosition).getListItems();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final ItemChildOrderHistoryWrapper viewHolder;
        final OrderHistoryItemModel mItem = listHistorySection.get(groupPosition).getListItems().get(childPosition);

        if (convertView == null) {
            LayoutInflater mInflater = mActivity.getLayoutInflater();
            convertView = mInflater.inflate(R.layout.list_item_order_history, parent, false);
            viewHolder = new ItemChildOrderHistoryWrapper(convertView);
            convertView.setTag(viewHolder);
        } else
            viewHolder = (ItemChildOrderHistoryWrapper) convertView.getTag();

        viewHolder.getTxtOrderHistoryTitle().setText(mItem.getTitle());
        viewHolder.getTxtOrderHistoryPrice().setText(mItem.getPrice());
        viewHolder.getBtnEditOrder().setVisibility(listHistorySection.get(groupPosition).getSectionTitle().contains("Scheduled") ? View.VISIBLE : View.GONE);


        return convertView;
    }

    @Override
    public int getChildrenCount(final int groupPosition) {
        return listHistorySection.get(groupPosition).getListItems().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return listHistorySection.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return listHistorySection.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        final ItemHeaderOrderHistoryWrapper viewHolder;
        final OrderHistoryItemSectionModel mHeader = listHistorySection.get(groupPosition);

        if (convertView == null) {
            LayoutInflater mInflater = mActivity.getLayoutInflater();
            convertView = mInflater.inflate(R.layout.list_item_header_order_history, parent, false);
            viewHolder = new ItemHeaderOrderHistoryWrapper(convertView);
            convertView.setTag(viewHolder);
        } else
            viewHolder = (ItemHeaderOrderHistoryWrapper) convertView.getTag();

        viewHolder.getTxtOhHeader().setText(mHeader.getSectionTitle());

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

}