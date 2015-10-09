package com.bentonow.bentonow.controllers.init;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.controllers.BaseFragmentActivity;
import com.bentonow.bentonow.model.BackendText;
import com.bentonow.bentonow.model.Settings;


public class GettingStartedActivity extends BaseFragmentActivity {

    private static final String TAG = "GettingStartedActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_getting_started);

        String price = String.valueOf(Settings.tax_percent);
        String title = BackendText.get("about-item-0").replace("$X", "$" + price + "!");

        Log.i(TAG, "price: " + price);
        Log.i(TAG, "title: " + title);

        ((TextView) findViewById(R.id.txt_title)).setText(title);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.STORE_STATUS, "main");
    }

    public void onGettingStartedPressed(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("checkLocation", true);
        startActivity(intent);
        finish();
    }
}
