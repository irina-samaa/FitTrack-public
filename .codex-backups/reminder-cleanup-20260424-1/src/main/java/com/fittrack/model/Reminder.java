package com.fittrack.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Reminder implements Comparable<Reminder>, ReminderDisplayItem {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final String label;
    private final LocalDateTime scheduledTime;
    private final Integer repeatIntervalDays;
    private final String note;

    public Reminder(String label, LocalDateTime scheduledTime, Integer repeatIntervalDays) {
        this(label, scheduledTime, repeatIntervalDays, null);
    }

    public Reminder(String label, LocalDateTime scheduledTime, Integer repeatIntervalDays, String note) {
        this.label = label;
        this.scheduledTime = scheduledTime;
        this.repeatIntervalDays = repeatIntervalDays;
        this.note = (note == null || note.isBlank()) ? null : note.trim();
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String getTitle() {
        return label;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public Integer getRepeatIntervalDays() {
        return repeatIntervalDays;
    }

    public String getNote() {
        return note;
    }

    @Override
    public String getMessage() {
        StringBuilder builder = new StringBuilder("Scheduled: ")
            .append(scheduledTime.format(FORMATTER));
        if (note != null) {
            builder.append(" | Note: ").append(note);
        }
        return builder.toString();
    }

    @Override
    public ReminderType getType() {
        return ReminderType.USER_DEFINED;
    }

    @Override
    public int compareTo(Reminder other) {
        return scheduledTime.compareTo(other.scheduledTime);
    }

    @Override
    public String toString() {
        if (repeatIntervalDays != null) {
            return note == null
                ? label + " @ " + scheduledTime.format(FORMATTER) + " (Repeats every " + repeatIntervalDays + " days)"
                : label + " @ " + scheduledTime.format(FORMATTER) + " (Repeats every " + repeatIntervalDays + " days) | Note: " + note;
        }
        return note == null
            ? label + " @ " + scheduledTime.format(FORMATTER)
            : label + " @ " + scheduledTime.format(FORMATTER) + " | Note: " + note;
    }
}
