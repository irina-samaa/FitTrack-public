package com.fittrack.model;

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
        update(thresholdDays, note);
    }

    public void update(Integer thresholdDays, String note) {
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

    public int getThresholdDays() {
        return thresholdDays;
    }

    public String getNote() {
        return note;
    }

    public String getTitle() {
        return bodyPartName + " Reminder";
    }

    public String formatStatusMessage(int inactiveDays) {
        String text = inactiveDays + " inactive day" + (inactiveDays == 1 ? "" : "s")
            + " | Threshold: " + thresholdDays + " day" + (thresholdDays == 1 ? "" : "s");

        if (note != null) {
            text += " | Note: " + note;
        }

        return text;
    }
}
