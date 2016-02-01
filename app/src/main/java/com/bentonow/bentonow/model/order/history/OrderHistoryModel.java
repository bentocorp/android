package com.bentonow.bentonow.model.order.history;

import java.util.ArrayList;

/**
 * Created by kokusho on 2/1/16.
 */
public class OrderHistoryModel {

    private ArrayList<OrderHistoryItemSectionModel> listHistorySection = new ArrayList<>();

    public ArrayList<OrderHistoryItemSectionModel> getListHistorySection() {
        return listHistorySection;
    }

    public void setListHistorySection(ArrayList<OrderHistoryItemSectionModel> listHistorySection) {
        this.listHistorySection = listHistorySection;
    }
}
