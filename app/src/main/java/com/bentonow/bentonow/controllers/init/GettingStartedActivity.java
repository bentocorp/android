package com.bentonow.bentonow.controllers.init;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.bentonow.bentonow.controllers.BaseActivity;
import com.bentonow.bentonow.R;
import com.bentonow.bentonow.controllers.BentoApplication;
import com.bentonow.bentonow.model.BackendText;


public class GettingStartedActivity extends BaseActivity {

    private static final String TAG = "GettingStartedActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_getting_started);

        String price = BackendText.get("price");
        String title = BackendText.get("about-item-0").replace("$X", "$" + price + "!");

        Log.i(TAG, "price: " + price);
        Log.i(TAG, "title: " + title);

        ((TextView) findViewById(R.id.txt_title)).setText(title);
    }

    @Override
    protected void onResume() {
        super.onResume();

        BentoApplication.status = "main";
    }

    public void onGettingStartedPressed(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("checkLocation", true);
        startActivity(intent);
        finish();
    }
}
