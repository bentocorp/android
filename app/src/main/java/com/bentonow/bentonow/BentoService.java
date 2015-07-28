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
import com.bentonow.bentonow.model.Shop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BentoService extends Service {

    private static final String TAG = "BentoService";
    private static BentoService instance;
    private AQuery aq;

    public static String lastStatus = "";

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
    public int onStartCommand(Intent intent,int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i(TAG, "Service.onStartCommand()");
        return(START_NOT_STICKY);
    }

    private void initTimerChecker() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                checkAll();
            }
        }, Config.TIME_TO_CHECK_IF_BENTO_OPEN);
    }

    public static void processMenuStock(JSONArray menu){
        Log.i(TAG,"processMenuStock(JSONArray menu)");
        try {
            String date = new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date());
            Log.i(TAG, "date: " + date);

            List<Dish> menu_de_hoy = Dish.find(Dish.class, "TODAY=?", date);
            for( Dish d : menu_de_hoy ){
                d.today = "";
                d.save();
            }

            for (int i = 0; i < menu.length(); i++) {
                JSONObject obj = menu.getJSONObject(i);
                String dish_id = obj.getString("itemId");
                String qty = obj.getString("qty");
                long did = Dish.getIdBy_id(dish_id);
                if (did != 0) {
                    Dish dish = Dish.findById(Dish.class, did);
                    dish.qty = qty;
                    dish.today = date;
                    dish.save();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static boolean forceCheckAll() {
        try {
            instance.checkAll();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void checkAll(){
        Log.i(TAG, "checkOverAll()" );
        if( Bentonow.app.isFocused && !Bentonow.app.is_first_access ) {
            String uri = getResources().getString(R.string.server_api_url) + Config.API.STATUS_ALL_URN;
            aq.ajax(uri, JSONObject.class, new AjaxCallback<JSONObject>() {
                @Override
                public void callback(String url, JSONObject json, AjaxStatus status) {
                    if (json != null) {
                        //Log.i(TAG, "json: " + json.toString());
                        try {
                            JSONObject overall = json.getJSONObject("overall");

                            lastStatus = Shop.isOpen() ? "open" : "closed";
                            Shop.status = overall.getString(Config.API.STATUS_OVERALL_LABEL_VALUE);

                            // Checks if the store is open from app logic
                            if (!Shop.isOpen() && Shop.status.equals("open")) {
                                Shop.status = "closed";
                            }

                            Log.i(TAG, "appStatus: " + Shop.status + " serverStatus: " + lastStatus + " isOpen: " + Shop.isOpen());
                            if (!lastStatus.equals(Shop.status)) {
                                lastStatus = Shop.status;
                                if (Shop.isOpen()) {
                                    // STATUS / MENU
                                    JSONArray menu = json.getJSONArray("menu");
                                    processMenuStock(menu);

                                    // STATUS / OVERALL
                                    goTo(Target.MainActivity);
                                } else if (Shop.isSoldOut()) {
                                    goTo(Target.ErrorSold);
                                } else {
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
        Intent dialogIntent;
        switch (deliveryLocation){
            case MainActivity:
                Log.i(TAG, "goTo: MainActivity");
                dialogIntent = new Intent(this, MainActivity.class);
                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(dialogIntent);
                Bentonow.app.current_activity.overridePendingTransition(R.anim.top_slide_in, R.anim.bottom_slide_out);
                break;
            case ErrorClosed:
                Log.i(TAG, "goTo: ClosedActivity");
                if( !Bentonow.app.current_activity.getLocalClassName().equals( "ErrorClosedActivity" ) ) {
                    dialogIntent = new Intent(this, ErrorClosedActivity.class);
                    dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(dialogIntent);
                    Bentonow.app.current_activity.overridePendingTransition(R.anim.bottom_slide_in, R.anim.top_slide_out);
                }
                break;
            case ErrorSold:
                Log.i(TAG, "goTo: SoldActivity");
                if( !Bentonow.app.current_activity.getLocalClassName().equals( "ErrorOutOfStockActivity" ) ) {
                    dialogIntent = new Intent(this, ErrorOutOfStockActivity.class);
                    dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(dialogIntent);
                    Bentonow.app.current_activity.overridePendingTransition(R.anim.bottom_slide_in, R.anim.top_slide_out);
                }
                break;
        }
    }

    private enum Target {
        MainActivity, ErrorClosed, ErrorSold
    }
}


