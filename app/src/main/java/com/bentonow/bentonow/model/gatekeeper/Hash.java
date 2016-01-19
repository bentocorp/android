package com.bentonow.bentonow.model.gatekeeper;

/**
 * Created by kokusho on 1/18/16.
 */
public class Hash {
    public static final String TAG = "Hash";

    private String pkMealType = "";
    private String name = "";
    private String order = "";
    private String active = "";
    private String startTime = "";
    private String endTime = "";
    private String oaCutoff = "";
    private String displayStartTime = "";

    public String getPkMealType() {
        return pkMealType;
    }

    public void setPkMealType(String pkMealType) {
        this.pkMealType = pkMealType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getActive() {
        return active;
    }

    public void setAtive(String active) {
        this.active = active;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getOaCutoff() {
        return oaCutoff;
    }

    public void setOaCutoff(String oaCutoff) {
        this.oaCutoff = oaCutoff;
    }

    public String getDisplayStartTime() {
        return displayStartTime;
    }

    public void setDisplayStartTime(String displayStartTime) {
        this.displayStartTime = displayStartTime;
    }
}
