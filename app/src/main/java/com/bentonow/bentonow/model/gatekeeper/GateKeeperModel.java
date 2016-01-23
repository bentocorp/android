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
    private AvailableServicesModel mAvailableServices = new AvailableServicesModel();
    private MealTypeModel mealTypes = new MealTypeModel();
    private String appState = "";
    private String CurrentMealType = "";
    private AppOnDemandWidgetModel appOnDemandWidget = new AppOnDemandWidgetModel();


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

    public AvailableServicesModel getAvailableServices() {
        return mAvailableServices;
    }

    public void setAvailableServices(AvailableServicesModel mAvailableServices) {
        this.mAvailableServices = mAvailableServices;
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
