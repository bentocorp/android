package com.bentonow.bentonow.model.gatekeeper;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by kokusho on 1/18/16.
 */
public class MealTypeModel {
    public static final String TAG = "MealTypeModel";

    public Hash two;
    public Hash three;
    public ArrayList<String> ordering = new ArrayList<>();

    public Hash getTwo() {
        return two;
    }

    public void setTwo(Hash two) {
        this.two = two;
    }

    public Hash getThree() {
        return three;
    }

    public void setThree(Hash three) {
        this.three = three;
    }

    public ArrayList<String> getOrdering() {
        return ordering;
    }

    public void setOrdering(ArrayList<String> ordering) {
        this.ordering = ordering;
    }
}
