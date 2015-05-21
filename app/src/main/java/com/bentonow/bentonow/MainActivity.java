package com.bentonow.bentonow;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.bentonow.bentonow.model.Checkpoint;
import com.bentonow.bentonow.model.Dish;
import com.bentonow.bentonow.model.Ioscopy;
import com.bentonow.bentonow.model.Orders;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    private AQuery aq;
    private ProgressBar home_preloader;
    private TextView splash_message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        aq = new AQuery(this);
        home_preloader = (ProgressBar)findViewById(R.id.home_preloader);
        splash_message = (TextView) findViewById(R.id.splash_message);

        Bentonow.app.current_activity = this;
    }

    @Override
    protected void onResume() {
        super.onResume();

        long cantidad = Ioscopy.count(Ioscopy.class,null,null);
        Log.i(TAG, "cantidad: " + cantidad);
        if( cantidad == 0 ) tryGetInit();
        else init();
    }

    private void init() {
        if(isFirstInit()) {
            goToHomeAbout();
        }else{
            checkOverAll();
        }
    }

    void tryGetInit(){
        String uri = Config.API.URL+Config.API.INIT;
        Log.i(TAG, "uri: " + uri);
        aq.ajax(uri, JSONObject.class, new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject json, AjaxStatus status) {
                if (json != null) {
                    try {
                        JSONArray init = json.getJSONArray(Config.API.IOSCOPY);
                        for (int i = 0; i < init.length(); i++) {
                            JSONObject row = (JSONObject) init.get(i);
                            Ioscopy ioscopy = new Ioscopy(row.getString(Config.API.INIT_KEY), row.getString(Config.API.INIT_VALUE), row.getString(Config.API.INIT_TYPE));
                            ioscopy.save();
                        }
                        init();
                    } catch (JSONException e) {
                        //Log.e(TAG, status.getError());
                        e.printStackTrace();
                    }
                } else {
                    Log.e(TAG, status.getError());
                }
            }
        });
    }

    private void goToHomeAbout() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                goToHomeAboutActivity();
            }
        }, 1000);
    }

    void goToHomeAboutActivity(){
        Intent intent = new Intent(getApplicationContext(), HomeAboutActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransitionGoRight();
    }

    /*private void overridePendingTransitionGoRigh() {
        overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out);
    }

    private void overridePendingTransitionGoLeft() {
        overridePendingTransition(R.anim.left_slide_in, R.anim.right_slide_out);
    }*/

    private boolean isFirstInit() {
        return Checkpoint.count(Checkpoint.class, null, null) == 0;
    }

    public void checkOverAll(){
        Log.i(TAG,"checkOverAll()");
        home_preloader.setVisibility(View.VISIBLE);
        hideSplashMessage();

        String uri = Config.API.URL+Config.API.STATUS_OVERALL_URN;
        Log.i(TAG,"uri: "+uri);
        aq.ajax(uri, JSONObject.class, new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject json, AjaxStatus status) {
                //home_preloader.setVisibility(View.INVISIBLE);
                if (json != null) {
                    try {
                        // IF BENTO NOW OPEN
                        if (json.get(Config.API.STATUS_OVERALL_LABEL_VALUE).equals(Config.API.STATUS_OVERALL_MESSAGE_OPEN)) {
                            checkForTodayMenu();
                            Bentonow.isOpen = true;
                        } if (json.get(Config.API.STATUS_OVERALL_LABEL_VALUE).equals(Config.API.STATUS_OVERALL_MESSAGE_SOLDOUT)) {
                            goToErrorSolded();
                        } else if (json.get(Config.API.STATUS_OVERALL_LABEL_VALUE).equals(Config.API.STATUS_OVERALL_MESSAGE_CLOSED)) {
                            goToErrorClosed();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    //Toast.makeText(aq.getContext(), "Error:" + status.getMessage(), Toast.LENGTH_LONG).show();
                    showSplashMessage(status.getMessage());
                }
            }
        });
    }

    private void checkForTodayMenu() {
        Log.i(TAG, "checkForTodayMenu()");
        //long todayMenu = Dish.count(Dish.class, "today=?", new String[]{todayDate});
        //if ( todayMenu==0 ) {
            String uri = Config.API.URL + Config.API.MENU_URN + "/" + todayDate;
            Log.i(TAG, "URI: "+uri);
            aq.ajax(uri, JSONObject.class, new AjaxCallback<JSONObject>() {
                @Override
                public void callback(String url, JSONObject json, AjaxStatus status) {
                    if (json != null) {
                        Log.i(TAG,"json: "+json.toString());
                        try {
                            /*JSONObject menu = json.getJSONObject("menus");
                            JSONObject dinner = menu.getJSONObject("dinner");
                            JSONArray MenuItems = (JSONArray) dinner.get(Config.API_MENUITEMS_TAG);*/
                            JSONArray MenuItems = (JSONArray) json.get("MenuItems");
                            for (int i = 0; i < MenuItems.length(); i++) {
                                JSONObject gDish = (JSONObject) MenuItems.get(i);
                                long menuHoy = Dish.count(Dish.class, "_id=?", new String[]{gDish.getString("itemId")});
                                if (menuHoy == 0) {
                                    Dish dish = new Dish(gDish.getString("itemId"), gDish.getString("name"), gDish.getString("description"), gDish.getString("type"), gDish.getString("image1"), gDish.getString("max_per_order"), todayDate, Config.aux_initial_stock);
                                    dish.save();
                                }else{
                                    long dish_id = Dish.getIdBy_id(gDish.getString("itemId"));
                                    if(dish_id!=0) {
                                        Dish dish = Dish.findById(Dish.class, dish_id);
                                        if(dish!=null){
                                            dish.name = gDish.getString("name");
                                            dish.description = gDish.getString("description");
                                            dish.type = gDish.getString("type");
                                            dish.image1 = gDish.getString("image1");
                                            dish.max_per_order = gDish.getString("max_per_order");
                                            dish.qty = Config.aux_initial_stock;
                                            dish.today = todayDate;
                                            dish.save();
                                        }
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        checkForPendingOrder();
                    } else {
                        Log.i(TAG, "json isNull");
                        checkForPendingOrder();
                    }
                }
            });
        /*}else{
            Log.i(TAG, "The menu of the day is changed");
            checkForPendingOrder();
        }*/
    }

    private void showSplashMessage(String message) {
        splash_message.setText(message);
        splash_message.setVisibility(View.VISIBLE);
        home_preloader.setVisibility(View.INVISIBLE);
    }


    private void hideSplashMessage() {
        splash_message.setText("");
        splash_message.setVisibility(View.INVISIBLE);
        home_preloader.setVisibility(View.VISIBLE);
    }

    private void checkForPendingOrder() {
        Log.i(TAG, "checkForPendingOrder()");
        List<Orders> pending_orders = Orders.find(Orders.class, "completed = ? AND today = ?", "no",todayDate);
        if( pending_orders.isEmpty() ) {
            // THERE IS NOT ORDERS
            // GO TO DELIVERY LOCATION
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    Intent intent = new Intent(getApplicationContext(), DeliveryLocationActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out);
                    finish();
                }
            }, 2000);
        }else{
            // THERE IS AN PENDING ORDER
            // GO TO BULD-BENTO
            for ( Orders order : pending_orders) {
                Bentonow.pending_order_id = order.getId();
                //Log.i(TAG, "set Bentonow.pending_order_id: "+Bentonow.pending_order_id);
            }
            Intent intent = new Intent(getApplicationContext(), BuildBentoActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out);
            finish();
        }
    }

    private void goToErrorClosed() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                Intent intent = new Intent(getApplicationContext(), ErrorClosedActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.bottom_slide_in, R.anim.none);
                finish();
            }
        }, 2000);
    }

    private void goToErrorSolded() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                Intent intent = new Intent(getApplicationContext(), ErrorOutOfStockActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.bottom_slide_in, R.anim.none);
                finish();
            }
        }, 2000);
    }

}
