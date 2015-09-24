package com.bentonow.bentonow.controllers.geolocation;


import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
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
import android.widget.Toast;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.ConstantUtils;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.LocationUtils;
import com.bentonow.bentonow.Utils.MixpanelUtils;
import com.bentonow.bentonow.Utils.WidgetsUtils;
import com.bentonow.bentonow.controllers.BaseFragmentActivity;
import com.bentonow.bentonow.controllers.errors.BummerActivity;
import com.bentonow.bentonow.controllers.help.HelpActivity;
import com.bentonow.bentonow.controllers.order.BuildBentoActivity;
import com.bentonow.bentonow.controllers.order.CompleteOrderActivity;
import com.bentonow.bentonow.model.BackendText;
import com.bentonow.bentonow.model.Order;
import com.bentonow.bentonow.model.Settings;
import com.bentonow.bentonow.model.User;
import com.bentonow.bentonow.ui.BackendTextView;
import com.bentonow.bentonow.ui.CustomDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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

public class DeliveryLocationActivity extends BaseFragmentActivity implements GoogleMap.OnMapLoadedCallback, GoogleMap.OnMapClickListener, View.OnClickListener, AdapterView.OnItemClickListener, View.OnKeyListener,
        TextView.OnEditorActionListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static final String TAG = "DeliveryLocationAct";

    public static final String TAG_DELIVERY_ACTION = "DeliveryAction";

    GoogleMap map; // Might be null if Google Play services APK is not available.
    Marker marker;
    AutoCompleteTextView txt_address;

    private BackendTextView txtAlertAgree;
    CheckBox chck_i_agree;
    Button btn_continue;
    ImageButton btn_clear;
    ImageButton btn_current_location;
    ProgressBar progressBar;

    InputMethodManager imm;

    CustomDialog dialog;

    private boolean mRequestingLocationUpdates;
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private LocationRequest mLocationRequest;
    private LatLng mLastLocations;
    private LatLng mLastOrderLocation;
    private Address sOrderAddress;

    private ConstantUtils.optDeliveryAction optDelivery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_delivery_location);

        try {
            optDelivery = (ConstantUtils.optDeliveryAction) getIntent().getExtras().getSerializable(TAG_DELIVERY_ACTION);
        } catch (Exception ex) {
            optDelivery = ConstantUtils.optDeliveryAction.NONE;
        }

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        txt_address = (AutoCompleteTextView) findViewById(R.id.txt_address);
        chck_i_agree = (CheckBox) findViewById(R.id.chck_iagree);
        btn_continue = (Button) findViewById(R.id.btn_continue);
        btn_clear = (ImageButton) findViewById(R.id.btn_clear);
        btn_current_location = (ImageButton) findViewById(R.id.btn_current_location);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        initActionbar();
        setupAutocomplete();

        mLastLocations = User.location;
        mLastOrderLocation = Order.location;
        sOrderAddress = Order.address;

        mRequestingLocationUpdates = true;
        buildGoogleApiClient();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && optDelivery != ConstantUtils.optDeliveryAction.CHANGE) {
            startLocationUpdates();
        }
        setupMap();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void initActionbar() {
        TextView actionbar_title = (TextView) findViewById(R.id.actionbar_title);
        actionbar_title.setText(getResources().getString(R.string.delivery_location_actionbar_title));

        if (optDelivery == ConstantUtils.optDeliveryAction.CHANGE) {
            ImageView actionbar_left_btn = (ImageView) findViewById(R.id.actionbar_left_btn);
            actionbar_left_btn.setImageResource(R.drawable.ic_ab_back);
            actionbar_left_btn.setOnClickListener(this);
        }

        ImageView actionbar_right_btn = (ImageView) findViewById(R.id.actionbar_right_btn);
        actionbar_right_btn.setImageResource(R.drawable.ic_ab_help);
        actionbar_right_btn.setOnClickListener(this);
    }

    void updateUI() {
        progressBar.setVisibility(View.GONE);

        if (chck_i_agree.isChecked() && sOrderAddress != null) {
            btn_continue.setBackgroundColor(getResources().getColor(R.color.btn_green));
        } else {
            btn_continue.setBackgroundColor(getResources().getColor(R.color.gray));
        }

        // btn_current_location.setVisibility(User.location != null ? View.VISIBLE : View.GONE);
        btn_clear.setVisibility(txt_address.getText().length() > 0 ? View.VISIBLE : View.GONE);
    }

    //****
    // Map
    //****

    private void setupMap() {
        if (map != null) return;
        map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

        Log.i(TAG, "get map fragment");
        if (map == null) return;

        Log.i(TAG, "setup marker");
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

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(point, zoom));
        map.setOnMapLoadedCallback(this);
    }

    @Override
    public void onMapLoaded() {
        if (mLastOrderLocation != null) {
            markerLocation(mLastOrderLocation);
        } else if (mLastLocations != null) {
            markerLocation(mLastLocations);
        }

        map.setOnMapClickListener(this);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (latLng != null) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
            txt_address.setText("", false);
            sOrderAddress = null;

            markerLocation(latLng);
            updateUI();
        }
    }

    private void markerLocation(LatLng latLng) {
        if (marker == null) {
            marker = map.addMarker(
                    new MarkerOptions()
                            .position(latLng)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.location_marker_hi))
            );
        }

        mLastOrderLocation = latLng;
        marker.setPosition(latLng);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, map.getCameraPosition().zoom > 11f ? map.getCameraPosition().zoom : 17f));
        scanCurrentLocation(latLng);
    }

    private void scanCurrentLocation(LatLng location) {
        txt_address.setText("", false);
        btn_clear.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        Geocoder geoCoder = new Geocoder(getApplicationContext());
        List<Address> matches = null;

        try {
            matches = geoCoder.getFromLocation(location.latitude, location.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        if (matches != null && !matches.isEmpty()) {
            sOrderAddress = matches.get(0);

            Log.i(TAG, "full address: " + LocationUtils.getFullAddress(sOrderAddress));
            Log.i(TAG, "address: " + LocationUtils.getStreetAddress(sOrderAddress));

            txt_address.setText(LocationUtils.getFullAddress(sOrderAddress), false);
        } else {
            txt_address.setText("", false);
        }

        updateUI();
    }

    //***
    //Click listeners
    //**

    public void onClearPressed(View view) {
        txt_address.setText("");
        sOrderAddress = null;
        updateUI();
    }

    public void onCurrentLocationPressed(View view) {
        WidgetsUtils.createShortToast("Getting your location");
        startLocationUpdates();
    }

    private void updateCurrentLocation() {
        if (mLastLocations == null || map == null)
            return;
        markerLocation(mLastLocations);
        scanCurrentLocation(mLastLocations);
        updateUI();
    }

    public void onAgreePressed(View view) {
        updateUI();
    }

    public void onHelpPressed(View view) {
        dialog = new CustomDialog(
                this,
                BackendText.get("delivery-agree-message"),
                BackendText.get("delivery-agree-confirmation-2"),
                BackendText.get("delivery-agree-confirmation-1")
        );
        dialog.setOnOkPressed(this);
        dialog.show();
    }

    private boolean isValidLocation() {
        boolean bIsValid = true;

        if (sOrderAddress == null || mLastOrderLocation == null || !chck_i_agree.isChecked()) {
            bIsValid = false;
            String sError = getString(R.string.alert_tab_checkbox);

            if (sOrderAddress == null || mLastOrderLocation == null)
                sError = getString(R.string.delivery_alert_no_address);

            txtAlertAgree().setText(sError);

            new FadeInOut(this, txtAlertAgree(), R.anim.fadein, R.anim.fadeout);
        }

        DebugUtils.logDebug(TAG, "isValidLocation(): " + bIsValid);

        return bIsValid;
    }

    public void onContinuePressed(View view) {

        if (!isValidLocation())
            return;

        Log.i(TAG, "onContinuePressed OK");

        boolean isInDeliveryArea = Settings.isInServiceArea(mLastOrderLocation);

        Log.i(TAG, "onContinuePressed isInDeliveryArea " + (isInDeliveryArea ? "YES" : "NO"));

        if (isInDeliveryArea) {
            Order.location = mLastOrderLocation;
            User.location = mLastLocations;
            Order.address = sOrderAddress;

            switch (optDelivery) {
                case CHANGE:
                    onBackPressed();
                    break;
                case COMPLETE_ORDER:
                    if (User.current != null)
                        startActivity(new Intent(this, CompleteOrderActivity.class));
                    else if (User.current == null) {
                        Intent intent = new Intent(this, BuildBentoActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(this, BuildBentoActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                    break;
                case NONE:
                    Intent intent = new Intent(this, BuildBentoActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    break;
            }

            finish();
        } else {
            try {
                JSONObject params = new JSONObject();
                params.put("address", LocationUtils.getFullAddress(sOrderAddress));
                MixpanelUtils.track("Selected address outside of service area", params);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Intent intent = new Intent(getApplicationContext(), BummerActivity.class);
            intent.putExtra("invalid_address", sOrderAddress.toString());
            startActivity(intent);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.actionbar_left_btn:
                onBackPressed();
                break;
            case R.id.actionbar_right_btn:
                startActivity(new Intent(DeliveryLocationActivity.this, HelpActivity.class));
                break;
            case R.id.btn_ok:
                chck_i_agree.setChecked(true);
                dialog.dismiss();
                updateUI();
                break;
        }
    }

    //***
    //Autocomplete
    //***

    class GooglePlacesAutocompleteAdapter extends ArrayAdapter<String> implements Filterable {
        private ArrayList<String> resultList;

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
                return resultList.get(index);
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
                        Log.i(TAG, "resultList: " + resultList);

                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
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
        txt_address.setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.listitem_locationaddress));
        txt_address.setText("", false);
        txt_address.setOnItemClickListener(this);
        txt_address.setOnKeyListener(this);
        txt_address.setOnEditorActionListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txt_address.getWindowToken(), 0);
        String str = (String) adapterView.getItemAtPosition(position);
        if (str != null) {
            checkAddress(str);
        }
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
                imm.hideSoftInputFromWindow(txt_address.getWindowToken(), 0);
                return true;
            }
            return false;
        }

        imm.hideSoftInputFromWindow(txt_address.getWindowToken(), 0);
        return true;
    }

    public ArrayList<String> autocomplete(String input) {
        ArrayList<String> resultList = null;

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
                resultList.add(list.getJSONObject(i).getString("description"));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Cannot process JSON results", e);
        }

        return resultList;
    }

    private void checkAddress(String str) {
        Log.i(TAG, "checkAddress()");

        Geocoder geoCoderClick = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> addresses = geoCoderClick.getFromLocationName(str, 5);
            if (addresses.size() > 0) {
                markerLocation(new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onConnected(Bundle bundle) {
        DebugUtils.logDebug("buildGoogleApiClient", "onConnected:");

        if (optDelivery != ConstantUtils.optDeliveryAction.CHANGE) {
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

    private BackendTextView txtAlertAgree() {
        if (txtAlertAgree == null)
            txtAlertAgree = (BackendTextView) findViewById(R.id.alert_i_agree);
        return txtAlertAgree;
    }
}