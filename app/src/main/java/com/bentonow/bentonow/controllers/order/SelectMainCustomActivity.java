package com.bentonow.bentonow.controllers.order;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.controllers.BaseActivity;
import com.bentonow.bentonow.controllers.adapter.CustomMainListAdapter;
import com.bentonow.bentonow.controllers.dialog.ConfirmationDialog;
import com.bentonow.bentonow.dao.DishDao;
import com.bentonow.bentonow.listener.ListenerCustomDish;
import com.bentonow.bentonow.model.DishModel;
import com.bentonow.bentonow.model.Menu;
import com.bentonow.bentonow.model.Order;

import java.util.ArrayList;


public class SelectMainCustomActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener, ListenerCustomDish {

    static final String TAG = "SelectMainCustomActivity";

    private ListView mListView;

    private CustomMainListAdapter mListAdapter;

    private int orderIndex;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_main);

        initActionbar();

        Menu menu = Menu.get();

        if (menu == null) {
            ConfirmationDialog mDialog = new ConfirmationDialog(SelectMainCustomActivity.this, "Error", "There is no current menu to show");
            mDialog.addAcceptButton("OK", SelectMainCustomActivity.this);
            mDialog.show();
        } else {
            getListView().setAdapter(getListAdapter());
            getListView().setOnItemClickListener(this);

            orderIndex = Order.current.currentOrderItem;

            getListAdapter().setCurrentAdded(Order.current.OrderItems.get(orderIndex).items.get(0));
            getListAdapter().setCurrentSelected(Order.current.OrderItems.get(orderIndex).items.get(0));

            ArrayList<DishModel> aSoldDish = new ArrayList<>();

            for (DishModel dishModel : menu.dishModels) {
                if (dishModel.type.equals("main")) {
                    if (DishDao.isSoldOut(dishModel, true))
                        aSoldDish.add(dishModel);
                    else
                        getListAdapter().add(dishModel);
                }
            }

            getListAdapter().addAll(aSoldDish);
        }
    }

    private void initActionbar() {
        TextView actionbar_title = (TextView) findViewById(R.id.actionbar_title);
        actionbar_title.setText(getResources().getString(R.string.bento_builder_actionbar_title));

        ImageView actionbar_left_btn = (ImageView) findViewById(R.id.actionbar_left_btn);
        actionbar_left_btn.setImageResource(R.drawable.ic_ab_back);
        actionbar_left_btn.setOnClickListener(this);
    }


    @Override
    public void onAddedClick(int iDishPosition) {
        //if (currentSelectedItem.itemId == Order.current.OrderItems.get(orderIndex).items.get(0).itemId)
        Order.current.OrderItems.get(orderIndex).items.set(0, null);
        getListAdapter().setCurrentAdded(null);

        getListAdapter().notifyDataSetChanged();
        //onBackPressed();
    }

    @Override
    public void onAddToBentoClick(int iDishPosition) {
        if (DishDao.isSoldOut(getListAdapter().getCurrentSelected(), true) || !DishDao.canBeAdded(getListAdapter().getCurrentSelected()))
            return;

        Order.current.OrderItems.get(orderIndex).items.set(0, getListAdapter().getCurrentSelected());

        onBackPressed();
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
        getListAdapter().setCurrentSelected(getListAdapter().getItem(position));
        getListAdapter().notifyDataSetChanged();
    }

    private CustomMainListAdapter getListAdapter() {
        if (mListAdapter == null)
            mListAdapter = new CustomMainListAdapter(SelectMainCustomActivity.this, SelectMainCustomActivity.this);
        return mListAdapter;
    }

    private ListView getListView() {
        if (mListView == null)
            mListView = (ListView) findViewById(R.id.list_select_main);
        return mListView;
    }


}
