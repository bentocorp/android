package com.bentonow.bentonow;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.bentonow.bentonow.model.Ioscopy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class OrderConfirmedActivity extends BaseActivity {

    private static final String TAG = "OrderConfirmedActivity";
    private TextView btn_build_another_bento;
    private AQuery aq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmed);
        aq = new AQuery(this);
        initElements();
        initListeners();
        updateStock();
    }

    private void initElements() {
        btn_build_another_bento = (TextView)findViewById(R.id.btn_build_another_bento);
    }

    private void initListeners() {
        btn_build_another_bento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), BuildBentoActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransitionGoLeft();
            }
        });
        findViewById(R.id.btn_faq).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToFAQ();
            }
        });
    }

    private void initActionbar() {
        Log.i(TAG, "initActionbar()");
        TextView actionbar_title = (TextView) findViewById(R.id.actionbar_title);
        actionbar_title.setText("Complete Order");

        ImageView actionbar_right_btn = (ImageView) findViewById(R.id.actionbar_right_btn);
        actionbar_right_btn.setImageResource(R.drawable.ic_ab_help);
        actionbar_right_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //finishThisActivity();
                goToFAQ();
            }
        });

    }

    void updateStock( ){
        Log.i(TAG, "updateStock()");
        String uri = Config.API.URL+"/status/menu";
        Log.i(TAG, "uri: " + uri);
        aq.ajax(uri, JSONArray.class, new AjaxCallback<JSONArray>() {
            @Override
            public void callback(String url, JSONArray json, AjaxStatus status) {
                Log.i(TAG, "status.getError(): " + status.getError());
                Log.i(TAG, "status.getMessage(): " + status.getMessage());
                Log.i(TAG, "status.getCode(): " + status.getCode());
                if (status.getCode() == 200) {
                    BentoService.processMenuStock(json);
                }
            }
        });
    }

}
