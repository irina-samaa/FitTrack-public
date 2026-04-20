package com.fittrack.model;

import java.time.LocalDateTime;

/**
 * Reminder.java — STUB cho UI team.
 * Implements Comparable để dùng được với PriorityQueue (min-heap).
 */
public class Reminder implements Comparable<Reminder> {
    private String label;
    private LocalDateTime scheduledTime;

    public Reminder(String label, LocalDateTime scheduledTime) {
        this.label = label;
        this.scheduledTime = scheduledTime;
    }

    public String getLabel() { return label; }
    public LocalDateTime getScheduledTime() { return scheduledTime; }

    /**
     * So sánh theo thời gian — cần thiết cho PriorityQueue min-heap.
     * TODO (Backend): implement compareTo.
     */
    @Override
    public int compareTo(Reminder other) {
        return this.scheduledTime.compareTo(other.scheduledTime); // TODO (Backend)
    }

    @Override
    public String toString() {
        return label + " @ " + scheduledTime.toString(); // TODO (Backend): format đẹp hơn
    }
}
