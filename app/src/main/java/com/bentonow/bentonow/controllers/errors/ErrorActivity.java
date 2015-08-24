package com.bentonow.bentonow.controllers.errors;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.Email;
import com.bentonow.bentonow.controllers.BentoApplication;
import com.bentonow.bentonow.controllers.help.HelpActivity;
import com.bentonow.bentonow.model.BackendText;
import com.bentonow.bentonow.model.Menu;
import com.bentonow.bentonow.model.Settings;
import com.bentonow.bentonow.model.User;
import com.bentonow.bentonow.ui.CustomDialog;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class ErrorActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "ErrorActivity";

    TextView txt_email;
    Button btn_next_day_menu;

    public static boolean bIsOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);

        initActionbar();

        BentoApplication.status = Settings.status;

        txt_email = (EditText) findViewById(R.id.txt_email);
        btn_next_day_menu = (Button) findViewById(R.id.btn_next_day_menu);
    }

    @Override
    protected void onResume() {
        bIsOpen = true;

        BentoApplication.onResume();

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY) * 100 + calendar.get(Calendar.MINUTE);

        TextView txt_title = (TextView) findViewById(R.id.txt_title);
        TextView txt_description = (TextView) findViewById(R.id.txt_description);

        if (Settings.status.equals("sold out") || Menu.get() == null) {
            txt_title.setText(BackendText.get("sold-out-title"));
            txt_description.setText(BackendText.get("sold-out-text"));
        } else {
            txt_title.setText(BackendText.get("closed-title"));
            if (hour >= 2000) {
                txt_description.setText(BackendText.get("closed-text-latenight"));
            } else {
                txt_description.setText(BackendText.get("closed-text"));
            }
        }

        BentoApplication.status = Settings.status;

        setupNextMenu();

        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        BentoApplication.onPause();
    }

    void setupNextMenu () {
        Menu menu = Menu.getNext();

        if (menu != null) {
            Log.i(TAG, "menu: " + menu.toString());
            btn_next_day_menu.setVisibility(View.VISIBLE);

            DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

            if (menu.for_date.replace("-", "").equals(Menu.getTodayDate())) {
                btn_next_day_menu.setText(BackendText.get("closed-sneak-preview-button"));
            } else {
                try {
                    String day = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(format.parse(menu.for_date));
                    String title = day + "'s " + menu.meal_name.substring(0, 1).toUpperCase() + menu.meal_name.substring(1);
                    btn_next_day_menu.setText(title);
                } catch (Exception e) {
                    e.printStackTrace();
                    btn_next_day_menu.setVisibility(View.INVISIBLE);
                }
            }
        } else {
            Log.i(TAG, "menu: null");
            btn_next_day_menu.setVisibility(View.INVISIBLE);
        }
    }

    void initActionbar() {
        ImageView actionbar_right_btn = (ImageView) findViewById(R.id.actionbar_right_btn);
        actionbar_right_btn.setImageResource(R.drawable.ic_ab_help);
        actionbar_right_btn.setOnClickListener(this);

        ImageView actionbar_left_btn = (ImageView) findViewById(R.id.actionbar_left_btn);
        actionbar_left_btn.setImageResource(R.drawable.ic_ab_back);
        actionbar_left_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.actionbar_right_btn:
                Intent intent = new Intent(this, HelpActivity.class);
                intent.putExtra("faq", true);
                startActivity(intent);
                break;
            case R.id.actionbar_left_btn:
                onBackPressed();
                break;
        }
    }

    @Override
    protected void onStop() {
        bIsOpen = false;

        super.onStop();
    }

    public void onSubmitPressed(View view) {
        if (!Email.isValid(txt_email.getText().toString())) {
            CustomDialog dialog = new CustomDialog(this, "Invalid email address.", null, "OK");
            dialog.show();
        } else {
            User.requestCoupon(
                    txt_email.getText().toString(),
                    Settings.status,
                    new TextHttpResponseHandler() {
                        @SuppressWarnings("deprecation")
                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            Log.e(TAG, responseString);
                            CustomDialog dialog = new CustomDialog(
                                    ErrorActivity.this,
                                    "We having issues connecting to the server, please try later.",
                                    null,
                                    "OK"
                            );
                            dialog.show();
                        }

                        @SuppressWarnings("deprecation")
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, String responseString) {
                            String message = Settings.status.equals("sold out") ?
                                    BackendText.get("sold-out-confirmation-text") :
                                    BackendText.get("closed-confirmation-text");
                            Log.i(TAG, responseString);
                            txt_email.setText("");
                            CustomDialog dialog = new CustomDialog(
                                    ErrorActivity.this,
                                    message,
                                    null,
                                    "OK"
                            );
                            dialog.show();
                        }
                    });
        }
    }

    public void onNextDayMenuPressed(View view) {
        if (Menu.get() == null) return;

        Intent intent = new Intent(this, NextDayMenuActivity.class);
        intent.putExtra("title", btn_next_day_menu.getText().toString());
        startActivity(intent);
    }

    public void onPrivacyPolicyPressed(View view) {
        Intent intent = new Intent(this, HelpActivity.class);
        intent.putExtra("privacy", true);
        startActivity(intent);
    }

    public void onTermAndConditionsPressed(View view) {
        Intent intent = new Intent(this, HelpActivity.class);
        intent.putExtra("tos", true);
        startActivity(intent);
    }
}
