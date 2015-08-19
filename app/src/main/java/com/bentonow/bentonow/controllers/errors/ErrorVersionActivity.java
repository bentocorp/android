package com.bentonow.bentonow.controllers.errors;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.controllers.BaseActivity;

public class ErrorVersionActivity extends BaseActivity {

    static final String TAG = "ErrorVersionActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_needed);

        Log.i(TAG, "onCreate");
    }

    public void onUpdatePressed (View view) {
        String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }
}
