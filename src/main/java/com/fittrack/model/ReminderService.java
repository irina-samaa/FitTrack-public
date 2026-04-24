package com.fittrack.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class ReminderService {
    private final Map<String, PriorityQueue<Reminder>> remindersByUser = new HashMap<>();
    private int recoveryDays = 5;

    public void scheduleReminder(User user, String label, LocalDateTime time) {
        remindersFor(user).add(new Reminder(label, time));
    }

    public void scheduleReminder(User user) {
        LocalDateTime nextTime = LocalDateTime.now().plusDays(recoveryDays);
        scheduleReminder(user, "Recovery Reminder", nextTime);
    }

    public Reminder getNextReminder(User user) {
        return remindersFor(user).peek();
    }

    public Reminder removeNextReminder(User user) {
        return remindersFor(user).poll();
    }

    public ArrayList<Reminder> getAllReminders(User user) {
        PriorityQueue<Reminder> copy = new PriorityQueue<>(remindersFor(user));
        ArrayList<Reminder> result = new ArrayList<>();
        while (!copy.isEmpty()) {
            result.add(copy.poll());
        }
        return result;
    }

    public void setRecoveryDays(int days) {
        if (days < 0) {
            throw new IllegalArgumentException("Recovery days cannot be negative.");
        }
        recoveryDays = days;
    }

    public int getRecoveryDays() {
        return recoveryDays;
    }

    private PriorityQueue<Reminder> remindersFor(User user) {
        return remindersByUser.computeIfAbsent(user.getUsername(), ignored -> new PriorityQueue<>());
    }
}
