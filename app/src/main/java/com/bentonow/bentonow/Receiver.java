package com.bentonow.bentonow;

/**
 * Created by gonzalo on 21/04/2015.
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Receiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context,BentoService.class);
        if(!BentoService.isRunning())context.startService(serviceIntent);
    }
}