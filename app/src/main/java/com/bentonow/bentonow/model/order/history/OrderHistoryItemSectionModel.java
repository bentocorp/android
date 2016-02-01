package com.bentonow.bentonow.model.order.history;

import java.util.ArrayList;

/**
 * Created by kokusho on 2/1/16.
 */
public class OrderHistoryItemSectionModel {
    private String sectionTitle;
    private ArrayList<OrderHistoryItemModel> listItems = new ArrayList<>();

    public String getSectionTitle() {
        return sectionTitle;
    }

    public void setSectionTitle(String sectionTitle) {
        this.sectionTitle = sectionTitle;
    }

    public ArrayList<OrderHistoryItemModel> getListItems() {
        return listItems;
    }

    public void setListItems(ArrayList<OrderHistoryItemModel> listItems) {
        this.listItems = listItems;
    }
}
