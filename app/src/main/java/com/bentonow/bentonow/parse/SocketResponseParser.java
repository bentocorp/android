package com.bentonow.bentonow.parse;

import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.model.socket.GlocSocketModel;
import com.bentonow.bentonow.model.socket.ResponseSocketModel;

import org.json.JSONObject;

/**
 * Created by kokusho on 3/4/16.
 */
public class SocketResponseParser extends MainParser {

    public static final String TAG = "GoogleDirectionParser";

    public static ResponseSocketModel parseResponse(String sResponse) {

        JSONObject jsonResponse;

        ResponseSocketModel mResponseSocket = new ResponseSocketModel();

        try {
            jsonResponse = new JSONObject(sResponse);

            mResponseSocket.setCode(jsonResponse.getInt("code"));
            mResponseSocket.setMsg(jsonResponse.getString("msg"));
            mResponseSocket.setRet(jsonResponse.getString("ret"));

        } catch (Exception ex) {
            DebugUtils.logDebug(TAG, ex);
            return mResponseSocket;
        }


        return mResponseSocket;

    }


    public static String parseToken(String sRet) {

        JSONObject jsonResponse;

        try {
            jsonResponse = new JSONObject(sRet);

            return jsonResponse.getString("token");
        } catch (Exception ex) {
            DebugUtils.logDebug(TAG, ex);
            return "";
        }

    }

    public static boolean parseTrack(String sRet) {

        JSONObject jsonResponse;

        try {
            jsonResponse = new JSONObject(sRet);

            return jsonResponse.getBoolean("connected");
        } catch (Exception ex) {
            DebugUtils.logDebug(TAG, ex);
            return false;
        }

    }

    public static String parseDriverPush(String sRet) {

        JSONObject jsonResponse;

        try {
            jsonResponse = new JSONObject(sRet);

            return jsonResponse.getString("clientId");
        } catch (Exception ex) {
            DebugUtils.logDebug(TAG, ex);
            return "";
        }

    }

    public static GlocSocketModel parseGloc(String sRet) {
        GlocSocketModel mGloc = null;

        JSONObject jsonResponse;

        try {
            jsonResponse = new JSONObject(sRet);
            mGloc = new GlocSocketModel();

            mGloc.setLat(jsonResponse.getString("lat"));
            mGloc.setLng(jsonResponse.getString("lng"));
            mGloc.setClientId(jsonResponse.getString("clientId"));
            if (mGloc.getClientId().contains("d-"))
                mGloc.setClientId(mGloc.getClientId().replace("d-", ""));

            return mGloc;
        } catch (Exception ex) {
            DebugUtils.logError(TAG, ex);
            return mGloc;
        }

    }

    public static GlocSocketModel parseLoc(String sRet) {
        GlocSocketModel mGloc = null;

        JSONObject jsonResponse;

        try {
            jsonResponse = new JSONObject(sRet);
            mGloc = new GlocSocketModel();

            mGloc.setLat(jsonResponse.getString("lat"));
            mGloc.setLng(jsonResponse.getString("lng"));

            return mGloc;
        } catch (Exception ex) {
            DebugUtils.logDebug(TAG, ex);
            return mGloc;
        }

    }
}
