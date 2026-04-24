package com.fittrack.model;

public class BodyPartInactivityAlert implements ReminderDisplayItem {
    private final String bodyPartName;
    private final int thresholdDays;
    private final Long inactiveDays;
    private final boolean thresholdReached;

    public BodyPartInactivityAlert(String bodyPartName, int thresholdDays, Long inactiveDays, boolean thresholdReached) {
        if (bodyPartName == null || bodyPartName.isBlank()) {
            throw new IllegalArgumentException("Body part name cannot be blank.");
        }
        if (thresholdDays <= 0) {
            throw new IllegalArgumentException("Threshold days must be greater than 0.");
        }
        this.bodyPartName = bodyPartName.trim();
        this.thresholdDays = thresholdDays;
        this.inactiveDays = inactiveDays;
        this.thresholdReached = thresholdReached;
    }

    public String getBodyPartName() {
        return bodyPartName;
    }

    public int getThresholdDays() {
        return thresholdDays;
    }

    public Long getInactiveDays() {
        return inactiveDays;
    }

    public boolean isThresholdReached() {
        return thresholdReached;
    }

    @Override
    public String getTitle() {
        return bodyPartName + " Inactivity";
    }

    @Override
    public String getMessage() {
        if (inactiveDays == null) {
            return "No workout logged yet. Reminder starts after " + thresholdDays
                + " day" + (thresholdDays == 1 ? "" : "s") + ".";
        }
        if (!thresholdReached) {
            long remainingDays = thresholdDays - inactiveDays;
            return inactiveDays + " inactive day" + (inactiveDays == 1 ? "" : "s")
                + " (" + remainingDays + " day" + (remainingDays == 1 ? "" : "s") + " left)";
        }
        return inactiveDays + " inactive day" + (inactiveDays == 1 ? "" : "s")
            + " (goal: every " + thresholdDays + " day" + (thresholdDays == 1 ? "" : "s") + ")";
    }

    @Override
    public ReminderType getType() {
        return ReminderType.BODY_PART_INACTIVITY;
    }
}
