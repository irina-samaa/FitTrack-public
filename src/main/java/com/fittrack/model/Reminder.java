package com.fittrack.model;

import java.util.Objects;

public class Reminder {
    public static final int DEFAULT_THRESHOLD_DAYS = 5;

    private final String bodyPartName;
    private int thresholdDays;
    private String note;

    public Reminder(String bodyPartName, Integer thresholdDays, String note) {
        if (bodyPartName == null || bodyPartName.isBlank()) {
            throw new IllegalArgumentException("Body part name cannot be blank.");
        }

        this.bodyPartName = bodyPartName.trim();
        int resolvedThreshold = thresholdDays == null ? DEFAULT_THRESHOLD_DAYS : thresholdDays;
        if (resolvedThreshold <= 0) {
            throw new IllegalArgumentException("Threshold days must be greater than 0.");
        }
        this.thresholdDays = resolvedThreshold;
        this.note = (note == null || note.isBlank()) ? null : note.trim();
    }

    public String getBodyPartName() {
        return bodyPartName;
    }

    public String getTitle() {
        return bodyPartName + " reminder";
    }

    public int getThresholdDays() {
        return thresholdDays;
    }

    public String getNote() {
        return note;
    }

    public void update(Integer thresholdDays, String note) {
        int resolvedThreshold = thresholdDays == null ? DEFAULT_THRESHOLD_DAYS : thresholdDays;
        if (resolvedThreshold <= 0) {
            throw new IllegalArgumentException("Threshold days must be greater than 0.");
        }
        this.thresholdDays = resolvedThreshold;
        this.note = (note == null || note.isBlank()) ? null : note.trim();
    }

    public String formatStatusMessage(int inactiveDays) {
        int safeInactiveDays = Math.max(inactiveDays, 0);
        return safeInactiveDays + " inactive day" + (safeInactiveDays == 1 ? "" : "s")
            + " | goal: every " + thresholdDays + " day" + (thresholdDays == 1 ? "" : "s")
            + (note == null ? "" : " | " + note);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Reminder other)) {
            return false;
        }
        return bodyPartName.equalsIgnoreCase(other.bodyPartName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bodyPartName.toLowerCase());
    }

    @Override
    public String toString() {
        return note == null
            ? bodyPartName + " | every " + thresholdDays + " days"
            : bodyPartName + " | every " + thresholdDays + " days | Note: " + note;
    }
}
