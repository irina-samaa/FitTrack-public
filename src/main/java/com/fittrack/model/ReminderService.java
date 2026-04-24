package com.fittrack.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class ReminderService {
    private static final int DEFAULT_BODY_PART_INACTIVITY_DAYS = 5;

    private final Map<String, PriorityQueue<Reminder>> remindersByUser = new HashMap<>();
    private final Map<String, Map<String, LocalDate>> bodyPartTrackingStartsByUser = new HashMap<>();

    public void scheduleReminder(User user, String label, LocalDateTime time, String note) {
        remindersFor(user).add(new Reminder(label, time, note));
    }

    public boolean removeReminder(User user, Reminder reminder) {
        if (reminder == null) {
            return false;
        }
        return remindersFor(user).remove(reminder);
    }

    private ArrayList<Reminder> getAllReminders(User user) {
        PriorityQueue<Reminder> copy = new PriorityQueue<>(remindersFor(user));
        ArrayList<Reminder> result = new ArrayList<>();
        while (!copy.isEmpty()) {
            result.add(copy.poll());
        }
        return result;
    }

    public ArrayList<ReminderDisplayItem> getDisplayItems(User user) {
        ArrayList<ReminderDisplayItem> items = new ArrayList<>();
        items.addAll(getBodyPartAlerts(user));
        items.addAll(getAllReminders(user));
        items.sort(displayComparator());
        return items;
    }

    public ReminderDisplayItem getNextDisplayItem(User user) {
        ArrayList<ReminderDisplayItem> items = getDisplayItems(user);
        return items.isEmpty() ? null : items.get(0);
    }

    public void ensureBodyPartTracking(User user, String bodyPartName) {
        if (bodyPartName == null || bodyPartName.isBlank()) {
            return;
        }
        trackingStartsFor(user).computeIfAbsent(
            normalizeBodyPartKey(bodyPartName),
            ignored -> LocalDate.now()
        );
    }

    public void seedBodyPartTracking(User user, String bodyPartName, LocalDate trackingStartDate) {
        trackingStartsFor(user).put(normalizeBodyPartKey(bodyPartName), trackingStartDate);
    }

    private PriorityQueue<Reminder> remindersFor(User user) {
        return remindersByUser.computeIfAbsent(user.getUsername(), ignored -> new PriorityQueue<>());
    }

    private Map<String, LocalDate> trackingStartsFor(User user) {
        return bodyPartTrackingStartsByUser.computeIfAbsent(user.getUsername(), ignored -> new HashMap<>());
    }

    private List<BodyPartInactivityAlert> getBodyPartAlerts(User user) {
        ArrayList<BodyPartInactivityAlert> alerts = new ArrayList<>();
        for (Map.Entry<String, LocalDate> entry : trackingStartsFor(user).entrySet()) {
            long inactiveDays = calculateInactiveDays(user, entry.getKey(), entry.getValue());
            if (inactiveDays >= DEFAULT_BODY_PART_INACTIVITY_DAYS) {
                alerts.add(new BodyPartInactivityAlert(
                    formatBodyPartName(entry.getKey()),
                    inactiveDays,
                    DEFAULT_BODY_PART_INACTIVITY_DAYS
                ));
            }
        }
        return alerts;
    }

    private long calculateInactiveDays(User user, String bodyPartKey, LocalDate trackingStartDate) {
        LocalDate lastWorkedDate = findLastWorkedDate(user, bodyPartKey);
        LocalDate referenceDate = lastWorkedDate == null ? trackingStartDate : lastWorkedDate;
        long inactiveDays = ChronoUnit.DAYS.between(referenceDate, LocalDate.now());
        return Math.max(inactiveDays, 0);
    }

    private LocalDate findLastWorkedDate(User user, String bodyPartKey) {
        LocalDate latest = null;
        for (WorkoutSession session : user.getWorkoutHistory()) {
            boolean matchesBodyPart = false;
            for (Exercise exercise : session.getExercises()) {
                if (normalizeBodyPartKey(exercise.getBodyPart().getName()).equals(bodyPartKey)) {
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

    private Comparator<ReminderDisplayItem> displayComparator() {
        return Comparator
            .comparingInt(this::displayPriority)
            .thenComparing((left, right) -> {
                if (left instanceof BodyPartInactivityAlert leftAlert && right instanceof BodyPartInactivityAlert rightAlert) {
                    return Long.compare(rightAlert.inactiveDays(), leftAlert.inactiveDays());
                }
                if (left instanceof Reminder leftReminder && right instanceof Reminder rightReminder) {
                    return leftReminder.getScheduledTime().compareTo(rightReminder.getScheduledTime());
                }
                return left.getTitle().compareToIgnoreCase(right.getTitle());
            });
    }

    private int displayPriority(ReminderDisplayItem item) {
        return item instanceof BodyPartInactivityAlert ? 0 : 1;
    }

    private String normalizeBodyPartKey(String bodyPartName) {
        return bodyPartName.trim().toLowerCase();
    }

    private String formatBodyPartName(String bodyPartKey) {
        return bodyPartKey.isEmpty()
            ? bodyPartKey
            : Character.toUpperCase(bodyPartKey.charAt(0)) + bodyPartKey.substring(1);
    }
}
