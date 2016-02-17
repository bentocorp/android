package com.bentonow.bentonow.controllers.init;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.ConstantUtils;
import com.bentonow.bentonow.Utils.GoogleAnalyticsUtil;
import com.bentonow.bentonow.Utils.MixpanelUtils;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.controllers.BaseFragmentActivity;
import com.bentonow.bentonow.controllers.geolocation.DeliveryLocationActivity;
import com.bentonow.bentonow.dao.IosCopyDao;
import com.bentonow.bentonow.ui.AutoFitTxtView;


public class GettingStartedActivity extends BaseFragmentActivity {

    private static final String TAG = "GettingStartedActivity";

    private AutoFitTxtView txtTitle;

    private String sPrice = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_getting_started);

        String title = IosCopyDao.get("about-item-0");

        getTxtTitle().setText(title);
    }

    @Override
    protected void onResume() {
        GoogleAnalyticsUtil.sendScreenView("Getting Started");

        super.onResume();
    }

    @Override
    protected void onDestroy() {
        MixpanelUtils.track("Viewed Intro Screen");
        super.onDestroy();
    }

    public void onGettingStartedPressed(View view) {
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.APP_FIRST_RUN, true);
        Intent intent = new Intent(this, DeliveryLocationActivity.class);
        intent.putExtra(ConstantUtils.TAG_OPEN_SCREEN, ConstantUtils.optOpenScreen.BUILD_BENTO);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        finish();
        startActivity(intent);
    }

    private AutoFitTxtView getTxtTitle() {
        if (txtTitle == null)
            txtTitle = (AutoFitTxtView) findViewById(R.id.txt_title);
        return txtTitle;
    }
}
