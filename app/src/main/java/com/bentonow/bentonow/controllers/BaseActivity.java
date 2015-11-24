package com.bentonow.bentonow.controllers;

import android.os.Bundle;

import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.model.Menu;
import com.bentonow.bentonow.model.Settings;

public class BaseActivity extends BaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onResume() {
        super.onResume();

        Menu mCurrentMenu = Menu.get();

        if (BentoNowUtils.isLastVersionApp(this)) {
            if (mCurrentMenu == null || !Settings.status.equals("open")) {
                DebugUtils.logDebug("Finish activity: " + Settings.status + mCurrentMenu);
                SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.STORE_STATUS, "closed");
                BentoNowUtils.openErrorActivity(this);
            }
        } else {
            finish();
        }


    }

    @Override
    protected void onPause() {
        super.onPause();

        if (!Settings.status.equals("open") || Menu.get() == null) {
            DebugUtils.logDebug("Finish activity: " + Settings.status + " or Menu is null");
            finish();
        }
    }

}
