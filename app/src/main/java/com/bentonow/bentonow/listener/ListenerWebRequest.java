package com.bentonow.bentonow.listener;

/**
 * Created by Jose Torres on 14/05/15.
 */
public abstract class ListenerWebRequest {
    public void onError(String sError, int statusCode) {
        onComplete();
    }

    public void onResponse(Object oResponse, int statusCode) {
        onComplete();
    }

    public void onComplete() {
    }
}
