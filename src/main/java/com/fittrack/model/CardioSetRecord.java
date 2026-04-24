package com.fittrack.model;

public class CardioSetRecord extends SetRecord {
    private final int durationMinutes;
    private final double distanceKm;

    public CardioSetRecord(int durationMinutes, double distanceKm) {
        super("Cardio");
        this.durationMinutes = durationMinutes;
        this.distanceKm = distanceKm;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    @Override
    public String getPrimaryMetricLabel() {
        return "Duration (min)";
    }

    @Override
    public String getSecondaryMetricLabel() {
        return "Distance (km)";
    }

    @Override
    public Number getPrimaryMetricValue() {
        return durationMinutes;
    }

    @Override
    public Number getSecondaryMetricValue() {
        return distanceKm;
    }

    @Override
    public double getWorkloadScore() {
        return durationMinutes * distanceKm;
    }
}
