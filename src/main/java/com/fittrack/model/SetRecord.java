package com.fittrack.model;

public abstract class SetRecord {
    private final String type;

    protected SetRecord(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public abstract String getPrimaryMetricLabel();

    public abstract String getSecondaryMetricLabel();

    public abstract Number getPrimaryMetricValue();

    public abstract Number getSecondaryMetricValue();

    public abstract double getWorkloadScore();
}
