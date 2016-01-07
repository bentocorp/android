package com.bentonow.bentonow.controllers.order;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.ImageUtils;
import com.bentonow.bentonow.Utils.WidgetsUtils;
import com.bentonow.bentonow.controllers.BaseFragmentActivity;
import com.bentonow.bentonow.controllers.adapter.DishFixGridListAdapter;
import com.bentonow.bentonow.controllers.dialog.ConfirmationDialog;
import com.bentonow.bentonow.dao.DishDao;
import com.bentonow.bentonow.model.DishModel;
import com.bentonow.bentonow.model.Menu;
import com.bentonow.bentonow.ui.GridViewHeader;
import com.bentonow.bentonow.ui.HeaderSideFixBento;

import java.util.ArrayList;


public class SelectSideFixActivity extends BaseFragmentActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    static final String TAG = "SelectSideFixActivity";

    private GridViewHeader mGridDish;
    private ImageView actionbarLeftBtn;
    private TextView txtToolbarTitle;

    private DishFixGridListAdapter mGridDishAdapter;

    private ArrayList<DishModel> aSideDish = new ArrayList<>();
    private DishModel mDishMain;
    private HeaderSideFixBento mHeader;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_side_fix);

        mDishMain = getIntent().getParcelableExtra(DishModel.TAG);
        aSideDish = getIntent().getParcelableArrayListExtra(DishModel.TAG_LIST);

        if (mDishMain == null || aSideDish == null) {
            WidgetsUtils.createShortToast(R.string.error_loading_dishes);
            finish();
        } else
            initToolbar();

        Menu menu = Menu.get();

        if (menu == null) {
            ConfirmationDialog mDialog = new ConfirmationDialog(SelectSideFixActivity.this, "Error", "There is no current menu to show");
            mDialog.addAcceptButton("OK", SelectSideFixActivity.this);
            mDialog.show();
        } else {
            getSideHeader().getTxtTitle().setText(mDishMain.name);
            getSideHeader().getTxtDescription().setText(mDishMain.description);
            getSideHeader().getImgIsSoldOut().setVisibility(DishDao.isSoldOut(mDishMain, true) ? View.VISIBLE : View.GONE);

            ImageUtils.initImageLoader().displayImage(mDishMain.image1, getSideHeader().getImgMainSide(), ImageUtils.dishSideImageOptions());
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
            case R.id.button_accept:
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