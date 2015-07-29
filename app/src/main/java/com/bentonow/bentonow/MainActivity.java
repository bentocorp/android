package com.bentonow.bentonow;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.bentonow.bentonow.Utils.Mixpanel;
import com.bentonow.bentonow.model.Dish;
import com.bentonow.bentonow.model.Ioscopy;
import com.bentonow.bentonow.model.Orders;

import com.bentonow.bentonow.model.Shop;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;
import com.squareup.picasso.Picasso;

import io.fabric.sdk.android.Fabric;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    private static final long LOCATION_REFRESH_TIME = 1;
    private static final float LOCATION_REFRESH_DISTANCE = 1;
    private AQuery aq;
    private TextView tx_slogan;
    private Activity activity;

    private String goingTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        aq = new AQuery(this);
        activity = this;
        Bentonow.app.current_activity = this;
        initElements();
    }

    private void initElements() {
        tx_slogan = (TextView) findViewById(R.id.tx_slogan);
    }

    @Override
    protected void onResume() {
        super.onResume();
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity.getApplicationContext());
        if (resultCode == ConnectionResult.SUCCESS) {
            tryGetAll(todayDate);
        } else if (resultCode == ConnectionResult.SERVICE_MISSING ||
                resultCode == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED ||
                resultCode == ConnectionResult.SERVICE_DISABLED) {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, activity, 1);
            dialog.show();
        }
    }

    private void tryToGetLocationFromGPS() {
        Log.i(TAG, "tryToGetLocationFromGPS");

        final TextView message = (TextView)findViewById(R.id.splash_message);
        message.setVisibility(View.VISIBLE);
        message.setText("Searching for your location...");
        int skipWaitTime = 3;
        new CountDownTimer(skipWaitTime * 1000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                message.append("\nTap the screen to skip");
                findViewById(R.id.content).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checkLocation(null);
                    }
                });
            }
        }.start();
        
        LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Config.current_location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (Config.current_location == null) {
            Config.current_location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if (Config.current_location != null) {
            Log.i(TAG, "provider location: " + Config.current_location.toString());
            Config.INIT_LAT_LONG = new LatLng(Config.current_location.getLatitude(), Config.current_location.getLongitude());
            checkLocation(Config.INIT_LAT_LONG);
        } else {
            final LocationListener mLocationListener = new LocationListener() {
                @Override
                public void onLocationChanged(final Location location) {
                    Log.i(TAG, "onLocationChanged() location: " + location.toString());
                    Config.current_location = location;
                    Config.INIT_LAT_LONG = new LatLng(Config.current_location.getLatitude(), Config.current_location.getLongitude());

                    checkLocation(Config.INIT_LAT_LONG);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {
                    checkLocation(null);
                }
            };

            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, mLocationListener);
        }
    }

    private void checkLocation(LatLng latlng) {
        if (latlng != null) {
            List<LatLng> sfpolygon = new ArrayList<>();
            String[] serviceArea_dinner = Config.serviceArea_dinner.split(" ");
            for (String aServiceArea_dinner : serviceArea_dinner) {
                String[] loc = aServiceArea_dinner.split(",");
                double lat = Double.valueOf(loc[1]);
                double lng = Double.valueOf(loc[0]);
                sfpolygon.add(new LatLng(lat, lng));
            }

            if (PolyUtil.containsLocation(latlng, sfpolygon, false)) {
                startActivity(new Intent(getApplicationContext(), BuildBentoActivity.class));
                overridePendingTransitionGoRight();
                finish();
                return;
            }
        }

        Mixpanel.track(this, "Opening app outside of service area");

        startActivity(new Intent(getApplicationContext(), DeliveryLocationActivity.class));
        overridePendingTransitionGoRight();
        finish();
    }

    private void init() {
        Log.i(TAG, "Config.android_min_version: " + Config.android_min_version);
        Log.i(TAG, "BuildConfig.VERSION_CODE: " + BuildConfig.VERSION_CODE);
        if ( BuildConfig.VERSION_CODE >= Config.android_min_version ) {
            if (isFirstInit()) {
                goTo goTo = new goTo();
                goTo.HomeAbout();
            } else {
                if(Shop.isOpen()) checkForPendingOrder();
            }
        }else {
            goTo goTo = new goTo();
            goTo.ErrorVersion();
        }
    }

    private void checkForPendingOrder() {
        Log.i(TAG, "checkForPendingOrder()");
        List<Orders> pending_orders = Orders.find(Orders.class, "completed = ? AND today = ?", "no", todayDate);
        if (pending_orders.isEmpty()) {
            tryToGetLocationFromGPS();
        } else {
            for (Orders order : pending_orders) {
                Bentonow.pending_order_id = order.getId();
            }
            Intent intent = new Intent(getApplicationContext(), BuildBentoActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out);
            finish();
        }
    }

    void tryGetAll( String date ){
        Log.i(TAG,"tryGetInit()");
        String uri = getResources().getString(R.string.server_api_url)+Config.API.INIT+"/"+date;
        Log.i(TAG, "uri: " + uri);
        aq.ajax(uri, JSONObject.class, new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject json, AjaxStatus status) {

                Log.i(TAG, "status.getCode(): " + status.getCode());
                Log.i(TAG, "status.getError(): " + status.getError());
                Log.i(TAG, "status.getMessage(): " + status.getMessage());
                Log.i(TAG, "status.getCode(): " + status.getCode());

                if(status.getCode()==200) {
                    JsonProcess JsonProcess = new JsonProcess();
                    // IOSCOPY
                    try {
                        JSONArray IOSCOPY = json.getJSONArray(Config.API.IOSCOPY);
                        JsonProcess.ioscopy(IOSCOPY);
                        tx_slogan.setText(Ioscopy.getKeyValue("launch-slogan"));
                    } catch (JSONException ignored) {

                    }

                    // IOSCOPY
                    try {
                        JSONObject meals = json.getJSONObject("meals");

                        JSONObject m2 = meals.getJSONObject("2");
                        Config.LunchStartTime = Integer.parseInt(m2.getString("startTime").replaceAll("[^0-9]", "").substring(0, 4));

                        JSONObject m3 = meals.getJSONObject("3");
                        Config.DinnerStartTime = Integer.parseInt(m3.getString("startTime").replaceAll("[^0-9]", "").substring(0, 4));
                    } catch (JSONException ignored) {

                    }

                    // /menu/next/{date}
                    try {
                        Shop.currentMenu = json.getJSONObject("/menu/{date}").getJSONObject("menus");
                        Log.i(TAG, "/menu/{date}: " + json.getJSONObject("/menu/{date}").getString("menus"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        Shop.nextMenu = json.getJSONObject("/menu/next/{date}").getJSONObject("menus");
                        Log.i(TAG, "/menu/next/{date}: " + json.getJSONObject("/menu/next/{date}").getString("menus"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    // STATUS/OVERALL
                    try {
                        BentoService.lastStatus = Shop.status = json.getJSONObject("/status/all").getJSONObject("overall").getString("value");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (Shop.isOpen()) {
                        // /menu/{date}
                        try {
                            JsonProcess.MenuItems(Shop.getCurrentMenuItems());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        // /status/all | menu
                        try {
                            JSONObject status_all = json.getJSONObject("/status/all");
                            JSONArray menu = status_all.getJSONArray("menu");
                            BentoService.processMenuStock(menu);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (Shop.isSoldOut()) {
                        new goTo().ErrorSolded();
                    } else {
                        new goTo().ErrorClosed();
                    }

                    try {
                        Config.android_min_version = json.getInt("android_min_version");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    /////////////////////////////////
                    try {
                        JSONObject settings = json.getJSONObject("settings");
                        Config.serviceArea_dinner = settings.getString("serviceArea_dinner");
                        Log.i(TAG, "Config.serviceArea_dinner: " + Config.serviceArea_dinner);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    init();
                }else{
                    Toast.makeText(getApplicationContext(), status.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    class JsonProcess {
        public void ioscopy(JSONArray IOSCOPY) {
            for (int i = 0; i < IOSCOPY.length(); i++) {
                JSONObject row;
                try {
                    row = (JSONObject) IOSCOPY.get(i);
                    String key = row.getString(Config.API.INIT_KEY);
                    String value = row.getString(Config.API.INIT_VALUE);
                    String type = row.getString(Config.API.INIT_TYPE);
                    long ioscopyId = Ioscopy.getIdByKey(key);
                    Ioscopy ioscopy;
                    if( ioscopyId!=0 ) {
                        ioscopy = Ioscopy.findById(Ioscopy.class, ioscopyId);
                        if( ioscopy.type.equals(type) && !ioscopy.value.equals(value) ) {
                            ioscopy.value = value;
                            ioscopy.save();
                        }
                    }else {
                        ioscopy = new Ioscopy(key, value, type);
                        ioscopy.save();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        public void MenuItems(JSONArray MenuItems) {
            Log.i(TAG, "MenuItems: " + (MenuItems != null ? MenuItems.toString() : "null"));
            if (MenuItems != null) {
                for (int i = 0; i < MenuItems.length(); i++) {
                    JSONObject gDish;
                    try {
                        gDish = (JSONObject) MenuItems.get(i);

                        Log.i(TAG, "gDish: " + gDish.toString());

                        String image = gDish.getString(Config.DISH.IMAGE1);
                        if (!TextUtils.isEmpty(image)) {
                            Picasso.with(getApplicationContext())
                                    .load(image)
                                    .fetch();
                        }

                        long menuHoy = Dish.count(Dish.class, "_id=?", new String[]{gDish.getString(Config.DISH.itemId)});

                        if (menuHoy == 0) {
                            Dish dish = new Dish();
                            dish._id = gDish.getString(Config.DISH.itemId);
                            dish.save();
                        }

                        Dish dish = Dish.findDish(gDish.getString(Config.DISH.itemId));

                        if (dish != null) {
                            dish.name = gDish.getString(Config.DISH.NAME);
                            dish.description = gDish.getString(Config.DISH.DESCRIPTION);
                            dish.type = gDish.getString(Config.DISH.TYPE);
                            dish.image1 = gDish.getString(Config.DISH.IMAGE1);
                            dish.max_per_order = gDish.getString(Config.DISH.MAX_PER_ORDER);
                            dish.qty = Config.aux_initial_stock;
                            dish.today = todayDate;
                            dish.save();

                            Log.i(TAG, "dish: " + dish.toString());
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, e.toString());
                    }
                }
            }
        }
    }

    class goTo{
        private void ErrorClosed() {
            Log.i(TAG, "goto: ClosedActivity");
            if( goingTo == null || !goingTo.equals("closed")) {
                goingTo = "closed";
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        startActivity(new Intent(getApplicationContext(), ErrorClosedActivity.class));
                        overridePendingTransition(R.anim.bottom_slide_in, R.anim.none);
                        finish();
                    }
                }, 2000);
            }
        }

        void ErrorSolded() {
            Log.i(TAG, "goto: SoldedActivity");
            if( goingTo == null || !goingTo.equals("solded")) {
                goingTo = "solded";
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

        void ErrorVersion() {
            Log.i(TAG, "goto: ErrorActivity");
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    Intent intent = new Intent(getApplicationContext(), ErrorVersionActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.bottom_slide_in, R.anim.none);
                    finish();
                }
            }, 2000);
        }

        void HomeAbout() {
            Log.i(TAG, "goto: HomeActivity");
            Log.i(TAG,"HomeAbout()");
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    HomeAboutActivity();
                }
            }, 1000);
        }

        void HomeAboutActivity(){
            Log.i(TAG, "goto: HomeAboutActivity");
            Log.i(TAG,"HomeAboutActivity()");
            Intent intent = new Intent(getApplicationContext(), HomeAboutActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransitionGoRight();
        }
    }

}
