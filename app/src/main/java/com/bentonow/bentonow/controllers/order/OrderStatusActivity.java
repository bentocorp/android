package com.bentonow.bentonow.controllers.order;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.GoogleAnalyticsUtil;
import com.bentonow.bentonow.Utils.LocationUtils;
import com.bentonow.bentonow.Utils.WidgetsUtils;
import com.bentonow.bentonow.controllers.BaseFragmentActivity;
import com.bentonow.bentonow.controllers.fragment.MySupportMapFragment;
import com.bentonow.bentonow.dao.IosCopyDao;
import com.bentonow.bentonow.model.order.history.OrderHistoryItemModel;
import com.bentonow.bentonow.service.OrderSocketService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class OrderStatusActivity extends BaseFragmentActivity implements View.OnClickListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

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

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mOrderLocation;
    private LatLng mDriverLocation;
    private Marker mDriverMarker;
    private MarkerOptions mDriverMarkerOpts;
    private Marker mOrderMarker;
    private MarkerOptions mOrderMarkerOpts;
    private ArrayList<MarkerOptions> aMarker = new ArrayList<>();
    private ArrayList<LatLng> lEmulateRoute = new ArrayList<>();

    private OrderHistoryItemModel mOrder;
    private int iPositionStart = 0;
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

        getMapFragment();

        getMenuItemLeft().setImageResource(R.drawable.vector_navigation_left_green);
        getMenuItemLeft().setOnClickListener(OrderStatusActivity.this);

        mOrder.setLat("37.8262328");
        mOrder.setLng("-122.3726696");

        mOrderLocation = new Location("Order Location");
        mOrderLocation.setLatitude(Double.parseDouble(mOrder.getLat()));
        mOrderLocation.setLongitude(Double.parseDouble(mOrder.getLng()));


        mDriverLocation = new LatLng(37.7648009, -122.4196905);
        lEmulateRoute.add(new LatLng(37.7699536, -122.4199623));
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
        lEmulateRoute.add(new LatLng(37.8261384, -122.3729018));

        updateStatus("delivery");

        mHandler = new Handler();
        mLoadingTask = new Runnable() {
            public void run() {
                if (iPositionStart + 1 < lEmulateRoute.size()) {

                    double lon1 = degToRad(lEmulateRoute.get(iPositionStart).longitude);
                    double lon2 = degToRad(lEmulateRoute.get(iPositionStart + 1).longitude);
                    double lat1 = degToRad(lEmulateRoute.get(iPositionStart).latitude);
                    double lat2 = degToRad(lEmulateRoute.get(iPositionStart + 1).latitude);

                    double a = Math.sin(lon2 - lon1) * Math.cos(lat2);
                    double b = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1);
                    fRotation = radToDeg(Math.atan2(a, b)) - 90; // bearing

                    DebugUtils.logDebug(TAG, "bearing: " + fRotation);
                    DebugUtils.logDebug(TAG, "bearing float: " + (float) fRotation);

                    mDriverLocation = lEmulateRoute.get(iPositionStart + 1);

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

    private void updateMapLocation() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (MarkerOptions marker : aMarker) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();
        int padding = 200; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        if (iPositionStart >= 1)
            googleMap.animateCamera(cu);

        mHandler.postDelayed(mLoadingTask, 1000 * 3);
    }

    private void updateMarkers() {
        aMarker.clear();
        // googleMap.clear();


        final long duration = 1000;
        final Handler handler = new Handler();

        final long start = SystemClock.uptimeMillis();
        Projection proj = googleMap.getProjection();

        Point startPoint = proj.toScreenLocation(getDriverMarker().getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);

        getDriverMarker().setRotation((float) fRotation);
        getDriverMarkerOpts().position(mDriverLocation);

        aMarker.add(getOrderMarkerOpts());
        aMarker.add(getDriverMarkerOpts());
        // googleMap.addMarker(getDriverMarkerOpts());

        final Interpolator interpolator = new LinearInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);
                double lng = t * mDriverLocation.longitude + (1 - t) * startLatLng.longitude;
                double lat = t * mDriverLocation.latitude + (1 - t) * startLatLng.latitude;
                getDriverMarker().setPosition(new LatLng(lat, lng));
                getDriverMarkerOpts().position(new LatLng(lat, lng));
                if (t < 1.0) {
                    // Post again 10ms later.
                    handler.postDelayed(this, 10);
                } else {
                    // animation ended
                }
            }
        });


        /*switch (LocationUtils.getBearingFromRotation(fRotation)) {
            case RIGHT:
                mMarkerDriver.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_maps_local_shipping_right));
                break;
            case DOWN:
                mMarkerDriver.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_maps_local_shipping_down));
                break;
            case LEFT:
                mMarkerDriver.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_maps_local_shipping_left));
                break;
            case UP:
                mMarkerDriver.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_maps_local_shipping_up));
                break;
            case NONE:
                mMarkerDriver.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_maps_local_shipping_right));
                break;
        }*/


        if (LocationUtils.CalculationByDistance(new LatLng(mOrderLocation.getLatitude(), mOrderLocation.getLongitude()), mDriverLocation) < 0.03) {
            WidgetsUtils.createLongToast("Your order is almost here, get ready");
        }

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

        googleMap.addMarker(getOrderMarkerOpts());
        updateMarkers();

        //buildGoogleApiClient();
    }


    protected synchronized void buildGoogleApiClient() {
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
            DebugUtils.logDebug(TAG, "buildGoogleApiClient:");
        }
    }

    protected LocationRequest getLocationRequest() {
        if (mLocationRequest == null) {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(10000);
            mLocationRequest.setFastestInterval(5000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
        return mLocationRequest;
    }

    @Override
    public void onConnected(Bundle bundle) {
        DebugUtils.logDebug(TAG, "onConnected:");
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, getLocationRequest(), OrderStatusActivity.this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        DebugUtils.logError(TAG, "onConnectionSuspended: " + i);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        DebugUtils.logError(TAG, "onConnectionFailed: " + connectionResult.toString());
    }


    @Override
    public void onLocationChanged(Location location) {
        mOrderLocation = location;
        mOrderLocation = new Location("Location");
        mOrderLocation.setLatitude(37.75717119999999);
        mOrderLocation.setLongitude(-122.3924873);
        DebugUtils.logDebug(TAG, "onLocationChanged: " + location.getLatitude() + "," + location.getLongitude());
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

        MarkerOptions mMarkerUser = new MarkerOptions().position(new LatLng(mOrderLocation.getLatitude(), mOrderLocation.getLongitude())).title("Kokusho Location");
        mMarkerUser.icon(BitmapDescriptorFactory.fromResource(R.drawable.point));
        aMarker.add(mMarkerUser);
        googleMap.addMarker(mMarkerUser);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateMarkers();
            }
        });
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
