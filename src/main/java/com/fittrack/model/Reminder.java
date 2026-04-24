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
        return bodyPartName + " reminder";
    }

    public void update(Integer thresholdDays, String note) {
        this.thresholdDays = resolveThresholdDays(thresholdDays);
        this.note = cleanNote(note);
    }

    private int resolveThresholdDays(Integer thresholdDays) {
        int resolvedThreshold = thresholdDays == null ? DEFAULT_THRESHOLD_DAYS : thresholdDays;
        if (resolvedThreshold <= 0) {
            throw new IllegalArgumentException("Threshold days must be greater than 0.");
        }
        return resolvedThreshold;
    }

    private String cleanNote(String note) {
        if (note == null) {
            return null;
        }

        String trimmedNote = note.trim();
        if (trimmedNote.isEmpty()) {
            return null;
        }
        return trimmedNote;
    }
}
