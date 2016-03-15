package com.bentonow.bentonow.controllers.init;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.ConstantUtils;
import com.bentonow.bentonow.Utils.GoogleAnalyticsUtil;
import com.bentonow.bentonow.Utils.MixpanelUtils;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.controllers.BaseFragmentActivity;
import com.bentonow.bentonow.controllers.geolocation.DeliveryLocationActivity;
import com.bentonow.bentonow.dao.IosCopyDao;
import com.bentonow.bentonow.ui.AutoFitTxtView;
import com.bentonow.bentonow.ui.BackendAutoFitTextView;
import com.bentonow.bentonow.ui.BackendButton;


public class GettingStartedActivity extends BaseFragmentActivity implements View.OnClickListener {

    private static final String TAG = "GettingStartedActivity";

    private LinearLayout layoutStartedSteps;
    private RelativeLayout layoutStartedNotification;
    private AutoFitTxtView txtTitle;
    private BackendButton btnGetStarted;
    private BackendAutoFitTextView txtNotificationDescription;
    private Button btnNotificationNo;
    private Button btnNotificationYes;

    private boolean bIsDailyNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_getting_started);

        String title = IosCopyDao.get("about-item-0");

        getLayoutStartedSteps().setOnClickListener(this);
        getLayoutStartedNotification().setOnClickListener(this);
        getBtnGetStarted().setOnClickListener(this);
        getBtnNotificationNo().setOnClickListener(this);
        getBtnNotificationYes().setOnClickListener(this);


        getTxtTitle().setText(title);
    }

    public void onGettingStartedPressed() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getLayoutStartedSteps().setVisibility(View.GONE);
                getLayoutStartedNotification().setVisibility(View.VISIBLE);
            }
        });
    }

    private void showDailyNotification() {
        getTxtNotificationDescription().setText(IosCopyDao.get("daily_reminder_question"));
        getTxtNotificationDescription().startAnimation(AnimationUtils.loadAnimation(GettingStartedActivity.this, R.anim.fade_in_text_view));
    }

    private void openNextScreen() {
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.APP_FIRST_RUN, true);
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ALREADY_SHOW_NOTIFICATIONS, true);
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ALREADY_SHOW_DAILY_NOTIFICATIONS, true);
        Intent intent = new Intent(this, DeliveryLocationActivity.class);
        intent.putExtra(ConstantUtils.TAG_OPEN_SCREEN, ConstantUtils.optOpenScreen.BUILD_BENTO);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_get_started:
                onGettingStartedPressed();
                break;
            case R.id.btn_notification_no:
                if (!bIsDailyNotification) {
                    SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.SHOW_NOTIFICATIONS, false);
                    showDailyNotification();
                    bIsDailyNotification = true;
                } else {
                    SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.SHOW_DAILY_NOTIFICATIONS, false);
                    openNextScreen();
                }
                break;
            case R.id.btn_notification_yes:
                if (!bIsDailyNotification) {
                    SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.SHOW_NOTIFICATIONS, true);
                    showDailyNotification();
                    bIsDailyNotification = true;
                } else {
                    SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.SHOW_DAILY_NOTIFICATIONS, true);
                    openNextScreen();
                }
                break;
        }
    }

    private AutoFitTxtView getTxtTitle() {
        if (txtTitle == null)
            txtTitle = (AutoFitTxtView) findViewById(R.id.txt_title);
        return txtTitle;
    }

    private LinearLayout getLayoutStartedSteps() {
        if (layoutStartedSteps == null)
            layoutStartedSteps = (LinearLayout) findViewById(R.id.layout_started_steps);
        return layoutStartedSteps;
    }

    private RelativeLayout getLayoutStartedNotification() {
        if (layoutStartedNotification == null)
            layoutStartedNotification = (RelativeLayout) findViewById(R.id.layout_started_notification);
        return layoutStartedNotification;
    }

    private BackendButton getBtnGetStarted() {
        if (btnGetStarted == null)
            btnGetStarted = (BackendButton) findViewById(R.id.btn_get_started);
        return btnGetStarted;
    }

    private Button getBtnNotificationNo() {
        if (btnNotificationNo == null)
            btnNotificationNo = (Button) findViewById(R.id.btn_notification_no);
        return btnNotificationNo;
    }

    private Button getBtnNotificationYes() {
        if (btnNotificationYes == null)
            btnNotificationYes = (Button) findViewById(R.id.btn_notification_yes);
        return btnNotificationYes;
    }

    private BackendAutoFitTextView getTxtNotificationDescription() {
        if (txtNotificationDescription == null)
            txtNotificationDescription = (BackendAutoFitTextView) findViewById(R.id.txt_notification_description);
        return txtNotificationDescription;
    }

}
