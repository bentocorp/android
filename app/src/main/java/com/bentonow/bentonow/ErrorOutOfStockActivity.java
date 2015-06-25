package com.bentonow.bentonow;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.bentonow.bentonow.Utils.Email;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class ErrorOutOfStockActivity extends BaseActivity {

    private static final String TAG = "ErrorOutOfStockActivity";
    private AQuery aq;
    View overlay;
    TextView email_address;
    private TextView btnNextDayMenu;
    private String jsonToSend;
    private String titleNextDayMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_out_of_stock);
        aq = new AQuery(this);
        Bentonow.app.current_activity = this;

        initActionbar();
        initElements();
        addListeners();
        initLegalFooter();

        if(Config.next_day_json!=null){
            try {
                processJson(new JSONObject(Config.next_day_json));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void postData() {
        String uri = getResources().getString(R.string.server_api_url) + Config.API.COUPON_REQUEST;
        String email = email_address.getText().toString();

        if( email.isEmpty() || !Email.isEmailValid(email)){
            Toast.makeText(getApplicationContext(),"Invalid email address.",Toast.LENGTH_LONG).show();
        }else {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("data", "{\n" +
                    "    \"reason\": \"OUT OF STOCK\",\n" +
                    "    \"email\": \"" + email + "\"\n" +
                    "}");
            aq.ajax(uri, params, String.class, new AjaxCallback<String>() {
                @Override
                public void callback(String url, String json, AjaxStatus status) {
                    overlay.setVisibility(View.VISIBLE);
                    email_address.setText("");
                }
            });
        }
    }

    private void initElements() {
        btnNextDayMenu = (TextView)findViewById(R.id.btnNextDayMenu);
        overlay = findViewById(R.id.overlay);
        email_address = (TextView)findViewById(R.id.email_address);
    }

    private void addListeners() {
        btnNextDayMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),NextDayMenuActivity.class);
                intent.putExtra("title", titleNextDayMenu);
                intent.putExtra("json", jsonToSend);
                startActivity(intent);
                overridePendingTransitionGoRight();
            }
        });

        findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overlay.setVisibility(View.GONE);
            }
        });

        findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postData();
            }
        });
    }

    private void initActionbar() {
        ImageView actionbar_right_btn = (ImageView) findViewById(R.id.actionbar_right_btn);
        actionbar_right_btn.setImageResource(R.drawable.ic_ab_help);
        actionbar_right_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToFAQ();
            }
        });
    }

    private void initLegalFooter() {
        findViewById(R.id.btn_privacy_policy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), PrivacyPolicyActivity.class);
                startActivity(intent);
                overridePendingTransitionGoRight();
            }
        });

        findViewById(R.id.btn_terms_conditions).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TermAndConditionsActivity.class);
                startActivity(intent);
                overridePendingTransitionGoRight();
            }
        });
    }

    private void processJson(JSONObject json) {
        try {
            Log.i(TAG, "json: " + json.toString());
            JSONObject menus = json.getJSONObject("menus");
            JSONObject dinner = menus.getJSONObject("dinner");
            JSONObject menu = dinner.getJSONObject("Menu");
            String day_text = menu.getString("day_text");
            String[] tmp = day_text.split(" ");
            String day = (tmp[0] != null) ? tmp[0] + "'s Menu" : "";
            Log.i(TAG, "menu.getString(day_text): " + day);
            if (!day.isEmpty()) {
                titleNextDayMenu = day;
                btnNextDayMenu.setText(day);
                btnNextDayMenu.setVisibility(View.VISIBLE);
            }
            jsonToSend = dinner.toString();
            JSONArray MenuItems = dinner.getJSONArray("MenuItems");
            preloadImages(MenuItems);
        } catch (JSONException e) {
            //Log.e(TAG, status.getError());
            e.printStackTrace();
        }
    }

    private void preloadImages(JSONArray menuItems) {
        for( int i = 0; i < menuItems.length(); i++ ){
            try {
                JSONObject row = menuItems.getJSONObject(i);
                if ( row.getString(Config.DISH.IMAGE1)!=null && !row.getString(Config.DISH.IMAGE1).isEmpty() ) {
                    Picasso.with(getApplicationContext())
                            .load(row.getString(Config.DISH.IMAGE1))
                            .fetch();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}