package com.bentonow.bentonow.web.request;

import android.util.Log;

import com.bentonow.bentonow.listener.InterfaceWebRequest;
import com.bentonow.bentonow.listener.ListenerWebRequest;
import com.bentonow.bentonow.web.BentoNowApi;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Jose Torres on 14/05/15.
 */
public class RequestGetPlaceDetail implements InterfaceWebRequest {

    public static final String TAG = "RequestGetPlaceDetail";

    private ListenerWebRequest mListener;
    private String sPlaceId;

    public RequestGetPlaceDetail(String sPlaceId, ListenerWebRequest mListener) {
        this.mListener = mListener;
        this.sPlaceId = sPlaceId;
    }

    @Override
    public void dispatchRequest() {

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            URL url = new URL(BentoNowApi.getPlaceDetailUrl(sPlaceId));

            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }

            JSONObject response = new JSONObject(jsonResults.toString());
            JSONObject jResult = response.getJSONObject("result");
            JSONObject jGeometry = jResult.getJSONObject("geometry");
            JSONObject jLocation = jGeometry.getJSONObject("location");
            double lat = jLocation.getDouble("lat");
            double lng = jLocation.getDouble("lng");

            mListener.onResponse(new LatLng(lat, lng), 200);

        } catch (Exception e) {
            mListener.onError(e.getLocalizedMessage(), 400);
            Log.e(TAG, "Cannot process JSON results", e);

        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

    }
}
