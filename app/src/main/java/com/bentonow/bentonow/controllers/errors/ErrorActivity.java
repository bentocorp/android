package com.bentonow.bentonow.controllers.errors;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.AndroidUtil;
import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.ConstantUtils;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.GoogleAnalyticsUtil;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.controllers.BaseFragmentActivity;
import com.bentonow.bentonow.controllers.dialog.ConfirmationDialog;
import com.bentonow.bentonow.controllers.help.HelpActivity;
import com.bentonow.bentonow.dao.IosCopyDao;
import com.bentonow.bentonow.dao.MenuDao;
import com.bentonow.bentonow.listener.InterfaceCustomerService;
import com.bentonow.bentonow.model.Menu;
import com.bentonow.bentonow.service.BentoCustomerService;
import com.bentonow.bentonow.ui.BackendTextView;
import com.bentonow.bentonow.web.request.UserRequest;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class ErrorActivity extends BaseFragmentActivity implements View.OnClickListener, InterfaceCustomerService {
    private static final String TAG = "ErrorActivity";
    public static boolean bIsOpen;
    String sStatus;
    private TextView txt_title;
    private TextView txt_description;
    private EditText edit_txt_email;
    private Button btn_next_day_menu;
    private Button btnSubmitEmail;
    private BackendTextView btnPolicy;
    private BackendTextView btnTermsConditions;
    private ImageView actionbar_left_btn;
    private ImageView actionbar_right_btn;
    private Menu mCurrentMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);

        getMenuItemInfo().setImageResource(R.drawable.ic_ab_help);
        getMenuItemProfile().setImageResource(R.drawable.ic_signup_profile);

        getMenuItemInfo().setOnClickListener(ErrorActivity.this);
        getMenuItemProfile().setOnClickListener(ErrorActivity.this);
        getBtnNextDayMenu().setOnClickListener(ErrorActivity.this);
        getBtnSubmitEmail().setOnClickListener(ErrorActivity.this);
        getBtnPolicy().setOnClickListener(ErrorActivity.this);
        getBtnTermsConditions().setOnClickListener(ErrorActivity.this);

        DebugUtils.logDebug(TAG, "Create: ");
    }

    @Override
    protected void onResume() {
        bIsOpen = true;

        mCurrentMenu = MenuDao.getNextMenu();

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY) * 100 + calendar.get(Calendar.MINUTE);

        if (MenuDao.gateKeeper.getAppOnDemandWidget() != null)
            sStatus = MenuDao.gateKeeper.getAppOnDemandWidget().getState();
        else
            sStatus = "closed";

        if (sStatus.equals("sold out")) {
            getTxtTitle().setText(IosCopyDao.get("sold-out-title"));
            getTxtDescription().setText(IosCopyDao.get("sold-out-text"));
            GoogleAnalyticsUtil.sendScreenView("Sold Out");
        } else {
            GoogleAnalyticsUtil.sendScreenView("Closed");
            getTxtTitle().setText(IosCopyDao.get("closed-title"));
            if (hour >= 2000) {
                getTxtDescription().setText(IosCopyDao.get("closed-text-latenight"));
            } else {
                getTxtDescription().setText(IosCopyDao.get("closed-text"));
            }

        }
        setupNextMenu();


        super.onResume();
    }

    void setupNextMenu() {

        if (mCurrentMenu != null) {
            DebugUtils.logDebug(TAG, "menu: " + mCurrentMenu.toString());
            getBtnNextDayMenu().setVisibility(View.VISIBLE);

            DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

            if (mCurrentMenu.for_date.replace("-", "").equals(BentoNowUtils.getTodayDate())) {
                getBtnNextDayMenu().setText(IosCopyDao.get("closed-sneak-preview-button"));
            } else {
                try {
                    String day = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(format.parse(mCurrentMenu.for_date));
                    String title = day + "'s " + mCurrentMenu.meal_name.substring(0, 1).toUpperCase() + mCurrentMenu.meal_name.substring(1);
                    getBtnNextDayMenu().setText(title);
                } catch (Exception e) {
                    DebugUtils.logError(TAG, "setupNextMenu: " + e.getLocalizedMessage());
                    getBtnNextDayMenu().setVisibility(View.INVISIBLE);
                }
            }

        } else {
            DebugUtils.logDebug(TAG, "setupNextMenu: " + "menu: null");
            getBtnNextDayMenu().setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ENABLE_ERROR_CLICK, hasFocus);
        DebugUtils.logDebug(TAG, "onWindowFocusChanged: " + hasFocus);
    }

    @Override
    public void onClick(View v) {
        if (SharedPreferencesUtil.getBooleanPreference(SharedPreferencesUtil.ENABLE_ERROR_CLICK)) {
            SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ENABLE_ERROR_CLICK, false);
            switch (v.getId()) {
                case R.id.actionbar_right_btn:
                    Intent intent = new Intent(ErrorActivity.this, HelpActivity.class);
                    intent.putExtra("faq", true);
                    startActivity(intent);
                    break;
                case R.id.actionbar_left_btn:
                    BentoNowUtils.openSettingsActivity(ErrorActivity.this);
                    break;
                case R.id.btn_next_day_menu:
                    onNextDayMenuPressed();
                    break;
                case R.id.btn_submit_email:
                    onSubmitPressed();
                    break;
                case R.id.btn_policy:
                    onPrivacyPolicyPressed();
                    break;
                case R.id.btn_terms_conditions:
                    onTermAndConditionsPressed();
                    break;
            }
        }
    }

    public void onSubmitPressed() {
        if (!AndroidUtil.isEmailValid(getEditTxtEmail().getText().toString())) {
            ConfirmationDialog mDialog = new ConfirmationDialog(ErrorActivity.this, "Error", "Invalid email address.");
            mDialog.addAcceptButton("OK", null);
            mDialog.show();
        } else {
            UserRequest.requestCoupon(getEditTxtEmail().getText().toString(), sStatus, new TextHttpResponseHandler() {
                @SuppressWarnings("deprecation")
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    DebugUtils.logError(TAG, responseString);
                    ConfirmationDialog mDialog = new ConfirmationDialog(ErrorActivity.this, "Error", "We having issues connecting to the server, please try later.");
                    mDialog.addAcceptButton("OK", null);
                    mDialog.show();
                }

                @SuppressWarnings("deprecation")
                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    String message = sStatus.equals("sold out") ? IosCopyDao.get("sold-out-confirmation-text") : IosCopyDao.get("closed-confirmation-text");
                    DebugUtils.logDebug(TAG, responseString);
                    getEditTxtEmail().setText("");

                    ConfirmationDialog mDialog = new ConfirmationDialog(ErrorActivity.this, null, message);
                    mDialog.addAcceptButton("OK", null);
                    mDialog.show();

                }

                @Override
                public void onFinish() {
                    super.onFinish();
                }
            });
        }
    }

    public void onNextDayMenuPressed() {
        if (MenuDao.getNextMenu() == null) {
            SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ENABLE_ERROR_CLICK, true);
            return;
        }

        Intent intent = new Intent(ErrorActivity.this, NextDayMenuActivity.class);
        intent.putExtra("title", getBtnNextDayMenu().getText().toString());
        startActivity(intent);
    }

    public void onPrivacyPolicyPressed() {
        Intent intent = new Intent(ErrorActivity.this, HelpActivity.class);
        intent.putExtra("privacy", true);
        startActivity(intent);
    }

    public void onTermAndConditionsPressed() {
        Intent intent = new Intent(ErrorActivity.this, HelpActivity.class);
        intent.putExtra("tos", true);
        startActivity(intent);
    }


    @Override
    public void onBackPressed() {
        BentoNowUtils.goToDashboard(ErrorActivity.this);
    }

    @Override
    public void onMapNoService() {
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.STORE_STATUS, MenuDao.gateKeeper.getAppState());
        finish();
        BentoNowUtils.openDeliveryLocationScreen(ErrorActivity.this, ConstantUtils.optOpenScreen.BUILD_BENTO);
    }

    @Override
    public void onBuild() {
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.STORE_STATUS, MenuDao.gateKeeper.getAppState());
        finish();
        BentoNowUtils.openBuildBentoActivity(ErrorActivity.this);
    }

    @Override
    public void onClosedWall() {
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.STORE_STATUS, MenuDao.gateKeeper.getAppState());
    }

    @Override
    public void onSold() {
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.STORE_STATUS, MenuDao.gateKeeper.getAppState());
    }

    @Override
    public void onConnectService() {
        DebugUtils.logDebug(TAG, "Service Connected");
        mBentoService.setServiceListener(ErrorActivity.this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, BentoCustomerService.class);
        //   bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        if (sStatus.equals("sold out"))
            trackViewedScreen("Viewed Sold-out Screen");
        else
            trackViewedScreen("Viewed Closed Screen");
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        bIsOpen = false;

        if (mBound) {
            mBentoService.setServiceListener(null);
            unbindService(mConnection);
            mBound = false;
        }
    }

    private TextView getTxtTitle() {
        if (txt_title == null)
            txt_title = (TextView) findViewById(R.id.txt_title);

        return txt_title;
    }

    private TextView getTxtDescription() {
        if (txt_description == null)
            txt_description = (TextView) findViewById(R.id.txt_description);

        return txt_description;
    }

    private Button getBtnNextDayMenu() {
        if (btn_next_day_menu == null)
            btn_next_day_menu = (Button) findViewById(R.id.btn_next_day_menu);

        return btn_next_day_menu;
    }

    private Button getBtnSubmitEmail() {
        if (btnSubmitEmail == null)
            btnSubmitEmail = (Button) findViewById(R.id.btn_submit_email);
        return btnSubmitEmail;
    }

    private BackendTextView getBtnPolicy() {
        if (btnPolicy == null)
            btnPolicy = (BackendTextView) findViewById(R.id.btn_policy);
        return btnPolicy;
    }

    private BackendTextView getBtnTermsConditions() {
        if (btnTermsConditions == null)
            btnTermsConditions = (BackendTextView) findViewById(R.id.btn_terms_conditions);
        return btnTermsConditions;
    }

    private ImageView getMenuItemInfo() {
        if (actionbar_right_btn == null)
            actionbar_right_btn = (ImageView) findViewById(R.id.actionbar_right_btn);

        return actionbar_right_btn;
    }

    private ImageView getMenuItemProfile() {
        if (actionbar_left_btn == null)
            actionbar_left_btn = (ImageView) findViewById(R.id.actionbar_left_btn);

        return actionbar_left_btn;
    }

    private EditText getEditTxtEmail() {
        if (edit_txt_email == null)
            edit_txt_email = (EditText) findViewById(R.id.txt_email);

        return edit_txt_email;
    }

}
