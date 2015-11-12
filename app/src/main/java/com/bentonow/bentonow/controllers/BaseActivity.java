package com.bentonow.bentonow.controllers;

import android.os.Bundle;

import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.model.Menu;

public class BaseActivity extends BaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onResume() {
        super.onResume();

        Menu mCurrentMenu = Menu.get();

        if (mCurrentMenu == null || !SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.STORE_STATUS).equals("open")) {
            BentoNowUtils.openMainActivity(this);
        }
    }

}
