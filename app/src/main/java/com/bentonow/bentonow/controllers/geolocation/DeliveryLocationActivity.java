package com.bentonow.bentonow.controllers.geolocation;


import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.AndroidUtil;
import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.ConstantUtils;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.LocationUtils;
import com.bentonow.bentonow.Utils.MixpanelUtils;
import com.bentonow.bentonow.Utils.WidgetsUtils;
import com.bentonow.bentonow.controllers.BaseFragmentActivity;
import com.bentonow.bentonow.controllers.BentoApplication;
import com.bentonow.bentonow.controllers.dialog.ConfirmationDialog;
import com.bentonow.bentonow.controllers.dialog.ProgressDialog;
import com.bentonow.bentonow.controllers.errors.BummerActivity;
import com.bentonow.bentonow.controllers.fragment.MySupportMapFragment;
import com.bentonow.bentonow.controllers.help.HelpActivity;
import com.bentonow.bentonow.dao.UserDao;
import com.bentonow.bentonow.listener.ListenerWebRequest;
import com.bentonow.bentonow.listener.OnCustomDragListener;
import com.bentonow.bentonow.model.AutoCompleteModel;
import com.bentonow.bentonow.model.BackendText;
import com.bentonow.bentonow.model.Order;
import com.bentonow.bentonow.model.Settings;
import com.bentonow.bentonow.ui.BackendTextView;
import com.bentonow.bentonow.web.request.RequestGetPlaceDetail;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.wsdcamp.anim.FadeInOut;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DeliveryLocationActivity extends BaseFragmentActivity implements GoogleMap.OnMapLoadedCallback, View.OnClickListener, AdapterView.OnItemClickListener, View.OnKeyListener,
        TextView.OnEditorActionListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static final String TAG = "DeliveryLocationAct";

    private GoogleMap googleMap; // Might be null if Google Play services APK is not available.
    private MySupportMapFragment mapFragment;
    private AutoCompleteTextView txt_address;

    private BackendTextView txtAlertAgree;
    private CheckBox check_i_agree;
    private Button btn_continue;
    private ImageButton btn_clear;
    private ImageButton btn_current_location;
    private ImageView actionbar_right_btn;
    private ImageView actionbar_left_btn;
    private ProgressBar progressBar;
    private ProgressDialog mProgressDialog;

    private boolean mRequestingLocationUpdates;
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private LocationRequest mLocationRequest;
    private LatLng mLastLocations;
    private LatLng mLastOrderLocation;
    private Address sOrderAddress;

    private ConstantUtils.optOpenScreen optOpenScreen;

    private ArrayList<AutoCompleteModel> resultList;

    private UserDao userDao = new UserDao();

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

        mLastLocations = LocationUtils.mCurrentLocation;
        mLastOrderLocation = Order.location;
        sOrderAddress = Order.address;

        mRequestingLocationUpdates = true;
        buildGoogleApiClient();

        getMapFragment().setOnDragListener(new OnCustomDragListener() {

            @Override
            public void onDrag(MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_UP:
                        sOrderAddress = null;
                        mLastLocations = getGoogleMap().getCameraPosition().target;
                        mLastOrderLocation = mLastLocations;

                        if (mLastLocations != null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    scanLocation(mLastLocations);
                                }
                            });
                        }
                        break;
                    default:
                        break;
                }
            }
        });

        if (mGoogleApiClient.isConnected() && optOpenScreen == ConstantUtils.optOpenScreen.NORMAL)
            startLocationUpdates();

        setupMap();

        if (mLastLocations != null)
            markerLocation(mLastLocations);
        else
            updateUI();

    }


    private void initActionbar() {
        TextView actionbar_title = (TextView) findViewById(R.id.actionbar_title);
        actionbar_title.setText(getResources().getString(R.string.delivery_location_actionbar_title));

        if (optOpenScreen == ConstantUtils.optOpenScreen.SUMMARY) {
            getImageView().setImageResource(R.drawable.ic_ab_back);
        } else {
            getImageView().setImageResource(R.drawable.ic_ab_user);
        }


        getImageView().setOnClickListener(this);

        getHelpMark().setImageResource(R.drawable.ic_ab_help);
        getHelpMark().setOnClickListener(this);
    }

    void updateUI() {

        getProgressBar().setVisibility(View.GONE);

        if (getCheckIAgree().isChecked() && sOrderAddress != null) {
            getBtnContinue().setBackgroundColor(getResources().getColor(R.color.btn_green));
        } else {
            getBtnContinue().setBackgroundColor(getResources().getColor(R.color.gray));
        }

        // btn_current_location.setVisibility(User.location != null ? View.VISIBLE : View.GONE);
        getBtnClear().setVisibility(getTxtAddress().getText().length() > 0 ? View.VISIBLE : View.GONE);
    }

    //****
    // Map
    //****

    private void setupMap() {
        DebugUtils.logDebug(TAG, "setupMap()");
        if (getGoogleMap() != null) return;

        DebugUtils.logDebug(TAG, "get map fragment");
        if (getMapFragment() == null) return;

        DebugUtils.logDebug(TAG, "setup marker");
        LatLng point = null;
        float zoom = 12f;

        if (mLastOrderLocation != null) {
            point = mLastOrderLocation;
        } else if (mLastLocations != null) {
            point = mLastLocations;
        }

        if (point == null) {
            point = new LatLng(37.772492, -122.420262);
        } else {
            markerLocation(point);
            zoom = 17f;
        }

        getGoogleMap().moveCamera(CameraUpdateFactory.newLatLngZoom(point, zoom));
        getGoogleMap().setOnMapLoadedCallback(this);
    }

    @Override
    public void onMapLoaded() {
        DebugUtils.logDebug(TAG, "onMapLoaded()");
        if (mLastOrderLocation != null) {
            markerLocation(mLastOrderLocation);
        } else if (mLastLocations != null) {
            markerLocation(mLastLocations);
        }
    }

    private void markerLocation(LatLng latLng) {
        mLastOrderLocation = latLng;
        getGoogleMap().moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, getGoogleMap().getCameraPosition().zoom > 11f ? getGoogleMap().getCameraPosition().zoom : 17f));
        scanLocation(latLng);
    }

    private void moveMapToCenter(LatLng mLocation, String sAddress) {
        mLastOrderLocation = mLocation;
        getGoogleMap().moveCamera(CameraUpdateFactory.newLatLngZoom(mLocation, getGoogleMap().getCameraPosition().zoom > 11f ? getGoogleMap().getCameraPosition().zoom : 17f));
        getTxtAddress().setText(sAddress, false);
        sOrderAddress = LocationUtils.getAddressFromLocation(mLocation);
        updateUI();
    }

    private void scanLocation(final LatLng mLocation) {
        getBtnClear().setVisibility(View.INVISIBLE);
        getProgressBar().setVisibility(View.VISIBLE);

        new Thread(new Runnable() {
            @Override
            public void run() {
                sOrderAddress = LocationUtils.getAddressFromLocation(mLocation);
                final String sCustomAddress = LocationUtils.getCustomAddress(sOrderAddress);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (sOrderAddress != null) {
                            getTxtAddress().setText(sCustomAddress, false);
                        } else {
                            getTxtAddress().setText("", false);
                        }

                        updateUI();
                    }
                });

            }
        }).start();

    }


    public void onClearPressed(View view) {
        getTxtAddress().setText("");
        sOrderAddress = null;
        updateUI();
    }

    public void onCurrentLocationPressed(View view) {
        WidgetsUtils.createShortToast("Getting your location");
        startLocationUpdates();
    }

    private void updateCurrentLocation() {
        if (mLastLocations == null || getGoogleMap() == null)
            return;
        markerLocation(mLastLocations);
    }

    public void onAgreePressed(View view) {
        updateUI();
    }

    public void onHelpPressed(View view) {
        ConfirmationDialog mDialog = new ConfirmationDialog(DeliveryLocationActivity.this, null, BackendText.get("delivery-agree-message"));
        mDialog.addAcceptButton(BackendText.get("delivery-agree-confirmation-2"), DeliveryLocationActivity.this);
        mDialog.addCancelButton(BackendText.get("delivery-agree-confirmation-1"), DeliveryLocationActivity.this);
        mDialog.show();
    }

    private boolean isValidLocation() {
        boolean bIsValid = true;

        if (sOrderAddress == null || mLastOrderLocation == null || !getCheckIAgree().isChecked()) {
            bIsValid = false;
            String sError = getString(R.string.alert_tab_checkbox);

            if (sOrderAddress == null || mLastOrderLocation == null)
                sError = getString(R.string.delivery_alert_no_address);

            getTxtAlertAgree().setText(sError);

            new FadeInOut(this, getTxtAlertAgree(), R.anim.fadein, R.anim.fadeout);
        }

        DebugUtils.logDebug(TAG, "isValidLocation(): " + bIsValid);

        return bIsValid;
    }

    public void onContinuePressed(View view) {

        if (!isValidLocation())
            return;

        DebugUtils.logDebug(TAG, "onContinuePressed OK");

        boolean isInDeliveryArea = Settings.isInServiceArea(mLastOrderLocation);

        DebugUtils.logDebug(TAG, "onContinuePressed isInDeliveryArea " + (isInDeliveryArea ? "YES" : "NO"));

        if (isInDeliveryArea) {
            Order.location = mLastOrderLocation;
            LocationUtils.mCurrentLocation = mLastOrderLocation;
            Order.address = sOrderAddress;

            switch (optOpenScreen) {
                case NORMAL:
                    onBackPressed();
                    break;
                case COMPLETE_ORDER:
                    if (BentoNowUtils.isValidCompleteOrder(DeliveryLocationActivity.this))
                        BentoNowUtils.openCompleteOrderActivity(DeliveryLocationActivity.this);
                    break;
                case BUILD_BENTO:
                    BentoNowUtils.openBuildBentoActivity(DeliveryLocationActivity.this);
                    break;
                case SUMMARY:
                    onBackPressed();
                    break;
            }
            finish();
        } else {
            try {
                JSONObject params = new JSONObject();
                params.put("address", LocationUtils.getCustomAddress(sOrderAddress));
                MixpanelUtils.track("Selected address outside of service area", params);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Intent intent = new Intent(DeliveryLocationActivity.this, BummerActivity.class);
            intent.putExtra(BummerActivity.TAG_INVALID_ADDRESS, LocationUtils.getCustomAddress(sOrderAddress));
            startActivity(intent);
        }
    }


    //***
    //Autocomplete
    //***

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
            } catch (IndexOutOfBoundsException ignore) {
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
                        // Retrieve the autocomplete results.
                        resultList = autocomplete(constraint.toString());

                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                notifyDataSetChanged();
                            }
                        });
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
        }
    }


    void setupAutocomplete() {
        getTxtAddress().setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.listitem_locationaddress));
        getTxtAddress().setText("", false);
        getTxtAddress().setOnItemClickListener(this);
        getTxtAddress().setOnKeyListener(this);
        getTxtAddress().setOnEditorActionListener(this);
    }


    public ArrayList<AutoCompleteModel> autocomplete(String input) {
        ArrayList<AutoCompleteModel> resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            URL url = new URL("https://maps.googleapis.com/maps/api/place/autocomplete/json?key=" + getResources().getString(R.string.google_server_key) + "&input=" + URLEncoder.encode(input, "utf8"));

            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(TAG, "Error processing Places API URL", e);
            return null;
        } catch (IOException e) {
            Log.e(TAG, "Error connecting to Places API", e);
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
                DebugUtils.logDebug(TAG, "Predictions: " + list.getJSONObject(i).toString());
                AutoCompleteModel mAutocomplete = new AutoCompleteModel();
                mAutocomplete.setAddress(list.getJSONObject(i).getString("description"));
                mAutocomplete.setPlaceId(list.getJSONObject(i).getString("place_id"));
                resultList.add(mAutocomplete);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Cannot process JSON results", e);
        }

        return resultList;
    }

    private void getExactLocationByPlaceId(final String sAddress, final String sPlaceId) {
        // checkAddress(sAddress);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDialog = new ProgressDialog(DeliveryLocationActivity.this, R.string.searching_label);
                mProgressDialog.show();
            }
        });
        BentoApplication.instance.webRequest(new RequestGetPlaceDetail(sPlaceId, new ListenerWebRequest() {
            @Override
            public void onError(String sError, int statusCode) {
                checkAddress(sAddress);
                onComplete();
            }

            @Override
            public void onResponse(final Object oResponse, int statusCode) {
                runOnUiThread(new Runnable() {
                    @Override

                    public void run() {
                        moveMapToCenter((LatLng) oResponse, sAddress);
                    }
                });
                onComplete();
            }


            @Override
            public void onComplete() {
                dismissDialog();
            }
        }));

    }

    private void checkAddress(String sAddress) {
        // DebugUtils.logDebug(TAG, "checkAddress(): " + str);
        Geocoder geoCoderClick = new Geocoder(DeliveryLocationActivity.this, Locale.getDefault());
        try {
            List<Address> addresses = geoCoderClick.getFromLocationName(sAddress, 5);
            if (addresses.size() > 0) {
                moveMapToCenter(new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude()), sAddress);
            } else {
                WidgetsUtils.createShortToast(R.string.error_location_place);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getTxtAddress().setText("", false);
                    }
                });
                DebugUtils.logDebug(TAG, "No Address Found");
            }
        } catch (IOException e) {
            DebugUtils.logError(TAG, e);
        }
    }

    private void dismissDialog() {
        if (mProgressDialog != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProgressDialog.dismiss();
                }
            });
        }
    }


    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, getLocationRequest(), this);
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
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
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
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, final int position, long id) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getTxtAddress().getWindowToken(), 0);
        getExactLocationByPlaceId(resultList.get(position).getAddress(), resultList.get(position).getPlaceId());
        // getExactLocationByPlaceId("aaezazaafñpke", "asdresd");
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

        if (optOpenScreen == ConstantUtils.optOpenScreen.NORMAL) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mCurrentLocation != null) {
                mLastLocations = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateCurrentLocation();
                    }
                });
            }

            if (mRequestingLocationUpdates) {
                startLocationUpdates();
            }
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
    public void onLocationChanged(Location location) {
        DebugUtils.logDebug("buildGoogleApiClient", "onLocationChanged:");
        mCurrentLocation = location;
        mLastLocations = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateCurrentLocation();
                mRequestingLocationUpdates = false;
                stopLocationUpdates();
            }
        });
    }


    private MySupportMapFragment getMapFragment() {
        if (mapFragment == null)
            mapFragment = ((MySupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapCurrentLocation));
        return mapFragment;
    }

    private GoogleMap getGoogleMap() {
        if (googleMap == null)
            googleMap = getMapFragment().getMap();
        return googleMap;
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

    private ImageButton getBtnClear() {
        if (btn_clear == null)
            btn_clear = (ImageButton) findViewById(R.id.btn_clear);
        return btn_clear;
    }

    private ImageButton getBtnCurrentLocation() {
        if (btn_current_location == null)
            btn_current_location = (ImageButton) findViewById(R.id.btn_current_location);
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

}