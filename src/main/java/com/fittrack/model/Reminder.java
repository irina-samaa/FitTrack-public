package com.fittrack.model;

import java.time.LocalDateTime;

public class Reminder {
    private final LocalDateTime scheduledTime;
    private final int intervalDays;

    public Reminder(LocalDateTime scheduledTime, int intervalDays) {
        if (scheduledTime == null) {
            throw new IllegalArgumentException("Scheduled time cannot be null.");
        }
        if (intervalDays <= 0) {
            throw new IllegalArgumentException("Interval days must be greater than 0.");
        }
        this.scheduledTime = scheduledTime;
        this.intervalDays = intervalDays;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public int getIntervalDays() {
        return intervalDays;
    }
}
