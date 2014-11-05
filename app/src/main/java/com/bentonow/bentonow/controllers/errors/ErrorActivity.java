package com.bentonow.bentonow.controllers.errors;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.Email;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.controllers.BaseFragmentActivity;
import com.bentonow.bentonow.controllers.dialog.ConfirmationDialog;
import com.bentonow.bentonow.controllers.help.HelpActivity;
import com.bentonow.bentonow.dao.UserDao;
import com.bentonow.bentonow.model.BackendText;
import com.bentonow.bentonow.model.Menu;
import com.bentonow.bentonow.model.Settings;
import com.bentonow.bentonow.model.User;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class ErrorActivity extends BaseFragmentActivity implements View.OnClickListener {
    private static final String TAG = "ErrorActivity";

    private TextView txt_title;
    private TextView txt_description;
    private EditText edit_txt_email;
    private Button btn_next_day_menu;
    private ImageView actionbar_left_btn;
    private ImageView actionbar_right_btn;

    public static boolean bIsOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);

        getMenuItemInfo().setImageResource(R.drawable.ic_ab_help);
        getMenuItemInfo().setOnClickListener(ErrorActivity.this);

        getMenuItemProfile().setImageResource(R.drawable.ic_signup_profile);
        getMenuItemProfile().setOnClickListener(ErrorActivity.this);

        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.IS_STORE_CHANGIN, false);
    }

    @Override
    protected void onResume() {
        bIsOpen = true;

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY) * 100 + calendar.get(Calendar.MINUTE);

        if (Settings.status.equals("sold out")) {
            getTxtTitle().setText(BackendText.get("sold-out-title"));
            getTxtDescription().setText(BackendText.get("sold-out-text"));
        } else {
            getTxtTitle().setText(BackendText.get("closed-title"));
            if (hour >= 2000) {
                getTxtDescription().setText(BackendText.get("closed-text-latenight"));
            } else {
                getTxtDescription().setText(BackendText.get("closed-text"));
            }

        }

        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.STORE_STATUS, Settings.status);

        setupNextMenu();

        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (SharedPreferencesUtil.getBooleanPreference(SharedPreferencesUtil.IS_STORE_CHANGIN))
            finish();
    }

    void setupNextMenu() {
        Menu menu = Menu.getNext();

        if (menu != null) {
            Log.i(TAG, "menu: " + menu.toString());
            getBtnNextDayMenu().setVisibility(View.VISIBLE);

            DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

            if (menu.for_date.replace("-", "").equals(BentoNowUtils.getTodayDate())) {
                getBtnNextDayMenu().setText(BackendText.get("closed-sneak-preview-button"));
            } else {
                try {
                    String day = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(format.parse(menu.for_date));
                    String title = day + "'s " + menu.meal_name.substring(0, 1).toUpperCase() + menu.meal_name.substring(1);
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.actionbar_right_btn:
                Intent intent = new Intent(ErrorActivity.this, HelpActivity.class);
                intent.putExtra("faq", true);
                startActivity(intent);
                break;
            case R.id.actionbar_left_btn:
                BentoNowUtils.openSettingsActivity(ErrorActivity.this);
                break;
        }
    }

    @Override
    protected void onStop() {
        bIsOpen = false;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bIsOpen = false;
    }


    @Override
    public void onBackPressed() {
        BentoNowUtils.goToDashboard(ErrorActivity.this);
    }

    public void onSubmitPressed(View view) {
        if (!Email.isValid(getEditTxtEmail().getText().toString())) {
            ConfirmationDialog mDialog = new ConfirmationDialog(ErrorActivity.this, "Error", "Invalid email address.");
            mDialog.addAcceptButton("OK", null);
            mDialog.show();
        } else {
            UserDao.requestCoupon(User.current, getEditTxtEmail().getText().toString(), Settings.status, new TextHttpResponseHandler() {
                @SuppressWarnings("deprecation")
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Log.e(TAG, responseString);
                    ConfirmationDialog mDialog = new ConfirmationDialog(ErrorActivity.this, "Error", "We having issues connecting to the server, please try later.");
                    mDialog.addAcceptButton("OK", null);
                    mDialog.show();
                }

                @SuppressWarnings("deprecation")
                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    String message = Settings.status.equals("sold out") ? BackendText.get("sold-out-confirmation-text") : BackendText.get("closed-confirmation-text");
                    Log.i(TAG, responseString);
                    getEditTxtEmail().setText("");

                    ConfirmationDialog mDialog = new ConfirmationDialog(ErrorActivity.this, null, message);
                    mDialog.addAcceptButton("OK", null);
                    mDialog.show();
                }
            });
        }
    }

    public void onNextDayMenuPressed(View view) {
        if (Menu.getNext() == null)
            return;

        Intent intent = new Intent(ErrorActivity.this, NextDayMenuActivity.class);
        intent.putExtra("title", getBtnNextDayMenu().getText().toString());
        startActivity(intent);
    }

    public void onPrivacyPolicyPressed(View view) {
        Intent intent = new Intent(ErrorActivity.this, HelpActivity.class);
        intent.putExtra("privacy", true);
        startActivity(intent);
    }

    public void onTermAndConditionsPressed(View view) {
        Intent intent = new Intent(ErrorActivity.this, HelpActivity.class);
        intent.putExtra("tos", true);
        startActivity(intent);
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
