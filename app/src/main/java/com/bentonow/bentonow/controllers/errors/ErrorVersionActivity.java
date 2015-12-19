package com.bentonow.bentonow.controllers.errors;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.controllers.BaseFragmentActivity;

public class ErrorVersionActivity extends BaseFragmentActivity {

    static final String TAG = "ErrorVersionActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_needed);

        DebugUtils.logDebug(TAG, "onCreate");
    }

    public void onUpdatePressed(View view) {
        String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
        try {
            Intent mIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName));
            if (mIntent.resolveActivity(getPackageManager()) != null)
                startActivityForResult(mIntent, 0);
            else
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        } catch (Exception anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }
}
