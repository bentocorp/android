package com.bentonow.bentonow.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.bentonow.bentonow.model.menu.TimesModel;

import java.util.ArrayList;
import java.util.List;

public class Menu implements Parcelable {
    public static final String TAG = "ModelMenu";

    public String menu_id = "";
    public String name = "";
    public String for_date = "";
    public String bgimg = "";
    public String menu_type = "";
    public String meal_type = "";
    public String meal_name = "";
    public String meal_order = "";
    public String day_text = "";
    public String day_text2 = "";
    public String displayStartTime = "";
    public List<DishModel> dishModels = new ArrayList<>();
    public List<DishModel> oaItems = new ArrayList<>();
    public List<TimesModel> listTimeModel = new ArrayList<>();

    public Menu() {

    }

    public Menu(Parcel parcel) {
        menu_id = parcel.readString();
        name = parcel.readString();
        for_date = parcel.readString();
        bgimg = parcel.readString();
        menu_type = parcel.readString();
        meal_type = parcel.readString();
        meal_name = parcel.readString();
        meal_order = parcel.readString();
        day_text = parcel.readString();
        day_text2 = parcel.readString();
        displayStartTime = parcel.readString();
        dishModels = new ArrayList<>();
        oaItems = new ArrayList<>();
        listTimeModel = new ArrayList<>();
        parcel.readList(dishModels, DishModel.class.getClassLoader());
        parcel.readList(oaItems, DishModel.class.getClassLoader());
        parcel.readList(listTimeModel, TimesModel.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(menu_id);
        dest.writeString(name);
        dest.writeString(for_date);
        dest.writeString(bgimg);
        dest.writeString(menu_type);
        dest.writeString(meal_type);
        dest.writeString(meal_name);
        dest.writeString(meal_order);
        dest.writeString(day_text);
        dest.writeString(day_text2);
        dest.writeString(displayStartTime);
        dest.writeList(dishModels);
        dest.writeList(oaItems);
        dest.writeList(listTimeModel);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Menu> CREATOR = new Creator<Menu>() {

        @Override
        public Menu[] newArray(int size) {
            return new Menu[size];
        }

        @Override
        public Menu createFromParcel(Parcel source) {
            return new Menu(source);
        }
    };
}
