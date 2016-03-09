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
import com.bentonow.bentonow.listener.OrderStatusListener;
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

public class OrderStatusActivity extends BaseFragmentActivity implements View.OnClickListener, OnMapReadyCallback, OrderStatusListener {

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
    private LatLng mDriverLastLocation;
    private WaypointModel mWaypoint;
    private ArrayList<LatLng> aListDriver = new ArrayList<>();

    private HashMap markersHashMap;
    private Iterator<Entry> markerIterator;
    private CameraUpdate cameraUpdate;
    private CustomMarker customMarkerDriver;
    private CustomMarker customMarkerOrder;

    private OrderHistoryItemModel mOrder;
    private User mCurrentUser;
    private int iPositionStart = 0;
    private int iDuration = 2000;
    private double fRotation;
    private boolean bUseGoogleDirections;

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

        mOrderLocation = new Location("Order Location");
        mOrderLocation.setLatitude(Double.parseDouble(mOrder.getLat()));
        mOrderLocation.setLongitude(Double.parseDouble(mOrder.getLng()));

        updateStatus(mOrder.getOrder_status());

        mHandler = new Handler();
        mLoadingTask = new Runnable() {
            public void run() {
                if (aListDriver.size() > iPositionStart && bUseGoogleDirections) {

                    double lon1 = LocationUtils.degToRad(mDriverLastLocation.latitude);
                    double lon2 = LocationUtils.degToRad(aListDriver.get(iPositionStart).latitude);
                    double lat1 = LocationUtils.degToRad(mDriverLastLocation.longitude);
                    double lat2 = LocationUtils.degToRad(aListDriver.get(iPositionStart).longitude);

                    //iDuration = mWaypoint.getaSteps().get(iPositionStart).getDuration();

                    double a = Math.sin(lon2 - lon1) * Math.cos(lat2);
                    double b = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1);
                    fRotation = LocationUtils.radToDeg(Math.atan2(a, b)); // bearing

                    //   DebugUtils.logDebug(TAG, "bearing: " + fRotation);
                    //    DebugUtils.logDebug(TAG, "bearing float: " + (float) fRotation);

                    // mDriverLocation = new LatLng(mWaypoint.getaSteps().get(iPositionStart).getStart_location_lat(), mWaypoint.getaSteps().get(iPositionStart).getStart_location_lng());
                    mDriverLastLocation = new LatLng(mDriverLocation.latitude, mDriverLocation.longitude);
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
                            //updateStatus("pickup");
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
        BentoRestClient.getDirections(mDriverLocation.latitude, mDriverLocation.longitude, mOrder.getLat(), mOrder.getLng(), new TextHttpResponseHandler() {
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

                if (mWaypoint != null) {
                    iPositionStart = 0;
                    aListDriver = LocationUtils.decodePoly(mWaypoint.getPoints());
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
        zoomAnimateLevelToFitMarkers(100);
    }

    private void updateMarkers() {
        animateMarker(getDriverMarker(), mDriverLocation, (float) fRotation);
        updateMapLocation();
        //cameraUpdate = CameraUpdateFactory.newLatLngZoom(mDriverLocation, 17);
    }

    private void updateStatus(final String sOrderStatus) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (sOrderStatus) {
                    case "Assigned":
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
                    case "En Route":
                        getWrapperStatusDelivery().setVisibility(View.VISIBLE);
                        getWrapperStatusPrep().setVisibility(View.GONE);

                        getTxtIndicatorDelivery().setBackground(getResources().getDrawable(R.drawable.background_circle_green));
                        getTxtDescriptionDelivery().setTextColor(getResources().getColor(R.color.primary));

                        getTxtIndicatorPrep().setBackground(getResources().getDrawable(R.drawable.background_circle_gray));
                        getTxtDescriptionPrep().setTextColor(getResources().getColor(R.color.gray));
                        getTxtIndicatorPickUp().setBackground(getResources().getDrawable(R.drawable.background_circle_gray));
                        getTxtDescriptionPickUp().setTextColor(getResources().getColor(R.color.gray));
                        break;
                    case "pickup":
                        getWrapperStatusPrep().setVisibility(View.VISIBLE);
                        getWrapperStatusDelivery().setVisibility(View.GONE);

                        getTxtOrderStatusTitle().setText(IosCopyDao.get("pickup_status_title"));
                        getTxtOrderStatusDescription().setText(IosCopyDao.get("pickup_status_description"));

                        getTxtIndicatorPickUp().setBackground(getResources().getDrawable(R.drawable.background_circle_green));
                        getTxtDescriptionPickUp().setTextColor(getResources().getColor(R.color.primary));

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
        });

    }


    private void bindService() {
        Intent intent = new Intent(this, OrderSocketService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    public void setUpMarkersHashMap() {
        if (markersHashMap == null) {
            markersHashMap = new HashMap();
        }
    }

    public void addMarker(CustomMarker customMarker, boolean bIsDriver) {
        MarkerOptions markerOption = new MarkerOptions().position(new LatLng(customMarker.getCustomMarkerLatitude(), customMarker.getCustomMarkerLongitude())).flat(true);
        markerOption.icon(bIsDriver ? BitmapDescriptorFactory.fromResource(R.drawable.marker_car) : BitmapDescriptorFactory.fromResource(R.drawable.location_marker_hi));
        markerOption.anchor(0.5f, 0.5f);
        Marker newMark = googleMap.addMarker(markerOption);
        addMarkerToHashMap(customMarker, newMark);
    }

    public void addMarkerToHashMap(CustomMarker customMarker, Marker marker) {
        setUpMarkersHashMap();
        markersHashMap.put(customMarker, marker);
    }


    public Marker findMarker(CustomMarker customMarker) {
        markerIterator = markersHashMap.entrySet().iterator();
        while (markerIterator.hasNext()) {
            Map.Entry mEntry = markerIterator.next();
            CustomMarker key = (CustomMarker) mEntry.getKey();
            if (customMarker.getCustomMarkerId().equals(key.getCustomMarkerId())) {
                Marker value = (Marker) mEntry.getValue();
                return value;
            }
        }
        return null;
    }

    public void animateMarker(CustomMarker customMarker, LatLng latlng, float fRotation) {
        if (findMarker(customMarker) != null) {

            LatLngInterpolator latlonInter = new LatLngInterpolator.Spherical();
            latlonInter.interpolate(20, new LatLng(customMarker.getCustomMarkerLatitude(), customMarker.getCustomMarkerLongitude()), latlng);

            customMarker.setCustomMarkerLatitude(latlng.latitude);
            customMarker.setCustomMarkerLongitude(latlng.longitude);

            MarkerAnimation.animateMarkerToICS(findMarker(customMarker), new LatLng(customMarker.getCustomMarkerLatitude(), customMarker.getCustomMarkerLongitude()), fRotation, bUseGoogleDirections ? iDuration : 0, latlonInter);

        }
    }

    public void zoomAnimateLevelToFitMarkers(int padding) {
        //   DebugUtils.logDebug(TAG, "Change Route");
        LatLngBounds.Builder b = new LatLngBounds.Builder();
        markerIterator = markersHashMap.entrySet().iterator();

        while (markerIterator.hasNext()) {
            Map.Entry mEntry = markerIterator.next();
            CustomMarker key = (CustomMarker) mEntry.getKey();
            LatLng ll = new LatLng(key.getCustomMarkerLatitude(), key.getCustomMarkerLongitude());
            b.include(ll);
        }
        LatLngBounds bounds = b.build();

        cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        //cameraUpdate = CameraUpdateFactory.newLatLngZoom(mDriverLocation, 17);

        googleMap.animateCamera(cameraUpdate, iDuration, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                mHandler.removeCallbacks(mLoadingTask);

                if (bUseGoogleDirections)
                    mHandler.postDelayed(mLoadingTask, iDuration);
            }

            @Override
            public void onCancel() {

            }
        });
    }

    @Override
    public void trackDriverByGoogleMaps() {
        bUseGoogleDirections = true;
        if (mDriverLocation != null) {
            getDirectionsByLocation();
        }
    }

    @Override
    public void trackDriverByGloc(double lat, double lng) {
        bUseGoogleDirections = true;

        if (mDriverLocation == null) {
            mDriverLocation = new LatLng(lat, lng);
            mDriverLastLocation = new LatLng(lat, lng);
            if (googleMap != null)
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        addMarker(getDriverMarker(), true);
                        getDirectionsByLocation();
                        //updateMarkers();
                    }
                });
        } else {
            mDriverLocation = new LatLng(lat, lng);
            getDirectionsByLocation();
        }
    }

    @Override
    public void onDriverLocation(double lat, double lng) {
        bUseGoogleDirections = false;
        mHandler.removeCallbacks(mLoadingTask);
        mDriverLocation = new LatLng(lat, lng);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateMarkers();
            }
        });
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

        addMarker(getOrderMarker(), false);

        if (mDriverLocation != null)
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addMarker(getDriverMarker(), true);
                    updateMarkers();
                }
            });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        mHandler.removeCallbacks(mLoadingTask);
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService();
    }

    @Override
    public void onConnectionError(String sError) {

    }

    @Override
    public void onAuthenticationFailure(String sError) {

    }

    @Override
    public void onAuthenticationSuccess() {
        webSocketService.trackDriver(mOrder.getDriverId());
       /* addMarker(getDriverMarker(), true);

        getDirectionsByLocation();*/
    }

    @Override
    public void onDisconnect(boolean onPurpose) {

    }

    @Override
    public void onReconnecting() {

    }

    private CustomMarker getDriverMarker() {
        if (customMarkerDriver == null)
            customMarkerDriver = new CustomMarker("Driver Marker", mDriverLocation.latitude, mDriverLocation.longitude);
        return customMarkerDriver;
    }

    private CustomMarker getOrderMarker() {
        if (customMarkerOrder == null)
            customMarkerOrder = new CustomMarker("Order Marker", mOrderLocation.getLatitude(), mOrderLocation.getLongitude());
        return customMarkerOrder;
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
            webSocketService.setWebSocketLister(OrderStatusActivity.this);
            webSocketService.setTrackingOrder(mOrder);
            webSocketService.connectWebSocket(mCurrentUser.email, mCurrentUser.api_token);

            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            DebugUtils.logDebug(TAG, "Disconnected from service " + name.toString());
            mBound = false;

        }

    }
}
