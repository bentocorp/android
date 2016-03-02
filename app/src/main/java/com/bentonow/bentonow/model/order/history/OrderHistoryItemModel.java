package com.bentonow.bentonow.model.order.history;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by kokusho on 2/1/16.
 */
public class OrderHistoryItemModel implements Parcelable {
    public static final String TAG = "OrderHistoryItemModel";

    private String title;
    private String price;
    private String driverId;
    @SerializedName("long")
    private String lng;
    private String lat;
    private String order_status;

    public OrderHistoryItemModel() {

    }

    public OrderHistoryItemModel(Parcel parcel) {
        title = parcel.readString();
        price = parcel.readString();
        driverId = parcel.readString();
        lng = parcel.readString();
        lat = parcel.readString();
        order_status = parcel.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(price);
        dest.writeString(driverId);
        dest.writeString(lng);
        dest.writeString(lat);
        dest.writeString(order_status);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<OrderHistoryItemModel> CREATOR = new Creator<OrderHistoryItemModel>() {

        @Override
        public OrderHistoryItemModel[] newArray(int size) {
            return new OrderHistoryItemModel[size];
        }

        @Override
        public OrderHistoryItemModel createFromParcel(Parcel source) {
            return new OrderHistoryItemModel(source);
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getOrder_status() {
        return order_status;
    }

    public void setOrder_status(String order_status) {
        this.order_status = order_status;
    }
}
