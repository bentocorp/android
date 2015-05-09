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
import com.bentonow.bentonow.model.Orders;

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
        setContentView(R.layout.activity_main);
        aq = new AQuery(this);
        home_preloader = (ProgressBar)findViewById(R.id.home_preloader);
        splash_message = (TextView) findViewById(R.id.splash_message);

        Bentonow.app.current_activity = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isFirstInit())
            goToHomeAbout();
        else{
            checkOverAll();
        }
    }

    private void goToHomeAbout() {
        //showSplashMessage("It is you first access!");
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                Intent intent = new Intent(getApplicationContext(), HomeAboutActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out);
                finish();
            }
        }, 1000);
    }

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
                        } else {
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
        long todayMenu = Dish.count(Dish.class, "today=?", new String[]{todayDate});
        if ( todayMenu==0 ) {
            String uri = Config.API.URL + Config.API.MENU_URN + "/" + todayDate;
            Log.i(TAG, "URI: "+uri);
            aq.ajax(uri, JSONObject.class, new AjaxCallback<JSONObject>() {
                @Override
                public void callback(String url, JSONObject json, AjaxStatus status) {
                    if (json != null) {
                        Log.i(TAG,"json: "+json.toString());
                        try {
                            JSONObject menu = json.getJSONObject("menus");
                            JSONObject dinner = menu.getJSONObject("dinner");
                            JSONArray MenuItems = (JSONArray) dinner.get(Config.API_MENUITEMS_TAG);
                            for (int i = 0; i < MenuItems.length(); i++) {
                                JSONObject gDish = (JSONObject) MenuItems.get(i);
                                long menuHoy = Dish.count(Dish.class, "_id=?", new String[]{gDish.getString("itemId")});
                                if (menuHoy == 0) {
                                    Dish dish = new Dish(gDish.getString("itemId"), gDish.getString("name"), gDish.getString("description"), gDish.getString("type"), gDish.getString("image1"), gDish.getString("max_per_order"), todayDate);
                                    dish.save();
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
        }else{
            Log.i(TAG, "The menu of the day is changed");
            checkForPendingOrder();
        }
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
        Log.i(TAG,"checkForPendingOrder()");
        Log.i(TAG,"Orders.listAll =========================");
        List<Orders> orders = Orders.listAll(Orders.class);
        for( Orders o : orders ){
            Log.i(TAG,o.toString());
        }

        Log.i(TAG,"Orders.find(Orders.class, \"completed = ? AND today = ?\", \"no\",todayDate) =========================");
        List<Orders> pending_orders = Orders.find(Orders.class, "completed = ? AND today = ?", "no",todayDate);
        for( Orders o : pending_orders ){
            Log.i(TAG,o.toString());
        }

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
                Log.i(TAG, "set Bentonow.pending_order_id: "+Bentonow.pending_order_id);
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

}
