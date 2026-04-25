package com.fittrack.model;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.PriorityQueue;

public class ReminderService {
    public void createOrUpdateReminder(BodyPart bodyPart, Integer thresholdDays, String note) {
        if (bodyPart == null) {
            throw new IllegalArgumentException("Body part is required.");
        }
        bodyPart.updateReminder(thresholdDays, note);
    }

    public Reminder getReminder(BodyPart bodyPart) {
        if (bodyPart == null) {
            return null;
        }
        return bodyPart.getReminder();
    }

    public ArrayList<Reminder> getReminders(List<BodyPart> bodyParts) {
        ArrayList<Reminder> reminders = new ArrayList<>();
        if (bodyParts == null) {
            return reminders;
        }

        for (BodyPart bodyPart : bodyParts) {
            if (bodyPart == null) {
                continue;
            }
            reminders.add(bodyPart.getReminder());
        }
        reminders.sort(new Comparator<Reminder>() {
            @Override
            public int compare(Reminder left, Reminder right) {
                return left.getBodyPartName().compareToIgnoreCase(right.getBodyPartName());
            }
        });
        return reminders;
    }

    public Reminder getNextReminder(User user, List<BodyPart> bodyParts) {
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

        for (Reminder reminder : getReminders(bodyParts)) {
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

    public int getDueCount(User user, List<BodyPart> bodyParts) {
        if (user == null) {
            return 0;
        }

        int dueCount = 0;
        for (Reminder reminder : getReminders(bodyParts)) {
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

    private String normalizeKey(String bodyPartName) {
        if (bodyPartName == null || bodyPartName.isBlank()) {
            throw new IllegalArgumentException("Body part name cannot be blank.");
        }
        return bodyPartName.trim().toLowerCase(Locale.ROOT);
    }
}
