package com.fittrack.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Reminder implements Comparable<Reminder> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final String label;
    private final LocalDateTime scheduledTime;
    private final Integer repeatIntervalDays;

    public Reminder(String label, LocalDateTime scheduledTime, Integer repeatIntervalDays) {
        this.label = label;
        this.scheduledTime = scheduledTime;
        this.repeatIntervalDays = repeatIntervalDays;
    }

    public String getLabel() {
        return label;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public Integer getRepeatIntervalDays() {
        return repeatIntervalDays;
    }

    @Override
    public int compareTo(Reminder other) {
        return scheduledTime.compareTo(other.scheduledTime);
    }

    @Override
    public String toString() {
        if (repeatIntervalDays != null) {
            return label + " @ " + scheduledTime.format(FORMATTER) + " (Repeats every " + repeatIntervalDays + " days)";
        }
        return label + " @ " + scheduledTime.format(FORMATTER);
    }
}
