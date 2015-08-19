package com.bentonow.bentonow.controllers.session;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.controllers.BaseActivity;
import com.bentonow.bentonow.controllers.help.HelpActivity;
import com.bentonow.bentonow.model.Settings;
import com.bentonow.bentonow.model.User;
import com.bentonow.bentonow.ui.CustomDialog;


public class SettingsActivity extends BaseActivity implements View.OnClickListener {
    static final String TAG = "SettingsActivity";

    String action = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initActionbar();
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateUI();
    }

    void initActionbar() {
        TextView actionbar_title = (TextView) findViewById(R.id.actionbar_title);
        actionbar_title.setText("Settings");

        ImageView actionbar_left_btn = (ImageView) findViewById(R.id.actionbar_left_btn);
        actionbar_left_btn.setImageResource(R.drawable.ab_x_close);
        actionbar_left_btn.setOnClickListener(this);

    }

    void updateUI () {
        findViewById(R.id.container_user).setVisibility(User.current == null ? View.GONE : View.VISIBLE);
        findViewById(R.id.container_sig_in).setVisibility(User.current != null ? View.GONE : View.VISIBLE);

        if (User.current != null) {
            ((TextView)findViewById(R.id.txt_name)).setText(
                    User.current.lastname != null ?
                            User.current.firstname + " " + User.current.lastname : User.current.firstname
            );

            ((TextView)findViewById(R.id.txt_phone)).setText(User.current.phone);
            ((TextView)findViewById(R.id.txt_email)).setText(User.current.email);
        }
    }

    //region onClick

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.actionbar_left_btn:
                onBackPressed();
                break;
            case R.id.btn_ok:
                if (action.equals("phone")) {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:4153001332"));
                    startActivity(intent);
                } else if (action.equals("logout")) {
                    User.current = null;
                    updateUI();
                    Settings.save(this);
                }

                action = "";
                break;
        }
    }

    public void onLogoutPressed (View v) {
        Log.i(TAG, "onLogoutPressed");
        action = "logout";
        CustomDialog dialog = new CustomDialog(this, "Confirmation\nAre you sure you want to log out?", "YES", "NO");
        dialog.setOnOkPressed(this);
        dialog.show();
    }

    public void onSignInPressed (View v) {
        Intent intent = new Intent(this, SignInActivity.class);
        intent.putExtra("settings", true);
        startActivity(intent);
    }

    public void onFaqPressed (View v) {
        Intent intent = new Intent(this, HelpActivity.class);
        intent.putExtra("faq", true);
        startActivity(intent);
    }

    public void onEmailSupportPressed (View v) {
        String[] TO = {"help@bentonow.com"};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getApplicationContext(), "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }

    public void onPhoneSupportPressed (View v) {
        action = "phone";
        CustomDialog dialog = new CustomDialog(this, "415.300.1332", "Call", "Cancel");
        dialog.show();
        dialog.setOnOkPressed(this);
    }

    //endregion
}