package com.bentonow.bentonow.listener;

/**
 * Created by Jose Torres on 11/10/15.
 */
public interface OrderStatusListener {
    void onConnectionError(String sError);
    void onAuthenticationFailure(String sError);
    void onAuthenticationSuccess(String sToken);
    void onDisconnect(boolean onPurpose);
    void onReconnecting();


}