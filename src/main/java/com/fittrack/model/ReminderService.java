package com.fittrack.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class ReminderService {
    private final Map<String, Map<String, Reminder>> remindersByUser = new HashMap<>();

    public void addReminder(User user, String bodyPartName) {
        if (user == null || bodyPartName == null || bodyPartName.isBlank()) {
            return;
        }

        Map<String, Reminder> reminders = remindersFor(user);
        String bodyPartKey = normalizeKey(bodyPartName);
        reminders.computeIfAbsent(bodyPartKey, ignored ->
            new Reminder(bodyPartName, Reminder.DEFAULT_THRESHOLD_DAYS, null));
    }

    public void createOrUpdateReminder(User user, String bodyPartName, Integer thresholdDays, String note) {
        if (user == null) {
            throw new IllegalArgumentException("User is required.");
        }

        String bodyPartKey = normalizeKey(bodyPartName);
        Map<String, Reminder> reminders = remindersFor(user);
        Reminder existingReminder = reminders.get(bodyPartKey);
        if (existingReminder == null) {
            reminders.put(bodyPartKey, new Reminder(bodyPartName, thresholdDays, note));
            return;
        }

        existingReminder.update(thresholdDays, note);
    }

    public Reminder getReminder(User user, String bodyPartName) {
        if (user == null || bodyPartName == null || bodyPartName.isBlank()) {
            return null;
        }
        return remindersFor(user).get(normalizeKey(bodyPartName));
    }

    public ArrayList<Reminder> getReminders(User user) {
        ArrayList<Reminder> reminders = new ArrayList<>(remindersFor(user).values());
        reminders.sort(reminderComparator(user));
        return reminders;
    }

    public Reminder getTopDueReminder(User user) {
        for (Reminder reminder : getReminders(user)) {
            if (isDue(user, reminder)) {
                return reminder;
            }
        }
        return null;
    }

    public int getInactiveDays(User user, Reminder reminder) {
        LocalDate lastWorkedDate = findLastWorkedDate(user, normalizeKey(reminder.getBodyPartName()));
        if (lastWorkedDate == null) {
            return 0;
        }
        long inactiveDays = ChronoUnit.DAYS.between(lastWorkedDate, LocalDate.now());
        return (int) Math.max(inactiveDays, 0);
    }

    public boolean isDue(User user, Reminder reminder) {
        return hasStarted(user, reminder) && getInactiveDays(user, reminder) >= reminder.getThresholdDays();
    }

    public boolean hasStarted(User user, Reminder reminder) {
        return findFirstWorkedDate(user, normalizeKey(reminder.getBodyPartName())) != null;
    }

    public int getDueCount(User user) {
        int dueCount = 0;
        for (Reminder reminder : remindersFor(user).values()) {
            if (isDue(user, reminder)) {
                dueCount++;
            }
        }
        return dueCount;
    }

    private Comparator<Reminder> reminderComparator(User user) {
        return Comparator
            .comparing((Reminder reminder) -> !isDue(user, reminder))
            .thenComparing((Reminder reminder) -> !hasStarted(user, reminder))
            .thenComparing((left, right) -> Integer.compare(
                getInactiveDays(user, right),
                getInactiveDays(user, left)
            ))
            .thenComparing(Reminder::getBodyPartName, String.CASE_INSENSITIVE_ORDER);
    }

    private LocalDate findLastWorkedDate(User user, String bodyPartKey) {
        LocalDate latest = null;
        for (WorkoutSession session : user.getWorkoutHistory()) {
            boolean matchesBodyPart = false;
            for (Exercise exercise : session.getExercises()) {
                if (normalizeKey(exercise.getBodyPart().getName()).equals(bodyPartKey)) {
                    matchesBodyPart = true;
                    break;
                }
            }
            if (!matchesBodyPart) {
                continue;
            }

            LocalDate sessionDate = LocalDate.parse(session.getDate());
            if (latest == null || sessionDate.isAfter(latest)) {
                latest = sessionDate;
            }
        }
        return latest;
    }

    private LocalDate findFirstWorkedDate(User user, String bodyPartKey) {
        LocalDate earliest = null;
        for (WorkoutSession session : user.getWorkoutHistory()) {
            boolean matchesBodyPart = false;
            for (Exercise exercise : session.getExercises()) {
                if (normalizeKey(exercise.getBodyPart().getName()).equals(bodyPartKey)) {
                    matchesBodyPart = true;
                    break;
                }
            }
            if (!matchesBodyPart) {
                continue;
            }

            LocalDate sessionDate = LocalDate.parse(session.getDate());
            if (earliest == null || sessionDate.isBefore(earliest)) {
                earliest = sessionDate;
            }
        }
        return earliest;
    }

    private Map<String, Reminder> remindersFor(User user) {
        return remindersByUser.computeIfAbsent(user.getUsername(), ignored -> new HashMap<>());
    }

    private String normalizeKey(String bodyPartName) {
        if (bodyPartName == null || bodyPartName.isBlank()) {
            throw new IllegalArgumentException("Body part name cannot be blank.");
        }
        return bodyPartName.trim().toLowerCase();
    }
}
