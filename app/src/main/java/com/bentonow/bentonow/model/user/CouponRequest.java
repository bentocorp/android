package com.bentonow.bentonow.model.user;

import android.os.Parcel;
import android.os.Parcelable;

public class CouponRequest implements Parcelable {

    public static final String TAG = "CouponRequest";

    public String reason;
    public String email;
    public String lat;
    public String lng;
    public String address;
    public String platform = "Android";

    public CouponRequest() {

    }

    public CouponRequest(Parcel parcel) {
        reason = parcel.readString();
        email = parcel.readString();
        lat = parcel.readString();
        lng = parcel.readString();
        address = parcel.readString();
        platform = parcel.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(reason);
        dest.writeString(email);
        dest.writeString(lat);
        dest.writeString(lng);
        dest.writeString(address);
        dest.writeString(platform);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CouponRequest> CREATOR = new Creator<CouponRequest>() {

        @Override
        public CouponRequest[] newArray(int size) {
            return new CouponRequest[size];
        }

        @Override
        public CouponRequest createFromParcel(Parcel source) {
            return new CouponRequest(source);
        }
    };
}
