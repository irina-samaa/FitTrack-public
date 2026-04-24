package com.fittrack.model;

public class StrengthSetRecord extends SetRecord {
    private final int reps;
    private final double weight;

    public StrengthSetRecord(int reps, double weight) {
        super("Strength");
        this.reps = reps;
        this.weight = weight;
    }

    public int getReps() {
        return reps;
    }

    public double getWeight() {
        return weight;
    }

    @Override
    public String getPrimaryMetricLabel() {
        return "Reps";
    }

    @Override
    public String getSecondaryMetricLabel() {
        return "Weight (kg)";
    }

    @Override
    public Number getPrimaryMetricValue() {
        return reps;
    }

    @Override
    public Number getSecondaryMetricValue() {
        return weight;
    }

    @Override
    public double getWorkloadScore() {
        return reps * weight;
    }
}
