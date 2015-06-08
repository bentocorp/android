package com.bentonow.bentonow;

import android.accounts.AccountAuthenticatorActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.bentonow.bentonow.model.Dish;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class SelectSideActivity extends BaseActivity {

    private static final String TAG = "BentoSelectMainActivity";
    private static Activity _this;
    private ImageView actionbar_ic_back;
    public static AQuery aq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bento_select_side);

        aq = new AQuery(this);
        _this = this;
        initActionbar();
        getSideListDb();


    }

    private void initActionbar() {
        TextView actionbar_title = (TextView) findViewById(R.id.actionbar_title);
        actionbar_title.setText(getResources().getString(R.string.bento_builder_actionbar_title));

        ImageView actionbar_left_btn = (ImageView) findViewById(R.id.actionbar_left_btn);
        actionbar_left_btn.setImageResource(R.drawable.ic_ab_back);
        actionbar_left_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransitionGoLeft();
                /*Intent intent = new Intent(getApplicationContext(), BuildBentoActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.left_slide_in, R.anim.right_slide_out);*/
            }
        });

    }

    public void getSideListDb(){
        ArrayList<HashMap<String, String>> sideMenuList = new ArrayList<HashMap<String, String>>();

        List<Dish> main_dishes = Dish.find(Dish.class, "today = ?",todayDate);
        for( Dish dish : main_dishes ){
            if( dish.type.equals("side") ) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(Config.DISH._ID, dish._id);
                map.put(Config.DISH.NAME, dish.name);
                map.put(Config.DISH.DESCRIPTION, dish.description);
                map.put(Config.DISH.TYPE, dish.type);
                map.put(Config.DISH.IMAGE1, dish.image1);
                map.put(Config.DISH.MAX_PER_ORDER, dish.max_per_order);
                map.put(Config.DISH.TODAY, dish.today);
                map.put(Config.DISH.QTY, dish.qty);
                sideMenuList.add(map);
            }
        }
        GridView CompanyListView = (GridView) findViewById(R.id.menu_item_side_listview);
        DishListAdapter adapter = new DishListAdapter(_this, sideMenuList);
        CompanyListView.setAdapter(adapter);
    }

    /*public static void goToMain(){
        Intent intent = new Intent(aq.getContext(),BuildBentoActivity.class);
        _this.startActivity(intent);
        _this.finish();
        _this.overridePendingTransition(R.anim.left_slide_in, R.anim.right_slide_out);
    }*/

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransitionGoLeft();
    }
}
