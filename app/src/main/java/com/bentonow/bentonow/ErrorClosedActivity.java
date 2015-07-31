package com.bentonow.bentonow;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.bentonow.bentonow.Utils.Email;
import com.bentonow.bentonow.model.Ioscopy;
import com.bentonow.bentonow.model.Shop;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class ErrorClosedActivity extends BaseActivity {

    private static final String TAG = "ErrorClosedActivity";
    private AQuery aq;
    View overlay;
    TextView email_address;
    private TextView btnNextDayMenu;
    private String jsonToSend;
    private String titleNextDayMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_closed);
        aq = new AQuery(this);
        Bentonow.app.current_activity = this;

        initActionbar();
        initElements();
        addListeners();
        initLegalFooter();

        BentoApplication.status = "closed";
    }

    @Override
    protected void onResume() {
        super.onResume();

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY) * 100 + calendar.get(Calendar.MINUTE);

        if (hour >= 2000) {
            ((TextView)findViewById(R.id.textView2)).setText(Ioscopy.getKeyValue("closed-text-latenight"));
        } else {
            ((TextView)findViewById(R.id.textView2)).setText(Ioscopy.getKeyValue("closed-text"));
        }

        showNextMenu();
    }

    private void postData() {
        String uri = getResources().getString(R.string.server_api_url) + Config.API.COUPON_REQUEST;
        String email = email_address.getText().toString();
        if( email.isEmpty() || !Email.isEmailValid(email)){
            Toast.makeText(getApplicationContext(), "Invalid email address.", Toast.LENGTH_LONG).show();
        }else {

            Map<String, Object> params = new HashMap<>();
            params.put("data", "{\n" +
                    "    \"reason\": \"closed\",\n" +
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

    private void showNextMenu() {
        try {
            Log.i(TAG, "json: " + Shop.getNextMenu().toString());
            JSONObject meals = Shop.getNextMenu();

            JSONObject menu = meals.getJSONObject("Menu");

            DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            String day = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(format.parse(menu.getString("for_date")));

            titleNextDayMenu = day + "'s " + Shop.getNextMenuType() + " Menu";
            btnNextDayMenu.setText("See " + titleNextDayMenu);
            btnNextDayMenu.setVisibility(View.VISIBLE);
            jsonToSend = meals.toString();
            JSONArray MenuItems = meals.getJSONArray("MenuItems");
            preloadImages(MenuItems);
        } catch (Exception e) {
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
