package com.bentonow.bentonow;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bentonow.bentonow.model.Checkpoint;


public class HomeAboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_about);

        Bentonow.app.is_first_access = true;
        ImageView btn_get_started = (ImageView) findViewById(R.id.btn_get_started);
        btn_get_started.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToHome();
            }
        });
    }

    private void goToHome() {
        checkIn();
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.left_slide_in, R.anim.right_slide_out);
        finish();
    }

    private void checkIn() {
        Bentonow.app.is_first_access = false;
        Checkpoint checkin = new Checkpoint("checkin");
        checkin.save();
    }
}
