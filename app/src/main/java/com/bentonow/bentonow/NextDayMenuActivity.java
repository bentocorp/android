package com.bentonow.bentonow;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class NextDayMenuActivity extends BaseActivity {

    private static final String TAG = "NextDayMenuActivity";
    private JSONArray MenuItems;
    private LayoutInflater inflater;
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG,"onCreate()");
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        setContentView(R.layout.activity_next_day_menu);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        try {
            title = String.valueOf(extras.get("title"));
            JSONObject json = new JSONObject(String.valueOf(extras.get("json")));
            Log.i(TAG,"json: "+json.toString());
            MenuItems = json.getJSONArray("MenuItems");
            listItems(MenuItems);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        initActionbar();
    }

    private static class Holder {
        public ImageView img;
        public TextView main_title;
        public ImageView img_2;
        public TextView main_title_2;
    }

    private void listItems(JSONArray menuItems) {
        LinearLayout list = (LinearLayout)findViewById(R.id.tomorrow_main_dishes_container);
        for( int i = 0; i < menuItems.length(); i++ ){
            try {
                JSONObject row = menuItems.getJSONObject(i);
                if(row.getString("type").equals("main")){
                    View vi = inflater.inflate(R.layout.inc_menu_item_row, null);
                    Holder holder = new Holder();
                    holder.img = (ImageView) vi.findViewById(R.id.menu_item_image);
                    holder.main_title = (TextView) vi.findViewById(R.id.main_menu_item_name);

                    holder.main_title.setText(row.getString("name").toUpperCase());
                    Picasso.with(getApplicationContext())
                            .load(row.getString("image1"))
                            .placeholder(R.drawable.tmp_trans)
                            .error(R.drawable.tmp_trans)
                            .into(holder.img);

                    list.addView(vi);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        ////////////////////////////



        ////////////////////////////

        LinearLayout sideList = (LinearLayout)findViewById(R.id.tomorrow_side_dishes_container);
        for( int i = 0; i < menuItems.length(); i++ ){
            Log.i(TAG,"for( int i = "+(i-1)+"; i < menuItems.length("+menuItems.length()+"); i = "+i+" )");
            try {
                JSONObject row = menuItems.getJSONObject(i);
                View vi = inflater.inflate(R.layout.inc_menu_item_side, null);
                if(row.getString("type").equals("side")){
                    Holder holder = new Holder();
                    holder.img = (ImageView) vi.findViewById(R.id.menu_item_image);
                    holder.main_title = (TextView) vi.findViewById(R.id.main_menu_item_name);

                    holder.main_title.setText(row.getString("name").toUpperCase());
                    Picasso.with(getApplicationContext())
                            .load(row.getString("image1"))
                            .placeholder(R.drawable.tmp_trans)
                            .error(R.drawable.tmp_trans)
                            .into(holder.img);

                    //sideList.addView(vi);
                }

                /////////////////////////
                i++;
                JSONObject row2 = menuItems.getJSONObject(i);
                if(row2.getString("type").equals("side")){
                    //View vi = inflater.inflate(R.layout.inc_menu_item_side, null);
                    Holder holder = new Holder();
                    holder.img_2 = (ImageView) vi.findViewById(R.id.menu_item_image2);
                    holder.main_title_2 = (TextView) vi.findViewById(R.id.main_menu_item_name2);

                    holder.main_title_2.setText(row2.getString("name").toUpperCase());
                    Picasso.with(getApplicationContext())
                            .load(row2.getString("image1"))
                            .placeholder(R.drawable.tmp_trans)
                            .error(R.drawable.tmp_trans)
                            .into(holder.img_2);

                    sideList.addView(vi);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private void initActionbar() {
        Log.i(TAG, "initActionbar()");
        TextView actionbar_title = (TextView) findViewById(R.id.actionbar_title);
        actionbar_title.setText(title);

        ImageView actionbar_left_btn = (ImageView) findViewById(R.id.actionbar_left_btn);
        actionbar_left_btn.setImageResource(R.drawable.ic_ab_back);
        actionbar_left_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Config.aux_deduct_day --;
                finish();
                overridePendingTransitionGoLeft();
            }
        });
    }

}
