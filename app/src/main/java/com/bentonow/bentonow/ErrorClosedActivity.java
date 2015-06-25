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

import java.util.Calendar;
import java.util.HashMap;
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
        setClosedText();

        if(Config.next_day_json!=null){
            try {
                processJson(new JSONObject(Config.next_day_json));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void setClosedText () {
        TextView textView = (TextView) findViewById(R.id.textView2);

        //String weekend = "Have a great weekend! We're back on Monday at 5pm with more deliciousness.";
        String weekend = "Have a great weekend! We're back on Monday with more deliciousness. Lunch: 11am-2pm, Dinner: 5pm-10pm";
        //String weekdays = "That's it for tonight! We're back tomorrow at 5pm...oh yeah!";
        String weekdays = "That's it for tonight! We're back tomorrow...oh yeah! Lunch: 11am-2pm, Dinner: 5pm-10pm";
        //String _else = "Bento opens at 5pm today! We're cookin' up some delicious dinner. Get excited!";
        String _else = "We're cookin' up something really delicious today. Get excited! Lunch: 11am-2pm, Dinner: 5pm-10pm";

        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if ((day == Calendar.FRIDAY && hour >= 20) || day == Calendar.SATURDAY || day == Calendar.SUNDAY) {
            textView.setText(weekend);
        } else if ((Calendar.MONDAY == day || Calendar.TUESDAY == day || Calendar.WEDNESDAY == day || Calendar.THURSDAY == day) && hour >= 20) {
            textView.setText(weekdays);
        } else {
            textView.setText(_else);
        }
    }

    private void postData() {
        String uri = getResources().getString(R.string.server_api_url) + Config.API.COUPON_REQUEST;
        String email = email_address.getText().toString();
        if( email.isEmpty() || !Email.isEmailValid(email)){
            Toast.makeText(getApplicationContext(), "Invalid email address.", Toast.LENGTH_LONG).show();
        }else {

            Map<String, Object> params = new HashMap<String, Object>();
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

    private void processJson(JSONObject json) {
        try {
            Log.i(TAG, "json: " + json.toString());
            JSONObject menus = json.getJSONObject("menus");

            JSONObject meals;
            if (getCurrentHourInt() < 1430) {
                meals = menus.getJSONObject("lunch");
            }else{
                meals = menus.getJSONObject("dinner");
            }

            JSONObject menu = meals.getJSONObject("Menu");
            String meal_name = menu.getString("meal_name");
            titleNextDayMenu = meal_name;
            btnNextDayMenu.setText(meal_name);
            btnNextDayMenu.setVisibility(View.VISIBLE);
            jsonToSend = meals.toString();
            JSONArray MenuItems = meals.getJSONArray("MenuItems");
            preloadImages(MenuItems);
        } catch (JSONException e) {
            //Log.e(TAG, status.getError());
            e.printStackTrace();
        }
    }

    private void preloadImages(JSONArray menuItems) {
        LinearLayout list = (LinearLayout)findViewById(R.id.tomorrow_main_dishes_container);
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
