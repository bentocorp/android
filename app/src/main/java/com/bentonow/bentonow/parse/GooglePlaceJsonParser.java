package com.bentonow.bentonow.parse;

import com.bentonow.bentonow.Utils.DebugUtils;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

/**
 * Created by Jose Torres on 18/05/15.
 */
public class GooglePlaceJsonParser extends MainParser {

    public static final String TAG = "GooglePlaceJsonParser";

    public static LatLng parseLocation(String json) {
        LatLng mLocation = null;
        startParsed();
        try {

            JSONObject response = new JSONObject(json.toString());
            JSONObject jResult = response.getJSONObject("result");
            JSONObject jGeometry = jResult.getJSONObject("geometry");
            JSONObject jLocation = jGeometry.getJSONObject("location");
            double lat = jLocation.getDouble("lat");
            double lng = jLocation.getDouble("lng");

            mLocation = new LatLng(lat, lng);

        } catch (Exception ex) {
            DebugUtils.logError(TAG, ex);

        }

        stopParsed();

        return mLocation;
    }

}
