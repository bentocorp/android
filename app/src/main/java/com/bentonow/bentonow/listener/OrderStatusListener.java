package com.bentonow.bentonow.listener;

/**
 * Created by Jose Torres on 11/10/15.
 */
public interface OrderStatusListener {
    void onConnectionError(String sError);

    void onAuthenticationFailure(String sError);

    void onAuthenticationSuccess();

    void onDisconnect(boolean onPurpose);

    void onReconnecting();

    void onDriverLocation(double lat, double lng);

    void trackDriverByGoogleMaps();

    void trackDriverByGloc(double lat, double lng);

    void onPush(String sResponse);

    void getOrderHistory();

}