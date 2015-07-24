package com.bentonow.bentonow;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bentonow.bentonow.model.Dish;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class SelectMainActivity extends BaseActivity {

    private static final String TAG = "BentoSelectMainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bento_select_main);

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
                finish();
                overridePendingTransitionGoLeft();
            }
        });

    }

    public void getMainListDb(){
        Log.i(TAG,"getMainListDb()");
        ArrayList<HashMap<String, String>> mainMenuList = new ArrayList<>();
        List<Dish> main_dishes = Dish.find(Dish.class,"today = ?",todayDate);
        Log.i(TAG,"count: " + main_dishes.size());
        for( Dish dish : main_dishes ){
            if( dish.type.equals("main") ) {
                Log.i(TAG,"dish: "+dish.toString());
                HashMap<String, String> map = new HashMap<>();
                map.put(Config.DISH._ID, dish._id);
                map.put(Config.DISH.NAME, dish.name);
                map.put(Config.DISH.DESCRIPTION, dish.description);
                map.put(Config.DISH.TYPE, dish.type);
                map.put(Config.DISH.IMAGE1, dish.image1);
                map.put(Config.DISH.MAX_PER_ORDER, dish.max_per_order);
                map.put(Config.DISH.TODAY, dish.today);
                map.put(Config.DISH.QTY, dish.qty);
                Log.i(TAG, "map: " + map.toString());
                mainMenuList.add(map);
            }
        }
        ListView DishListView = (ListView) findViewById(R.id.main_menu_list_items);
        DishListAdapter adapter = new DishListAdapter(this, mainMenuList);
        DishListView.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
       finish();
        overridePendingTransitionGoLeft();
    }


}
