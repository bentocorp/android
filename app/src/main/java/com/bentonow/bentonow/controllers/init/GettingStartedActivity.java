package com.bentonow.bentonow.controllers.init;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.controllers.BaseFragmentActivity;
import com.bentonow.bentonow.model.BackendText;
import com.bentonow.bentonow.model.Settings;

import java.text.DecimalFormat;


public class GettingStartedActivity extends BaseFragmentActivity {

    private static final String TAG = "GettingStartedActivity";

    private String sPrice = "";
    private DecimalFormat df = new DecimalFormat("###.#");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_getting_started);

        double bPrice = Settings.price;
        double bSalePrice = Settings.sale_price;

        try {
            if (bSalePrice < bPrice)
                sPrice = df.format(bPrice);
            else
                sPrice = df.format(bSalePrice);
        } catch (Exception ex) {
            DebugUtils.logError(TAG, ex);
            sPrice = String.valueOf(Settings.price);
        }


        String title = BackendText.get("about-item-0").replace("$X", "$" + sPrice + "!");

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
