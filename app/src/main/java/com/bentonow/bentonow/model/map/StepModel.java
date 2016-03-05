package com.bentonow.bentonow.model.map;

/**
 * Created by kokusho on 3/4/16.
 */
public class StepModel {

    private int distance;
    private int duration;
    private String polyline;
    private double start_location_lat;
    private double start_location_lng;
    private double end_location_lat;
    private double end_location_lng;

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

    public String getPolyline() {
        return polyline;
    }

    public void setPolyline(String polyline) {
        this.polyline = polyline;
    }

    public double getStart_location_lat() {
        return start_location_lat;
    }

    public void setStart_location_lat(double start_location_lat) {
        this.start_location_lat = start_location_lat;
    }

    public double getStart_location_lng() {
        return start_location_lng;
    }

    public void setStart_location_lng(double start_location_lng) {
        this.start_location_lng = start_location_lng;
    }

    public double getEnd_location_lat() {
        return end_location_lat;
    }

    public void setEnd_location_lat(double end_location_lat) {
        this.end_location_lat = end_location_lat;
    }

    public double getEnd_location_lng() {
        return end_location_lng;
    }

    public void setEnd_location_lng(double end_location_lng) {
        this.end_location_lng = end_location_lng;
    }
}
