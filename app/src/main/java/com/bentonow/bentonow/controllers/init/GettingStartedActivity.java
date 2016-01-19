package com.bentonow.bentonow.controllers.init;

import android.os.Bundle;
import android.view.View;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.MixpanelUtils;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.controllers.BaseFragmentActivity;
import com.bentonow.bentonow.dao.IosCopyDao;
import com.bentonow.bentonow.dao.SettingsDao;
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

        String title = IosCopyDao.get("about-item-0").replace("$X", "$" + sPrice + "!");

        MixpanelUtils.track("App Installed");


        getTxtTitle().setText(title);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        MixpanelUtils.track("Viewed Intro Screen");
        super.onDestroy();
    }

    public void onGettingStartedPressed(View view) {
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.APP_FIRST_RUN, true);
        if (!SettingsDao.getCurrent().status.equals("open")) {
            finish();
            BentoNowUtils.openErrorActivity(this);
        } else {
            finish();
            BentoNowUtils.openBuildBentoActivity(this);
        }
    }

    private AutoFitTxtView getTxtTitle() {
        if (txtTitle == null)
            txtTitle = (AutoFitTxtView) findViewById(R.id.txt_title);
        return txtTitle;
    }
}
