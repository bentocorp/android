package com.bentonow.bentonow.controllers.order;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.BentoRestClient;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.GoogleAnalyticsUtil;
import com.bentonow.bentonow.Utils.maps.CustomMarker;
import com.bentonow.bentonow.Utils.maps.LatLngInterpolator;
import com.bentonow.bentonow.Utils.maps.LocationUtils;
import com.bentonow.bentonow.Utils.maps.MarkerAnimation;
import com.bentonow.bentonow.controllers.BaseFragmentActivity;
import com.bentonow.bentonow.controllers.fragment.MySupportMapFragment;
import com.bentonow.bentonow.dao.IosCopyDao;
import com.bentonow.bentonow.model.User;
import com.bentonow.bentonow.model.map.WaypointModel;
import com.bentonow.bentonow.model.order.history.OrderHistoryItemModel;
import com.bentonow.bentonow.parse.GoogleDirectionParser;
import com.bentonow.bentonow.service.OrderSocketService;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.TextHttpResponseHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import cz.msebera.android.httpclient.Header;

public class OrderStatusActivity extends BaseFragmentActivity implements View.OnClickListener, OnMapReadyCallback {

    private static final String TAG = "OrderStatusActivity";
    private Handler mHandler;
    private Runnable mLoadingTask;
    private TextView actionbarTitle;
    private TextView txtIndicatorPrep;
    private TextView txtDescriptionPrep;
    private TextView txtIndicatorDelivery;
    private TextView txtDescriptionDelivery;
    private TextView txtIndicatorPickUp;
    private TextView txtDescriptionPickUp;
    private TextView txtOrderStatusTitle;
    private TextView txtOrderStatusDescription;

    private ImageView menuItemLeft;
    private GoogleMap googleMap;
    private MySupportMapFragment mapFragment;
    private RelativeLayout wrapperStatusPrep;
    private RelativeLayout wrapperStatusDelivery;

    private OrderSocketService webSocketService = null;
    private ServiceConnection mConnection = new OrderStatusServiceConnection();

    private Location mOrderLocation;
    private LatLng mDriverLocation;
    private Marker mDriverMarker;
    private MarkerOptions mDriverMarkerOpts;
    private Marker mOrderMarker;
    private MarkerOptions mOrderMarkerOpts;
    private WaypointModel mWaypoint;
    private ArrayList<LatLng> aListDriver = new ArrayList<>();

    private HashMap markersHashMap;
    private Iterator<Entry> iter;
    private CameraUpdate cu;
    private CustomMarker customMarkerOne, customMarkerTwo;

    private OrderHistoryItemModel mOrder;
    private User mCurrentUser;
    private int iPositionStart = 0;
    private int iDuration = 1000;
    private double fRotation;

    public static double degToRad(double deg) {
        return deg * Math.PI / 180.0;
    }

    public static double radToDeg(double rad) {
        rad = rad * (180.0 / Math.PI);
        if (rad < 0) rad = 360.0 + rad;
        return rad;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        getActionbarTitle().setText("Order Status");

        mOrder = getIntent().getParcelableExtra(OrderHistoryItemModel.TAG);
        mCurrentUser = userDao.getCurrentUser();

        getMapFragment();

        getMenuItemLeft().setImageResource(R.drawable.vector_navigation_left_green);
        getMenuItemLeft().setOnClickListener(OrderStatusActivity.this);

        mOrder.setLat("37.8262328");
        mOrder.setLng("-122.3726696");

        mOrderLocation = new Location("Order Location");
        mOrderLocation.setLatitude(Double.parseDouble(mOrder.getLat()));
        mOrderLocation.setLongitude(Double.parseDouble(mOrder.getLng()));

        mDriverLocation = new LatLng(37.7648009, -122.4196905);

       /* lEmulateRoute.add(new LatLng(37.7699536, -122.4199623));
        lEmulateRoute.add(new LatLng(37.7699536, -122.4199623));
        lEmulateRoute.add(new LatLng(37.76976700000001, -122.4181974));
        lEmulateRoute.add(new LatLng(37.7692352, -122.417882));
        lEmulateRoute.add(new LatLng(37.7690925, -122.409333));
        lEmulateRoute.add(new LatLng(37.7709375, -122.4057751));
        lEmulateRoute.add(new LatLng(37.8083016, -122.36696));
        lEmulateRoute.add(new LatLng(37.8093742, -122.3699022));
        lEmulateRoute.add(new LatLng(37.81645049999999, -122.3716935));
        lEmulateRoute.add(new LatLng(37.8226519, -122.3761121));
        lEmulateRoute.add(new LatLng(37.8245082, -122.3718595));
        lEmulateRoute.add(new LatLng(37.8261384, -122.3729018));*/

        updateStatus("delivery");

        mHandler = new Handler();
        mLoadingTask = new Runnable() {
            public void run() {
                if (iPositionStart < aListDriver.size() + 1) {

                   /* double lon1 = degToRad(mWaypoint.getaSteps().get(iPositionStart).getStart_location_lat());
                    double lon2 = degToRad(mWaypoint.getaSteps().get(iPositionStart).getEnd_location_lat());
                    double lat1 = degToRad(mWaypoint.getaSteps().get(iPositionStart).getStart_location_lng());
                    double lat2 = degToRad(mWaypoint.getaSteps().get(iPositionStart).getEnd_location_lng());*/
                    double lon1 = degToRad(aListDriver.get(iPositionStart).latitude);
                    double lon2 = degToRad(aListDriver.get(iPositionStart + 1).latitude);
                    double lat1 = degToRad(aListDriver.get(iPositionStart).longitude);
                    double lat2 = degToRad(aListDriver.get(iPositionStart + 1).longitude);

                    //iDuration = mWaypoint.getaSteps().get(iPositionStart).getDuration();

                    double a = Math.sin(lon2 - lon1) * Math.cos(lat2);
                    double b = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1);
                    //   fRotation = radToDeg(Math.atan2(a, b)) - 90; // bearing

                    //   DebugUtils.logDebug(TAG, "bearing: " + fRotation);
                    //    DebugUtils.logDebug(TAG, "bearing float: " + (float) fRotation);

                    // mDriverLocation = new LatLng(mWaypoint.getaSteps().get(iPositionStart).getStart_location_lat(), mWaypoint.getaSteps().get(iPositionStart).getStart_location_lng());
                    mDriverLocation = new LatLng(aListDriver.get(iPositionStart).latitude, aListDriver.get(iPositionStart).longitude);


                    DebugUtils.logDebug(TAG, "Change Route");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateMarkers();
                        }
                    });

                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateStatus("pickup");
                        }
                    });
                    DebugUtils.logError(TAG, "Didnt Change Route");
                }
                iPositionStart++;
            }
        };
    }

    @Override
    protected void onResume() {
        GoogleAnalyticsUtil.sendScreenView("Order Status");
        super.onResume();
    }

    private void getDirectionsByLocation() {
        BentoRestClient.getDirections("37.7648009", "-122.4196905", mOrder.getLat(), mOrder.getLng(), new TextHttpResponseHandler() {
            @SuppressWarnings("deprecation")
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                DebugUtils.logError(TAG, "getUserInfo:  " + responseString);

            }

            @SuppressWarnings("deprecation")
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                DebugUtils.logDebug(TAG, "getDirectionsByLocation: Status Code" + statusCode);

                mWaypoint = GoogleDirectionParser.parseDirections(responseString);

                aListDriver = LocationUtils.decodePoly(mWaypoint.getPoints());

                if (mWaypoint != null) {
                    DebugUtils.logDebug(TAG, "getDirectionsByLocation: Num Steps: " + mWaypoint.getaSteps().size());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            updateMapLocation();
                        }
                    });
                }

            }
        });
    }

    private void updateMapLocation() {
        zoomAnimateLevelToFitMarkers(200);

        mHandler.postDelayed(mLoadingTask, iDuration);
    }

    private void updateMarkers() {
        //customMarkerOne.setRotation((float) fRotation);
        animateMarker(customMarkerOne, mDriverLocation, (float) fRotation);

        updateMapLocation();
    }

    private void updateStatus(String sOrderStatus) {
        switch (sOrderStatus) {
            case "assign":
            case "prep":
                getWrapperStatusPrep().setVisibility(View.VISIBLE);
                getWrapperStatusDelivery().setVisibility(View.GONE);

                getTxtOrderStatusTitle().setText(IosCopyDao.get("prep_status_title"));
                getTxtOrderStatusDescription().setText(IosCopyDao.get("prep_status_description"));

                getTxtIndicatorPrep().setBackground(getResources().getDrawable(R.drawable.background_circle_green));
                getTxtDescriptionPrep().setTextColor(getResources().getColor(R.color.primary));

                getTxtIndicatorDelivery().setBackground(getResources().getDrawable(R.drawable.background_circle_gray));
                getTxtDescriptionDelivery().setTextColor(getResources().getColor(R.color.gray));
                getTxtIndicatorPickUp().setBackground(getResources().getDrawable(R.drawable.background_circle_gray));
                getTxtDescriptionPickUp().setTextColor(getResources().getColor(R.color.gray));
                break;
            case "delivery":
                getWrapperStatusDelivery().setVisibility(View.VISIBLE);
                getWrapperStatusPrep().setVisibility(View.GONE);

                getTxtIndicatorDelivery().setBackground(getResources().getDrawable(R.drawable.background_circle_green));
                getTxtDescriptionDelivery().setTextColor(getResources().getColor(R.color.gray));

                getTxtIndicatorPrep().setBackground(getResources().getDrawable(R.drawable.background_circle_gray));
                getTxtDescriptionPrep().setTextColor(getResources().getColor(R.color.primary));
                getTxtIndicatorPickUp().setBackground(getResources().getDrawable(R.drawable.background_circle_gray));
                getTxtDescriptionPickUp().setTextColor(getResources().getColor(R.color.primary));
                break;
            case "pickup":
                getWrapperStatusPrep().setVisibility(View.VISIBLE);
                getWrapperStatusDelivery().setVisibility(View.GONE);

                getTxtOrderStatusTitle().setText(IosCopyDao.get("pickup_status_title"));
                getTxtOrderStatusDescription().setText(IosCopyDao.get("pickup_status_description"));

                getTxtIndicatorPickUp().setBackground(getResources().getDrawable(R.drawable.background_circle_green));
                getTxtDescriptionPickUp().setTextColor(getResources().getColor(R.color.gray));

                getTxtIndicatorDelivery().setBackground(getResources().getDrawable(R.drawable.background_circle_gray));
                getTxtDescriptionDelivery().setTextColor(getResources().getColor(R.color.gray));
                getTxtIndicatorPrep().setBackground(getResources().getDrawable(R.drawable.background_circle_gray));
                getTxtDescriptionPrep().setTextColor(getResources().getColor(R.color.gray));
                break;
            default:
                DebugUtils.logError(TAG, "Unhandled Status:: " + sOrderStatus);
                break;
        }
    }

    private void bindService() {
        Intent intent = new Intent(this, OrderSocketService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    //this is method to help us set up a Marker that stores the Markers we want to plot on the map
    public void setUpMarkersHashMap() {
        if (markersHashMap == null) {
            markersHashMap = new HashMap();
        }
    }

    //this is method to help us add a Marker to the map
    public void addMarker(CustomMarker customMarker) {
        MarkerOptions markerOption = new MarkerOptions().position(new LatLng(customMarker.getCustomMarkerLatitude(), customMarker.getCustomMarkerLongitude())).icon(BitmapDescriptorFactory.fromResource(R.drawable.location_marker_hi)).flat(true);

        Marker newMark = googleMap.addMarker(markerOption);
        addMarkerToHashMap(customMarker, newMark);
    }

    //this is method to help us add a Marker into the hashmap that stores the Markers
    public void addMarkerToHashMap(CustomMarker customMarker, Marker marker) {
        setUpMarkersHashMap();
        markersHashMap.put(customMarker, marker);
    }


    //this is method to help us find a Marker that is stored into the hashmap
    public Marker findMarker(CustomMarker customMarker) {
        iter = markersHashMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry mEntry = (Map.Entry) iter.next();
            CustomMarker key = (CustomMarker) mEntry.getKey();
            if (customMarker.getCustomMarkerId().equals(key.getCustomMarkerId())) {
                Marker value = (Marker) mEntry.getValue();
                return value;
            }
        }
        return null;
    }

    //this is method to animate the Marker. There are flavours for all Android versions
    public void animateMarker(CustomMarker customMarker, LatLng latlng, float fRotation) {
        if (findMarker(customMarker) != null) {

            LatLngInterpolator latlonInter = new LatLngInterpolator.Spherical();
            latlonInter.interpolate(20, new LatLng(customMarker.getCustomMarkerLatitude(), customMarker.getCustomMarkerLongitude()), latlng);

            customMarker.setCustomMarkerLatitude(latlng.latitude);
            customMarker.setCustomMarkerLongitude(latlng.longitude);

            MarkerAnimation.animateMarkerToICS(findMarker(customMarker), new LatLng(customMarker.getCustomMarkerLatitude(), customMarker.getCustomMarkerLongitude()), fRotation, iDuration, latlonInter);

        }
    }

    //this is method to help us fit the Markers into specific bounds for camera position
    public void zoomAnimateLevelToFitMarkers(int padding) {
        LatLngBounds.Builder b = new LatLngBounds.Builder();
        iter = markersHashMap.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry mEntry = iter.next();
            CustomMarker key = (CustomMarker) mEntry.getKey();
            LatLng ll = new LatLng(key.getCustomMarkerLatitude(), key.getCustomMarkerLongitude());
            b.include(ll);
        }
        LatLngBounds bounds = b.build();

        // Change the padding as per needed
        cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        cu = CameraUpdateFactory.newLatLngZoom(mDriverLocation, 17);

        googleMap.animateCamera(cu, iDuration, null);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.actionbar_left_btn:
                onBackPressed();
                break;
            default:
                DebugUtils.logError(TAG, v.getId() + "");
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.setMyLocationEnabled(false);
        map.setTrafficEnabled(false);
        map.setIndoorEnabled(true);
        map.setBuildingsEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(false);
        googleMap = map;

        customMarkerOne = new CustomMarker("Driver Marker", mDriverLocation.latitude, mDriverLocation.longitude);
        customMarkerTwo = new CustomMarker("Order Marker", mOrderLocation.getLatitude(), mOrderLocation.getLongitude());

        addMarker(customMarkerTwo);
        addMarker(customMarkerOne);

        getDirectionsByLocation();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DebugUtils.logDebug(TAG, "OnDestroy()");
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService();
    }

    private Marker getDriverMarker() {
        if (mDriverMarker == null)
            mDriverMarker = googleMap.addMarker(getDriverMarkerOpts());
        return mDriverMarker;
    }

    private Marker getOrderMarker() {
        if (mOrderMarker == null) {
            mOrderMarker = googleMap.addMarker(getOrderMarkerOpts());
        }
        return mOrderMarker;
    }

    private MarkerOptions getDriverMarkerOpts() {
        if (mDriverMarkerOpts == null) {
            mDriverMarkerOpts = (new MarkerOptions().position(mDriverLocation).title("Driver Location"));
            mDriverMarkerOpts.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_car));
            mDriverMarkerOpts.flat(true);
        }
        return mDriverMarkerOpts;
    }

    private MarkerOptions getOrderMarkerOpts() {
        if (mOrderMarkerOpts == null) {
            mOrderMarkerOpts = (new MarkerOptions().position(mDriverLocation).title("Order Location"));
            mOrderMarkerOpts.icon(BitmapDescriptorFactory.fromResource(R.drawable.center_map));
            mOrderMarkerOpts.position(new LatLng(mOrderLocation.getLatitude(), mOrderLocation.getLongitude()));
            mOrderMarkerOpts.flat(true);
        }
        return mOrderMarkerOpts;
    }

    private ImageView getMenuItemLeft() {
        if (menuItemLeft == null)
            menuItemLeft = (ImageView) findViewById(R.id.actionbar_left_btn);
        return menuItemLeft;
    }

    private TextView getActionbarTitle() {
        if (actionbarTitle == null)
            actionbarTitle = (TextView) findViewById(R.id.actionbar_title);
        return actionbarTitle;
    }

    private MySupportMapFragment getMapFragment() {
        if (mapFragment == null) {
            mapFragment = ((MySupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_driver_location));
            mapFragment.getMapAsync(OrderStatusActivity.this);
        }
        return mapFragment;
    }

    private RelativeLayout getWrapperStatusPrep() {
        if (wrapperStatusPrep == null)
            wrapperStatusPrep = (RelativeLayout) findViewById(R.id.wrapper_status_prep);
        return wrapperStatusPrep;
    }

    private RelativeLayout getWrapperStatusDelivery() {
        if (wrapperStatusDelivery == null)
            wrapperStatusDelivery = (RelativeLayout) findViewById(R.id.wrapper_status_delivery);
        return wrapperStatusDelivery;
    }

    private TextView getTxtIndicatorPrep() {
        if (txtIndicatorPrep == null)
            txtIndicatorPrep = (TextView) findViewById(R.id.txt_indicator_prep);
        return txtIndicatorPrep;
    }

    private TextView getTxtDescriptionPrep() {
        if (txtDescriptionPrep == null)
            txtDescriptionPrep = (TextView) findViewById(R.id.txt_description_prep);
        return txtDescriptionPrep;
    }

    private TextView getTxtIndicatorDelivery() {
        if (txtIndicatorDelivery == null)
            txtIndicatorDelivery = (TextView) findViewById(R.id.txt_indicator_delivery);
        return txtIndicatorDelivery;
    }

    private TextView getTxtDescriptionDelivery() {
        if (txtDescriptionDelivery == null)
            txtDescriptionDelivery = (TextView) findViewById(R.id.txt_description_delivery);
        return txtDescriptionDelivery;
    }

    private TextView getTxtIndicatorPickUp() {
        if (txtIndicatorPickUp == null)
            txtIndicatorPickUp = (TextView) findViewById(R.id.txt_indicator_pick_up);
        return txtIndicatorPickUp;
    }

    private TextView getTxtDescriptionPickUp() {
        if (txtDescriptionPickUp == null)
            txtDescriptionPickUp = (TextView) findViewById(R.id.txt_description_pick_up);
        return txtDescriptionPickUp;
    }

    private TextView getTxtOrderStatusTitle() {
        if (txtOrderStatusTitle == null)
            txtOrderStatusTitle = (TextView) findViewById(R.id.txt_order_status_title);
        return txtOrderStatusTitle;
    }

    private TextView getTxtOrderStatusDescription() {
        if (txtOrderStatusDescription == null)
            txtOrderStatusDescription = (TextView) findViewById(R.id.txt_order_status_description);
        return txtOrderStatusDescription;
    }


    private class OrderStatusServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            DebugUtils.logDebug(TAG, "Successfully bounded to " + name.getClassName());
            OrderSocketService.WebSocketServiceBinder webSocketServiceBinder = (OrderSocketService.WebSocketServiceBinder) binder;
            webSocketService = webSocketServiceBinder.getService();
            webSocketService.onNodeEventListener();

            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            DebugUtils.logDebug(TAG, "Disconnected from service " + name.toString());
            mBound = false;

        }

    }
}
