package com.bentonow.bentonow.web;


import android.location.Location;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.controllers.BentoApplication;

import java.net.URLEncoder;

/**
 * Created by Jose Torres on 8/17/15.
 */
public class BentoNowApi {

    public static String getPlaceDetailUrl(String sPlaceId) {
        String sUrl = "https://maps.googleapis.com/maps/api/place/details/json?placeid=" + sPlaceId + "&key=" + BentoApplication.instance.getResources().getString(R.string.google_server_key);
        return sUrl;
    }

    public static String getUrlGoogleAutocomplete(Location mLocation, String sInput) {
        String sAddress;

        try {
            sAddress = URLEncoder.encode(sInput, "utf8");
        } catch (Exception ex) {
            sAddress = sInput;
            DebugUtils.logError(ex);
        }
        if (mLocation == null)
            return BentoApplication.instance.getString(R.string.bento_api_url_autocomplete, BentoApplication.instance.getString(R.string.google_server_key), sAddress);
        else
            return BentoApplication.instance.getString(R.string.bento_api_url_autocomplete_location, BentoApplication.instance.getString(R.string.google_server_key), sAddress, mLocation.getLatitude(), mLocation.getLongitude());

    }

    public static String getUrlGoogleDirections(double dOriginLat, double dOriginLong, String dEndLat, String dEndLong) {
        return BentoApplication.instance.getString(R.string.google_api_url_direction, dOriginLat + "", dOriginLong + "", dEndLat, dEndLong, BentoApplication.instance.getString(R.string.google_server_key));
    }

    public static String getOrderStatusNode() {
        //Todo return to previous
        return BentoApplication.instance.getString(R.string.node_url);
        //DebugUtils.logDebug("URL:", "https://node.bentonow.com:8443");
        //return "https://node.bentonow.com:8443";
    }

    public static String getOrderStatusNode(String sUsername, String sPassword) {
        String sUrl = String.format("/api/authenticate?username=%s&token=%s&type=customer", sUsername, sPassword);
        return sUrl;
    }

    public static String getDriverTrackUrl(String sDriverId, String sUid) {
        String sUrl = String.format("/api/track?clientId=d-%s&uid=%s", sDriverId, sUid);
        return sUrl;
    }

    public static String getDriverTrackGloc(String sDriverId, String sToken) {
        String sUrl = String.format("/api/gloc?token=%s&clientId=d-%s", sToken, sDriverId);
        return sUrl;
    }

    public static String getDriverUntrack(String sUid, String sDriverId) {
        String sUrl = String.format("/api/untrack?uid=%s&clientId=d-%s", sUid, sDriverId);
        return sUrl;
    }

}
