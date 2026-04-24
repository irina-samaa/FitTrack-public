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
    public String getFirstMetric() {
        return "Duration (min)";
    }

    @Override
    public String getSecondMetric() {
        return "Distance (km)";
    }

    @Override
    public Number getFirstMetricValue() {
        return durationMinutes;
    }

    @Override
    public Number getSecondMetricValue() {
        return distanceKm;
    }

    @Override
    public double getWorkloadScore() {
        return durationMinutes * distanceKm;
    }

    @Override
    public SetRecord copy() {
        return new CardioSetRecord(durationMinutes, distanceKm);
    }
}
