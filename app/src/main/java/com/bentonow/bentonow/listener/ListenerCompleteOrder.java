package com.bentonow.bentonow.listener;

/**
 * Created by Jose Torres on 20/09/15.
 */
public interface ListenerCompleteOrder {
    void onAddAnotherBento();

    void onAddAnotherAddOn();

    void onEditBento(int iPk);

    void onEditAddOn();

    void onRemoveBento(int iPk);

    void onRemoveAddOn(int iPk);

}
