package com.bentonow.bentonow;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bentonow.bentonow.Utils.Mixpanel;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class NextDayMenuActivity extends BaseActivity {

    private static final String TAG = "NextDayMenuActivity";
    private JSONArray MenuItems;
    private LayoutInflater inflater;
    private String title;
    private Activity _this;
    private String activeRow = "";
    OIListAdapter sideAdapter;
    Holder[] mainListItems;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");
        _this = this;
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

        Mixpanel.track(this, "Previewed Today's Menu");

        initActionbar();
    }

    private static class Holder {
        public ImageView img;
        public TextView main_title;
        public TextView desc_title;
        public RelativeLayout overlay_menu_detail;
        public TextView main_menu_item_description;

    }

    private void listItems(JSONArray menuItems) {
        LinearLayout list = (LinearLayout)findViewById(R.id.tomorrow_main_dishes_container);
        mainListItems = new Holder[menuItems.length()];
        for( int i = 0; i < menuItems.length(); i++ ){
            try {
                JSONObject row = menuItems.getJSONObject(i);

                if(row.getString("type").equals("main")){
                    View vi = inflater.inflate(R.layout.inc_menu_item_side, null);
                    Holder holder = new Holder();
                    holder.img = (ImageView) vi.findViewById(R.id.menu_item_image);
                    holder.main_title = (TextView) vi.findViewById(R.id.main_menu_item_name);
                    holder.desc_title = (TextView) vi.findViewById(R.id.main_menu_item_name_2);
                    holder.overlay_menu_detail = (RelativeLayout) vi.findViewById(R.id.overlay_menu_detail);
                    holder.main_menu_item_description = (TextView)vi.findViewById(R.id.main_menu_item_description);
                    vi.findViewById(R.id.btn_added).setVisibility(View.GONE);
                    vi.findViewById(R.id.btn_add_to_bento).setVisibility(View.GONE);
                    vi.findViewById(R.id.btn_add_to_bento_solded).setVisibility(View.GONE);

                    holder.main_title.setText(row.getString(Config.DISH.NAME));
                    holder.desc_title.setText(row.getString(Config.DISH.NAME));
                    holder.main_menu_item_description.setText(row.getString(Config.DISH.DESCRIPTION));
                    if ( row.getString(Config.DISH.IMAGE1)!=null && !row.getString(Config.DISH.IMAGE1).isEmpty() ) {
                        Picasso.with(getApplicationContext())
                                .load(row.getString(Config.DISH.IMAGE1))
                                .placeholder(R.drawable.tmp_trans)
                                .error(R.drawable.tmp_trans)
                                .into(holder.img);
                    }

                    final int position = i;

                    holder.main_title.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            notifyAllDataSetChanged("m" + position);
                        }
                    });

                    holder.overlay_menu_detail.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            notifyAllDataSetChanged("");
                        }
                    });

                    mainListItems[i] = holder;

                    list.addView(vi);
                } else {
                    mainListItems[i] = null;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        getSideList();
    }

    private void notifyAllDataSetChanged (String row) {
        activeRow = row;

        sideAdapter.notifyDataSetChanged();

        for (int i=0; i < mainListItems.length; ++i) {
            if (mainListItems[i] == null) continue;
            else if (activeRow.equals("m" + i)) {
                mainListItems[i].main_title.setVisibility(View.GONE);
                mainListItems[i].overlay_menu_detail.setVisibility(View.VISIBLE);
            } else {
                mainListItems[i].main_title.setVisibility(View.VISIBLE);
                mainListItems[i].overlay_menu_detail.setVisibility(View.GONE);
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
                finish();
                overridePendingTransitionGoLeft();
            }
        });
    }

    public void getSideList(){
        ArrayList<HashMap<String, String>> sideMenuList = new ArrayList<>();

        for( int i = 0; i < MenuItems.length(); i++ ){
            try {
                JSONObject row = MenuItems.getJSONObject(i);
                if(row.getString("type").equals("side")){
                    HashMap<String, String> map = new HashMap<>();
                    map.put(Config.DISH.NAME, row.getString(Config.DISH.NAME).toUpperCase());
                    map.put(Config.DISH.DESCRIPTION, row.getString(Config.DISH.DESCRIPTION));
                    map.put(Config.DISH.IMAGE1, row.getString(Config.DISH.IMAGE1));
                    sideMenuList.add(map);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        GridView SideListView = (GridView) findViewById(R.id.menu_item_side_listview);
        sideAdapter = new OIListAdapter(_this, sideMenuList);
        SideListView.setAdapter(sideAdapter);
    }

    public class OIListAdapter extends BaseAdapter {

        private static final String TAG = "OIListAdapter";
        private final ArrayList<HashMap<String, String>> data;
        private final Activity activity;
        private final LayoutInflater inflater;


        public OIListAdapter(Activity a, ArrayList<HashMap<String, String>> datos) {
            data = datos;
            activity = a;
            inflater = (LayoutInflater) a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return data.size();
        }

        public Object getItem(int i) {
            return i;
        }

        public long getItemId(int i) {
            return i;
        }
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;
            Holder holder = new Holder();
            if (view == null) {
                view = inflater.inflate(R.layout.inc_menu_item_side, null);
                holder.img = (ImageView) view.findViewById(R.id.menu_item_image);
                holder.main_title = (TextView) view.findViewById(R.id.main_menu_item_name);
                holder.desc_title = (TextView) view.findViewById(R.id.main_menu_item_name_2);
                holder.overlay_menu_detail = (RelativeLayout) view.findViewById(R.id.overlay_menu_detail);
                holder.main_menu_item_description = (TextView)view.findViewById(R.id.main_menu_item_description);
                view.findViewById(R.id.btn_added).setVisibility(View.GONE);
                view.findViewById(R.id.btn_add_to_bento).setVisibility(View.GONE);
                view.findViewById(R.id.btn_add_to_bento_solded).setVisibility(View.GONE);
                view.setTag(holder);
            } else {
                holder = (Holder) view.getTag();
            }

            try {
                holder.row = data.get(position);
                Log.i(TAG, "row: " + holder.row.toString());
                String title = "";
                String description = "";
                try {
                    title = holder.row.get(Config.DISH.NAME).toUpperCase();
                    description = holder.row.get(Config.DISH.DESCRIPTION);
                }catch (NullPointerException ignored){

                }

                holder.main_title.setText(title);
                holder.desc_title.setText(title);
                holder.main_menu_item_description.setText(description);

                if(!holder.row.get(Config.DISH.IMAGE1).isEmpty()) {
                    Picasso.with(activity)
                            .load(holder.row.get(Config.DISH.IMAGE1))
                            .placeholder(R.drawable.tmp_trans)
                            .error(R.drawable.tmp_trans)
                            .into(holder.img);
                }

                if (activeRow.equals("s" + position)) {
                    holder.main_title.setVisibility(View.GONE);
                    holder.overlay_menu_detail.setVisibility(View.VISIBLE);
                } else {
                    holder.main_title.setVisibility(View.VISIBLE);
                    holder.overlay_menu_detail.setVisibility(View.GONE);
                }

                holder.main_title.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        notifyAllDataSetChanged("s" + position);
                    }
                });
                holder.overlay_menu_detail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        notifyAllDataSetChanged("");
                    }
                });
            }catch (IndexOutOfBoundsException ignore){
                Log.i(TAG,"Col 1 empty");
                holder.main_title.setText("");
                holder.img.setImageResource(R.drawable.tmp_trans);
            }

            return view;
        }

        private class Holder {
            public ImageView img;
            public TextView main_title;
            public TextView desc_title;
            public RelativeLayout overlay_menu_detail;
            public TextView main_menu_item_description;
            public HashMap<String, String> row;
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransitionGoLeft();
    }
}
