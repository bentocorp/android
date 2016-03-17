package com.bentonow.bentonow.model.socket;

/**
 * Created by kokusho on 3/7/16.
 */
public class GlocSocketModel {
    private String lat;
    private String lng;
    private String clientId;

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
