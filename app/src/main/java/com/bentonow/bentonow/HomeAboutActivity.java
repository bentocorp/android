package com.bentonow.bentonow;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bentonow.bentonow.model.Checkpoint;
import com.bentonow.bentonow.model.Ioscopy;


public class HomeAboutActivity extends BaseActivity {

    private static final String TAG = "HomeAboutActivity";
    private TextView about_item_0, about_item_1, about_item_2, about_item_3;
    private TextView btn_get_started;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_about);

        Bentonow.app.is_first_access = true;

        initElements();
        loadElements();
        addListeners();

    }

    private void initElements() {
        about_item_0 = (TextView)findViewById(R.id.about_item_0);
        about_item_1 = (TextView)findViewById(R.id.about_item_1);
        about_item_2 = (TextView)findViewById(R.id.about_item_2);
        about_item_3 = (TextView)findViewById(R.id.about_item_3);
        btn_get_started = (TextView)findViewById(R.id.btn_get_started);
    }

    private void loadElements() {
        String title = Ioscopy.getKeyValue("about-item-0");
        String price = Ioscopy.getKeyValue("price");
        Log.i(TAG,"price: "+price);
        title = title.replace("$X!","$"+price+"!");
        about_item_0.setText(title);
        about_item_1.setText(Ioscopy.getKeyValue("about-item-1"));
        about_item_2.setText(Ioscopy.getKeyValue("about-item-2"));
        about_item_3.setText(Ioscopy.getKeyValue("about-item-3"));
        btn_get_started.setText(Ioscopy.getKeyValue("about-button"));
    }

    private void addListeners() {
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
