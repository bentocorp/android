package com.bentonow.bentonow.controllers.errors;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.Email;
import com.bentonow.bentonow.Utils.MixpanelUtils;
import com.bentonow.bentonow.controllers.BaseFragmentActivity;
import com.bentonow.bentonow.controllers.dialog.ConfirmationDialog;
import com.bentonow.bentonow.model.Order;
import com.bentonow.bentonow.model.Settings;
import com.bentonow.bentonow.ui.BackendEditText;
import com.bentonow.bentonow.web.request.UserRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;


public class BummerActivity extends BaseFragmentActivity implements View.OnClickListener {

    public static final String TAG = "BummerActivity";
    public static final String TAG_INVALID_ADDRESS = "invalid_address";

    EditText txt_email;
    private TextView txtAddress;
    GoogleMap map;

    String sCurrentLocation = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_bummer);

        initActionbar();

        sCurrentLocation = getIntent().getExtras().getString(TAG_INVALID_ADDRESS);

        txt_email = (BackendEditText) findViewById(R.id.txt_email);

        if (sCurrentLocation == null || sCurrentLocation.isEmpty())
            sCurrentLocation = Order.getFullAddress();

        getTxtAddress().setText(sCurrentLocation);

        try {
            JSONObject params = new JSONObject();
            params.put("Address", Order.getFullAddress());
            MixpanelUtils.track("Selected Address Outside of Service Area", params);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onChangePressed(View view) {
        onBackPressed();
    }

    public void onSubmitPressed(View view) {
        if (!Email.isValid(txt_email.getText().toString())) {
            ConfirmationDialog mDialog = new ConfirmationDialog(BummerActivity.this, "Error", "Invalid email address.");
            mDialog.addAcceptButton("OK", null);
            mDialog.show();
        } else {
            UserRequest.requestCoupon(txt_email.getText().toString(), "outside of delivery zone", new TextHttpResponseHandler() {
                @SuppressWarnings("deprecation")
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    DebugUtils.logError(TAG, responseString);
                    ConfirmationDialog mDialog = new ConfirmationDialog(BummerActivity.this, "Error", "We having issues connecting to the server, please try later.");
                    mDialog.addAcceptButton("OK", null);
                    mDialog.show();
                }

                @SuppressWarnings("deprecation")
                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    DebugUtils.logDebug(TAG, responseString);
                    txt_email.setText("");
                    ConfirmationDialog mDialog = new ConfirmationDialog(BummerActivity.this, null, "Thanks! We'll let you know when we're in your area.");
                    mDialog.addAcceptButton("OK", null);
                    mDialog.show();
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        onBackPressed();
    }

    private void initActionbar() {
        TextView actionbar_title = (TextView) findViewById(R.id.actionbar_title);
        actionbar_title.setText(getResources().getString(R.string.delivery_location_actionbar_title));
        //
        ImageView actionbar_left_btn = (ImageView) findViewById(R.id.actionbar_left_btn);
        actionbar_left_btn.setImageResource(R.drawable.ic_ab_back);
        actionbar_left_btn.setOnClickListener(BummerActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupMap();
    }

    private void setupMap() {
        if (map != null) return;
        map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

        if (map == null) return;

        float zoom = 11f;

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.772492, -122.420262), zoom));
        PolygonOptions rectOptions = new PolygonOptions();
        for (String aServiceArea_dinner : Settings.getServiceArea()) {
            String[] loc = aServiceArea_dinner.split(",");
            double lat = Double.valueOf(loc[1]);
            double lng = Double.valueOf(loc[0]);
            rectOptions.add(new LatLng(lat, lng));
        }
        rectOptions.fillColor(getResources().getColor(R.color.blue_20));
        rectOptions.strokeWidth(5);
        rectOptions.strokeColor(getResources().getColor(R.color.blue));

        map.addPolygon(rectOptions);
    }


    private TextView getTxtAddress() {
        if (txtAddress == null)
            txtAddress = (TextView) findViewById(R.id.txt_address);
        return txtAddress;
    }
}
