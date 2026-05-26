package com.fittrack.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class ReminderService {
    public static final int DEFAULT_INTERVAL_DAYS = 5;

    private final Map<String, PriorityQueue<Reminder>> remindersByUser = new HashMap<>();

    public void loadReminder(User user, Reminder reminder) {
        upsertReminder(user, reminder.getBodyPartName(), reminder.getScheduledTime(), reminder.getIntervalDays());
    }

    public void logBodyParts(User user, Collection<String> bodyPartNames) {
        for (String bodyPartName : bodyPartNames) {
            Reminder existing = findReminder(user, bodyPartName);
            int intervalDays = existing == null ? DEFAULT_INTERVAL_DAYS : existing.getIntervalDays();
            upsertReminder(user, bodyPartName, LocalDateTime.now().plusDays(intervalDays), intervalDays);
        }
    }

    public void syncRemindersFromLoggedHistory(User user, Collection<WorkoutSession> sessions) {
        Map<String, LocalDate> latestLogByBodyPart = latestLogDatesByBodyPart(sessions);
        for (Map.Entry<String, LocalDate> entry : latestLogByBodyPart.entrySet()) {
            Reminder existing = findReminder(user, entry.getKey());
            int intervalDays = existing == null ? DEFAULT_INTERVAL_DAYS : existing.getIntervalDays();
            upsertReminder(user, entry.getKey(), entry.getValue().atStartOfDay().plusDays(intervalDays), intervalDays);
        }
    }

    public boolean updateBodyPartReminderDays(User user, String bodyPartName, int days, Collection<WorkoutSession> sessions) {
        if (days <= 0) {
            throw new IllegalArgumentException("The number of days must be greater than 0.");
        }
        if (findReminder(user, bodyPartName) == null) {
            return false;
        }
        LocalDate latestLogDate = findLatestLogDate(bodyPartName, sessions);
        LocalDateTime scheduledTime = latestLogDate == null
            ? LocalDateTime.now().plusDays(days)
            : latestLogDate.atStartOfDay().plusDays(days);
        upsertReminder(user, bodyPartName, scheduledTime, days);
        return true;
    }

    public Reminder getNextReminder(User user) {
        return remindersFor(user).peek();
    }

    public ArrayList<Reminder> getAllReminders(User user) {
        PriorityQueue<Reminder> copy = new PriorityQueue<>(remindersFor(user));
        ArrayList<Reminder> result = new ArrayList<>();
        while (!copy.isEmpty()) {
            result.add(copy.poll());
        }
        return result;
    }

    private Reminder findReminder(User user, String bodyPartName) {
        if (bodyPartName == null) {
            return null;
        }
        for (Reminder reminder : remindersFor(user)) {
            if (reminder.getBodyPartName().equalsIgnoreCase(bodyPartName.trim())) {
                return reminder;
            }
        }
        return null;
    }

    private void upsertReminder(User user, String bodyPartName, LocalDateTime scheduledTime, int intervalDays) {
        PriorityQueue<Reminder> reminders = remindersFor(user);
        reminders.removeIf(reminder -> reminder.getBodyPartName().equalsIgnoreCase(bodyPartName.trim()));
        reminders.add(new Reminder(bodyPartName, scheduledTime, intervalDays));
    }

    private Map<String, LocalDate> latestLogDatesByBodyPart(Collection<WorkoutSession> sessions) {
        Map<String, String> displayNamesByKey = new LinkedHashMap<>();
        Map<String, LocalDate> latestDatesByKey = new HashMap<>();
        for (WorkoutSession session : sessions) {
            LocalDate sessionDate = LocalDate.parse(session.getDate());
            for (Exercise exercise : session.getExercises()) {
                String bodyPartName = exercise.getBodyPart().getName();
                if (bodyPartName == null || bodyPartName.isBlank() || "History".equalsIgnoreCase(bodyPartName)) {
                    continue;
                }
                String key = bodyPartName.toLowerCase();
                displayNamesByKey.putIfAbsent(key, bodyPartName);
                LocalDate current = latestDatesByKey.get(key);
                if (current == null || sessionDate.isAfter(current)) {
                    latestDatesByKey.put(key, sessionDate);
                }
            }
        }

        Map<String, LocalDate> result = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : displayNamesByKey.entrySet()) {
            result.put(entry.getValue(), latestDatesByKey.get(entry.getKey()));
        }
        return result;
    }

    private LocalDate findLatestLogDate(String bodyPartName, Collection<WorkoutSession> sessions) {
        for (Map.Entry<String, LocalDate> entry : latestLogDatesByBodyPart(sessions).entrySet()) {
            if (entry.getKey().equalsIgnoreCase(bodyPartName)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private PriorityQueue<Reminder> remindersFor(User user) {
        return remindersByUser.computeIfAbsent(user.getUsername(), ignored -> new PriorityQueue<>());
    }
}
