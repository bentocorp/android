package com.bentonow.bentonow.model;

import android.os.Parcel;
import android.os.Parcelable;

public class DishModel implements Parcelable {

    public static final String TAG = "DishModel";
    public static final String TAG_LIST = "DishModel List";

    public int itemId;
    public String name;
    public String description;
    public String type;
    public String image1;
    public int max_per_order;
    public double price;

    public DishModel() {

    }

    public DishModel(Parcel parcel) {
        itemId = parcel.readInt();
        name = parcel.readString();
        description = parcel.readString();
        type = parcel.readString();
        image1 = parcel.readString();
        max_per_order = parcel.readInt();
        price = parcel.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(itemId);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(type);
        dest.writeString(image1);
        dest.writeInt(max_per_order);
        dest.writeDouble(price);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DishModel> CREATOR = new Creator<DishModel>() {

        @Override
        public DishModel[] newArray(int size) {
            return new DishModel[size];
        }

        @Override
        public DishModel createFromParcel(Parcel source) {
            return new DishModel(source);
        }
    };
}