package com.bentonow.bentonow;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.bentonow.bentonow.model.Dish;
import com.bentonow.bentonow.model.Item;
import com.bentonow.bentonow.model.Orders;
import com.bentonow.bentonow.model.User;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.List;


public class BuildBentoActivity extends BaseActivity {

    private static final String TAG = "BuildBentoActivity";
    private AQuery aq;
    private LinearLayout build_main;

    private ImageView main_img;

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
    private int bento_count = 0;
    private TextView btn_continue, btn_finalize_order;
    private RelativeLayout overlay_autocomplete_bento;
    private TextView btn_no_complete_for_me,btn_yes_complete_for_me;
    private ImageView actionbar_right_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_build_bento);
        Log.i(TAG, "onCreate");
        aq = new AQuery(this);
        initActionbar();
        changeViewsElemnts();
        initListeners();
    }

    private void changeViewsElemnts() {
        build_main_label_textview = (TextView)findViewById(R.id.build_main_label_textview);
        build_main_imageview = (ImageView)findViewById(R.id.build_main_imageview);

        build_side1_label_textview = (TextView)findViewById(R.id.build_side1_label_textview);
        build_side1_imageview = (ImageView)findViewById(R.id.build_side1_imageview);

        build_side2_label_textview = (TextView)findViewById(R.id.build_side2_label_textview);
        build_side2_imageview = (ImageView)findViewById(R.id.build_side2_imageview);

        build_side3_label_textview = (TextView)findViewById(R.id.build_side3_label_textview);
        build_side3_imageview = (ImageView)findViewById(R.id.build_side3_imageview);

        build_side4_label_textview = (TextView)findViewById(R.id.build_side4_label_textview);
        build_side4_imageview = (ImageView)findViewById(R.id.build_side4_imageview);

        btn_add_another_bento = (TextView)findViewById(R.id.btn_add_another_bento);
        btn_add_another_bento_disabled = (TextView)findViewById(R.id.btn_add_another_bento_disabled);

        bento_box_counter_textview = (TextView)findViewById(R.id.bento_box_counter_textview);

        btn_continue = (TextView)findViewById(R.id.btn_continue_inactive);
        btn_finalize_order = (TextView)findViewById(R.id.btn_continue_active);

        // OVERLAY
        overlay_autocomplete_bento = (RelativeLayout)findViewById(R.id.overlay_autocomplete_bento);
        btn_no_complete_for_me = (TextView)findViewById(R.id.btn_no_complete_for_me);
        btn_yes_complete_for_me = (TextView)findViewById(R.id.btn_yes_complete_for_me);
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
        Bentonow.pending_order_id = pending_order_id;
        Log.i(TAG, "Bentonow.pending_bento_id: " + Bentonow.pending_bento_id);
        if( Bentonow.pending_bento_id == null ) {
            List<Item> pending_bento = Item.find(Item.class, "orderid=?", new String[]{String.valueOf(Bentonow.pending_order_id)}, null, "id DESC", "1");
            if (pending_bento.isEmpty()) {
                createNewBentoBox();
            } else {
                for (Item item : pending_bento) {
                    Log.i(TAG, "Each pending item: " + pending_bento.toString());
                    bento_count++;
                    Bentonow.pending_bento_id = item.getId();
                    if (Bentonow.pending_bento_id != null) {
                        Item current_betno = Item.findById(Item.class, Bentonow.pending_bento_id);
                        showCurrentBento(current_betno);
                    } else {
                        createNewBentoBox();
                    }
                }
            }
        }else{
            Item current_betno = Item.findById(Item.class, Bentonow.pending_bento_id);
            showCurrentBento(current_betno);
        }
    }

    public void createNewBentoBox() {
        Log.i(TAG,"createNewBentoBox()");
        Item item = new Item();
        item.completed = "no";
        item.orderid = String.valueOf(Bentonow.pending_order_id);
        item.save();
        Bentonow.pending_bento_id = item.getId();
        showCurrentBento(item);
    }

    private void showCurrentBento(Item current_bento) {
        Log.i(TAG,"showCurrentBento(Item current_bento)");
        if( current_bento == null ){
            Log.i(TAG, "current_bento isNULL");
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

        // MAIN DISH
        if( current_bento.main != null ) {
            Dish main_dish = findDish(current_bento.main);
            if (!main_dish.image1.isEmpty()) {
                build_main_label_textview.setVisibility(View.INVISIBLE);
                build_main_imageview.setVisibility(View.VISIBLE);
                //aq.id(build_main_imageview).image(main_dish.image1, true, true, 0, 0, null, AQuery.FADE_IN_NETWORK, 1.0f);
                Picasso.with(getApplicationContext())
                        .load(main_dish.image1)
                        .placeholder(R.drawable.tmp_trans)
                        .error(R.drawable.tmp_trans)
                        .into(build_main_imageview);
            }
        }else{
            build_main_label_textview.setVisibility(View.VISIBLE);
            build_main_imageview.setVisibility(View.INVISIBLE);
        }

        // SIDE 1 DISH
        if( current_bento.side1 != null ) {
            Dish side1_dish = findDish(current_bento.side1);
            if (!side1_dish.image1.isEmpty()) {
                Log.i(TAG, "side1_dish.image1: " + side1_dish.image1);
                build_side1_label_textview.setVisibility(View.INVISIBLE);
                build_side1_imageview.setVisibility(View.VISIBLE);
                //aq.id(build_side1_imageview).image(side1_dish.image1, true, true, 0, 0, null, AQuery.FADE_IN_NETWORK, 1.0f);
                Picasso.with(getApplicationContext())
                        .load(side1_dish.image1)
                        .placeholder(R.drawable.tmp_trans)
                        .error(R.drawable.tmp_trans)
                        .into(build_side1_imageview);
            }
        }else{
            build_side1_label_textview.setVisibility(View.VISIBLE);
            build_side1_imageview.setVisibility(View.INVISIBLE);
        }

        // SIDE 2 DISH
        if( current_bento.side2 != null ) {
            Dish side2_dish = findDish(current_bento.side2);
            if (!side2_dish.image1.isEmpty()) {
                Log.i(TAG, "side2_dish.image1: " + side2_dish.image1);
                build_side2_label_textview.setVisibility(View.INVISIBLE);
                build_side2_imageview.setVisibility(View.VISIBLE);
                //aq.id(build_side2_imageview).image(side2_dish.image1, true, true, 0, 0, null, AQuery.FADE_IN_NETWORK, 1.0f);
                Picasso.with(getApplicationContext())
                        .load(side2_dish.image1)
                        .placeholder(R.drawable.tmp_trans)
                        .error(R.drawable.tmp_trans)
                        .into(build_side2_imageview);
            }
        }else{
            build_side2_label_textview.setVisibility(View.VISIBLE);
            build_side2_imageview.setVisibility(View.INVISIBLE);
        }

        // SIDE 3 DISH
        if( current_bento.side3 != null ) {
            Dish side3_dish = findDish(current_bento.side3);
            if (!side3_dish.image1.isEmpty()) {
                Log.i(TAG, "side3_dish.image1: " + side3_dish.image1);
                build_side3_label_textview.setVisibility(View.INVISIBLE);
                build_side3_imageview.setVisibility(View.VISIBLE);
                //aq.id(build_side3_imageview).image(side3_dish.image1, true, true, 0, 0, null, AQuery.FADE_IN_NETWORK, 1.0f);
                Picasso.with(getApplicationContext())
                        .load(side3_dish.image1)
                        .placeholder(R.drawable.tmp_trans)
                        .error(R.drawable.tmp_trans)
                        .into(build_side3_imageview);
            }
        }else{
            build_side3_label_textview.setVisibility(View.VISIBLE);
            build_side3_imageview.setVisibility(View.INVISIBLE);
        }

        // SIDE 4 DISH
        if( current_bento.side4 != null ) {
            Dish side4_dish = findDish(current_bento.side4);
            if (!side4_dish.image1.isEmpty()) {
                Log.i(TAG, "side4_dish.image1: " + side4_dish.image1);
                build_side4_label_textview.setVisibility(View.INVISIBLE);
                build_side4_imageview.setVisibility(View.VISIBLE);
                //aq.id(build_side4_imageview).image(side4_dish.image1, true, true, 0, 0, null, AQuery.FADE_IN_NETWORK, 1.0f);
                Picasso.with(getApplicationContext())
                        .load(side4_dish.image1)
                        .placeholder(R.drawable.tmp_trans)
                        .error(R.drawable.tmp_trans)
                        .into(build_side4_imageview);
            }
        }else{
            build_side4_label_textview.setVisibility(View.VISIBLE);
            build_side4_imageview.setVisibility(View.INVISIBLE);
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
                finish();
                overridePendingTransitionGoRight();
            }
        });

        actionbar_right_btn = (ImageView)findViewById(R.id.actionbar_right_btn);
        actionbar_right_btn.setImageResource(R.drawable.ic_ab_bento);
        actionbar_right_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Item bento = Item.findById(Item.class, Bentonow.pending_bento_id);
                if (bento.isFull()){
                    Intent intent = new Intent(getApplicationContext(), CompleteOrderActivity.class);
                    startActivity(intent);
                    finish();
                    overridePendingTransitionGoRight();
                }else{
                    showDialogForAutocompleteBento();
                }
            }
        });
    }

    private void initListeners() {
        //go to BentoSelectMainActivity
        build_main = (LinearLayout)findViewById(R.id.build_main);
        build_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SelectMainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out);
                finish();
            }
        });

        // side 1 btn
        LinearLayout btn_bento_side_1 = (LinearLayout) findViewById(R.id.btn_bento_side_1);
        btn_bento_side_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bentonow.current_side = 1;
                Intent intent = new Intent(getApplicationContext(),SelectSideActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out);
                finish();
            }
        });

        // side 2 btn
        LinearLayout btn_bento_side_2 = (LinearLayout) findViewById(R.id.btn_bento_side_2);
        btn_bento_side_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bentonow.current_side = 2;
                Intent intent = new Intent(getApplicationContext(), SelectSideActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out);
                finish();
            }
        });

        // side 3 btn
        LinearLayout btn_bento_side_3 = (LinearLayout) findViewById(R.id.btn_bento_side_3);
        btn_bento_side_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bentonow.current_side = 3;
                Intent intent = new Intent(getApplicationContext(),SelectSideActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out);
                finish();
            }
        });

        // side 4 btn
        LinearLayout btn_bento_side_4 = (LinearLayout) findViewById(R.id.btn_bento_side_4);
        btn_bento_side_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bentonow.current_side = 4;
                Intent intent = new Intent(getApplicationContext(),SelectSideActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out);
                finish();
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
                    current_betno.completed = "yes";
                    current_betno.save();
                    createNewBentoBox();
                } else {
                    Toast.makeText(getApplicationContext(), "Current Bento Box is not completed", Toast.LENGTH_LONG).show();
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
                    if (!bento2.isFull()) {
                        showDialogForAutocompleteBento();
                    }
                } else if (!bento.isFull()) {
                    showDialogForAutocompleteBento();
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
                overlay_autocomplete_bento.setVisibility(View.INVISIBLE);
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

        if( !autocomplete_bento.isFull() ) stockProcess();

        processHolder ph = new processHolder();

        if ( autocomplete_bento.main == null ) {
            List<Dish> main_dishes = Dish.find(Dish.class,"type=? and today = ? and qty!=?", new String[]{"main",todayDate,"0"}, null, "RANDOM()", "1");
            for( Dish dish : main_dishes ) {
                Log.i(TAG,"dish._id: "+dish._id);
                autocomplete_bento.main = dish._id;
            }
        }else{
            Log.i(TAG,"Main is NO null");
        }

        List<Dish> tmp = Dish.find(Dish.class,"type=?", new String[]{"side"}, null, "RANDOM()", null);
        for( Dish dish : tmp ) {
            Log.i(TAG,"dish: "+dish.toString());
        }

        List<Dish> sides_dishes = Dish.find(Dish.class,"type=? and today = ? and qty != ?", new String[]{"side",todayDate,"0"}, null, "RANDOM()", null);
        if( sides_dishes.isEmpty() )
            Log.i(TAG,"No dishes for today");

        if ( autocomplete_bento.side1 == null ) {
            for( Dish dish : sides_dishes ) {
                if( !Arrays.asList(autocomplete_bento.sideItems()).contains(dish._id) ) {
                    Integer qty = Integer.valueOf(dish.qty);
                    if (qty > 0) {
                        Log.i(TAG, "String.valueOf(qty-1): " + String.valueOf(qty - 1));
                        autocomplete_bento.side1 = dish._id;
                        dish.qty = String.valueOf(qty - 1);
                        dish.save();
                    }
                }
            }
            if ( autocomplete_bento.side1 == null ) {
                for( Dish dish : sides_dishes ) {
                    Integer qty = Integer.valueOf(dish.qty);
                    if(qty>0) {
                        Log.i(TAG,"String.valueOf(qty-1): "+String.valueOf(qty-1));
                        autocomplete_bento.side1 = dish._id;
                        dish.qty = String.valueOf(qty-1);
                        dish.save();
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
                if( !Arrays.asList(autocomplete_bento.sideItems()).contains(dish._id) ){
                    Integer qty = Integer.valueOf(dish.qty);
                    if(qty>0) {
                        Log.i(TAG,"String.valueOf(qty-1): "+String.valueOf(qty-1));
                        autocomplete_bento.side2 = dish._id;
                        dish.qty = String.valueOf(qty-1);
                        dish.save();
                    }
                }
            }
            if ( autocomplete_bento.side2 == null ) {
                for( Dish dish : sides_dishes2 ) {
                    Integer qty = Integer.valueOf(dish.qty);
                    if(qty>0) {
                        Log.i(TAG,"String.valueOf(qty-1): "+String.valueOf(qty-1));
                        autocomplete_bento.side2 = dish._id;
                        dish.qty = String.valueOf(qty-1);
                        dish.save();
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
                if( !Arrays.asList(autocomplete_bento.sideItems()).contains(dish._id) ){
                    Integer qty = Integer.valueOf(dish.qty);
                    if(qty>0) {
                        Log.i(TAG,"String.valueOf(qty-1): "+String.valueOf(qty-1));
                        autocomplete_bento.side3 = dish._id;
                        dish.qty = String.valueOf(qty-1);
                        dish.save();
                    }
                }
            }
            if(autocomplete_bento.side3 == null){
                for( Dish dish : sides_dishes3 ) {
                    Integer qty = Integer.valueOf(dish.qty);
                    if(qty>0) {
                        Log.i(TAG,"String.valueOf(qty-1): "+String.valueOf(qty-1));
                        autocomplete_bento.side3 = dish._id;
                        dish.qty = String.valueOf(qty-1);
                        dish.save();
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
                if( !Arrays.asList(autocomplete_bento.sideItems()).contains(dish._id) ){
                    Integer qty = Integer.valueOf(dish.qty);
                    if(qty>0) {
                        Log.i(TAG,"String.valueOf(qty-1): "+String.valueOf(qty-1));
                        autocomplete_bento.side4 = dish._id;
                        dish.qty = String.valueOf(qty-1);
                        dish.save();
                    }
                }
            }
            if ( autocomplete_bento.side4 == null ) {
                for( Dish dish : sides_dishes4 ) {
                    Integer qty = Integer.valueOf(dish.qty);
                    if(qty>0) {
                        Log.i(TAG,"String.valueOf(qty-1): "+String.valueOf(qty-1));
                        autocomplete_bento.side4 = dish._id;
                        dish.qty = String.valueOf(qty-1);
                        dish.save();
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

    private void stockProcess() {
        Log.i(TAG, "stockProcess()");
        //Config.processing_stock = true;
        List<Item> order_items = Item.find(Item.class, "orderid=?", String.valueOf(Bentonow.pending_order_id));
        Log.i(TAG,"order_items.size(): "+order_items.size());
        for( Item item : order_items ){
            if(item.isFull()) {
                Log.i(TAG,"item.isFull");
                processHolder ph = new processHolder();
                ph.mDish = Dish.findById(Dish.class, Dish.getIdBy_id(item.main));
                ph.mQty = Integer.valueOf(ph.mDish.qty);
                ph.mDish.qty = String.valueOf(ph.mQty - 1);
                ph.mDish.save();

                ph.s1Dish = Dish.findById(Dish.class, Dish.getIdBy_id(item.side1));
                ph.s1Qty = Integer.valueOf(ph.s1Dish.qty);
                if(ph.s1Qty>0) {
                    ph.s1Qty--;
                    ph.s1Dish.qty = String.valueOf(ph.s1Qty);
                    ph.s1Dish.save();
                }

                ph.s2Dish = Dish.findById(Dish.class, Dish.getIdBy_id(item.side2));
                ph.s2Qty = Integer.valueOf(ph.s2Dish.qty);
                if(ph.s2Qty>0) {
                    ph.s2Qty--;
                    ph.s2Dish.qty = String.valueOf(ph.s2Qty);
                    ph.s2Dish.save();
                }

                ph.s3Dish = Dish.findById(Dish.class, Dish.getIdBy_id(item.side3));
                ph.s3Qty = Integer.valueOf(ph.s3Dish.qty);
                if(ph.s3Qty>0) {
                    ph.s3Qty--;
                    ph.s3Dish.qty = String.valueOf(ph.s3Qty);
                    ph.s3Dish.save();
                }

                ph.s4Dish = Dish.findById(Dish.class, Dish.getIdBy_id(item.side4));
                ph.s4Qty = Integer.valueOf(ph.s4Dish.qty);
                if(ph.s4Qty>0) {
                    ph.s4Qty--;
                    ph.s4Dish.qty = String.valueOf(ph.s4Qty);
                    ph.s4Dish.save();
                }

            }else{
                Log.i(TAG,"item.isEmpty");
            }
        }
    }

    class processHolder {
        public Dish mDish;
        public Integer mQty;
        public Dish s1Dish;
        public Integer s1Qty;
        public Dish s2Dish;
        public Integer s2Qty;
        public Dish s3Dish;
        public Integer s3Qty;
        public Dish s4Dish;
        public Integer s4Qty;
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
        User user = User.findById(User.class, (long) 1);
        if( user != null && user.apitoken != null && !user.apitoken.isEmpty() ){
            goToCompleteOrder();
        }else {
            Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransitionGoRight();
        }
    }

}
