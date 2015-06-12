package com.bentonow.bentonow;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class ErrorInvalidAddressActivity extends BaseActivity {

    private String invalid_address;
    private TextView btn_change;
    private TextView btn_submit;
    private EditText email_address;
    private RelativeLayout overlay;
    private TextView btn_ok;
    private AQuery aq;

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
                if( !email_address.getText().toString().isEmpty() ){
                    sendEmail();
                }
            }
        });

        btn_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DeliveryLocationActivity.class);
                startActivity(intent);
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
                //startActivity(new Intent(getApplicationContext(), DeliveryLocationActivity.class));
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
}
