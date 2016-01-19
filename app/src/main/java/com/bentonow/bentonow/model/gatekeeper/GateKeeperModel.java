package com.bentonow.bentonow.model.gatekeeper;

/**
 * Created by kokusho on 1/18/16.
 */
public class GateKeeperModel {
    public static String TAG = "";

    private boolean isInAnyZone;
    private boolean onDemand;
    private boolean orderAhead;
    private boolean hasServices;
    private boolean availableServicesOndemand;
    private MealTypeModel mealTypes;
    private String appState = "";
    private String CurrentMealType = "";
    private AppOnDemandWidgetModel appOnDemandWidget;


    public boolean isInAnyZone() {
        return isInAnyZone;
    }

    public void setIsInAnyZone(boolean isInAnyZone) {
        this.isInAnyZone = isInAnyZone;
    }

    public boolean isOnDemand() {
        return onDemand;
    }

    public void setOnDemand(boolean onDemand) {
        this.onDemand = onDemand;
    }

    public boolean isOrderAhead() {
        return orderAhead;
    }

    public void setOrderAhead(boolean orderAhead) {
        this.orderAhead = orderAhead;
    }

    public boolean isHasServices() {
        return hasServices;
    }

    public void setHasServices(boolean hasServices) {
        this.hasServices = hasServices;
    }

    public boolean isAvailableServicesOndemand() {
        return availableServicesOndemand;
    }

    public void setAvailableServicesOndemand(boolean availableServicesOndemand) {
        this.availableServicesOndemand = availableServicesOndemand;
    }

    public MealTypeModel getMealTypes() {
        return mealTypes;
    }

    public void setMealTypes(MealTypeModel mealTypes) {
        this.mealTypes = mealTypes;
    }

    public AppOnDemandWidgetModel getAppOnDemandWidget() {
        return appOnDemandWidget;
    }

    public void setAppOnDemandWidget(AppOnDemandWidgetModel appOnDemandWidget) {
        this.appOnDemandWidget = appOnDemandWidget;
    }

    public String getAppState() {
        return appState;
    }

    public void setAppState(String appState) {
        this.appState = appState;
    }

    public String getCurrentMealType() {
        return CurrentMealType;
    }

    public void setCurrentMealType(String currentMealType) {
        CurrentMealType = currentMealType;
    }
}
