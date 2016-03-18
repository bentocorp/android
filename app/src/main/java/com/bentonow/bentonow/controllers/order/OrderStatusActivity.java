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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.BentoRestClient;
import com.bentonow.bentonow.Utils.ConstantUtils;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.GoogleAnalyticsUtil;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.Utils.maps.CustomMarker;
import com.bentonow.bentonow.Utils.maps.LatLngInterpolator;
import com.bentonow.bentonow.Utils.maps.LocationUtils;
import com.bentonow.bentonow.Utils.maps.MarkerAnimation;
import com.bentonow.bentonow.controllers.BaseFragmentActivity;
import com.bentonow.bentonow.controllers.fragment.MySupportMapFragment;
import com.bentonow.bentonow.controllers.geolocation.DeliveryLocationActivity;
import com.bentonow.bentonow.dao.IosCopyDao;
import com.bentonow.bentonow.dao.MenuDao;
import com.bentonow.bentonow.listener.OrderStatusListener;
import com.bentonow.bentonow.model.User;
import com.bentonow.bentonow.model.map.WaypointModel;
import com.bentonow.bentonow.model.order.history.OrderHistoryItemModel;
import com.bentonow.bentonow.model.order.history.OrderHistoryItemSectionModel;
import com.bentonow.bentonow.model.order.history.OrderHistoryModel;
import com.bentonow.bentonow.parse.GoogleDirectionParser;
import com.bentonow.bentonow.parse.OrderHistoryJsonParser;
import com.bentonow.bentonow.service.OrderSocketService;
import com.bentonow.bentonow.ui.BackendButton;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.RequestParams;
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
    private TextView txtDescriptionAssembly;
    private TextView txtOrderStatusTitle;
    private TextView txtOrderStatusDescription;
    private TextView txtIndicatorAssembly;
    private BackendButton btnAddOtherBento;
    private FrameLayout frameLayoutPrepOne;
    private FrameLayout frameLayoutPrepTwo;
    private FrameLayout frameLayoutPrepThree;
    private FrameLayout frameLayoutDelOne;
    private FrameLayout frameLayoutDelTwo;
    private FrameLayout frameLayoutDelThree;
    private FrameLayout frameLayoutAssOne;
    private FrameLayout frameLayoutAssTwo;
    private FrameLayout frameLayoutAssThree;

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
    private int iDurationDirections = 0;
    private int iPadding = 200;
    private double fRotation;
    private boolean bUseGoogleDirections;
    private boolean bGetGoogleDirections = true;
    private String sEta = "";

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
        getBtnAddOtherBento().setOnClickListener(OrderStatusActivity.this);

        mOrderLocation = new Location("Order Location");
        mOrderLocation.setLatitude(Double.parseDouble(mOrder.getLat()));
        mOrderLocation.setLongitude(Double.parseDouble(mOrder.getLng()));

        updateStatus(false);

        mHandler = new Handler();
        mLoadingTask = new Runnable() {
            public void run() {
                if (aListDriver.size() > iPositionStart && bUseGoogleDirections) {
                    //iDuration = mWaypoint.getaSteps().get(iPositionStart).getDuration();

                    fRotation = LocationUtils.getRotationFromLocations(aListDriver.get(iPositionStart), mDriverLastLocation);
                    // mDriverLocation = new LatLng(mWaypoint.getaSteps().get(iPositionStart).getStart_location_lat(), mWaypoint.getaSteps().get(iPositionStart).getStart_location_lng());

                    mDriverLastLocation = new LatLng(mDriverLocation.latitude, mDriverLocation.longitude);
                    mDriverLocation = new LatLng(aListDriver.get(iPositionStart).latitude, aListDriver.get(iPositionStart).longitude);

                    DebugUtils.logDebug(TAG, "Change Route");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateMarkers(2000);
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
                bGetGoogleDirections = true;
            }

            @SuppressWarnings("deprecation")
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                DebugUtils.logDebug(TAG, "getDirectionsByLocation: Status Code" + statusCode);

                mWaypoint = GoogleDirectionParser.parseDirections(responseString);

                if (mWaypoint != null) {
                    iPositionStart = 0;
                    aListDriver = LocationUtils.decodePoly(mWaypoint.getPoints());
                    try {
                        iDurationDirections = (mWaypoint.getDuration() / aListDriver.size()) * 1000;
                    } catch (Exception ex) {
                        iDurationDirections = 2000;
                    }

                    DebugUtils.logDebug(TAG, "Duration in Millis:: " + iDurationDirections);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sEta = String.format(getString(R.string.order_status_eta), LocationUtils.getStringSecondsLeft(mWaypoint.getDuration()));
                            updateMapLocation();
                        }
                    });
                }

            }
        });
    }

    @Override
    public void getOrderHistory() {
        RequestParams params = new RequestParams();
        params.put("api_token", mCurrentUser.api_token);

        BentoRestClient.get("/user/orderhistory", params, new TextHttpResponseHandler() {
            @SuppressWarnings("deprecation")
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                DebugUtils.logError(TAG, "getOrderHistoryByUser failed: " + responseString + " StatusCode: " + statusCode);
            }

            @SuppressWarnings("deprecation")
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                DebugUtils.logDebug(TAG, "getOrderHistoryByUser: " + responseString);
                OrderHistoryModel mOrderHistory = OrderHistoryJsonParser.parseOrderHistory(responseString);

                boolean bIsStillInProgress = false;

                for (OrderHistoryItemSectionModel mOrderSection : mOrderHistory.getListHistorySection()) {
                    if (mOrderSection.getSectionTitle().contains("In Progress")) {
                        for (OrderHistoryItemModel mOrderItem : mOrderSection.getListItems()) {
                            if (mOrderItem.getOrderId().equals(mOrderItem.getOrderId())) {
                                bIsStillInProgress = true;
                                if (!mOrder.getOrder_status().equals(mOrderItem.getOrder_status())) {
                                    SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ORDER_HISTORY_FORCE_REFRESH, true);
                                    mOrder = mOrderItem;
                                    updateStatus(true);
                                    DebugUtils.logDebug(TAG, "New Order Status:: " + mOrder.getOrder_status());
                                }
                                break;
                            }
                        }

                    }
                }

                if (!bIsStillInProgress) {
                    SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ORDER_HISTORY_FORCE_REFRESH, true);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            OrderStatusActivity.this.finish();
                        }
                    });
                }
            }
        });
    }

    private void updateMapLocation() {
        if (mOrder.getOrder_status().equals("En Route")) {
            zoomAnimateLevelToFitMarkers();
        }
    }

    private void updateMarkers(int iAnimate) {
        animateMarker(getDriverMarker(), mDriverLocation, (float) fRotation, iAnimate);
        updateMapLocation();
        //cameraUpdate = CameraUpdateFactory.newLatLngZoom(mDriverLocation, 17);
    }

    private void updateStatus(final boolean isRequestData) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (mOrder.getOrder_status()) {
                    case "Assigned":
                        getWrapperStatusPrep().setVisibility(View.VISIBLE);
                        getWrapperStatusDelivery().setVisibility(View.GONE);

                        getTxtOrderStatusTitle().setText(IosCopyDao.get("prep_status_title"));
                        getTxtOrderStatusDescription().setText(IosCopyDao.get("prep_status_description"));

                        getTxtIndicatorPrep().setBackground(getResources().getDrawable(R.drawable.background_circle_green));
                        getTxtDescriptionPrep().setTextColor(getResources().getColor(R.color.primary));

                        getFrameLayoutPrepOne().setBackground(getResources().getDrawable(R.drawable.background_circle_gray));
                        getFrameLayoutPrepTwo().setBackground(getResources().getDrawable(R.drawable.background_circle_gray));
                        getFrameLayoutPrepThree().setBackground(getResources().getDrawable(R.drawable.background_circle_gray));
                        getFrameLayoutDelOne().setBackground(getResources().getDrawable(R.drawable.background_circle_gray));
                        getFrameLayoutDelTwo().setBackground(getResources().getDrawable(R.drawable.background_circle_gray));
                        getFrameLayoutDelThree().setBackground(getResources().getDrawable(R.drawable.background_circle_gray));
                        getFrameLayoutAssOne().setBackground(getResources().getDrawable(R.drawable.background_circle_gray));
                        getFrameLayoutAssTwo().setBackground(getResources().getDrawable(R.drawable.background_circle_gray));
                        getFrameLayoutAssThree().setBackground(getResources().getDrawable(R.drawable.background_circle_gray));

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
                        getTxtIndicatorPrep().setBackground(getResources().getDrawable(R.drawable.background_circle_green));
                        getTxtDescriptionPrep().setTextColor(getResources().getColor(R.color.primary));

                        getFrameLayoutPrepOne().setBackground(getResources().getDrawable(R.drawable.background_circle_green));
                        getFrameLayoutPrepTwo().setBackground(getResources().getDrawable(R.drawable.background_circle_green));
                        getFrameLayoutPrepThree().setBackground(getResources().getDrawable(R.drawable.background_circle_green));

                        getFrameLayoutDelOne().setBackground(getResources().getDrawable(R.drawable.background_circle_gray));
                        getFrameLayoutDelTwo().setBackground(getResources().getDrawable(R.drawable.background_circle_gray));
                        getFrameLayoutDelThree().setBackground(getResources().getDrawable(R.drawable.background_circle_gray));
                        getFrameLayoutAssOne().setBackground(getResources().getDrawable(R.drawable.background_circle_gray));
                        getFrameLayoutAssTwo().setBackground(getResources().getDrawable(R.drawable.background_circle_gray));
                        getFrameLayoutAssThree().setBackground(getResources().getDrawable(R.drawable.background_circle_gray));

                        getTxtIndicatorPickUp().setBackground(getResources().getDrawable(R.drawable.background_circle_gray));
                        getTxtDescriptionPickUp().setTextColor(getResources().getColor(R.color.gray));

                        if (isRequestData)
                            webSocketService.trackDriver(mOrder.getDriverId());

                        break;
                    case "Arrived":
                        getWrapperStatusPrep().setVisibility(View.VISIBLE);
                        getWrapperStatusDelivery().setVisibility(View.GONE);

                        getTxtOrderStatusTitle().setText(IosCopyDao.get("pickup_status_title"));
                        getTxtOrderStatusDescription().setText(IosCopyDao.get("pickup_status_description"));

                        getTxtIndicatorPickUp().setBackground(getResources().getDrawable(R.drawable.background_circle_green));
                        getTxtDescriptionPickUp().setTextColor(getResources().getColor(R.color.primary));
                        getTxtIndicatorAssembly().setBackground(getResources().getDrawable(R.drawable.background_circle_green));
                        getTxtDescriptionAssembly().setTextColor(getResources().getColor(R.color.primary));
                        getTxtIndicatorDelivery().setBackground(getResources().getDrawable(R.drawable.background_circle_green));
                        getTxtDescriptionDelivery().setTextColor(getResources().getColor(R.color.primary));
                        getTxtIndicatorPrep().setBackground(getResources().getDrawable(R.drawable.background_circle_green));
                        getTxtDescriptionPrep().setTextColor(getResources().getColor(R.color.primary));


                        getFrameLayoutPrepOne().setBackground(getResources().getDrawable(R.drawable.background_circle_green));
                        getFrameLayoutPrepTwo().setBackground(getResources().getDrawable(R.drawable.background_circle_green));
                        getFrameLayoutPrepThree().setBackground(getResources().getDrawable(R.drawable.background_circle_green));
                        getFrameLayoutDelOne().setBackground(getResources().getDrawable(R.drawable.background_circle_green));
                        getFrameLayoutDelTwo().setBackground(getResources().getDrawable(R.drawable.background_circle_green));
                        getFrameLayoutDelThree().setBackground(getResources().getDrawable(R.drawable.background_circle_green));
                        getFrameLayoutAssOne().setBackground(getResources().getDrawable(R.drawable.background_circle_green));
                        getFrameLayoutAssTwo().setBackground(getResources().getDrawable(R.drawable.background_circle_green));
                        getFrameLayoutAssThree().setBackground(getResources().getDrawable(R.drawable.background_circle_green));

                        break;
                    default:
                        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ORDER_HISTORY_FORCE_REFRESH, true);
                        finish();
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

        if (bIsDriver)
            newMark.setTitle(mOrder.getDriverName());

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

    public void animateMarker(CustomMarker customMarker, LatLng latlng, float fRotation, int iAnimate) {
        sEta = String.format(getString(R.string.order_status_eta), LocationUtils.getStringSecondsLeft((iDurationDirections) / 1000 * (aListDriver.size() - iPositionStart)));
        // findMarker(customMarker).setSnippet(LocationUtils.getStringSecondsLeft((iDurationDirections) / 1000 * (aListDriver.size() - iPositionStart)));
        // findMarker(customMarker).showInfoWindow();
        if (findMarker(customMarker) != null) {
            LatLngInterpolator latlonInter = new LatLngInterpolator.Spherical();
            latlonInter.interpolate(20, new LatLng(customMarker.getCustomMarkerLatitude(), customMarker.getCustomMarkerLongitude()), latlng);

            customMarker.setCustomMarkerLatitude(latlng.latitude);
            customMarker.setCustomMarkerLongitude(latlng.longitude);

            MarkerAnimation.animateMarkerToICS(findMarker(customMarker), new LatLng(customMarker.getCustomMarkerLatitude(), customMarker.getCustomMarkerLongitude()), fRotation, latlonInter, sEta, iAnimate);

        }
    }

    public void zoomAnimateLevelToFitMarkers() {
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

        cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, iPadding);
        //cameraUpdate = CameraUpdateFactory.newLatLngZoom(mDriverLocation, 17);
        try {
            googleMap.animateCamera(cameraUpdate, iPositionStart == 0 ? 1000 : iDurationDirections, new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    mHandler.removeCallbacks(mLoadingTask);

                    if (bUseGoogleDirections)
                        mHandler.post(mLoadingTask);
                }

                @Override
                public void onCancel() {

                }
            });
        } catch (Exception ex) {

        }


    }

    @Override
    public void onPush(String sResponse) {
        getOrderHistory();
    }

    @Override
    public void trackDriverByGoogleMaps() {
        bUseGoogleDirections = true;

        if (mDriverLocation != null && mOrder.getOrder_status().equals("En Route")) {
            getDirectionsByLocation();
        }
    }

    @Override
    public void trackDriverByGloc(double lat, double lng) {
        bUseGoogleDirections = true;

        if (mOrder.getOrder_status().equals("En Route"))
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
        if (mOrder.getOrder_status().equals("En Route")) {
            mHandler.removeCallbacks(mLoadingTask);
            fRotation = LocationUtils.getRotationFromLocations(mDriverLocation, new LatLng(lat, lng));
            mDriverLocation = new LatLng(lat, lng);
            iPositionStart = 0;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateMarkers(5000);
                }
            });

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
                        sEta = String.format(getString(R.string.order_status_eta), LocationUtils.getStringSecondsLeft(mWaypoint.getDuration()));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                findMarker(getDriverMarker()).setSnippet(sEta);
                            }
                        });
                    }

                }
            });
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.actionbar_left_btn:
                onBackPressed();
                break;
            case R.id.btn_add_other_bento:
                if (MenuDao.gateKeeper.getAppState().contains("build")) {
                    Intent iBuildBento = new Intent(OrderStatusActivity.this, BuildBentoActivity.class);
                    iBuildBento.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(iBuildBento);
                    finish();
                } else {
                    Intent intent = new Intent(this, DeliveryLocationActivity.class);
                    intent.putExtra(ConstantUtils.TAG_OPEN_SCREEN, ConstantUtils.optOpenScreen.BUILD_BENTO);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    finish();
                    startActivity(intent);
                }
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

        cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(mOrderLocation.getLatitude(), mOrderLocation.getLongitude()), 17);
        googleMap.animateCamera(cameraUpdate);

        if (mDriverLocation != null)
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addMarker(getDriverMarker(), true);
                    updateMarkers(2000);
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

    private TextView getTxtIndicatorAssembly() {
        if (txtIndicatorAssembly == null)
            txtIndicatorAssembly = (TextView) findViewById(R.id.txt_indicator_assembly);
        return txtIndicatorAssembly;
    }

    private TextView getTxtDescriptionAssembly() {
        if (txtDescriptionAssembly == null)
            txtDescriptionAssembly = (TextView) findViewById(R.id.txt_description_assembly);
        return txtDescriptionAssembly;
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

    private BackendButton getBtnAddOtherBento() {
        if (btnAddOtherBento == null)
            btnAddOtherBento = (BackendButton) findViewById(R.id.btn_add_other_bento);
        return btnAddOtherBento;
    }

    private FrameLayout getFrameLayoutPrepOne() {
        if (frameLayoutPrepOne == null)
            frameLayoutPrepOne = (FrameLayout) findViewById(R.id.frame_prep_one);
        return frameLayoutPrepOne;
    }

    private FrameLayout getFrameLayoutPrepTwo() {
        if (frameLayoutPrepTwo == null)
            frameLayoutPrepTwo = (FrameLayout) findViewById(R.id.frame_prep_two);
        return frameLayoutPrepTwo;
    }

    private FrameLayout getFrameLayoutPrepThree() {
        if (frameLayoutPrepThree == null)
            frameLayoutPrepThree = (FrameLayout) findViewById(R.id.frame_prep_three);
        return frameLayoutPrepThree;
    }

    private FrameLayout getFrameLayoutDelOne() {
        if (frameLayoutDelOne == null)
            frameLayoutDelOne = (FrameLayout) findViewById(R.id.frame_del_one);
        return frameLayoutDelOne;
    }

    private FrameLayout getFrameLayoutDelTwo() {
        if (frameLayoutDelTwo == null)
            frameLayoutDelTwo = (FrameLayout) findViewById(R.id.frame_del_two);
        return frameLayoutDelTwo;
    }

    private FrameLayout getFrameLayoutDelThree() {
        if (frameLayoutDelThree == null)
            frameLayoutDelThree = (FrameLayout) findViewById(R.id.frame_del_three);
        return frameLayoutDelThree;
    }

    private FrameLayout getFrameLayoutAssOne() {
        if (frameLayoutAssOne == null)
            frameLayoutAssOne = (FrameLayout) findViewById(R.id.frame_ass_one);
        return frameLayoutAssOne;
    }

    private FrameLayout getFrameLayoutAssTwo() {
        if (frameLayoutAssTwo == null)
            frameLayoutAssTwo = (FrameLayout) findViewById(R.id.frame_ass_two);
        return frameLayoutAssTwo;
    }

    private FrameLayout getFrameLayoutAssThree() {
        if (frameLayoutAssThree == null)
            frameLayoutAssThree = (FrameLayout) findViewById(R.id.frame_ass_three);
        return frameLayoutAssThree;
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
