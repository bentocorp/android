package com.bentonow.bentonow.controllers.geolocation;


import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.AndroidUtil;
import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.BentoRestClient;
import com.bentonow.bentonow.Utils.ConstantUtils;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.GoogleAnalyticsUtil;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.Utils.WidgetsUtils;
import com.bentonow.bentonow.Utils.maps.LocationUtils;
import com.bentonow.bentonow.controllers.BaseFragmentActivity;
import com.bentonow.bentonow.controllers.dialog.ConfirmationDialog;
import com.bentonow.bentonow.controllers.dialog.ProgressDialog;
import com.bentonow.bentonow.controllers.errors.BummerActivity;
import com.bentonow.bentonow.controllers.fragment.MySupportMapFragment;
import com.bentonow.bentonow.controllers.help.HelpActivity;
import com.bentonow.bentonow.dao.IosCopyDao;
import com.bentonow.bentonow.dao.MenuDao;
import com.bentonow.bentonow.dao.SettingsDao;
import com.bentonow.bentonow.listener.OnCustomDragListener;
import com.bentonow.bentonow.model.AutoCompleteModel;
import com.bentonow.bentonow.model.Order;
import com.bentonow.bentonow.model.user.CouponRequest;
import com.bentonow.bentonow.parse.GooglePlaceJsonParser;
import com.bentonow.bentonow.parse.InitParse;
import com.bentonow.bentonow.ui.BackendTextView;
import com.bentonow.bentonow.web.BentoNowApi;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.TextHttpResponseHandler;
import com.wsdcamp.anim.FadeInOut;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class DeliveryLocationActivity extends BaseFragmentActivity implements GoogleMap.OnMapClickListener, View.OnClickListener, AdapterView.OnItemClickListener, View.OnKeyListener,
        TextView.OnEditorActionListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, OnMapReadyCallback {

    public static final String TAG = "DeliveryLocationAct";
    private static final long SCROLL_TIME = 100L;
    private GoogleMap googleMap;
    private MySupportMapFragment mapFragment;
    private AutoCompleteTextView txt_address;
    private BackendTextView txtAlertAgree;
    private CheckBox check_i_agree;
    private Button btn_continue;
    private ImageView btn_clear;
    private ImageView btn_current_location;
    private ImageView actionbar_right_btn;
    private ImageView actionbar_left_btn;
    private ProgressBar progressBar;
    private ProgressDialog mProgressDialog;
    private ConfirmationDialog mConfirmationDialog;
    private GoogleApiClient mGoogleApiClient;
    private LatLng mCurrentLocation;
    private LocationRequest mLocationRequest;
    private LatLng mLastOrderLocation;
    private Address mOrderAddress;
    private ConstantUtils.optOpenScreen optOpenScreen;
    private ArrayList<AutoCompleteModel> resultList;

    private float previousZoomLevel = 17.7f;
    private long lastTouched = 0;
    private boolean bAllowRequest = true;
    private boolean bUpdateOrderLocation = false;
    private boolean bUseOtherLocation = false;
    private String sTextToSearch = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_delivery_location);


        try {
            optOpenScreen = (ConstantUtils.optOpenScreen) getIntent().getExtras().getSerializable(ConstantUtils.TAG_OPEN_SCREEN);
        } catch (Exception ex) {
            DebugUtils.logError(TAG, ex);
        }

        if (optOpenScreen == null)
            optOpenScreen = ConstantUtils.optOpenScreen.NORMAL;

        initActionbar();
        setupAutocomplete();

        mLastOrderLocation = BentoNowUtils.getOrderLocation();

        getMapFragment();

        updateUI();

        getBtnContinue().setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        GoogleAnalyticsUtil.sendScreenView("Delivery Location");
        super.onResume();
    }

    private void initActionbar() {
        TextView actionbar_title = (TextView) findViewById(R.id.actionbar_title);
        actionbar_title.setText(getResources().getString(R.string.delivery_location_actionbar_title));

        if (optOpenScreen == ConstantUtils.optOpenScreen.SUMMARY) {
            getImageView().setImageResource(R.drawable.vector_navigation_left_green);
        } else {
            getImageView().setImageResource(R.drawable.vector_user_profile);
        }


        getImageView().setOnClickListener(this);

        getHelpMark().setImageResource(R.drawable.vector_faq_green);
        getHelpMark().setOnClickListener(this);
    }

    void updateUI() {
        getProgressBar().setVisibility(View.GONE);

        if (getCheckIAgree().isChecked() && mOrderAddress != null) {
            getBtnContinue().setBackground(getResources().getDrawable(R.drawable.btn_rounded_green));
        } else {
            getBtnContinue().setBackground(getResources().getDrawable(R.drawable.btn_rounded_gray));
        }

        getBtnClear().setVisibility(getTxtAddress().getText().length() > 0 ? View.VISIBLE : View.GONE);

    }

    private void setupMap() {
        previousZoomLevel = 17f;

        DebugUtils.logDebug(TAG, "setup marker");
        LatLng point = null;

        if (mLastOrderLocation != null) {
            point = mLastOrderLocation;
        }

        if (point == null) {
            point = new LatLng(37.772492, -122.420262);
        }

        markerLocation(point);
    }


    private void forceMoveMapLocation(final LatLng latLng, final boolean bMovedMap) {
        restartSearch();

        if (latLng != null) {
            mLastOrderLocation = latLng;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (bMovedMap && googleMap != null)
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, previousZoomLevel));

                    getAddressByLocation(latLng, "");
                }
            });

            updateUI();
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        if ((ContextCompat.checkSelfPermission(DeliveryLocationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(DeliveryLocationActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED))
            map.setMyLocationEnabled(false);

        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.setTrafficEnabled(false);
        map.setIndoorEnabled(true);
        map.setBuildingsEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(false);
        googleMap = map;

        googleMap.setOnMapClickListener(DeliveryLocationActivity.this);
        getMapFragment().setOnDragListener(new OnCustomDragListener() {

            @Override
            public void onDrag(MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastTouched = SystemClock.uptimeMillis();
                        break;
                    case MotionEvent.ACTION_UP:
                        final long now = SystemClock.uptimeMillis();
                        if (now - lastTouched > SCROLL_TIME)
                            forceMoveMapLocation(googleMap.getCameraPosition().target, false);

                        break;
                    default:
                        break;
                }
            }
        });

        googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                DebugUtils.logDebug(TAG, "Camera Position: " + cameraPosition.zoom);
                if (previousZoomLevel != cameraPosition.zoom) {
                    forceMoveMapLocation(cameraPosition.target, false);
                }
                previousZoomLevel = cameraPosition.zoom;

            }
        });


        buildGoogleApiClient();
        setupMap();

    }

    @Override
    public void onMapClick(final LatLng latLng) {
        forceMoveMapLocation(latLng, true);
    }

    private void markerLocation(LatLng latLng) {
        restartSearch();
        mLastOrderLocation = latLng;
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, previousZoomLevel));
        getAddressByLocation(latLng, "");
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void moveMapToCenter(final LatLng mLocation) {
        mLastOrderLocation = mLocation;
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLocation, previousZoomLevel));
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void getAddressByLocation(final String sAddress) {
        getBtnClear().setVisibility(View.GONE);
        getProgressBar().setVisibility(View.VISIBLE);

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    mOrderAddress = LocationUtils.getAddressFromString(sAddress);
                    mLastOrderLocation = new LatLng(mOrderAddress.getLatitude(), mOrderAddress.getLongitude());
                    moveMapToCenter(mLastOrderLocation);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (AndroidUtil.isJellyBean())
                                getTxtAddress().setText(sAddress, false);
                            else
                                getTxtAddress().setText(sAddress);

                            updateUI();
                            AndroidUtil.hideKeyboard(getProgressBar());
                        }
                    });

                } catch (Exception ex) {
                    DebugUtils.logError(TAG, "getAddressByLocation: " + ex.toString());

                    WidgetsUtils.createShortToast(R.string.error_location_address);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (AndroidUtil.isJellyBean())
                                getTxtAddress().setText("", false);
                            else
                                getTxtAddress().setText("");

                            updateUI();
                            AndroidUtil.hideKeyboard(getProgressBar());

                        }
                    });
                }

            }
        }).start();

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void getAddressByLocation(final LatLng mLocation, final String sAddress) {
        getBtnClear().setVisibility(View.GONE);
        getProgressBar().setVisibility(View.VISIBLE);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mOrderAddress = LocationUtils.getAddressFromLocation(mLocation);
                    final String sCustomAddress = LocationUtils.getCustomAddress(mOrderAddress);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (AndroidUtil.isJellyBean())
                                getTxtAddress().setText(sCustomAddress, false);
                            else
                                getTxtAddress().setText(sCustomAddress);

                            updateUI();
                        }
                    });

                } catch (Exception ex) {
                    DebugUtils.logError(TAG, "getAddressByLocation: " + ex.toString());

                    if (sAddress.isEmpty()) {
                        WidgetsUtils.createShortToast(R.string.error_location_place);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (AndroidUtil.isJellyBean())
                                    getTxtAddress().setText("", false);
                                else
                                    getTxtAddress().setText("");

                                updateUI();
                            }
                        });
                    } else
                        getAddressByLocation(sAddress);
                }

            }
        }).start();

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void onClearPressed(View view) {
        restartSearch();

        AndroidUtil.hideKeyboard(getProgressBar());
        updateUI();
    }

    public void onCurrentLocationPressed(View view) {
        if (LocationUtils.isGpsEnable(DeliveryLocationActivity.this)) {
            if ((ContextCompat.checkSelfPermission(DeliveryLocationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(DeliveryLocationActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(DeliveryLocationActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, ConstantUtils.MANIFEST_LOCATION_PERMISSION);
            } else {
                WidgetsUtils.createShortToast("Getting your location");
                bUpdateOrderLocation = true;
                startLocationUpdates();
            }
        } else if (mConfirmationDialog == null || !mConfirmationDialog.isShowing()) {
            mConfirmationDialog = new ConfirmationDialog(DeliveryLocationActivity.this, "Enable GPS", "GPS is disabled in your device. Enable it?");
            mConfirmationDialog.addAcceptButton("Yes", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent callGPSSettingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(callGPSSettingIntent);
                }
            });
            mConfirmationDialog.addCancelButton("No", null);
            mConfirmationDialog.show();
        }
    }

    private void updateCurrentLocation() {
        if (mLastOrderLocation == null || googleMap == null)
            return;

        markerLocation(mLastOrderLocation);
    }

    public void onAgreePressed(View view) {
        updateUI();
    }

    public void onHelpPressed(View view) {
        ConfirmationDialog mDialog = new ConfirmationDialog(DeliveryLocationActivity.this, null, IosCopyDao.get("delivery-agree-message"));
        mDialog.addAcceptButton(IosCopyDao.get("delivery-agree-confirmation-2"), DeliveryLocationActivity.this);
        mDialog.addCancelButton(IosCopyDao.get("delivery-agree-confirmation-1"), DeliveryLocationActivity.this);
        mDialog.show();
    }

    private boolean isValidLocation() {
        boolean bIsValid = true;

        if (mOrderAddress == null || mLastOrderLocation == null || !getCheckIAgree().isChecked() || getTxtAddress().getText().toString().isEmpty()) {
            bIsValid = false;
            String sError = getString(R.string.alert_tab_checkbox);

            if (mOrderAddress == null || mLastOrderLocation == null || getTxtAddress().getText().toString().isEmpty())
                sError = getString(R.string.delivery_alert_no_address);

            getTxtAlertAgree().setText(sError);

            new FadeInOut(this, getTxtAlertAgree(), R.anim.fadein, R.anim.fadeout);
        }


        if (optOpenScreen == ConstantUtils.optOpenScreen.BUILD_BENTO && mCurrentLocation != null && !bUseOtherLocation) {
            double iMeters = 100;
            try {
                iMeters = SettingsDao.getCurrent().geofence_order_radius_meters == 0 ? 100 : SettingsDao.getCurrent().geofence_order_radius_meters;
            } catch (Exception ex) {
                DebugUtils.logError(TAG, ex.getMessage());
            }

            iMeters = iMeters / 1000;

            if (iMeters <= LocationUtils.getDistanceInTwoPoints(mCurrentLocation, mLastOrderLocation)) {
                showNotHereDialog();
                bIsValid = false;
            }
        }

        DebugUtils.logDebug(TAG, "isValidLocation(): " + bIsValid);

        return bIsValid;
    }

    private void getMenusByLocation(String responseString) {
        InitParse.parseInitTwo(responseString);

        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.POD_MODE, SettingsDao.getCurrent().pod_mode);
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.STORE_STATUS, MenuDao.gateKeeper.getAppState());

        if (optOpenScreen != ConstantUtils.optOpenScreen.SUMMARY)
            BentoNowUtils.saveOrderLocation(mLastOrderLocation, mOrderAddress);

        DebugUtils.logDebug(TAG, "onContinuePressed AppState " + MenuDao.gateKeeper.getAppState());

        nextStepViaAppState(MenuDao.gateKeeper.getAppState());
    }

    public void onContinuePressed() {
        if (!isValidLocation())
            return;

        showCustomDialog(R.string.processing_label);
        BentoRestClient.get(BentoRestClient.getInit2Url(mLastOrderLocation), null, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                DebugUtils.logError(TAG, "Cannot loadData: " + responseString);
                onFinish();
                String sError;
                switch (statusCode) {
                    case 0:// No internet Connection
                        sError = getString(R.string.error_no_internet_connection);
                        break;
                    default:
                        sError = getString(R.string.error_web_request);
                        break;
                }

                ConfirmationDialog mDialog = new ConfirmationDialog(DeliveryLocationActivity.this, "Error", sError);
                mDialog.addAcceptButton("OK", null);
                mDialog.show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                onFinish();

                switch (optOpenScreen) {
                    case SUMMARY:
                        Order mCurrentOrder = mOrderDao.getCurrentOrder();
                        if (mCurrentOrder != null)
                            if (responseString.contains("\"menu_id\":\"" + mCurrentOrder.MenuId + "\"")) {
                                BentoNowUtils.saveOrderLocation(mLastOrderLocation, mOrderAddress);
                                onBackPressed();
                            } else {
                                String sAppState = InitParse.getAppState(responseString);
                                if (sAppState.contains("build"))
                                    showChangeMenuDialog(responseString);
                                else
                                    nextStepViaAppState(sAppState);
                            }
                        break;
                    default:
                        getMenusByLocation(responseString);
                        break;
                }

            }

            @Override
            public void onFinish() {
                dismissDialog();
            }
        });
    }

    private void nextStepViaAppState(String sAppState) {
        if (sAppState.contains("map,no_service")) {
            CouponRequest mCoupon = new CouponRequest();
            mCoupon.reason = "outside of delivery zone";
            mCoupon.address = getTxtAddress().getText().toString();
            mCoupon.lat = String.valueOf(mLastOrderLocation.latitude);
            mCoupon.lng = String.valueOf(mLastOrderLocation.longitude);

            Intent mIntentBummer = new Intent(DeliveryLocationActivity.this, BummerActivity.class);
            mIntentBummer.putExtra(CouponRequest.TAG, mCoupon);
            startActivity(mIntentBummer);
        } else if (sAppState.contains("build")) {
            switch (optOpenScreen) {
                case COMPLETE_ORDER:
                    if (BentoNowUtils.isValidCompleteOrder(DeliveryLocationActivity.this))
                        BentoNowUtils.openCompleteOrderActivity(DeliveryLocationActivity.this, MenuDao.getCurrentMenu());
                    break;
                case BUILD_BENTO:
                    BentoNowUtils.openBuildBentoActivity(DeliveryLocationActivity.this);
                    break;
                case SUMMARY:
                    BentoNowUtils.saveOrderLocation(mLastOrderLocation, mOrderAddress);
                    BentoNowUtils.openBuildBentoActivity(DeliveryLocationActivity.this);
                    break;
                default:
                    onBackPressed();
                    break;
            }
            finish();
        } else if (sAppState.contains("closed_wall")) {
            BentoNowUtils.openErrorActivity(DeliveryLocationActivity.this);
            finish();
        } else if (sAppState.contains("sold")) {
            BentoNowUtils.openErrorActivity(DeliveryLocationActivity.this);
            finish();
        } else {
            DebugUtils.logError(TAG, "Unknown State: " + MenuDao.gateKeeper.getAppState());
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    void setupAutocomplete() {
        getTxtAddress().setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.listitem_locationaddress));
        if (AndroidUtil.isJellyBean())
            getTxtAddress().setText("", false);
        else
            getTxtAddress().setText("");

        getTxtAddress().setOnItemClickListener(this);
        getTxtAddress().setOnKeyListener(this);
        getTxtAddress().setOnEditorActionListener(this);
    }

    public ArrayList<AutoCompleteModel> autocomplete(String input) {
        ArrayList<AutoCompleteModel> resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            URL url = new URL(BentoNowApi.getUrlGoogleAutocomplete(mCurrentLocation, input));

            DebugUtils.logDebug(TAG, "URL Search: " + url.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            DebugUtils.logError(TAG, "Error processing Places API URL", e);
            return null;
        } catch (IOException e) {
            DebugUtils.logError(TAG, "Error connecting to Places API", e);
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject response = new JSONObject(jsonResults.toString());
            JSONArray list = response.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList<>(list.length());
            for (int i = 0; i < list.length(); i++) {
                //   DebugUtils.logDebug(TAG, "Predictions: " + list.getJSONObject(i).toString());
                AutoCompleteModel mAutocomplete = new AutoCompleteModel();
                mAutocomplete.setAddress(list.getJSONObject(i).getString("description"));
                mAutocomplete.setPlaceId(list.getJSONObject(i).getString("place_id"));
                resultList.add(mAutocomplete);
            }
        } catch (Exception e) {
            DebugUtils.logError(TAG, "Cannot process JSON results: " + e.getMessage());
        }

        return resultList;
    }

    private void getExactLocationByPlaceId(final String sAddress, final String sPlaceId) {
        showCustomDialog(R.string.searching_label);

        BentoRestClient.getCustom(BentoNowApi.getPlaceDetailUrl(sPlaceId), null, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                DebugUtils.logError(TAG, "Cannot loadData: " + responseString);
                getAddressByLocation(sAddress);
                onFinish();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, final String responseString) {
                //  DebugUtils.logDebug(TAG, "Location: " + responseString);
                mLastOrderLocation = GooglePlaceJsonParser.parseLocation(responseString);

                runOnUiThread(new Runnable() {
                    @Override

                    public void run() {
                        if (mLastOrderLocation != null) {
                            moveMapToCenter(mLastOrderLocation);
                            getAddressByLocation(mLastOrderLocation, sAddress);
                        } else
                            getAddressByLocation(sAddress);
                    }
                });
                onFinish();

            }

            @Override
            public void onFinish() {
                dismissDialog();
            }
        });

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void restartSearch() {
        if (AndroidUtil.isJellyBean())
            getTxtAddress().setText("", false);
        else
            getTxtAddress().setText("");

        mOrderAddress = null;
        mLastOrderLocation = null;
    }

    private void showNotHereDialog() {
        if (mConfirmationDialog == null || !mConfirmationDialog.isShowing()) {
            String sAddress = IosCopyDao.get("geofence-outside-msg");
            sAddress = sAddress.replace("%address", getTxtAddress().getText());

            mConfirmationDialog = new ConfirmationDialog(DeliveryLocationActivity.this, null, sAddress);
            mConfirmationDialog.addAcceptButton("Keep", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bUseOtherLocation = true;
                    onContinuePressed();
                }
            });
            mConfirmationDialog.addCancelButton("Change", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bUseOtherLocation = true;
                }
            });
            mConfirmationDialog.show();
        }

    }

    private void showChangeMenuDialog(final String sResponse) {
        if (mConfirmationDialog == null || !mConfirmationDialog.isShowing()) {
            mConfirmationDialog = new ConfirmationDialog(DeliveryLocationActivity.this, "Change Menu", IosCopyDao.get("change-zone-msg"));
            mConfirmationDialog.addAcceptButton("Change", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getMenusByLocation(sResponse);
                }
            });
            mConfirmationDialog.addCancelButton("Keep", null);
            mConfirmationDialog.show();
        }

    }

    private void dismissDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null)
                    mProgressDialog.dismiss();
            }
        });
    }

    private void showCustomDialog(final int idLabel) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDialog = new ProgressDialog(DeliveryLocationActivity.this, idLabel, true);
                mProgressDialog.show();
            }
        });
    }

    protected void startLocationUpdates() {
        if ((ContextCompat.checkSelfPermission(DeliveryLocationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(DeliveryLocationActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, getLocationRequest(), this);
        }
    }

    protected void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
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

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case ConstantUtils.MANIFEST_LOCATION_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    WidgetsUtils.createShortToast("Getting your location");
                    bUpdateOrderLocation = true;
                    startLocationUpdates();
                }
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();

        AndroidUtil.hideKeyboard(getTxtAddress());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.actionbar_left_btn:
                switch (optOpenScreen) {
                    case SUMMARY:
                        onBackPressed();
                        break;
                    default:
                        BentoNowUtils.openSettingsActivity(DeliveryLocationActivity.this);
                        break;
                }
                break;
            case R.id.actionbar_right_btn:
                Intent iHelp = new Intent(DeliveryLocationActivity.this, HelpActivity.class);
                iHelp.putExtra("faq", true);
                startActivity(iHelp);
                break;
            case R.id.button_accept:
                getCheckIAgree().setChecked(true);
                updateUI();
                break;
            case R.id.btn_continue:
                onContinuePressed();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, final int position,
                            long id) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getTxtAddress().getWindowToken(), 0);
        restartSearch();
        getExactLocationByPlaceId(resultList.get(position).getAddress(), resultList.get(position).getPlaceId());
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        updateUI();
        return false;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (event != null) {
            // if shift key is down, then we want to insert the '\n' char in the TextView;
            // otherwise, the default action is to send the message.
            if (!event.isShiftPressed()) {
                AndroidUtil.hideKeyboard(v);
                return true;
            }
            return false;
        }
        AndroidUtil.hideKeyboard(v);
        return true;
    }

    @Override
    public void onConnected(Bundle bundle) {
        DebugUtils.logDebug("buildGoogleApiClient", "onConnected:");

        if ((ContextCompat.checkSelfPermission(DeliveryLocationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(DeliveryLocationActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            Location mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (mLocation != null)
                mCurrentLocation = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());

            startLocationUpdates();
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        DebugUtils.logDebug("buildGoogleApiClient", "onConnectionSuspended: " + i);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        DebugUtils.logDebug("buildGoogleApiClient", connectionResult.toString());
    }

    @Override
    public void onLocationChanged(Location mNewLocation) {
        DebugUtils.logDebug("buildGoogleApiClient", "onLocationChanged:");
        mCurrentLocation = new LatLng(mNewLocation.getLatitude(), mNewLocation.getLongitude());

        if (bUpdateOrderLocation) {
            bUpdateOrderLocation = false;

            mLastOrderLocation = new LatLng(mNewLocation.getLatitude(), mNewLocation.getLongitude());

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateCurrentLocation();
                    stopLocationUpdates();
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        if (googleMap != null) {
            googleMap.clear();
            googleMap.setOnMapClickListener(null);
            googleMap.setOnMapClickListener(null);
            googleMap.setOnCameraChangeListener(null);
        }

        getTxtAddress().setAdapter(null);
        mGoogleApiClient = null;
        super.onDestroy();
    }

    private MySupportMapFragment getMapFragment() {
        if (mapFragment == null) {
            mapFragment = ((MySupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapCurrentLocation));
            mapFragment.getMapAsync(DeliveryLocationActivity.this);
        }
        return mapFragment;
    }

    private BackendTextView getTxtAlertAgree() {
        if (txtAlertAgree == null)
            txtAlertAgree = (BackendTextView) findViewById(R.id.alert_i_agree);
        return txtAlertAgree;
    }

    private AutoCompleteTextView getTxtAddress() {
        if (txt_address == null)
            txt_address = (AutoCompleteTextView) findViewById(R.id.txt_address);
        return txt_address;
    }

    private CheckBox getCheckIAgree() {
        if (check_i_agree == null)
            check_i_agree = (CheckBox) findViewById(R.id.chck_iagree);
        return check_i_agree;
    }

    private Button getBtnContinue() {
        if (btn_continue == null)
            btn_continue = (Button) findViewById(R.id.btn_continue);
        return btn_continue;
    }

    private ImageView getHelpMark() {
        if (actionbar_right_btn == null)
            actionbar_right_btn = (ImageView) findViewById(R.id.actionbar_right_btn);
        return actionbar_right_btn;
    }

    private ImageView getBtnClear() {
        if (btn_clear == null)
            btn_clear = (ImageView) findViewById(R.id.btn_clear);
        return btn_clear;
    }

    private ImageView getBtnCurrentLocation() {
        if (btn_current_location == null)
            btn_current_location = (ImageView) findViewById(R.id.btn_current_location);
        return btn_current_location;
    }

    private ProgressBar getProgressBar() {
        if (progressBar == null)
            progressBar = (ProgressBar) findViewById(R.id.progressBar);
        return progressBar;
    }

    private ImageView getImageView() {
        if (actionbar_left_btn == null)
            actionbar_left_btn = (ImageView) findViewById(R.id.actionbar_left_btn);
        return actionbar_left_btn;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    class GooglePlacesAutocompleteAdapter extends ArrayAdapter<String> implements Filterable {


        public GooglePlacesAutocompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            return (resultList != null) ? resultList.size() : 0;
        }

        @Override
        public String getItem(int index) {
            try {
                return resultList.get(index).getAddress();
            } catch (Exception ignore) {
                return "";
            }
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        if (bAllowRequest) {
                            bAllowRequest = false;
                            resultList = autocomplete(constraint.toString());

                            filterResults.values = resultList;
                            filterResults.count = resultList.size();

                        } else {
                            sTextToSearch = getTxtAddress().getText().toString();
                            DebugUtils.logDebug(TAG, "Text Pending Search: " + sTextToSearch);
                        }
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(final CharSequence constraint, final FilterResults results) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (results != null && results.count > 0) {
                                notifyDataSetChanged();
                            } else {
                                notifyDataSetInvalidated();
                            }
                        }
                    });

                    bAllowRequest = true;
                }
            };
        }
    }

}