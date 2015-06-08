package com.bentonow.bentonow;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bentonow.bentonow.model.Orders;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.PolyUtil;


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
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DeliveryLocationActivity extends BaseFragmentActivity {

    private static final String TAG = "DeliveryLocationActivit";
    private static boolean FLAG_LOCALIZED = false;

    int c = 0;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private boolean i_agree = false;
    private ImageView btn_help;
    private RelativeLayout overlay_help;
    private TextView btn_cancel;
    private TextView btn_iagree;
    private Location current_location;
    private ImageView btn_go_to_current_location;
    private AutoCompleteTextView autoCompView;
    private CheckBox chckIagree;
    private TextView alert_iagree;
    private TextView btn_continue;
    private TextView btn_confirm_address;
    private String newAddress = "";
    private ImageView actionbar_right_btn;
    private LatLng auxTarget;
    private Orders order;
    private FragmentActivity activity;
    private ImageView btn_clear;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_location);

        //setUpMapIfNeeded();
        activity = this;
        Bentonow.app.current_activity = this;

        Log.i(TAG, "onCreate()");
        if( Bentonow.pending_order_id == null ) tryGetPendingOrder();
        Long lastOrderId = Orders.getLastOrderId();
        if ( lastOrderId != null && lastOrderId != 0 ){
            order = Orders.findById(Orders.class,lastOrderId);
            if (order!=null) newAddress = order.getOrderAddress();
        }
        String address = "";
        if( order != null ) address = order.getOrderAddress();
        //////////// INI GOOGLE PLACE AUTOCOMPLETE //////////////
        autoCompView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
        autoCompView.setAdapter(new GooglePlacesAutocompleteAdapter(getApplicationContext(), R.layout.listitem_locationaddress));
        autoCompView.setText(address, false);
        //Log.i(TAG, "autoCompView: " + autoCompView.toString());
        autoCompView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                InputMethodManager imm = (InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(autoCompView.getWindowToken(), 0);
                String str = (String) adapterView.getItemAtPosition(position);
                if (str != null) {
                    checkAddress(str);
                }
            }
        });
        autoCompView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(!autoCompView.getText().toString().isEmpty()){
                    btn_clear.setVisibility(View.VISIBLE);
                }
                return true;
            }
        });
        //////////// END GOOGLE PLACE AUTOCOMPLETE //////////////

        alert_iagree = (TextView) findViewById(R.id.alert_iagree);
        btn_continue = (TextView) findViewById(R.id.btn_continue);
        btn_confirm_address = (TextView) findViewById(R.id.btn_confirm_address);
        btn_clear = (ImageView)findViewById(R.id.btn_clear);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        ///
        chckIagree = (CheckBox) findViewById(R.id.chckIagree);
        if( order != null ) {
            chckIagree.setChecked(true);
            btn_continue.setVisibility(View.INVISIBLE);
            btn_confirm_address.setVisibility(View.VISIBLE);
        }
        addListeners();
        initActionbar();

    }

    private void checkAddress (String str) {
        if (!newAddress.equals(str)) {
            newAddress = str;
            Geocoder geoCoderClick = new Geocoder(getApplicationContext(), Locale.getDefault());
            try {
                List<Address> addresses = geoCoderClick.getFromLocationName(str, 5);
                String add = "";
                if (addresses.size() > 0) {
                    //String coords = "geo:" + String.valueOf(addresses.get(0).getLatitude()) + "," + String.valueOf(addresses.get(0).getLongitude());
                    Location searched_location = new Location("searchedLocation");
                    //LatLng point = mMap.getCameraPosition().target;
                    searched_location.setLatitude(addresses.get(0).getLatitude());
                    searched_location.setLongitude(addresses.get(0).getLongitude());
                    searched_location.setTime(new Date().getTime());
                    //scanCurrentLocation(searched_location);
                    goToCenterLocation(searched_location);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void tryGetPendingOrder() {
        Log.i(TAG, "checkForPendingOrder()");
        List<Orders> pending_orders = Orders.find(Orders.class, "completed = ? AND today = ?", Config.ORDER.STATUS.UNCOMPLETED,todayDate);
        if( !pending_orders.isEmpty() ) {
            for ( Orders order : pending_orders) {
                Bentonow.pending_order_id = order.getId();
            }
        }else{
            //Toast.makeText(getApplicationContext(),"There is not pending order",Toast.LENGTH_LONG).show();
            Log.i(TAG,"There is not pending order");
        }
    }


    private void initActionbar() {
        TextView actionbar_title = (TextView) findViewById(R.id.actionbar_title);
        actionbar_title.setText(getResources().getString(R.string.delivery_location_actionbar_title));

        final Activity _this = this;

        //
        actionbar_right_btn = (ImageView)findViewById(R.id.actionbar_right_btn);
        actionbar_right_btn.setImageResource(R.drawable.ic_ab_question_mark);
        actionbar_right_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(_this, FaqActivity.class));
                overridePendingTransitionGoRight();
            }
        });
    }

    private void addListeners() {

        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoCompView.setText("");
                btn_clear.setVisibility(View.INVISIBLE);
            }
        });

        chckIagree.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    btn_continue.setVisibility(View.INVISIBLE);
                    btn_confirm_address.setVisibility(View.VISIBLE);
                } else {
                    btn_continue.setVisibility(View.VISIBLE);
                    btn_confirm_address.setVisibility(View.INVISIBLE);
                }
            }
        });

        overlay_help = (RelativeLayout) findViewById(R.id.overlay_help);
        btn_help = (ImageView) findViewById(R.id.btn_help);
        btn_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openOverlayMessage();
            }
        });

        btn_cancel = (TextView) findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeOverlayMessage();
            }
        });

        btn_iagree = (TextView) findViewById(R.id.btn_iagree);
        btn_iagree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
                chckIagree.setChecked(true);
                closeOverlayMessage();
            }
        });

        btn_go_to_current_location = (ImageView) findViewById(R.id.btn_go_to_current_location);
        btn_go_to_current_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToCenterLocation(current_location);
            }
        });

        btn_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
                Animation animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadein);
                if (!chckIagree.isChecked()) {
                    alert_iagree.startAnimation(animFadeIn);
                    animFadeIn.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            alert_iagree.setVisibility(View.VISIBLE);
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                public void run() {
                                    Animation animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadeout);
                                    alert_iagree.startAnimation(animFadeOut);
                                    animFadeOut.setAnimationListener(new Animation.AnimationListener() {
                                        @Override
                                        public void onAnimationStart(Animation animation) {

                                        }

                                        @Override
                                        public void onAnimationEnd(Animation animation) {
                                            alert_iagree.setVisibility(View.INVISIBLE);
                                        }

                                        @Override
                                        public void onAnimationRepeat(Animation animation) {

                                        }
                                    });
                                }
                            }, 2000);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                }
            }
        });
        //

        btn_confirm_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BuildConfig.DEBUG) {
                    checkAddress(autoCompView.getText().toString());
                }

                String[] address = newAddress.split(", ");
                String address_number = "";
                String address_street = "";
                String address_city = "";
                String address_state = "";
                String address_zip = "";

                try {
                    //if (address[0]!=null) {
                    Log.i(TAG, "address[0]: " + address[0]);
                    String[] tmp = address[0].split(" ");
                    for (int i = 0; i < tmp.length; i++) {
                        if (i == 0) {
                            address_number = tmp[i];
                        } else {
                            address_street += tmp[i] + " ";
                        }
                    }
                } catch (ArrayIndexOutOfBoundsException ignored) {

                }

                try {
                    //if (address[1]!=null) {
                    Log.i(TAG, "address[1]: " + address[1]);
                    address_city = address[1];
                } catch (ArrayIndexOutOfBoundsException ignored) {

                }

                try {
                    //if (address[2]!=null) {
                    Log.i(TAG, "address[2]: " + address[2]);
                    String[] tmp = address[2].split(" ");
                    for (int i = 0; i < tmp.length; i++) {
                        if (i == tmp.length - 1) {
                            address_zip = tmp[i];
                        } else {
                            address_state += tmp[i] + " ";
                        }
                    }
                } catch (ArrayIndexOutOfBoundsException ignored) {

                }

                List<LatLng> sfpolygon = new ArrayList<LatLng>();
                sfpolygon.add(new LatLng(37.8095806, -122.44983680000001));
                sfpolygon.add(new LatLng(37.77783170000001, -122.44335350000001));
                sfpolygon.add(new LatLng(37.7460824, -122.43567470000002));
                sfpolygon.add(new LatLng(37.7490008, -122.37636569999998));
                sfpolygon.add(new LatLng(37.78611430000001, -122.37928390000002));
                sfpolygon.add(new LatLng(37.8135812, -122.40348819999998));
                sfpolygon.add(new LatLng(37.8095806, -122.44983680000001));


                //Log.i(TAG, "sfpolygon: " + sfpolygon.toString());
                if (PolyUtil.containsLocation(mMap.getCameraPosition().target, sfpolygon, false)) {
                    Orders order = null;
                    if (Bentonow.pending_order_id == null) {
                        order = new Orders();
                        order.today = todayDate;
                        order.coords_lat = String.valueOf(((LatLng) mMap.getCameraPosition().target).latitude);
                        order.coords_long = String.valueOf(((LatLng) mMap.getCameraPosition().target).longitude);
                        order.address_number = address_number;
                        order.address_street = address_street;
                        order.address_city = address_city;
                        order.address_state = address_state;
                        order.address_zip = address_zip;
                        order.completed = Config.ORDER.STATUS.UNCOMPLETED;
                        order.save();
                        Log.i(TAG, "New order generated");
                        Bentonow.pending_order_id = order.getId();
                        goTo(BuildBentoActivity.class);
                    } else {
                        order = Orders.findById(Orders.class, Bentonow.pending_order_id);
                        order.coords_lat = String.valueOf(((LatLng) mMap.getCameraPosition().target).latitude);
                        order.coords_long = String.valueOf(((LatLng) mMap.getCameraPosition().target).longitude);
                        order.address_number = address_number;
                        order.address_street = address_street;
                        order.address_city = address_city;
                        order.address_state = address_state;
                        order.address_zip = address_zip;
                        order.save();
                        Log.i(TAG, "Pending order has changed");
                        goTo(CompleteOrderActivity.class);
                    }
                } else {
                    Intent intent = new Intent(getApplicationContext(), ErrorInvalidAddressActivity.class);
                    intent.putExtra(Config.invalid_address_extra_label, newAddress);
                    startActivity(intent);
                    overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out);
                    finish();
                }
            }
        });

    }

    private void goTo(Class activity_class) {
        Intent intent = new Intent(getApplicationContext(), activity_class);
        startActivity(intent);
        overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out);
        finish();
    }


    private static List<LatLng> makeList(double... coords) {
        int size = coords.length / 2;
        ArrayList<LatLng> list = new ArrayList<LatLng>(size);
        for (int i = 0; i < size; ++i) {
            list.add(new LatLng(coords[i + i], coords[i + i + 1]));
        }
        return list;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();


            /////////////////////////
            if (mMap != null) {
                Log.i(TAG,"Bentonow.pending_order_id: "+Bentonow.pending_order_id);
                if( order == null )
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Config.INIT_LAT_LONG, 11.0f));
                else{
                    double lat = Double.parseDouble(order.coords_lat);
                    double lng = Double.parseDouble(order.coords_long);
                    LatLng LatLong = new LatLng(lat, lng);
                    Log.i( TAG, "LatLong: "+LatLong.toString() );
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLong, 16.0f));
                }

                //setMarkers();
                if( order == null ){
                    mMap.setMyLocationEnabled(true);
                    mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                        @Override
                        public boolean onMyLocationButtonClick() {
                            mMap.setMyLocationEnabled(false);
                            return false;
                        }
                    });
                }

                mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {

                        Location new_location = new Location("newLocation");
                        LatLng point = Config.INIT_LAT_LONG;
                        new_location.setLatitude(point.latitude);
                        new_location.setLongitude(point.longitude);
                        new_location.setTime(new Date().getTime());
                        scanCurrentLocation(new_location);

                        GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
                            @Override
                            public void onMyLocationChange(Location location) {
                                //Log.i(TAG, "OnMyLocationChangeListener");
                                if (!FLAG_LOCALIZED) {
                                    mMap.setMyLocationEnabled(false);
                                    current_location = location;
                                    btn_go_to_current_location.setVisibility(View.VISIBLE);
                                    FLAG_LOCALIZED = true;
                                    scanCurrentLocation(location);

                                    LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 16.0f));
                                }
                            }
                        };
                        if( Bentonow.pending_order_id == null ){
                            mMap.setOnMyLocationChangeListener(myLocationChangeListener);
                        }
                        ////Log.i(TAG,"map loaded, mMap.getCameraPosition().target: "+mMap.getCameraPosition().target);
                        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                            @Override
                            public void onCameraChange(CameraPosition cameraPosition) {
                                Log.i(TAG, "mMap.getCameraPosition().target: " + mMap.getCameraPosition().target);
                                //auxTarget = mMap.getCameraPosition().target;

                                Location new_location = new Location("newLocation");
                                LatLng point = mMap.getCameraPosition().target;
                                new_location.setLatitude(point.latitude);
                                new_location.setLongitude(point.longitude);
                                new_location.setTime(new Date().getTime());
                                scanCurrentLocation(new_location);

                                /*Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    public void run() {
                                        if ( auxTarget.equals(mMap.getCameraPosition().target) ) {
                                            Log.i(TAG,"call google api");
                                            Location new_location = new Location("newLocation");
                                            LatLng point = mMap.getCameraPosition().target;
                                            new_location.setLatitude(point.latitude);
                                            new_location.setLongitude(point.longitude);
                                            new_location.setTime(new Date().getTime());
                                            scanCurrentLocation(new_location);
                                        }
                                    }
                                }, 3500);*/
                            }
                        });
                    }
                });
            }
            ///////////////////////////
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void scanCurrentLocation(Location location) {

        btn_clear.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        Geocoder geoCoder = new Geocoder(getApplicationContext());
        List<Address> matches = null;
        try {
            matches = geoCoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "e.getMessage(): " + e.getMessage());
            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
        }
        Address bestMatch = null;
        if( matches != null ) {
            bestMatch = matches.isEmpty() ? null : matches.get(0);
        }

        //TODO
        //assert bestMatch != null;
        try{
            newAddress = "";
            if( bestMatch != null ) {
                newAddress += bestMatch.getAddressLine(0) != null ? bestMatch.getAddressLine(0) : "";
                newAddress += bestMatch.getAddressLine(1) != null ? ", " + bestMatch.getAddressLine(1) : "";
                newAddress += bestMatch.getAddressLine(2) != null ? ", " + bestMatch.getAddressLine(2) : "";
                autoCompView.setText(newAddress);
                btn_clear.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
            }
        }catch (NullPointerException ignored){
            btn_clear.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        }
        ///////////////////////
        //geoCoder = new Geocoder(this, Locale.getDefault());
        /*try {
            List<Address> addresses = geoCoder.getFromLocationName(newAddress, 5);
            String add = "";
            if (addresses.size() > 0) {
                //String coords = "geo:" + String.valueOf(addresses.get(0).getLatitude()) + "," + String.valueOf(addresses.get(0).getLongitude());
                Location searched_location = new Location("searchedLocation");
                //LatLng point = mMap.getCameraPosition().target;
                location.setLatitude(addresses.get(0).getLatitude());
                location.setLongitude(addresses.get(0).getLongitude());
                location.setTime(new Date().getTime());
                scanCurrentLocation(location);
                //goToCenterLocation(searched_location);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        /////////////////////////
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    ///////////////////////////////


    public void goToCenterLocation(Location find_location) {
        double latitude = find_location.getLatitude();
        double longitude = find_location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        //mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }

    private void openOverlayMessage() {
        overlay_help.setVisibility(View.VISIBLE);
        final Animation animationFadeIn = AnimationUtils.loadAnimation(this, R.anim.fadein);
        overlay_help.startAnimation(animationFadeIn);
    }

    private void closeOverlayMessage() {
        final Animation animationFadeOut = AnimationUtils.loadAnimation(this, R.anim.fadeout);
        overlay_help.startAnimation(animationFadeOut);
        animationFadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                overlay_help.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }


    ////////////// INI AUTOCOMPLETE GOOGLE PLACE /////////////////
    public ArrayList<String> autocomplete(String input) {
        ArrayList<String> resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(Config.PLACES_API_BASE + Config.TYPE_AUTOCOMPLETE + Config.OUT_JSON);
            sb.append("?key=" + Config.API_KEY);
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());

            Log.i(TAG,"URL: " + url);
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
            return resultList;
        } catch (IOException e) {
            Log.e(TAG, "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {

            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList<String>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                //Log.i(TAG,predsJsonArray.getJSONObject(i).getString("description"));
                //Log.i(TAG,"============================================================");
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Cannot process JSON results", e);
        }

        return resultList;
    }

    class GooglePlacesAutocompleteAdapter extends ArrayAdapter<String> implements Filterable {
        private ArrayList<String> resultList;

        public GooglePlacesAutocompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public String getItem(int index) {
            try{
                return resultList.get(index);
            }catch (IndexOutOfBoundsException ignore){
                return "";
            }
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        // Retrieve the autocomplete results.
                        resultList = autocomplete(constraint.toString());
                        Log.i(TAG,"resultList: "+resultList);

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
            return filter;
        }
    }
    ////////////// END AUTOCOMPLETE GOOGLE PLACE /////////////////

}