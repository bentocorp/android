package com.bentonow.bentonow.parse;

import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.model.order.history.OrderHistoryItemModel;
import com.bentonow.bentonow.model.order.history.OrderHistoryItemSectionModel;
import com.bentonow.bentonow.model.order.history.OrderHistoryModel;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jose Torres on 18/05/15.
 */
public class OrderHistoryJsonParser extends MainParser {

    public static final String TAG = "OrderHistoryJsonParser";

    public static OrderHistoryModel parseOrderHistory(String json) {


        OrderHistoryModel mOrderHistory = new OrderHistoryModel();

        startParsed();
        try {

            JSONArray jsonOrders = new JSONArray(json);
            for (int a = 0; a < jsonOrders.length(); a++) {
                JSONObject jsonOrder = jsonOrders.getJSONObject(a);
                OrderHistoryItemSectionModel mOrderSection = new OrderHistoryItemSectionModel();
                mOrderSection.setSectionTitle(jsonOrder.getString(TAG_SECTION_TITLE));
                if (jsonOrder.has(TAG_ITEMS)) {
                    ArrayList<OrderHistoryItemModel> listOrder = gson.fromJson(jsonOrder.getString(TAG_ITEMS), new TypeToken<List<OrderHistoryItemModel>>() {
                    }.getType());
                    mOrderSection.setListItems(listOrder);
                    if (listOrder != null && !listOrder.isEmpty())
                        mOrderHistory.getListHistorySection().add(mOrderSection);
                }

                DebugUtils.logDebug(TAG, "Num Of Orders in  " + mOrderSection.getSectionTitle() + " : " + mOrderSection.getListItems().size());

            }

        } catch (Exception ex) {
            DebugUtils.logError(TAG, ex);

        }

        stopParsed();

        return mOrderHistory;
    }

}
