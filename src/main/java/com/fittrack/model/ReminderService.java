package com.fittrack.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class ReminderService {
    private final Map<String, PriorityQueue<Reminder>> remindersByUser = new HashMap<>();

    public void scheduleReminder(User user, String label, LocalDateTime time, Integer repeatIntervalDays) {
        scheduleReminder(user, label, time, repeatIntervalDays, null);
    }

    public void scheduleReminder(User user, String label, LocalDateTime time, Integer repeatIntervalDays, String note) {
        remindersFor(user).add(new Reminder(label, time, repeatIntervalDays, note));
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

    public void resetRepeatingReminders(User user) {
        PriorityQueue<Reminder> currentReminders = remindersFor(user);
        ArrayList<Reminder> toReAdd = new ArrayList<>();
        ArrayList<Reminder> toKeep = new ArrayList<>();

        while (!currentReminders.isEmpty()) {
            Reminder r = currentReminders.poll();
            if (r.getRepeatIntervalDays() != null) {
                toReAdd.add(new Reminder(
                    r.getLabel(),
                    LocalDateTime.now().plusDays(r.getRepeatIntervalDays()),
                    r.getRepeatIntervalDays(),
                    r.getNote()
                ));
            } else {
                toKeep.add(r);
            }
        }

        currentReminders.addAll(toKeep);
        currentReminders.addAll(toReAdd);
    }


    private PriorityQueue<Reminder> remindersFor(User user) {
        return remindersByUser.computeIfAbsent(user.getUsername(), ignored -> new PriorityQueue<>());
    }
}
