package com.bentonow.bentonow;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.bentonow.bentonow.model.Dish;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

/**
 * Created by gonzalo on 28/01/2015.
 */
public class BentoService extends Service {

    public static String IMEI;
    private static final String TAG = "BentoService";
    private static BentoService instance;
    private AQuery aq;

    public static boolean isRunning() {
        return instance != null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.i(TAG,"onCreate()");
        instance=this;
        aq = new AQuery(this);
        initTimerChecker();
    }

    @Override
    public void onDestroy() {
        instance = null;
        Log.i(TAG, "onDestroy()");
        super.onDestroy();
    }

    @Override
    public void onStart(Intent intent, int startid) {
        Log.i(TAG, "Servicio MyService iniciado");
    }

    @Override
    public int onStartCommand(Intent intent,int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i(TAG, "Service.onStartCommand()");
        return(START_NOT_STICKY);
    }

    private void initTimerChecker() {
        Handler handler = new Handler();
        //handler.removeCallbacks();
        handler.postDelayed(new Runnable() {
            public void run() {
                checkAll();
            }
        }, Config.TIME_TO_CHECK_IF_BENTO_OPEN);
    }

    public void checkAll(){
        Log.i(TAG, "checkOverAll()" );
        if( Bentonow.app.isFocused && !Bentonow.app.is_first_access ) {
            String uri = Config.API.URL + Config.API.STATUS_ALL_URN;
            aq.ajax(uri, JSONObject.class, new AjaxCallback<JSONObject>() {
                @Override
                public void callback(String url, JSONObject json, AjaxStatus status) {
                    if (json != null) {
                        //Log.i(TAG, "json: " + json.toString());
                        try {
                            if (Bentonow.isOpen) {
                                // STATUS / MENU
                                JSONArray menu = json.getJSONArray("menu");
                                for (int i = 0; i < menu.length() - 1; i++) {
                                    JSONObject obj = menu.getJSONObject(i);
                                    String dish_id = obj.getString("itemId");
                                    String qty = obj.getString("qty");
                                    long did = Dish.getIdBy_id(dish_id);
                                    if (did != 0) {
                                        Dish dish = Dish.findById(Dish.class, did);
                                        if (!dish.qty.equals(qty)) {
                                            dish.qty = qty;
                                            dish.save();
                                        }
                                    }
                                }
                            }

                            // STATUS / OVERALL
                            JSONObject overall = json.getJSONObject("overall");
                            if (overall.get(Config.API.STATUS_OVERALL_LABEL_VALUE).equals(Config.API.STATUS_OVERALL_MESSAGE_OPEN)) {
                                if (!Bentonow.isOpen) {
                                    Bentonow.isOpen = true;
                                    goTo(Target.DeliveryLocation);
                                }
                            } else if (overall.get(Config.API.STATUS_OVERALL_LABEL_VALUE).equals(Config.API.STATUS_OVERALL_MESSAGE_SOLDOUT)) {
                                if (!Bentonow.isSolded) {
                                    Bentonow.isSolded = true;
                                    goTo(Target.ErrorSoulded);
                                }
                            } else if (overall.get(Config.API.STATUS_OVERALL_LABEL_VALUE).equals(Config.API.STATUS_OVERALL_MESSAGE_CLOSED)) {
                                if (Bentonow.isOpen) {
                                    Bentonow.isOpen = false;
                                    goTo(Target.ErrorClosed);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(aq.getContext(), "Error:" + status.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    initTimerChecker();
                }
            });
        }else initTimerChecker();
    }

    private void goTo(Target deliveryLocation) {
        Intent dialogIntent = null;
        switch (deliveryLocation){
            case DeliveryLocation:
                dialogIntent = new Intent(this, DeliveryLocationActivity.class);
                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(dialogIntent);
                Bentonow.app.current_activity.finish();
                Bentonow.app.current_activity.overridePendingTransition(R.anim.top_slide_in, R.anim.bottom_slide_out);
                break;
            case ErrorClosed:
                dialogIntent = new Intent(this, ErrorClosedActivity.class);
                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //dialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(dialogIntent);
                Bentonow.app.current_activity.finish();
                Bentonow.app.current_activity.overridePendingTransition(R.anim.bottom_slide_in, R.anim.top_slide_out);
                break;
            case ErrorSoulded:
                dialogIntent = new Intent(this, ErrorOutOfStockActivity.class);
                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(dialogIntent);
                Bentonow.app.current_activity.finish();
                Bentonow.app.current_activity.overridePendingTransition(R.anim.bottom_slide_in, R.anim.top_slide_out);
                break;
        }
    }

    private enum Target {
        DeliveryLocation, ErrorClosed, ErrorSoulded;
    }
}


