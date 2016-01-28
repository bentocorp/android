package com.bentonow.bentonow.controllers.errors;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.MixpanelUtils;
import com.bentonow.bentonow.controllers.BaseFragmentActivity;
import com.bentonow.bentonow.controllers.adapter.NextDayMainListAdapter;
import com.bentonow.bentonow.dao.MenuDao;
import com.bentonow.bentonow.model.DishModel;
import com.bentonow.bentonow.model.Menu;

import java.util.ArrayList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class NextDayMenuActivity extends BaseFragmentActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    static final String TAG = "NextDayMenuActivity";

    private ListView mListMain;
    private GridView gridSide;
    private ListView mListAddOn;


    private NextDayMainListAdapter mainAdapter;
    private NextDayMainListAdapter sideAdapter;
    private NextDayMainListAdapter addOnAdapter;

    private List<DishModel> data_main;
    private List<DishModel> data_side;
    private List<DishModel> data_add_on;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DebugUtils.logDebug(TAG, "onCreate()");

        setContentView(R.layout.activity_next_day_menu);

        MixpanelUtils.track("Previewed Today's Menu");

        Menu menu = MenuDao.getNextMenu();

        if (menu == null) {
            onBackPressed();
        } else {
            data_main = new ArrayList<>();
            data_side = new ArrayList<>();
            data_add_on = new ArrayList<>();

            for (DishModel dishModel : menu.dishModels) {
                switch (dishModel.type) {
                    case "main":
                        data_main.add(dishModel);
                        break;
                    case "side":
                        data_side.add(dishModel);
                        break;
                    case "addon":
                        data_add_on.add(dishModel);
                        break;
                    default:
                        DebugUtils.logError(TAG, "Unknown Type: " + dishModel.type + " Dish: " + dishModel.name);
                        break;
                }

            }

            getListMain().setAdapter(getMainAdapter());
            getListAddOn().setAdapter(getAddOnAdapter());
            getGridSide().setAdapter(getSideAdapter());

            getMainAdapter().addAll(data_main);
            getSideAdapter().addAll(data_side);
            getAddOnAdapter().addAll(data_add_on);

            getListMain().setOnItemClickListener(this);
            getListAddOn().setOnItemClickListener(this);
            getGridSide().setOnItemClickListener(this);

        }
        initActionbar();
    }

    private void initActionbar() {
        TextView actionbar_title = (TextView) findViewById(R.id.actionbar_title);
        actionbar_title.setText(getIntent().getStringExtra("title"));

        ImageView actionbar_left_btn = (ImageView) findViewById(R.id.actionbar_left_btn);
        actionbar_left_btn.setImageResource(R.drawable.ic_ab_back);
        actionbar_left_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.actionbar_left_btn:
                onBackPressed();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            DishModel mDish = (DishModel) parent.getAdapter().getItem(position);
            DebugUtils.logDebug(TAG, "Type: " + mDish.type + " Name: " + mDish.name);

            switch (mDish.type) {
                case "main":
                    getMainAdapter().setCurrentSelected(mDish);
                    getSideAdapter().setCurrentSelected(null);
                    getAddOnAdapter().setCurrentSelected(null);
                    break;
                case "side":
                    getMainAdapter().setCurrentSelected(null);
                    getSideAdapter().setCurrentSelected(mDish);
                    getAddOnAdapter().setCurrentSelected(null);
                    break;
                case "addon":
                    getMainAdapter().setCurrentSelected(null);
                    getSideAdapter().setCurrentSelected(null);
                    getAddOnAdapter().setCurrentSelected(mDish);
                    break;
                default:
                    DebugUtils.logError(TAG, "Unknown Type: " + mDish.type + " Dish: " + mDish.name);
                    break;
            }

            getSideAdapter().notifyDataSetChanged();
            getAddOnAdapter().notifyDataSetChanged();
            getMainAdapter().notifyDataSetChanged();

        } catch (Exception ex) {
            DebugUtils.logError(TAG, "OnClick");
        }

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    private NextDayMainListAdapter getMainAdapter() {
        if (mainAdapter == null)
            mainAdapter = new NextDayMainListAdapter(NextDayMenuActivity.this);
        return mainAdapter;
    }

    private NextDayMainListAdapter getSideAdapter() {
        if (sideAdapter == null)
            sideAdapter = new NextDayMainListAdapter(NextDayMenuActivity.this);
        return sideAdapter;
    }

    private NextDayMainListAdapter getAddOnAdapter() {
        if (addOnAdapter == null)
            addOnAdapter = new NextDayMainListAdapter(NextDayMenuActivity.this);
        return addOnAdapter;
    }

    private ListView getListMain() {
        if (mListMain == null)
            mListMain = (ListView) findViewById(R.id.list_next_main);
        return mListMain;
    }

    private GridView getGridSide() {
        if (gridSide == null)
            gridSide = (GridView) findViewById(R.id.grid_next_side);
        return gridSide;
    }

    private ListView getListAddOn() {
        if (mListAddOn == null)
            mListAddOn = (ListView) findViewById(R.id.list_next_add_on);
        return mListAddOn;
    }
}
