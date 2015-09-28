package com.bentonow.bentonow.controllers.order;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.ImageUtils;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.controllers.BaseActivity;
import com.bentonow.bentonow.controllers.adapter.DishFixGridListAdapter;
import com.bentonow.bentonow.model.Item;
import com.bentonow.bentonow.model.Menu;
import com.bentonow.bentonow.ui.CustomDialog;
import com.bentonow.bentonow.ui.GridViewHeader;
import com.bentonow.bentonow.ui.HeaderSideFixBento;

import java.util.ArrayList;


public class SelectSideFixActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    static final String TAG = "SelectSideFixActivity";

    private GridViewHeader mGridDish;
    private ImageView actionbarLeftBtn;
    private TextView txtToolbarTitle;

    private DishFixGridListAdapter mGridDishAdapter;
    private CustomDialog dialog;

    private ArrayList<Item> aSideDish = new ArrayList<>();
    private Item mDishMain;
    private HeaderSideFixBento mHeader;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_side_fix);

        mDishMain = getIntent().getParcelableExtra(Item.TAG);
        aSideDish = getIntent().getParcelableArrayListExtra(Item.TAG_LIST);

        initToolbar();

        Menu menu = Menu.get();

        if (menu == null) {
            dialog = new CustomDialog(this, "There is no current menu to show", "OK", null);
            dialog.setOnOkPressed(this);
            dialog.show();
        } else {
            getSideHeader().getTxtTitle().setText(mDishMain.name);
            getSideHeader().getTxtDescription().setText(mDishMain.description);
            getSideHeader().getImgIsSoldOut().setVisibility(mDishMain.isSoldOut(true) ? View.VISIBLE : View.GONE);

            ImageUtils.initImageLoader().displayImage(mDishMain.image1, getSideHeader().getImgMainSide(), ImageUtils.dishImageOptions());
            getSideHeader().getImgMainSide().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    tapOnDish(true);
                }
            });

            getGridDish().addHeaderView(getSideHeader(), null, true);
            getGridDish().setAdapter(getGridDishAdapter());
            getGridDish().setOnItemClickListener(this);
            getGridDishAdapter().addAll(aSideDish);
        }
    }

    private void initToolbar() {
        getTxtToolbarTitle().setText(mDishMain.name);

        getActionbarLeftBtn().setImageResource(R.drawable.ic_ab_back);
        getActionbarLeftBtn().setOnClickListener(this);
    }

    private void tapOnDish(final boolean bTouchMainDish) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (bTouchMainDish) {
                    getGridDishAdapter().setItemSelected(-1);
                    getGridDishAdapter().notifyDataSetChanged();
                }
                getSideHeader().getTxtDescription().setVisibility(bTouchMainDish ? View.VISIBLE : View.GONE);
            }
        });
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
        getGridDishAdapter().setItemSelected(position - 2);
        getGridDishAdapter().notifyDataSetChanged();

        tapOnDish(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (SharedPreferencesUtil.getBooleanPreference(SharedPreferencesUtil.IS_STORE_CHANGIN))
            finish();
    }

    private ImageView getActionbarLeftBtn() {
        if (actionbarLeftBtn == null)
            actionbarLeftBtn = (ImageView) findViewById(R.id.actionbar_left_btn);
        return actionbarLeftBtn;
    }

    private TextView getTxtToolbarTitle() {
        if (txtToolbarTitle == null)
            txtToolbarTitle = (TextView) findViewById(R.id.actionbar_title);
        return txtToolbarTitle;
    }

    private GridViewHeader getGridDish() {
        if (mGridDish == null)
            mGridDish = (GridViewHeader) findViewById(R.id.grid_side_fix);
        return mGridDish;
    }

    private DishFixGridListAdapter getGridDishAdapter() {
        if (mGridDishAdapter == null)
            mGridDishAdapter = new DishFixGridListAdapter(this);
        return mGridDishAdapter;
    }

    private HeaderSideFixBento getSideHeader() {
        if (mHeader == null)
            mHeader = new HeaderSideFixBento(this);
        return mHeader;
    }

}