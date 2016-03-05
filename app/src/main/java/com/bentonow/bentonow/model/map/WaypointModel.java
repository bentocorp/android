package com.bentonow.bentonow.model.map;

import java.util.ArrayList;

/**
 * Created by kokusho on 3/4/16.
 */
public class WaypointModel {

    private int distance;
    private int duration;
    private ArrayList<StepModel> aSteps = new ArrayList<>();
    private String points;

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public ArrayList<StepModel> getaSteps() {
        return aSteps;
    }

    public void setaSteps(ArrayList<StepModel> aSteps) {
        this.aSteps = aSteps;
    }

    public String getPoints() {
        return points;
    }

    public void setPoints(String points) {
        this.points = points;
    }
}
