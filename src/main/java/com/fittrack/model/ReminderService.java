package com.fittrack.model;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;

public class ReminderService {
    private final Map<String, Map<String, Reminder>> remindersByUser = new HashMap<>();

    public void addReminder(User user, String bodyPartName) {
        if (user == null || bodyPartName == null || bodyPartName.isBlank()) {
            return;
        }

        Map<String, Reminder> reminders = remindersFor(user);
        String bodyPartKey = normalizeKey(bodyPartName);
        if (!reminders.containsKey(bodyPartKey)) {
            reminders.put(bodyPartKey, new Reminder(bodyPartName, null, null));
        }
    }

    public void createOrUpdateReminder(User user, String bodyPartName, Integer thresholdDays, String note) {
        if (user == null) {
            throw new IllegalArgumentException("User is required.");
        }

        Map<String, Reminder> reminders = remindersFor(user);
        String bodyPartKey = normalizeKey(bodyPartName);
        Reminder reminder = reminders.get(bodyPartKey);
        if (reminder == null) {
            reminders.put(bodyPartKey, new Reminder(bodyPartName, thresholdDays, note));
            return;
        }

        reminder.update(thresholdDays, note);
    }

    public Reminder getReminder(User user, String bodyPartName) {
        if (user == null || bodyPartName == null || bodyPartName.isBlank()) {
            return null;
        }
        return remindersFor(user).get(normalizeKey(bodyPartName));
    }

    public ArrayList<Reminder> getReminders(User user) {
        ArrayList<Reminder> reminders = new ArrayList<>();
        if (user == null) {
            return reminders;
        }

        reminders.addAll(remindersFor(user).values());
        reminders.sort(new Comparator<Reminder>() {
            @Override
            public int compare(Reminder left, Reminder right) {
                return left.getBodyPartName().compareToIgnoreCase(right.getBodyPartName());
            }
        });
        return reminders;
    }

    public Reminder getNextReminder(User user) {
        if (user == null) {
            return null;
        }

        PriorityQueue<Reminder> reminderQueue = new PriorityQueue<>(new Comparator<Reminder>() {
            @Override
            public int compare(Reminder first, Reminder second) {
                int daysCompare = Integer.compare(
                    getDaysRemaining(user, first),
                    getDaysRemaining(user, second)
                );
                if (daysCompare != 0) {
                    return daysCompare;
                }
                return first.getBodyPartName().compareToIgnoreCase(second.getBodyPartName());
            }
        });

        for (Reminder reminder : getReminders(user)) {
            if (hasStarted(user, reminder)) {
                reminderQueue.offer(reminder);
            }
        }

        return reminderQueue.poll();
    }

    public int getInactiveDays(User user, Reminder reminder) {
        if (user == null || reminder == null) {
            return 0;
        }

        LocalDate lastWorkoutDate = findLastWorkoutDate(user, reminder.getBodyPartName());
        if (lastWorkoutDate == null) {
            return 0;
        }

        long inactiveDays = ChronoUnit.DAYS.between(lastWorkoutDate, LocalDate.now());
        return (int) Math.max(inactiveDays, 0);
    }

    public int getDaysRemaining(User user, Reminder reminder) {
        if (user == null || reminder == null) {
            return 0;
        }
        return reminder.getThresholdDays() - getInactiveDays(user, reminder);
    }

    public boolean hasStarted(User user, Reminder reminder) {
        if (user == null || reminder == null) {
            return false;
        }
        return findLastWorkoutDate(user, reminder.getBodyPartName()) != null;
    }

    public boolean isDue(User user, Reminder reminder) {
        if (reminder == null) {
            return false;
        }
        return hasStarted(user, reminder) && getInactiveDays(user, reminder) >= reminder.getThresholdDays();
    }

    public int getDueCount(User user) {
        if (user == null) {
            return 0;
        }

        int dueCount = 0;
        for (Reminder reminder : getReminders(user)) {
            if (isDue(user, reminder)) {
                dueCount++;
            }
        }
        return dueCount;
    }

    private LocalDate findLastWorkoutDate(User user, String bodyPartName) {
        String bodyPartKey = normalizeKey(bodyPartName);
        List<WorkoutSession> workoutHistory = user.getWorkoutHistory();
        for (int index = workoutHistory.size() - 1; index >= 0; index--) {
            WorkoutSession session = workoutHistory.get(index);
            if (!sessionTargetsBodyPart(session, bodyPartKey)) {
                continue;
            }

            LocalDate sessionDate = parseSessionDate(session.getDate());
            if (sessionDate != null) {
                return sessionDate;
            }
        }
        return null;
    }

    private boolean sessionTargetsBodyPart(WorkoutSession session, String bodyPartKey) {
        if (session == null) {
            return false;
        }

        for (Exercise exercise : session.getExercises()) {
            if (exercise == null || exercise.getBodyPart() == null) {
                continue;
            }

            String currentBodyPartName = exercise.getBodyPart().getName();
            if (currentBodyPartName != null && normalizeKey(currentBodyPartName).equals(bodyPartKey)) {
                return true;
            }
        }
        return false;
    }

    private LocalDate parseSessionDate(String sessionDate) {
        if (sessionDate == null || sessionDate.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(sessionDate);
        } catch (DateTimeException exception) {
            return null;
        }
    }

    private Map<String, Reminder> remindersFor(User user) {
        String username = user.getUsername();
        if (!remindersByUser.containsKey(username)) {
            remindersByUser.put(username, new HashMap<String, Reminder>());
        }
        return remindersByUser.get(username);
    }

    private String normalizeKey(String bodyPartName) {
        if (bodyPartName == null || bodyPartName.isBlank()) {
            throw new IllegalArgumentException("Body part name cannot be blank.");
        }
        return bodyPartName.trim().toLowerCase(Locale.ROOT);
    }
}
