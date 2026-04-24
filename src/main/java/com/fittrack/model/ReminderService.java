package com.fittrack.model;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
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
            reminders.put(bodyPartKey, new Reminder(bodyPartName, Reminder.DEFAULT_THRESHOLD_DAYS, null));
        }
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
        if (user == null) {
            return new ArrayList<>();
        }

        ArrayList<Reminder> reminders = new ArrayList<>(remindersFor(user).values());
        reminders.sort(new Comparator<Reminder>() {
            @Override
            public int compare(Reminder left, Reminder right) {
                return left.getBodyPartName().compareToIgnoreCase(right.getBodyPartName());
            }
        });
        return reminders;
    }

    public Reminder getAnnouncementReminder(User user) {
        if (user == null) {
            return null;
        }

        PriorityQueue<Reminder> reminderQueue = new PriorityQueue<>(new Comparator<Reminder>() {
            @Override
            public int compare(Reminder left, Reminder right) {
                return compareAnnouncementPriority(user, left, right);
            }
        });

        for (Reminder reminder : remindersFor(user).values()) {
            if (hasStarted(user, reminder)) {
                reminderQueue.offer(reminder);
            }
        }

        return reminderQueue.poll();
    }

    public Reminder getTopDueReminder(User user) {
        Reminder reminder = getAnnouncementReminder(user);
        if (isDue(user, reminder)) {
            return reminder;
        }
        return null;
    }

    public int getInactiveDays(User user, Reminder reminder) {
        if (user == null || reminder == null) {
            return 0;
        }

        LocalDate lastWorkedDate = findLastWorkedDate(user, normalizeKey(reminder.getBodyPartName()));
        if (lastWorkedDate == null) {
            return 0;
        }
        long inactiveDays = ChronoUnit.DAYS.between(lastWorkedDate, LocalDate.now());
        return (int) Math.max(inactiveDays, 0);
    }

    public boolean isDue(User user, Reminder reminder) {
        if (reminder == null) {
            return false;
        }
        return hasStarted(user, reminder) && getInactiveDays(user, reminder) >= reminder.getThresholdDays();
    }

    public boolean hasStarted(User user, Reminder reminder) {
        if (user == null || reminder == null) {
            return false;
        }
        return findLastWorkedDate(user, normalizeKey(reminder.getBodyPartName())) != null;
    }

    public int getDueCount(User user) {
        if (user == null) {
            return 0;
        }

        int dueCount = 0;
        for (Reminder reminder : remindersFor(user).values()) {
            if (isDue(user, reminder)) {
                dueCount++;
            }
        }
        return dueCount;
    }

    public int getDaysRemaining(User user, Reminder reminder) {
        if (user == null || reminder == null) {
            return 0;
        }

        return reminder.getThresholdDays() - getInactiveDays(user, reminder);
    }

    private int compareAnnouncementPriority(User user, Reminder left, Reminder right) {
        boolean leftDue = isDue(user, left);
        boolean rightDue = isDue(user, right);
        if (leftDue != rightDue) {
            return leftDue ? -1 : 1;
        }

        int leftDaysRemaining = getDaysRemaining(user, left);
        int rightDaysRemaining = getDaysRemaining(user, right);
        if (leftDaysRemaining != rightDaysRemaining) {
            return Integer.compare(leftDaysRemaining, rightDaysRemaining);
        }

        return left.getBodyPartName().compareToIgnoreCase(right.getBodyPartName());
    }

    private LocalDate findLastWorkedDate(User user, String bodyPartKey) {
        List<WorkoutSession> workoutHistory = workoutHistoryOf(user);
        for (int i = workoutHistory.size() - 1; i >= 0; i--) {
            WorkoutSession session = workoutHistory.get(i);
            if (!sessionTargetsBodyPart(session, bodyPartKey)) {
                continue;
            }

            LocalDate sessionDate = parseSessionDate(session);
            if (sessionDate != null) {
                return sessionDate;
            }
        }
        return null;
    }

    private Map<String, Reminder> remindersFor(User user) {
        Objects.requireNonNull(user, "User is required.");
        String username = user.getUsername();
        Map<String, Reminder> reminders = remindersByUser.get(username);
        if (reminders == null) {
            reminders = new HashMap<>();
            remindersByUser.put(username, reminders);
        }
        return reminders;
    }

    private String normalizeKey(String bodyPartName) {
        if (bodyPartName == null || bodyPartName.isBlank()) {
            throw new IllegalArgumentException("Body part name cannot be blank.");
        }
        return bodyPartName.trim().toLowerCase(Locale.ROOT);
    }

    private boolean sessionTargetsBodyPart(WorkoutSession session, String bodyPartKey) {
        if (session == null) {
            return false;
        }

        for (Exercise exercise : exercisesOf(session)) {
            if (exercise == null || exercise.getBodyPart() == null) {
                continue;
            }

            String exerciseBodyPart = exercise.getBodyPart().getName();
            if (exerciseBodyPart != null && normalizeKey(exerciseBodyPart).equals(bodyPartKey)) {
                return true;
            }
        }
        return false;
    }

    private LocalDate parseSessionDate(WorkoutSession session) {
        if (session == null || session.getDate() == null || session.getDate().isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(session.getDate());
        } catch (DateTimeException exception) {
            return null;
        }
    }

    private List<WorkoutSession> workoutHistoryOf(User user) {
        if (user == null || user.getWorkoutHistory() == null) {
            return Collections.emptyList();
        }
        return user.getWorkoutHistory();
    }

    private List<Exercise> exercisesOf(WorkoutSession session) {
        if (session == null || session.getExercises() == null) {
            return Collections.emptyList();
        }
        return session.getExercises();
    }
}
