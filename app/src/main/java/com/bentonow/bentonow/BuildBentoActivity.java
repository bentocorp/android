package com.bentonow.bentonow;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bentonow.bentonow.Utils.Mixpanel;
import com.bentonow.bentonow.model.Dish;
import com.bentonow.bentonow.model.Item;
import com.bentonow.bentonow.model.Orders;
import com.bentonow.bentonow.model.User;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;


public class BuildBentoActivity extends BaseActivity {

    private static final String TAG = "BuildBentoActivity";

    private TextView build_main_label_textview;
    private ImageView build_main_imageview;

    private TextView build_side1_label_textview;
    private ImageView build_side1_imageview;

    private TextView build_side2_label_textview;
    private ImageView build_side2_imageview;

    private TextView build_side3_label_textview;
    private ImageView build_side3_imageview;

    private TextView build_side4_label_textview;
    private ImageView build_side4_imageview;
    private TextView btn_add_another_bento;
    private TextView btn_add_another_bento_disabled;
    private TextView bento_box_counter_textview;
    private TextView btn_continue, btn_finalize_order;
    private RelativeLayout overlay_autocomplete_bento;
    private TextView btn_no_complete_for_me,btn_yes_complete_for_me;
    private ImageView actionbar_right_btn;
    private RelativeLayout main_title_container;
    private TextView main_title;
    private RelativeLayout side1_title_container, side2_title_container, side3_title_container, side4_title_container;
    private TextView side1_title,side2_title,side3_title,side4_title;
    private ImageView flag_soldout_main, flag_soldout_side_1, flag_soldout_side_2, flag_soldout_side_3, flag_soldout_side_4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_build_bento);
        Log.i(TAG, "onCreate");
        initActionbar();
        chargeViewsElemnts();
        initListeners();

        Mixpanel.track(this, "Began building a Bento");
    }

    private void chargeViewsElemnts() {
        build_main_label_textview = (TextView)findViewById(R.id.build_main_label_textview);
        build_main_imageview = (ImageView)findViewById(R.id.build_main_imageview);
        main_title_container = (RelativeLayout)findViewById(R.id.main_title_container);
        main_title = (TextView)findViewById(R.id.main_title);

        build_side1_label_textview = (TextView)findViewById(R.id.build_side1_label_textview);
        build_side1_imageview = (ImageView)findViewById(R.id.build_side1_imageview);
        side1_title_container = (RelativeLayout)findViewById(R.id.side1_title_container);
        side1_title = (TextView)findViewById(R.id.side1_title);

        build_side2_label_textview = (TextView)findViewById(R.id.build_side2_label_textview);
        build_side2_imageview = (ImageView)findViewById(R.id.build_side2_imageview);
        side2_title_container = (RelativeLayout)findViewById(R.id.side2_title_container);
        side2_title = (TextView)findViewById(R.id.side2_title);

        build_side3_label_textview = (TextView)findViewById(R.id.build_side3_label_textview);
        build_side3_imageview = (ImageView)findViewById(R.id.build_side3_imageview);
        side3_title_container = (RelativeLayout)findViewById(R.id.side3_title_container);
        side3_title = (TextView)findViewById(R.id.side3_title);

        build_side4_label_textview = (TextView)findViewById(R.id.build_side4_label_textview);
        build_side4_imageview = (ImageView)findViewById(R.id.build_side4_imageview);
        side4_title_container = (RelativeLayout)findViewById(R.id.side4_title_container);
        side4_title = (TextView)findViewById(R.id.side4_title);

        btn_add_another_bento = (TextView)findViewById(R.id.btn_add_another_bento);
        btn_add_another_bento_disabled = (TextView)findViewById(R.id.btn_add_another_bento_disabled);

        bento_box_counter_textview = (TextView)findViewById(R.id.bento_box_counter_textview);

        btn_continue = (TextView)findViewById(R.id.btn_continue_inactive);
        btn_finalize_order = (TextView)findViewById(R.id.btn_continue_active);

        // OVERLAY
        overlay_autocomplete_bento = (RelativeLayout)findViewById(R.id.overlay_autocomplete_bento);
        btn_no_complete_for_me = (TextView)findViewById(R.id.btn_no_complete_for_me);
        btn_yes_complete_for_me = (TextView)findViewById(R.id.btn_yes_complete_for_me);

        // Flags
        flag_soldout_main = (ImageView) findViewById(R.id.soldout_flag_main);
        flag_soldout_side_1 = (ImageView) findViewById(R.id.soldout_flag_side_1);
        flag_soldout_side_2 = (ImageView) findViewById(R.id.soldout_flag_side_2);
        flag_soldout_side_3 = (ImageView) findViewById(R.id.soldout_flag_side_3);
        flag_soldout_side_4 = (ImageView) findViewById(R.id.soldout_flag_side_4);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //builBento();
        resume();
    }

    private void resume() {
        Log.i(TAG,"resume()");
        checkPendingBuildBento();
        showCountBentos();
    }

    private void showCountBentos() {
        Log.i(TAG, "showCountBentos()");
        long pending_bento = Item.count(Item.class, "orderid=? and completed=?", new String[]{String.valueOf(Bentonow.pending_order_id), "yes"});
        if(pending_bento>0) {
            actionbar_right_btn.setImageResource(R.drawable.ic_ab_bento_completed);
            bento_box_counter_textview.setText(String.valueOf(pending_bento));
            bento_box_counter_textview.setVisibility(View.VISIBLE);
        }
    }

    private void checkPendingBuildBento() {
        Log.i(TAG, "checkPendingBuildBento()");
        long pending_order_id = Orders.findPendingOrderId(todayDate);
        Log.i(TAG, "pending_order_id: " + pending_order_id);
        if( pending_order_id != 0 ) {
            Bentonow.pending_order_id = pending_order_id;
            Log.i(TAG, "Bentonow.pending_bento_id: " + Bentonow.pending_bento_id);
            if (Bentonow.pending_bento_id == null) {
                List<Item> pending_bento = Item.find(Item.class, "orderid=?", new String[]{String.valueOf(Bentonow.pending_order_id)}, null, "id DESC", "1");
                if (pending_bento.isEmpty()) {
                    createNewBentoBox();
                } else {
                    for (Item item : pending_bento) {
                        Log.i(TAG, "Each pending item: " + pending_bento.toString());
                        Bentonow.pending_bento_id = item.getId();
                        if (Bentonow.pending_bento_id != null) {
                            Item current_betno = Item.findById(Item.class, Bentonow.pending_bento_id);
                            showCurrentBento(current_betno);
                        } else {
                            createNewBentoBox();
                        }
                    }
                }
            } else {
                Item current_betno = Item.findById(Item.class, Bentonow.pending_bento_id);
                showCurrentBento(current_betno);
            }
        }else{
            createNewOrder();
        }
    }

    private void createNewOrder() {
        Log.i(TAG, "createNewOrder()");
        if (Bentonow.pending_order_id == null) {

            List<Orders> pending_orders = Orders.find(Orders.class, null);

            Orders order = new Orders();
            order.today = todayDate;
            for (Orders o : pending_orders) {
                if(o.coords_lat!=null && !o.coords_lat.isEmpty() ){
                    order.coords_lat = o.coords_lat;
                    order.coords_long = o.coords_long;
                    order.address_number = o.address_number;
                    order.address_street = o.address_street;
                    order.address_city = o.address_city;
                    order.address_state = o.address_state;
                    order.address_zip = o.address_zip;
                }
            }
            order.completed = Config.ORDER.STATUS.UNCOMPLETED;
            order.save();
            Log.i(TAG, "New order generated");
            Bentonow.pending_order_id = order.getId();
            createNewBentoBox();
        }
    }

    public void createNewBentoBox() {
        Log.i(TAG,"createNewBentoBox()");
        Item item = new Item();
        item.completed = "no";
        item.orderid = String.valueOf(Bentonow.pending_order_id);
        item.save();
        Bentonow.pending_bento_id = item.getId();
        //showCurrentBento(item);
        resume();
    }

    private void showCurrentBento(Item current_bento) {
        Log.i(TAG,"showCurrentBento(Item current_bento)");
        if( current_bento == null ){
            Log.i(TAG, "current_bento isNULL");
            createNewBentoBox();
            return;
        }
        Log.i(TAG,"current_bento: " + current_bento.toString());
        Log.i(TAG,"current_bento.isFull(): " + current_bento.isFull());


        if(current_bento.isFull()){
            Log.i(TAG,"current_bento is full");
            current_bento.completed = "yes";
            current_bento.save();
            enableBtnAddAnotherBento();
        }else{
            disableBtnAddAnotherBento();
        }

        flag_soldout_main.setVisibility(View.INVISIBLE);
        flag_soldout_side_1.setVisibility(View.INVISIBLE);
        flag_soldout_side_2.setVisibility(View.INVISIBLE);
        flag_soldout_side_3.setVisibility(View.INVISIBLE);
        flag_soldout_side_4.setVisibility(View.INVISIBLE);

        // MAIN DISH
        if( current_bento.main != null ) {
            Dish main_dish = findDish(current_bento.main);

            if ( main_dish != null ) {
                if (main_dish.isSoldOut(false)) {
                    flag_soldout_main.setVisibility(View.VISIBLE);
                }

                if (!main_dish.image1.isEmpty()) {
                    build_main_label_textview.setVisibility(View.INVISIBLE);
                    build_main_imageview.setVisibility(View.VISIBLE);
                    main_title.setText(main_dish.name.toUpperCase());
                    main_title_container.setVisibility(View.VISIBLE);
                    Picasso.with(getApplicationContext())
                            .load(main_dish.image1)
                            .placeholder(R.drawable.tmp_trans)
                            .error(R.drawable.tmp_trans)
                            .into(build_main_imageview);
                } else {
                    build_main_label_textview.setVisibility(View.INVISIBLE);
                    build_main_imageview.setVisibility(View.VISIBLE);
                    build_main_imageview.setImageResource(R.drawable.tmp_trans);
                    main_title.setText(main_dish.name.toUpperCase());
                    main_title_container.setVisibility(View.VISIBLE);
                }
            }else{
                build_main_label_textview.setVisibility(View.VISIBLE);
                build_main_imageview.setImageResource(R.drawable.tmp_trans);
                build_main_imageview.setVisibility(View.INVISIBLE);
                main_title.setText("");
                main_title_container.setVisibility(View.GONE);
            }

        }else{
            build_main_label_textview.setVisibility(View.VISIBLE);
            build_main_imageview.setImageResource(R.drawable.tmp_trans);
            build_main_imageview.setVisibility(View.INVISIBLE);
            main_title.setText("");
            main_title_container.setVisibility(View.GONE);
        }

        // SIDE 1 DISH
        if( current_bento.side1 != null ) {
            Dish side1_dish = findDish(current_bento.side1);

            if ( side1_dish != null ) {
                if (side1_dish.isSoldOut(false)) {
                    flag_soldout_side_1.setVisibility(View.VISIBLE);
                }

                if (!side1_dish.image1.isEmpty()) {
                    Log.i(TAG, "side1_dish.image1: " + side1_dish.image1);
                    build_side1_label_textview.setVisibility(View.INVISIBLE);
                    build_side1_imageview.setVisibility(View.VISIBLE);
                    side1_title.setText(side1_dish.name.toUpperCase());
                    side1_title_container.setVisibility(View.VISIBLE);
                    Picasso.with(getApplicationContext())
                            .load(side1_dish.image1)
                            .placeholder(R.drawable.tmp_trans)
                            .error(R.drawable.tmp_trans)
                            .into(build_side1_imageview);
                } else {
                    build_side1_label_textview.setVisibility(View.INVISIBLE);
                    build_side1_imageview.setVisibility(View.VISIBLE);
                    build_side1_imageview.setImageResource(R.drawable.tmp_trans);
                    side1_title.setText(side1_dish.name.toUpperCase());
                    side1_title_container.setVisibility(View.VISIBLE);
                }
            }else{
                build_side1_label_textview.setVisibility(View.VISIBLE);
                build_side1_imageview.setVisibility(View.INVISIBLE);
                build_side1_imageview.setImageResource(R.drawable.tmp_trans);
                side1_title.setText("");
                side1_title_container.setVisibility(View.GONE);
            }
        }else{
            build_side1_label_textview.setVisibility(View.VISIBLE);
            build_side1_imageview.setVisibility(View.INVISIBLE);
            build_side1_imageview.setImageResource(R.drawable.tmp_trans);
            side1_title.setText("");
            side1_title_container.setVisibility(View.GONE);
        }

        // SIDE 2 DISH
        if( current_bento.side2 != null ) {
            Dish side2_dish = findDish(current_bento.side2);

            if( side2_dish != null ) {
                if (side2_dish.isSoldOut(false)) {
                    flag_soldout_side_2.setVisibility(View.VISIBLE);
                }

                if (!side2_dish.image1.isEmpty()) {
                    Log.i(TAG, "side2_dish.image1: " + side2_dish.image1);
                    build_side2_label_textview.setVisibility(View.INVISIBLE);
                    build_side2_imageview.setVisibility(View.VISIBLE);
                    side2_title.setText(side2_dish.name.toUpperCase());
                    side2_title_container.setVisibility(View.VISIBLE);
                    Picasso.with(getApplicationContext())
                            .load(side2_dish.image1)
                            .placeholder(R.drawable.tmp_trans)
                            .error(R.drawable.tmp_trans)
                            .into(build_side2_imageview);
                } else {
                    build_side2_label_textview.setVisibility(View.INVISIBLE);
                    build_side2_imageview.setVisibility(View.VISIBLE);
                    build_side2_imageview.setImageResource(R.drawable.tmp_trans);
                    side2_title.setText(side2_dish.name.toUpperCase());
                    side2_title_container.setVisibility(View.VISIBLE);
                }
            }else{
                build_side2_label_textview.setVisibility(View.VISIBLE);
                build_side2_imageview.setVisibility(View.INVISIBLE);
                build_side2_imageview.setImageResource(R.drawable.tmp_trans);
                side2_title.setText("");
                side2_title_container.setVisibility(View.GONE);
            }
        }else{
            build_side2_label_textview.setVisibility(View.VISIBLE);
            build_side2_imageview.setVisibility(View.INVISIBLE);
            build_side2_imageview.setImageResource(R.drawable.tmp_trans);
            side2_title.setText("");
            side2_title_container.setVisibility(View.GONE);
        }

        // SIDE 3 DISH
        if( current_bento.side3 != null ) {
            Dish side3_dish = findDish(current_bento.side3);

            if ( side3_dish != null ) {
                if (side3_dish.isSoldOut(false)) {
                    flag_soldout_side_3.setVisibility(View.VISIBLE);
                }

                if (!side3_dish.image1.isEmpty()) {
                    Log.i(TAG, "side3_dish.image1: " + side3_dish.image1);
                    build_side3_label_textview.setVisibility(View.INVISIBLE);
                    build_side3_imageview.setVisibility(View.VISIBLE);
                    side3_title.setText(side3_dish.name.toUpperCase());
                    side3_title_container.setVisibility(View.VISIBLE);
                    Picasso.with(getApplicationContext())
                            .load(side3_dish.image1)
                            .placeholder(R.drawable.tmp_trans)
                            .error(R.drawable.tmp_trans)
                            .into(build_side3_imageview);
                } else {
                    build_side3_label_textview.setVisibility(View.INVISIBLE);
                    build_side3_imageview.setVisibility(View.VISIBLE);
                    build_side3_imageview.setImageResource(R.drawable.tmp_trans);
                    side3_title.setText(side3_dish.name.toUpperCase());
                    side3_title_container.setVisibility(View.VISIBLE);
                }
            }else{
                build_side3_label_textview.setVisibility(View.VISIBLE);
                build_side3_imageview.setVisibility(View.INVISIBLE);
                build_side3_imageview.setImageResource(R.drawable.tmp_trans);
                side3_title.setText("");
                side3_title_container.setVisibility(View.GONE);
            }
        }else{
            build_side3_label_textview.setVisibility(View.VISIBLE);
            build_side3_imageview.setVisibility(View.INVISIBLE);
            build_side3_imageview.setImageResource(R.drawable.tmp_trans);
            side3_title.setText("");
            side3_title_container.setVisibility(View.GONE);
        }

        // SIDE 4 DISH
        if( current_bento.side4 != null ) {
            Dish side4_dish = findDish(current_bento.side4);

            if ( side4_dish != null ) {
                if (side4_dish.isSoldOut(false)) {
                    flag_soldout_side_4.setVisibility(View.VISIBLE);
                }

                if (!side4_dish.image1.isEmpty()) {
                    Log.i(TAG, "side4_dish.image1: " + side4_dish.image1);
                    build_side4_label_textview.setVisibility(View.INVISIBLE);
                    build_side4_imageview.setVisibility(View.VISIBLE);
                    side4_title.setText(side4_dish.name.toUpperCase());
                    side4_title_container.setVisibility(View.VISIBLE);
                    Picasso.with(getApplicationContext())
                            .load(side4_dish.image1)
                            .placeholder(R.drawable.tmp_trans)
                            .error(R.drawable.tmp_trans)
                            .into(build_side4_imageview);
                } else {
                    build_side4_label_textview.setVisibility(View.INVISIBLE);
                    build_side4_imageview.setVisibility(View.VISIBLE);
                    build_side4_imageview.setImageResource(R.drawable.tmp_trans);
                    side4_title.setText(side4_dish.name.toUpperCase());
                    side4_title_container.setVisibility(View.VISIBLE);
                }
            }else{
                build_side4_label_textview.setVisibility(View.VISIBLE);
                build_side4_imageview.setVisibility(View.INVISIBLE);
                build_side4_imageview.setImageResource(R.drawable.tmp_trans);
                side4_title.setText("");
                side4_title_container.setVisibility(View.GONE);
            }
        }else{
            build_side4_label_textview.setVisibility(View.VISIBLE);
            build_side4_imageview.setVisibility(View.INVISIBLE);
            build_side4_imageview.setImageResource(R.drawable.tmp_trans);
            side4_title.setText("");
            side4_title_container.setVisibility(View.GONE);
        }
    }

    private void enableBtnAddAnotherBento() {
        Log.i(TAG,"enableBtnAddAnotherBento()");
        // btn continue
        btn_continue.setVisibility(View.INVISIBLE);
        btn_finalize_order.setVisibility(View.VISIBLE);
        //btn add another bento
        btn_add_another_bento_disabled.setVisibility(View.INVISIBLE);
        btn_add_another_bento.setVisibility(View.VISIBLE);
    }

    private void disableBtnAddAnotherBento() {
        Log.i(TAG,"disableBtnAddAnotherBento()");
        // btn continue
        btn_continue.setVisibility(View.VISIBLE);
        btn_finalize_order.setVisibility(View.INVISIBLE);
        // btn add another bento
        btn_add_another_bento_disabled.setVisibility(View.VISIBLE);
        btn_add_another_bento.setVisibility(View.INVISIBLE);
    }

    Dish findDish( String dish_id ){
        List<Dish> dishes = Dish.find(Dish.class, "_id=?", dish_id);
        Long main_dish_id = null;
        for (Dish dsh : dishes) {
            main_dish_id = dsh.getId();
        }
        return Dish.findById(Dish.class, main_dish_id);
    }

    private void initActionbar() {
        TextView actionbar_title = (TextView) findViewById(R.id.actionbar_title);
        actionbar_title.setText(getResources().getString(R.string.bento_builder_actionbar_title));

        ImageView actionbar_left_btn = (ImageView) findViewById(R.id.actionbar_left_btn);
        actionbar_left_btn.setImageResource(R.drawable.ic_ab_user);
        actionbar_left_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
                startActivity(intent);
                //finish();
                overridePendingTransitionGoRight();
            }
        });

        actionbar_right_btn = (ImageView)findViewById(R.id.actionbar_right_btn);
        actionbar_right_btn.setImageResource(R.drawable.ic_ab_bento);
        actionbar_right_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Item bento = Item.findById(Item.class, Bentonow.pending_bento_id);
                if ( bento != null && bento.isFull()){
                    finalizeOrder();
                }else{
                    showDialogForAutocompleteBento();
                }
            }
        });
    }

    private void initListeners() {
        //go to BentoSelectMainActivity
        LinearLayout build_main = (LinearLayout) findViewById(R.id.build_main);
        build_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bentonow.current_side = Config.SIDE.MAIN;
                startActivity(new Intent(getApplicationContext(), SelectMainActivity.class));
                overridePendingTransitionGoRight();
                //finish();
            }
        });

        // side 1 btn
        LinearLayout btn_bento_side_1 = (LinearLayout) findViewById(R.id.btn_bento_side_1);
        btn_bento_side_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bentonow.current_side = Config.SIDE.SIDE_1;
                startActivity(new Intent(getApplicationContext(), SelectSideActivity.class));
                overridePendingTransitionGoRight();
                //finish();
            }
        });

        // side 2 btn
        LinearLayout btn_bento_side_2 = (LinearLayout) findViewById(R.id.btn_bento_side_2);
        btn_bento_side_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bentonow.current_side = Config.SIDE.SIDE_2;
                startActivity(new Intent(getApplicationContext(), SelectSideActivity.class));
                overridePendingTransitionGoRight();
                //finish();
            }
        });

        // side 3 btn
        LinearLayout btn_bento_side_3 = (LinearLayout) findViewById(R.id.btn_bento_side_3);
        btn_bento_side_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bentonow.current_side = Config.SIDE.SIDE_3;
                startActivity(new Intent(getApplicationContext(), SelectSideActivity.class));
                overridePendingTransitionGoRight();
                //finish();
            }
        });

        // side 4 btn
        LinearLayout btn_bento_side_4 = (LinearLayout) findViewById(R.id.btn_bento_side_4);
        btn_bento_side_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bentonow.current_side = Config.SIDE.SIDE_4;
                startActivity(new Intent(getApplicationContext(), SelectSideActivity.class));
                overridePendingTransitionGoRight();
                //finish();
            }
        });


        //////////////////
        btn_add_another_bento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Bentonow.pending_bento_id: " + Bentonow.pending_bento_id);
                Item current_betno = Item.findById(Item.class, Bentonow.pending_bento_id);
                Log.i(TAG, "current_betno.isFull(): " + current_betno.isFull());
                disableBtnAddAnotherBento();
                if (current_betno.isFull()) {
                    track();
                    current_betno.completed = "yes";
                    current_betno.save();
                    createNewBentoBox();
                }
            }
        });

        //////////////////////
        btn_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Item bento = Item.findById(Item.class, Bentonow.pending_bento_id);
                if (bento == null) {
                    Log.i(TAG, "bento is NULL");
                    checkPendingBuildBento();
                    Item bento2 = Item.findById(Item.class, Bentonow.pending_bento_id);
                    if (bento2 != null && !bento2.isFull()) {
                        showDialogForAutocompleteBento();
                    }
                } else if (!bento.isFull()) {
                    if (bento.main == null) {
                        Bentonow.current_side = Config.SIDE.MAIN;
                        startActivity(new Intent(getApplicationContext(), SelectMainActivity.class));
                        overridePendingTransitionGoRight();
                    } else if (bento.side1 == null) {
                        Bentonow.current_side = Config.SIDE.SIDE_1;
                        startActivity(new Intent(getApplicationContext(), SelectSideActivity.class));
                        overridePendingTransitionGoRight();
                    } else if (bento.side2 == null) {
                        Bentonow.current_side = Config.SIDE.SIDE_2;
                        startActivity(new Intent(getApplicationContext(), SelectSideActivity.class));
                        overridePendingTransitionGoRight();
                    } else if (bento.side3 == null) {
                        Bentonow.current_side = Config.SIDE.SIDE_3;
                        startActivity(new Intent(getApplicationContext(), SelectSideActivity.class));
                        overridePendingTransitionGoRight();
                    } else if (bento.side4 == null) {
                        Bentonow.current_side = Config.SIDE.SIDE_4;
                        startActivity(new Intent(getApplicationContext(), SelectSideActivity.class));
                        overridePendingTransitionGoRight();
                    }
                    //showDialogForAutocompleteBento();
                } else {
                    Log.i(TAG, "bento is FULL");
                }
            }
        });

        btn_finalize_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Item bento = Item.findById(Item.class, Bentonow.pending_bento_id);
                if (bento.isFull()) {
                    finalizeOrder();
                } else {
                    showDialogForAutocompleteBento();
                }
            }
        });

        /// OVERLAY
        btn_no_complete_for_me.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Item last_item = Item.findById(Item.class,Bentonow.pending_bento_id);
                if ( last_item != null ) {
                    last_item.delete();
                }
                Bentonow.pending_bento_id = Orders.getLastItemId();
                overlay_autocomplete_bento.setVisibility(View.INVISIBLE);
                finalizeOrder();
            }
        });

        btn_yes_complete_for_me.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overlay_autocomplete_bento.setVisibility(View.INVISIBLE);
                autoCompletePendingBento();
            }

        });

    }

    private void autoCompletePendingBento() {

        Item autocomplete_bento = Item.findById(Item.class, Bentonow.pending_bento_id);

        if ( autocomplete_bento.main == null ) {
            List<Dish> main_dishes = Dish.find(Dish.class,"type=? and today = ? and qty!=?", new String[]{"main",todayDate,"0"}, null, "RANDOM()", "1");
            for( Dish dish : main_dishes ) {
                Log.i(TAG,"dish._id: "+dish._id);
                autocomplete_bento.main = dish._id;
                autocomplete_bento.save();

            }
        }else{
            Log.i(TAG,"Main is NO null");
        }


        List<Dish> sides_dishes = Dish.find(Dish.class,"type=? and today = ? and qty != ?", new String[]{"side",todayDate,"0"}, null, "RANDOM()", null);
        if( sides_dishes.isEmpty() )
            Log.i(TAG,"No dishes for today");

        if ( autocomplete_bento.side1 == null ) {
            for( Dish dish : sides_dishes ) {
                if( !dish.isSoldOut(true) && dish.canBeAdded() && !Arrays.asList(autocomplete_bento.sideItems()).contains(dish._id) ) {
                    Integer qty = Integer.valueOf(dish.qty);
                    if (qty > 0) {
                        Log.i(TAG, "String.valueOf(qty-1): " + String.valueOf(qty - 1));
                        autocomplete_bento.side1 = dish._id;
                        autocomplete_bento.save();
                    }
                }
            }
            if ( autocomplete_bento.side1 == null ) {
                for( Dish dish : sides_dishes ) {
                    if( !dish.isSoldOut(true) && dish.canBeAdded() ) {
                        Integer qty = Integer.valueOf(dish.qty);
                        if (qty > 0) {
                            Log.i(TAG, "String.valueOf(qty-1): " + String.valueOf(qty - 1));
                            autocomplete_bento.side1 = dish._id;
                            autocomplete_bento.save();
                        }
                    }
                }
            }
        }else{
            Log.i(TAG, "autocomplete_bento.side1: "+autocomplete_bento.side1);
        }

        List<Dish> sides_dishes2 = Dish.find(Dish.class, "type=? and today = ? and qty != ?", new String[]{"side", todayDate, "0"}, null, "RANDOM()", null);
        if( sides_dishes2.isEmpty() )
            Log.i(TAG,"No dishes for today 2");
        if ( autocomplete_bento.side2 == null ) {
            for( Dish dish : sides_dishes2 ) {
                if( !dish.isSoldOut(true) && dish.canBeAdded() && !Arrays.asList(autocomplete_bento.sideItems()).contains(dish._id) ){
                    Integer qty = Integer.valueOf(dish.qty);
                    if(qty>0) {
                        Log.i(TAG,"String.valueOf(qty-1): "+String.valueOf(qty-1));
                        autocomplete_bento.side2 = dish._id;
                        autocomplete_bento.save();
                    }
                }
            }
            if ( autocomplete_bento.side2 == null ) {
                for( Dish dish : sides_dishes2 ) {
                    if( !dish.isSoldOut(true) && dish.canBeAdded() ) {
                        Integer qty = Integer.valueOf(dish.qty);
                        if (qty > 0) {
                            Log.i(TAG, "String.valueOf(qty-1): " + String.valueOf(qty - 1));
                            autocomplete_bento.side2 = dish._id;
                            autocomplete_bento.save();
                        }
                    }
                }
            }
        }else{
            Log.i(TAG, "autocomplete_bento.side2: "+autocomplete_bento.side2);
        }

        List<Dish> sides_dishes3 = Dish.find(Dish.class, "type=? and today = ? and qty != ?", new String[]{"side", todayDate, "0"}, null, "RANDOM()", null);
        if( sides_dishes3.isEmpty() )
            Log.i(TAG,"No dishes for today 3");
        if ( autocomplete_bento.side3 == null ) {
            for( Dish dish : sides_dishes3 ) {
                if( !dish.isSoldOut(true) && dish.canBeAdded() && !Arrays.asList(autocomplete_bento.sideItems()).contains(dish._id) ){
                    Integer qty = Integer.valueOf(dish.qty);
                    if(qty>0) {
                        Log.i(TAG,"String.valueOf(qty-1): "+String.valueOf(qty-1));
                        autocomplete_bento.side3 = dish._id;
                        autocomplete_bento.save();
                    }
                }
            }
            if(autocomplete_bento.side3 == null){
                for( Dish dish : sides_dishes3 ) {
                    if ( !dish.isSoldOut(true) && dish.canBeAdded() ) {
                        Integer qty = Integer.valueOf(dish.qty);
                        if (qty > 0) {
                            Log.i(TAG, "String.valueOf(qty-1): " + String.valueOf(qty - 1));
                            autocomplete_bento.side3 = dish._id;
                            autocomplete_bento.save();
                        }
                    }
                }
            }
        }else{
            Log.i(TAG, "autocomplete_bento.side3: "+autocomplete_bento.side3);
        }

        List<Dish> sides_dishes4 = Dish.find(Dish.class, "type=? and today = ? and qty != ?", new String[]{"side", todayDate, "0"}, null, "RANDOM()", null);
        if( sides_dishes4.isEmpty() )
            Log.i(TAG,"No dishes for today 4");
        if ( autocomplete_bento.side4 == null ) {
            for( Dish dish : sides_dishes4 ) {
                if( !dish.isSoldOut(true) && dish.canBeAdded() && !Arrays.asList(autocomplete_bento.sideItems()).contains(dish._id) ){
                    Integer qty = Integer.valueOf(dish.qty);
                    if(qty>0) {
                        Log.i(TAG,"String.valueOf(qty-1): "+String.valueOf(qty-1));
                        autocomplete_bento.side4 = dish._id;
                        autocomplete_bento.save();
                    }
                }
            }
            if ( autocomplete_bento.side4 == null ) {
                for( Dish dish : sides_dishes4 ) {
                    if ( !dish.isSoldOut(true) && dish.canBeAdded() ) {
                        Integer qty = Integer.valueOf(dish.qty);
                        if (qty > 0) {
                            Log.i(TAG, "String.valueOf(qty-1): " + String.valueOf(qty - 1));
                            autocomplete_bento.side4 = dish._id;
                            autocomplete_bento.save();
                        }
                    }
                }
            }
        }else{
            Log.i(TAG, "autocomplete_bento.side4: "+autocomplete_bento.side4);
        }

        autocomplete_bento.save();
        resume(); // reload view
        //Config.processing_stock = false; // reset
        if( autocomplete_bento.isFull() ) {
            Bentonow.pending_bento_id = null; // reset
            finalizeOrder();
        }
    }

    private void goToCompleteOrder() {
        Intent intent = new Intent(getApplicationContext(), CompleteOrderActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransitionGoRight();
    }

    private void showDialogForAutocompleteBento() {
        overlay_autocomplete_bento.setVisibility(View.VISIBLE);
    }

    private void finalizeOrder() {
        track();
        User user = User.findById(User.class, (long) 1);
        if( user != null && user.apitoken != null && !user.apitoken.isEmpty() ){
            goToCompleteOrder();
        }else {
            Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
            startActivity(intent);
            overridePendingTransitionGoRight();
        }
    }

    private void track () {
        try {
            Item current_bento = Item.findById(Item.class, Bentonow.pending_bento_id);

            JSONObject params = new JSONObject();
            params.put("main", current_bento.main);
            params.put("side1", current_bento.side1);
            params.put("side2", current_bento.side2);
            params.put("side3", current_bento.side3);
            params.put("side4", current_bento.side4);

            Mixpanel.track(this, "Bento requested", params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
