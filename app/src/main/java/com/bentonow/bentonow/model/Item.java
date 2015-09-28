package com.bentonow.bentonow.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Item implements Parcelable {

    public static final String TAG = "DishModel";
    public static final String TAG_LIST = "DishModel List";

    public int itemId;
    public String name;
    public String description;
    public String type;
    public String image1;
    public int max_per_order;

    public Item() {

    }

    public Item(Parcel parcel) {
        itemId = parcel.readInt();
        name = parcel.readString();
        description = parcel.readString();
        type = parcel.readString();
        image1 = parcel.readString();
        max_per_order = parcel.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(itemId);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(type);
        dest.writeString(image1);
        dest.writeInt(max_per_order);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Item> CREATOR = new Creator<Item>() {

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }

        @Override
        public Item createFromParcel(Parcel source) {
            return new Item(source);
        }
    };

    public boolean isSoldOut(boolean countCurrent) {
        boolean value = Stock.isSold(itemId, countCurrent);
        Log.i(TAG, "isSoldOut " + name + " " + (value ? "YES" : "NO"));
        return value;
    }

    public boolean canBeAdded() {
        boolean value = max_per_order > Order.countItemsById(itemId);
        Log.i(TAG, "canBeAdded " + name + " " + (value ? "YES" : "NO"));
        return value;
    }

    public static Item getFirstAvailable(String type, int[] tryExcludeIds) {
        Menu menu = Menu.get();

        if (menu != null) {
            List<Item> aDishes = new ArrayList<>();

            for (int a = 0; a < menu.items.size(); a++) {
                aDishes.add(menu.items.get(a).clone());
            }

            Collections.shuffle(aDishes);

            if (tryExcludeIds != null) {
                String ids = Arrays.toString(tryExcludeIds);

                for (Item item : aDishes) {
                    if (item.type.equals(type) && !item.isSoldOut(true) && item.canBeAdded() && !ids.contains("" + item.itemId))
                        return item;
                }
            }

            for (Item item : aDishes) {
                if (item.type.equals(type) && !item.isSoldOut(true) && item.canBeAdded())
                    return item;
            }
        }

        return null;
    }

    public Item clone() {
        Gson gson = new GsonBuilder().serializeNulls().create();
        return gson.fromJson(gson.toJson(this), Item.class);
    }
}
