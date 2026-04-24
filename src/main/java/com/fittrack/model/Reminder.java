package com.fittrack.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Reminder implements Comparable<Reminder> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final String label;
    private final LocalDateTime scheduledTime;

    public Reminder(String label, LocalDateTime scheduledTime) {
        this.label = label;
        this.scheduledTime = scheduledTime;
    }

    public String getLabel() {
        return label;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    @Override
    public int compareTo(Reminder other) {
        return scheduledTime.compareTo(other.scheduledTime);
    }

    @Override
    public String toString() {
        return label + " @ " + scheduledTime.format(FORMATTER);
    }
}
