package com.fittrack.model;

public class EnduranceSetRecord extends SetRecord {
    private final int durationMinutes;
    private final int heartRate;

    public EnduranceSetRecord(int durationMinutes, int heartRate) {
        super("Endurance");
        this.durationMinutes = durationMinutes;
        this.heartRate = heartRate;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public int getHeartRate() {
        return heartRate;
    }

    @Override
    public String getFirstMetric() {
        return "Duration (min)";
    }

    @Override
    public String getSecondMetric() {
        return "Heart Rate";
    }

    @Override
    public Number getFirstMetricValue() {
        return durationMinutes;
    }

    @Override
    public Number getSecondMetricValue() {
        return heartRate;
    }

    @Override
    public double getWorkloadScore() {
        return durationMinutes * heartRate;
    }

    @Override
    public SetRecord copy() {
        return new EnduranceSetRecord(durationMinutes, heartRate);
    }
}
