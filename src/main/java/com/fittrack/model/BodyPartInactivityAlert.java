package com.fittrack.model;

public record BodyPartInactivityAlert(String bodyPartName, long inactiveDays, int thresholdDays)
    implements ReminderDisplayItem {

    public BodyPartInactivityAlert {
        if (bodyPartName == null || bodyPartName.isBlank()) {
            throw new IllegalArgumentException("Body part name cannot be blank.");
        }
        if (inactiveDays < thresholdDays) {
            throw new IllegalArgumentException("Inactive days must meet the threshold.");
        }
        if (thresholdDays <= 0) {
            throw new IllegalArgumentException("Threshold days must be greater than 0.");
        }
        bodyPartName = bodyPartName.trim();
    }

    @Override
    public String getTitle() {
        return bodyPartName + " Inactivity";
    }

    @Override
    public String getMessage() {
        return inactiveDays + " inactive day" + (inactiveDays == 1 ? "" : "s")
            + " (goal: every " + thresholdDays + " day" + (thresholdDays == 1 ? "" : "s") + ")";
    }
}
