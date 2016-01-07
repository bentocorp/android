package com.bentonow.bentonow.controllers.adapter;

import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.dao.DishDao;
import com.bentonow.bentonow.dao.OrderDao;
import com.bentonow.bentonow.listener.ListenerCompleteOrder;
import com.bentonow.bentonow.model.BackendText;
import com.bentonow.bentonow.model.DishModel;
import com.bentonow.bentonow.model.Stock;
import com.bentonow.bentonow.model.order.OrderItem;
import com.bentonow.bentonow.ui.wrapper.ItemChildOrderWrapper;
import com.bentonow.bentonow.ui.wrapper.ItemHeaderOrderWrapper;
import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kokusho on 1/4/16.
 */
public class ExpandableListOrderAdapter extends BaseExpandableListAdapter {

    public static String TAG = "ExpandableListOrderAdapter";

    private FragmentActivity mActivity;
    private ListenerCompleteOrder mListener;
    private List<OrderItem> aOrderList = new ArrayList<>();
    private List<DishModel> aAddOnList = new ArrayList<>();
    private int iSelectedOrder = -1;
    private int iSelectedAddOn = -1;
    private boolean bEditOrder;
    private boolean bEditAddOn;

    public ExpandableListOrderAdapter(FragmentActivity mActivity, ListenerCompleteOrder mListener) {
        this.mActivity = mActivity;
        this.mListener = mListener;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        if (groupPosition == 0)
            return aOrderList.get(childPosition);
        else
            return aAddOnList.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        final ItemChildOrderWrapper viewHolder;

        if (convertView == null) {
            LayoutInflater mInflater = mActivity.getLayoutInflater();
            convertView = mInflater.inflate(R.layout.list_item_order, parent, false);
            viewHolder = new ItemChildOrderWrapper(convertView);
            convertView.setTag(viewHolder);
        } else
            viewHolder = (ItemChildOrderWrapper) convertView.getTag();

        switch (groupPosition) {
            case 0:
                final OrderItem mOrder = aOrderList.get(childPosition);
                viewHolder.getTxtName().setTextColor(mOrder.bIsSoldoOut ? mActivity.getResources().getColor(R.color.orange) : mActivity.getResources().getColor(R.color.btn_green));
                viewHolder.getTxtPrice().setTextColor(mOrder.bIsSoldoOut ? mActivity.getResources().getColor(R.color.orange) : mActivity.getResources().getColor(R.color.dark_gray));
                viewHolder.getTxtName().setText(mOrder.items.get(0).name);
                viewHolder.getTxtPrice().setText("$" + BentoNowUtils.getNumberFromPrice(OrderDao.getPriceByOrder(mOrder)));
                viewHolder.getBtnRemove().setVisibility(iSelectedOrder == childPosition ? View.VISIBLE : View.GONE);
                viewHolder.getBtnEdit().setVisibility(bEditOrder && iSelectedOrder != childPosition ? View.VISIBLE : View.GONE);
                viewHolder.getBtnRemove().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mListener != null) {
                            restartSelected();
                            mListener.onRemoveBento(mOrder.order_pk);
                        } else {
                            Crashlytics.log("ExpandableListOrderAdapter null pointer Listener");
                        }
                    }
                });
                viewHolder.getTxtName().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mListener != null) {
                            mListener.onEditBento(mOrder.order_pk);
                        } else {
                            Crashlytics.log("ExpandableListOrderAdapter null pointer Listener");
                        }
                    }
                });
                break;
            case 1:
                final DishModel mDish = aAddOnList.get(childPosition);
                boolean bIsSoldOut = DishDao.isSoldOut(mDish, false);
                viewHolder.getTxtName().setTextColor(bIsSoldOut ? mActivity.getResources().getColor(R.color.orange) : mActivity.getResources().getColor(R.color.btn_green));
                viewHolder.getTxtPrice().setTextColor(bIsSoldOut ? mActivity.getResources().getColor(R.color.orange) : mActivity.getResources().getColor(R.color.dark_gray));
                viewHolder.getTxtName().setText("(" + mDish.qty + "x) " + mDish.name);
                viewHolder.getTxtPrice().setText("$" + DishDao.getDefaultPriceBento(mDish.price));
                viewHolder.getBtnRemove().setVisibility(iSelectedAddOn == childPosition ? View.VISIBLE : View.GONE);
                viewHolder.getBtnEdit().setVisibility(bEditAddOn && iSelectedAddOn != childPosition ? View.VISIBLE : View.GONE);
                viewHolder.getBtnRemove().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mListener != null) {
                            restartSelected();
                            mListener.onRemoveAddOn(mDish.itemId);
                        } else {
                            Crashlytics.log("ExpandableListOrderAdapter null pointer Listener");
                        }
                    }
                });
                viewHolder.getTxtName().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mListener != null) {
                            mListener.onEditAddOn();
                        } else {
                            Crashlytics.log("ExpandableListOrderAdapter null pointer Listener");
                        }
                    }
                });
                break;
        }

        viewHolder.getBtnEdit().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (groupPosition == 0) {
                    iSelectedOrder = childPosition;
                    iSelectedAddOn = -1;
                } else {
                    iSelectedAddOn = childPosition;
                    iSelectedOrder = -1;
                }

                notifyDataSetChanged();
            }
        });

        return convertView;
    }

    @Override
    public int getChildrenCount(final int groupPosition) {
        if (groupPosition == 0)
            return aOrderList.size();
        else
            return aAddOnList.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return "Number:" + groupPosition;
    }

    @Override
    public int getGroupCount() {
        return 2;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        final ItemHeaderOrderWrapper viewHolder;

        if (convertView == null) {
            LayoutInflater mInflater = mActivity.getLayoutInflater();
            convertView = mInflater.inflate(R.layout.list_item_header_order, parent, false);
            viewHolder = new ItemHeaderOrderWrapper(convertView);
            convertView.setTag(viewHolder);
        } else
            viewHolder = (ItemHeaderOrderWrapper) convertView.getTag();

        if (groupPosition == 0) {
            viewHolder.getBtnOrderName().setText("ADD ANOTHER BENTO");
            viewHolder.getBtnOrderName().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        mListener.onAddAnotherBento();
                    } else {
                        Crashlytics.log("ExpandableListOrderAdapter null pointer Listener");
                    }

                }
            });
        } else {
            viewHolder.getBtnOrderName().setText("ADD ANOTHER ADD-ON");
            viewHolder.getBtnOrderName().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        mListener.onAddAnotherAddOn();
                    } else {
                        Crashlytics.log("ExpandableListOrderAdapter null pointer Listener");
                    }

                }
            });
        }

        if (groupPosition == 0) {
            viewHolder.getBtnDelete().setTextColor(bEditOrder ? mActivity.getResources().getColor(R.color.orange) : mActivity.getResources().getColor(R.color.btn_green));
            viewHolder.getBtnDelete().setText(bEditOrder ? BackendText.get("complete-done") : BackendText.get("complete-edit"));
            viewHolder.getBtnDelete().setVisibility(aOrderList.size() > 0 ? View.VISIBLE : View.INVISIBLE);
        } else {
            viewHolder.getBtnDelete().setTextColor(bEditAddOn ? mActivity.getResources().getColor(R.color.orange) : mActivity.getResources().getColor(R.color.btn_green));
            viewHolder.getBtnDelete().setText(bEditAddOn ? BackendText.get("complete-done") : BackendText.get("complete-edit"));
            viewHolder.getBtnDelete().setVisibility(aAddOnList.size() > 0 ? View.VISIBLE : View.INVISIBLE);
        }


        viewHolder.getBtnDelete().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iSelectedOrder = -1;
                iSelectedAddOn = -1;

                if (groupPosition == 0) {
                    bEditOrder = !bEditOrder;
                    bEditAddOn = false;
                } else {
                    bEditAddOn = !bEditAddOn;
                    bEditOrder = false;
                }

                notifyDataSetChanged();
            }
        });

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private void restartSelected() {
        iSelectedOrder = -1;
        iSelectedAddOn = -1;
        bEditAddOn = false;
        bEditOrder = false;
    }

    public void setaOrderList(List<OrderItem> aOrderList) {
        this.aOrderList = aOrderList;
    }

    public void setaAddOnList(List<DishModel> aAddOnList) {
        this.aAddOnList = aAddOnList;
    }

    public List<OrderItem> getaOrderList() {
        return aOrderList;
    }

    public List<DishModel> getaAddOnList() {
        return aAddOnList;
    }
}