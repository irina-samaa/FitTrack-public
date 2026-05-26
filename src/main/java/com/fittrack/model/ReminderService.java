package com.fittrack.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;

public class ReminderService {
    public static final int DEFAULT_INTERVAL_DAYS = 5;
    private static final Comparator<BodyPart> REMINDER_ORDER = Comparator
        .comparing((BodyPart bodyPart) -> bodyPart.getReminder().getScheduledTime())
        .thenComparing(BodyPart::getName, String.CASE_INSENSITIVE_ORDER);

    public void logBodyParts(List<BodyPart> bodyParts, Collection<String> bodyPartNames) {
        for (String bodyPartName : bodyPartNames) {
            BodyPart bodyPart = findBodyPart(bodyParts, bodyPartName);
            if (bodyPart == null) {
                continue;
            }
            Reminder existing = bodyPart.getReminder();
            int intervalDays = existing == null ? DEFAULT_INTERVAL_DAYS : existing.getIntervalDays();
            bodyPart.scheduleReminder(LocalDateTime.now().plusDays(intervalDays), intervalDays);
        }
    }

    public void syncRemindersFromLoggedHistory(List<BodyPart> bodyParts, Collection<WorkoutSession> sessions) {
        Map<String, LocalDate> latestLogByBodyPart = latestLogDatesByBodyPart(sessions);
        for (Map.Entry<String, LocalDate> entry : latestLogByBodyPart.entrySet()) {
            BodyPart bodyPart = findBodyPart(bodyParts, entry.getKey());
            if (bodyPart == null) {
                continue;
            }
            Reminder existing = bodyPart.getReminder();
            int intervalDays = existing == null ? DEFAULT_INTERVAL_DAYS : existing.getIntervalDays();
            bodyPart.scheduleReminder(entry.getValue().atStartOfDay().plusDays(intervalDays), intervalDays);
        }
    }

    public boolean updateBodyPartReminderDays(BodyPart bodyPart, int days, Collection<WorkoutSession> sessions) {
        if (days <= 0) {
            throw new IllegalArgumentException("The number of days must be greater than 0.");
        }
        if (bodyPart == null || !bodyPart.hasReminder()) {
            return false;
        }
        LocalDate latestLogDate = latestLogDatesByBodyPart(sessions).get(normalizeBodyPartName(bodyPart.getName()));
        LocalDateTime scheduledTime = latestLogDate == null
            ? LocalDateTime.now().plusDays(days)
            : latestLogDate.atStartOfDay().plusDays(days);
        bodyPart.scheduleReminder(scheduledTime, days);
        return true;
    }

    public BodyPart getNextReminderBodyPart(List<BodyPart> bodyParts) {
        return buildReminderQueue(bodyParts).peek();
    }

    public ArrayList<BodyPart> getBodyPartsWithReminders(List<BodyPart> bodyParts) {
        PriorityQueue<BodyPart> reminderQueue = buildReminderQueue(bodyParts);
        ArrayList<BodyPart> result = new ArrayList<>();
        while (!reminderQueue.isEmpty()) {
            result.add(reminderQueue.poll());
        }
        return result;
    }

    private PriorityQueue<BodyPart> buildReminderQueue(List<BodyPart> bodyParts) {
        PriorityQueue<BodyPart> reminderQueue = new PriorityQueue<>(REMINDER_ORDER);
        for (BodyPart bodyPart : bodyParts) {
            if (bodyPart.hasReminder()) {
                reminderQueue.add(bodyPart);
            }
        }
        return reminderQueue;
    }

    private BodyPart findBodyPart(List<BodyPart> bodyParts, String bodyPartName) {
        if (bodyPartName == null) {
            return null;
        }
        for (BodyPart bodyPart : bodyParts) {
            if (bodyPart.getName().equalsIgnoreCase(bodyPartName.trim())) {
                return bodyPart;
            }
        }
        return null;
    }

    private Map<String, LocalDate> latestLogDatesByBodyPart(Collection<WorkoutSession> sessions) {
        Map<String, LocalDate> latestDatesByKey = new LinkedHashMap<>();
        for (WorkoutSession session : sessions) {
            LocalDate sessionDate = LocalDate.parse(session.getDate());
            for (Exercise exercise : session.getExercises()) {
                String bodyPartName = exercise.getBodyPart().getName();
                if (bodyPartName == null || bodyPartName.isBlank() || "History".equalsIgnoreCase(bodyPartName)) {
                    continue;
                }
                String key = normalizeBodyPartName(bodyPartName);
                LocalDate current = latestDatesByKey.get(key);
                if (current == null || sessionDate.isAfter(current)) {
                    latestDatesByKey.put(key, sessionDate);
                }
            }
        }
        return latestDatesByKey;
    }

    private String normalizeBodyPartName(String bodyPartName) {
        return bodyPartName.trim().toLowerCase(Locale.ROOT);
    }

}
