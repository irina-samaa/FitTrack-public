package com.fittrack.model;

import java.time.LocalDate;

public class BodyPartReminderRule {
    private final String bodyPartName;
    private final int thresholdDays;
    private final boolean enabled;
    private final LocalDate trackingStartDate;

    public BodyPartReminderRule(String bodyPartName, int thresholdDays) {
        this(bodyPartName, thresholdDays, true, LocalDate.now());
    }

    public BodyPartReminderRule(String bodyPartName, int thresholdDays, boolean enabled) {
        this(bodyPartName, thresholdDays, enabled, LocalDate.now());
    }

    public BodyPartReminderRule(String bodyPartName, int thresholdDays, boolean enabled, LocalDate trackingStartDate) {
        if (bodyPartName == null || bodyPartName.isBlank()) {
            throw new IllegalArgumentException("Body part name cannot be blank.");
        }
        if (thresholdDays <= 0) {
            throw new IllegalArgumentException("Threshold days must be greater than 0.");
        }
        if (trackingStartDate == null) {
            throw new IllegalArgumentException("Tracking start date cannot be null.");
        }
        this.bodyPartName = bodyPartName.trim();
        this.thresholdDays = thresholdDays;
        this.enabled = enabled;
        this.trackingStartDate = trackingStartDate;
    }

    public String getBodyPartName() {
        return bodyPartName;
    }

    public int getThresholdDays() {
        return thresholdDays;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public LocalDate getTrackingStartDate() {
        return trackingStartDate;
    }
}
