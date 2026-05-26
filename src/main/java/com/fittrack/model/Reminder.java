package com.fittrack.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Reminder implements Comparable<Reminder> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final String bodyPartName;
    private final LocalDateTime scheduledTime;
    private final int intervalDays;

    public Reminder(String bodyPartName, LocalDateTime scheduledTime, int intervalDays) {
        if (bodyPartName == null || bodyPartName.isBlank()) {
            throw new IllegalArgumentException("Body part name cannot be blank.");
        }
        if (scheduledTime == null) {
            throw new IllegalArgumentException("Scheduled time cannot be null.");
        }
        if (intervalDays <= 0) {
            throw new IllegalArgumentException("Interval days must be greater than 0.");
        }
        this.bodyPartName = bodyPartName.trim();
        this.scheduledTime = scheduledTime;
        this.intervalDays = intervalDays;
    }

    public String getBodyPartName() {
        return bodyPartName;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public int getIntervalDays() {
        return intervalDays;
    }

    @Override
    public int compareTo(Reminder other) {
        int timeComparison = scheduledTime.compareTo(other.scheduledTime);
        if (timeComparison != 0) {
            return timeComparison;
        }
        return bodyPartName.compareToIgnoreCase(other.bodyPartName);
    }

    @Override
    public String toString() {
        return bodyPartName + " @ " + scheduledTime.format(FORMATTER) + " (after " + intervalDays + " days)";
    }
}
