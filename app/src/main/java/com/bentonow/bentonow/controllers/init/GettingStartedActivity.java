package com.bentonow.bentonow.controllers.init;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.controllers.BaseFragmentActivity;
import com.bentonow.bentonow.model.BackendText;
import com.bentonow.bentonow.model.Settings;
import com.bentonow.bentonow.ui.AutoFitTextView;


public class GettingStartedActivity extends BaseFragmentActivity {

    private static final String TAG = "GettingStartedActivity";

    private AutoFitTextView txtTitle;

    private String sPrice = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_getting_started);

        double bPrice = Settings.price;
        double bSalePrice = Settings.sale_price;

        if (bSalePrice < bPrice)
            sPrice = BentoNowUtils.getNumberFromPrice(bPrice);
        else
            sPrice = BentoNowUtils.getNumberFromPrice(bSalePrice);

        String title = BackendText.get("about-item-0").replace("$X", "$" + sPrice + "!");

        getTxtTitle().setText(title);
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

    private AutoFitTextView getTxtTitle() {
        if (txtTitle == null)
            txtTitle = (AutoFitTextView) findViewById(R.id.txt_title);
        return txtTitle;
    }
}
