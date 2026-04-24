package com.fittrack.model;

public abstract class SetRecord {
    private final String type;

    protected SetRecord(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public abstract String getFirstMetric();

    public abstract String getSecondMetric();

    public abstract Number getFirstMetricValue();

    public abstract Number getSecondMetricValue();

    public abstract double getWorkloadScore();

    public abstract SetRecord copy();
}
