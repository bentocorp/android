package com.bentonow.bentonow;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.bentonow.bentonow.Utils.Email;
import com.google.android.gms.identity.intents.AddressConstants;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ErrorInvalidAddressActivity extends BaseFragmentActivity {

    private static final String TAG = "ErrorInvalidAddressActivity";
    private String invalid_address;
    private TextView btn_change;
    private TextView btn_submit;
    private EditText email_address;
    private RelativeLayout overlay;
    private TextView btn_ok;
    private AQuery aq;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_invalid_address);

        aq = new AQuery(this);

        Intent intent = getIntent();
        invalid_address = intent.getStringExtra(Config.invalid_address_extra_label);

        initActionbar();
        initElements();
        showAddress();
        addListeners();
    }

    private void showAddress() {
        TextView invalid_address_textview = (TextView) findViewById(R.id.invalid_address_textview);
        invalid_address_textview.setText(invalid_address);
    }

    private void initElements() {
        btn_change = (TextView) findViewById(R.id.btn_change);
        email_address = (EditText)findViewById(R.id.email_address);
        btn_submit = (TextView) findViewById(R.id.btn_submit);
        overlay = (RelativeLayout)findViewById(R.id.overlay);
        btn_ok = (TextView)findViewById(R.id.btn_ok);
    }

    private void addListeners() {

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overlay.setVisibility(View.GONE);
            }
        });

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!email_address.getText().toString().isEmpty()) {
                    sendEmail();
                }
            }
        });

        btn_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransitionGoLeft();
            }
        });
    }

    private void initActionbar() {
        TextView actionbar_title = (TextView) findViewById(R.id.actionbar_title);
        actionbar_title.setText(getResources().getString(R.string.delivery_location_actionbar_title));
        //
        ImageView actionbar_left_btn = (ImageView) findViewById(R.id.actionbar_left_btn);
        actionbar_left_btn.setImageResource(R.drawable.ic_ab_back);
        actionbar_left_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransitionGoLeft();
            }
        });
    }

    private void sendEmail(){
        String uri = Config.API.URL + Config.API.COUPON_REQUEST;
        String email = email_address.getText().toString();
        if( email.isEmpty() || !Email.isEmailValid(email)){
            Toast.makeText(getApplicationContext(), "Invalid email address.", Toast.LENGTH_LONG).show();
        }else {

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("data", "{\n" +
                    "    \"reason\": \"Out of area\",\n" +
                    "    \"email\": \"" + email + "\"\n" +
                    "}");
            aq.ajax(uri, params, String.class, new AjaxCallback<String>() {
                @Override
                public void callback(String url, String json, AjaxStatus status) {
                    Log.i("PaymentActivity", "JSONObject: " + json);
                    Log.i("PaymentActivity", "AjaxStatus: " + status.getCode());

                    overlay.setVisibility(View.VISIBLE);
                    email_address.setText("");
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransitionGoLeft();
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

            ///////////////////////////
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }



    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.772492, -122.420262), 11.0f));
        PolygonOptions rectOptions = new PolygonOptions();
        String[] serviceArea_dinner = Config.serviceArea_dinner.split(" ");
        for (String aServiceArea_dinner : serviceArea_dinner) {
            String[] loc = aServiceArea_dinner.split(",");
            double lat = Double.valueOf(loc[1]);
            double lng = Double.valueOf(loc[0]);
            rectOptions.add(new LatLng(lat, lng));
        }
        rectOptions.fillColor(getResources().getColor(R.color.btn_green_trans));
        rectOptions.strokeWidth(5);
        rectOptions.strokeColor(getResources().getColor(R.color.btn_green));

        Polygon polygon = mMap.addPolygon(rectOptions);
        /*mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                //List<LatLng> sfpolygon = new ArrayList<LatLng>();

            }
        });*/
    }

}
