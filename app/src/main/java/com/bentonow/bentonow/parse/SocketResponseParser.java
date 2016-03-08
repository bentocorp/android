package com.bentonow.bentonow.parse;

import com.bentonow.bentonow.Utils.DebugUtils;
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

        } catch (Exception ex) {
            DebugUtils.logDebug(TAG, ex);
            return mResponseSocket;
        }


        return mResponseSocket;

    }
}
