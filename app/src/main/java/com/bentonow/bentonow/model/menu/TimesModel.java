package com.bentonow.bentonow.model.menu;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by kokusho on 1/21/16.
 */
public class TimesModel implements Parcelable {
    static final String TAG = "TimesModel";

    public String start = "";
    public String end = "";
    public boolean available;
    public String delivery_price = "";
    public boolean isSelected;
    public boolean isDefault;


    public TimesModel() {

    }

    public TimesModel(Parcel parcel) {
        start = parcel.readString();
        end = parcel.readString();
        available = parcel.readInt() == 1;
        delivery_price = parcel.readString();
        isSelected = parcel.readInt() == 1;
        isDefault = parcel.readInt() == 1;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(start);
        dest.writeString(end);
        dest.writeInt(available ? 1 : 0);
        dest.writeString(delivery_price);
        dest.writeInt(isSelected ? 1 : 0);
        dest.writeInt(isDefault ? 1 : 0);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TimesModel> CREATOR = new Creator<TimesModel>() {

        @Override
        public TimesModel[] newArray(int size) {
            return new TimesModel[size];
        }

        @Override
        public TimesModel createFromParcel(Parcel source) {
            return new TimesModel(source);
        }
    };
}
