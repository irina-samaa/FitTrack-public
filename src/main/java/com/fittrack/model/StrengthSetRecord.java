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
    public String getFirstMetric() {
        return "Reps";
    }

    @Override
    public String getSecondMetric() {
        return "Weight (kg)";
    }

    @Override
    public Number getFirstMetricValue() {
        return reps;
    }

    @Override
    public Number getSecondMetricValue() {
        return weight;
    }

    @Override
    public double getWorkloadScore() {
        return reps * weight;
    }

    @Override
    public SetRecord copy() {
        return new StrengthSetRecord(reps, weight);
    }
}
