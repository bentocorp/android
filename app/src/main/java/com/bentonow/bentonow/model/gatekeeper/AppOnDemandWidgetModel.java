package com.bentonow.bentonow.model.gatekeeper;

/**
 * Created by kokusho on 1/18/16.
 */
public class AppOnDemandWidgetModel {
    public static final String TAG = "AppOnDemandWidgetModel";

    private boolean selected;
    private String title = "";
    private String text = "";
    private String state = "";
    private String mealMode = "";

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getMealMode() {
        return mealMode;
    }

    public void setMealMode(String mealMode) {
        this.mealMode = mealMode;
    }
}
