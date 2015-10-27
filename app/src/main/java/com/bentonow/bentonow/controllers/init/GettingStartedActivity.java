package com.bentonow.bentonow.controllers.init;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.controllers.BaseFragmentActivity;
import com.bentonow.bentonow.model.BackendText;
import com.bentonow.bentonow.ui.AutoFitTxtView;


public class GettingStartedActivity extends BaseFragmentActivity {

    private static final String TAG = "GettingStartedActivity";

    private AutoFitTxtView txtTitle;

    private String sPrice = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_getting_started);

        sPrice = BentoNowUtils.getDefaultPriceBento(0);

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

    private AutoFitTxtView getTxtTitle() {
        if (txtTitle == null)
            txtTitle = (AutoFitTxtView) findViewById(R.id.txt_title);
        return txtTitle;
    }
}
