package com.bentonow.bentonow;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.bentonow.bentonow.model.Dish;
import com.bentonow.bentonow.model.Item;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class SelectMainActivity extends BaseActivity {

    private static final String TAG = "BentoSelectMainActivity";
    private static Activity _this;
    private ImageView actionbar_ic_back;
    private static AQuery aq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bento_select_main);
        _this = this;
        aq = new AQuery(this);

        initActionbar();
        getMainListDb();
    }


    private void initActionbar() {
        TextView actionbar_title = (TextView) findViewById(R.id.actionbar_title);
        actionbar_title.setText(getResources().getString(R.string.bento_builder_actionbar_title));

        ImageView actionbar_left_btn = (ImageView) findViewById(R.id.actionbar_left_btn);
        actionbar_left_btn.setImageResource(R.drawable.ic_ab_back);
        actionbar_left_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //finishThisActivity();
                Intent intent = new Intent(getApplicationContext(),BuildBentoActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.left_slide_in, R.anim.right_slide_out);
            }
        });

    }

    public void getMainListDb(){
        Log.i(TAG,"getMainListDb()");
        ArrayList<HashMap<String, String>> mainMenuList = new ArrayList<HashMap<String, String>>();
        List<Dish> main_dishes = Dish.find(Dish.class,"today = ?",todayDate);
        for( Dish dish : main_dishes ){
            if( dish.type.equals("main") ) {
                Log.i(TAG,"dish: "+dish.toString());
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("_id", dish._id);
                map.put("name", dish.name);
                map.put("description", dish.description);
                map.put("type", dish.type);
                map.put("image1", dish.image1);
                map.put("max_per_order", dish.max_per_order);
                map.put("today", dish.today);
                map.put("qty", dish.qty);
                Log.i(TAG, "map: " + map.toString());
                mainMenuList.add(map);
            }
        }
        ListView CompanyListView = (ListView) findViewById(R.id.main_menu_list_items);
        MenuItemMainAdapter adapter = new MenuItemMainAdapter(getApplicationContext(), mainMenuList);
        CompanyListView.setAdapter(adapter);
    }

    public static void goToMain(){
        Intent intent = new Intent(aq.getContext(),BuildBentoActivity.class);
        _this.startActivity(intent);
        _this.overridePendingTransition(R.anim.left_slide_in, R.anim.right_slide_out);
        _this.finish();
    }
}
