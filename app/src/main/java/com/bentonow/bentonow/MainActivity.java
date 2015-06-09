package com.bentonow.bentonow;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.bentonow.bentonow.model.Dish;
import com.bentonow.bentonow.model.Ioscopy;
import com.bentonow.bentonow.model.Orders;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
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
    private ProgressBar home_preloader;
    private TextView splash_message;
    private TextView tx_slogan;
    private Activity activity;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private String next_day_json;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        aq = new AQuery(this);
        activity = this;
        initElements();
        tx_slogan.setText(Ioscopy.getKeyValue("launch-slogan"));
    }

    private void showGPSDisabledAlertToUser(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Goto Settings Page To Enable GPS",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        startActivity(new Intent(getApplicationContext(),DeliveryLocationActivity.class));
                        overridePendingTransitionGoRight();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    private void initElements() {
        home_preloader = (ProgressBar)findViewById(R.id.home_preloader);
        splash_message = (TextView) findViewById(R.id.splash_message);
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
        LocationManager mLocationManager;
        final LocationListener mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {
                Log.i(TAG,"onLocationChanged() location: "+location.toString());
                if(Config.current_location==null) {
                    Config.current_location = location;
                    Config.INIT_LAT_LONG = new LatLng(Config.current_location.getLatitude(), Config.current_location.getLongitude());

                    ///////////////////////
                    List<LatLng> sfpolygon = new ArrayList<LatLng>();
                    String[] serviceArea_dinner = Config.serviceArea_dinner.split(" ");
                    for (int i = 0; i < serviceArea_dinner.length; i++) {
                        String[] loc = serviceArea_dinner[i].split(",");
                        double lat = Double.valueOf(loc[1]);
                        double lng = Double.valueOf(loc[0]);
                        sfpolygon.add(new LatLng(lat, lng));
                    }
                /*sfpolygon.add(new LatLng(37.8095806, -122.44983680000001));
                sfpolygon.add(new LatLng(37.77783170000001, -122.44335350000001));
                sfpolygon.add(new LatLng(37.7460824, -122.43567470000002));
                sfpolygon.add(new LatLng(37.7490008, -122.37636569999998));
                sfpolygon.add(new LatLng(37.78611430000001, -122.37928390000002));
                sfpolygon.add(new LatLng(37.8135812, -122.40348819999998));
                sfpolygon.add(new LatLng(37.8095806, -122.44983680000001));*/


                    //Log.i(TAG, "sfpolygon: " + sfpolygon.toString());
                    if (PolyUtil.containsLocation(Config.INIT_LAT_LONG, sfpolygon, false)) {
                        startActivity(new Intent(getApplicationContext(), BuildBentoActivity.class));
                        overridePendingTransitionGoRight();
                        finish();
                    } else {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                startActivity(new Intent(getApplicationContext(), DeliveryLocationActivity.class));
                                overridePendingTransitionGoRight();
                                finish();
                            }
                        }, 2000);
                    }
                }else{
                    //finalMLocationManager.removeUpdates(mLocationListener);
                }
                ///////////////////////
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            //Toast.makeText(this, "GPS is Enabled in your devide", Toast.LENGTH_SHORT).show();
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, mLocationListener);
            //mLocationManager.removeUpdates();
        }else{
            showGPSDisabledAlertToUser();
        }
    }

    private void init() {
        Log.i(TAG, "Config.android_min_version: " + Config.android_min_version);
        if ( Config.current_version >= Config.android_min_version ) {
            if (isFirstInit()) {
                goTo goTo = new goTo();
                goTo.HomeAbout();
            } else {
                if(Bentonow.isOpen) checkForPendingOrder();
            }
        }else {
            goTo goTo = new goTo();
            goTo.ErrorVersion();
        }
    }

    void tryGetAll( String date ){
        Log.i(TAG,"tryGetInit()");
        String uri = Config.API.URL+Config.API.INIT+"/"+date;
        Log.i(TAG, "uri: " + uri);
        aq.ajax(uri, JSONObject.class, 30*1000, new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject json, AjaxStatus status) {
                JsonProcess JsonProcess = new JsonProcess();
                // IOSCOPY
                try {
                    JSONArray IOSCOPY = json.getJSONArray(Config.API.IOSCOPY);
                    JsonProcess.ioscopy(IOSCOPY);
                } catch (JSONException ignored) {

                }

                // STATUS/OVERALL
                try {
                    JSONObject statusall = json.getJSONObject("/status/all");
                    JsonProcess.OverAll(statusall.getJSONObject("overall"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // /menu/{date}
                try {
                    JSONObject menu_date = json.getJSONObject("/menu/{date}");
                    JSONObject menu = menu_date.getJSONObject("menus");
                    JSONObject dinner = menu.getJSONObject("dinner");
                    //JSONObject dinner = menu.getJSONObject("lunch");
                    JSONArray MenuItems = (JSONArray) dinner.get(Config.API_MENUITEMS_TAG);
                    JsonProcess.MenuItems(MenuItems);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // /menu/next/{date}
                try {
                    JSONObject menu_date = json.getJSONObject("/menu/next/{date}");
//                    JSONObject menu = menu_date.getJSONObject("menus");
//                    JSONObject dinner = menu.getJSONObject("dinner");
                    next_day_json = menu_date.toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (Bentonow.isOpen) {
                    // /status/all | menu
                    try {
                        JSONObject status_all = json.getJSONObject("/status/all");
                        JSONArray menu = status_all.getJSONArray("menu");
                        BentoService.processMenuStock(menu);
                    } catch (JSONException e) {
                        //e.printStackTrace();
                    }
                }

                try {
                    Log.i(TAG,"Config.android_min_version: "+Config.android_min_version);
                    Config.android_min_version = json.getInt("android_min_version");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                /////////////////////////////////
                try {
                    JSONObject settings = json.getJSONObject("settings");
                    Config.serviceArea_dinner = settings.getString("serviceArea_dinner");
                    Log.i(TAG,"Config.serviceArea_dinner: "+Config.serviceArea_dinner);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                init();
            }
        });
    }

    class JsonProcess {
        public void OverAll( JSONObject json ){
            if( !isFirstInit() ) {
                Log.i(TAG, "OverAll(json)");
                try {
                    // IF BENTO NOW OPEN
                    String overall = json.getString(Config.API.STATUS_OVERALL_LABEL_VALUE);
                    if (overall.equals(Config.API.STATUS_OVERALL_MESSAGE_OPEN)) {
                        Log.i(TAG, "Bentonow.isOpen turn true");
                        Bentonow.isOpen = true;
                    }
                    if (overall.equals(Config.API.STATUS_OVERALL_MESSAGE_SOLDOUT)) {
                        Bentonow.isSolded = true;
                        goTo goTo = new goTo();
                        goTo.ErrorSolded();
                    } else if (overall.equals(Config.API.STATUS_OVERALL_MESSAGE_CLOSED)) {
                        Bentonow.isOpen = false;
                        goTo goTo = new goTo();
                        goTo.ErrorClosed();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

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
            //init();
        }

        public void MenuItems(JSONArray MenuItems){
            if (BuildConfig.DEBUG) {
                Picasso.with(getApplicationContext()).setIndicatorsEnabled(true);
                Picasso.with(getApplicationContext()).setLoggingEnabled(true);
            }
            for (int i = 0; i < MenuItems.length(); i++) {
                JSONObject gDish;
                try {
                    gDish = (JSONObject) MenuItems.get(i);

                    String image = gDish.getString(Config.DISH.IMAGE1);
                    if (!TextUtils.isEmpty(image)) {
                        Picasso.with(getApplicationContext())
                                .load(image)
                                //.resizeDimen(R.dimen.article_image_preview_width, R.dimen.article_image_preview_height)
                                //.centerCrop()
                                .fetch();
                    }

                    long menuHoy = Dish.count(Dish.class, "_id=?", new String[]{gDish.getString(Config.DISH.itemId)});
                    if (menuHoy == 0) {
                        Dish dish = new Dish(gDish.getString(Config.DISH.itemId), gDish.getString(Config.DISH.NAME), gDish.getString(Config.DISH.DESCRIPTION), gDish.getString(Config.DISH.TYPE), gDish.getString(Config.DISH.IMAGE1), gDish.getString(Config.DISH.MAX_PER_ORDER), todayDate, Config.aux_initial_stock);
                        dish.save();
                    } else {
                        long dish_id = Dish.getIdBy_id(gDish.getString(Config.DISH.itemId));
                        if (dish_id != 0) {
                            Dish dish = Dish.findById(Dish.class, dish_id);
                            if (dish != null) {
                                dish.name = gDish.getString(Config.DISH.NAME);
                                dish.description = gDish.getString(Config.DISH.DESCRIPTION);
                                dish.type = gDish.getString(Config.DISH.TYPE);
                                dish.image1 = gDish.getString(Config.DISH.IMAGE1);
                                dish.max_per_order = gDish.getString(Config.DISH.MAX_PER_ORDER);
                                dish.qty = Config.aux_initial_stock;
                                dish.today = todayDate;
                                dish.save();
                            }
                        }
                    }
                } catch (JSONException e) {
                    //e.printStackTrace();
                }
            }
            //tryToGetMenuSock();
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

    class goTo{
        private void ErrorClosed() {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    Intent intent = new Intent(getApplicationContext(), ErrorClosedActivity.class);
                    if(next_day_json!=null) intent.putExtra("json",next_day_json);
                    startActivity(intent);
                    overridePendingTransition(R.anim.bottom_slide_in, R.anim.none);
                    finish();
                }
            }, 2000);
        }

        void ErrorSolded() {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    Intent intent = new Intent(getApplicationContext(), ErrorOutOfStockActivity.class);
                    if(next_day_json!=null) intent.putExtra("json",next_day_json);
                    startActivity(intent);
                    overridePendingTransition(R.anim.bottom_slide_in, R.anim.none);
                    finish();
                }
            }, 2000);
        }

        void ErrorVersion() {
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
            Log.i(TAG,"HomeAbout()");
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    HomeAboutActivity();
                }
            }, 1000);
        }

        void HomeAboutActivity(){
            Log.i(TAG,"HomeAboutActivity()");
            Intent intent = new Intent(getApplicationContext(), HomeAboutActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransitionGoRight();
        }
    }

}
