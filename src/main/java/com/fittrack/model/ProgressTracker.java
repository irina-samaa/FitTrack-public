package com.fittrack.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ProgressTracker {
    private static final DateTimeFormatter LABEL_FORMATTER = DateTimeFormatter.ofPattern("MMM d");

    public ArrayList<Double> generateWeightGraph(User user) {
        return getMovingAverage(user, 3);
    }

    public ArrayList<Double> getMovingAverage(User user, int window) {
        List<Double> history = user.getWeightHistory();
        ArrayList<Double> result = new ArrayList<>();
        if (window <= 0) {
            throw new IllegalArgumentException("Window must be greater than 0.");
        }

        for (int i = 0; i < history.size(); i++) {
            int start = Math.max(0, i - window + 1);
            double sum = 0;
            for (int j = start; j <= i; j++) {
                sum += history.get(j);
            }
            result.add(sum / (i - start + 1));
        }
        return result;
    }

    public ArrayList<String> getLabels(User user) {
        ArrayList<String> labels = new ArrayList<>();
        for (var date : user.getWeightHistoryDates()) {
            labels.add(LABEL_FORMATTER.format(date));
        }
        return labels;
    }

    public ArrayList<String> getWorkloadLabels(Collection<WorkoutSession> sessions) {
        ArrayList<String> labels = new ArrayList<>();
        for (var date : dailyWorkloadsByDate(sessions).keySet()) {
            labels.add(LABEL_FORMATTER.format(date));
        }
        return labels;
    }

    public ArrayList<Double> getDailyWorkloads(Collection<WorkoutSession> sessions) {
        return new ArrayList<>(dailyWorkloadsByDate(sessions).values());
    }

    public double getAverageWorkload(Collection<WorkoutSession> sessions) {
        ArrayList<Double> dailyWorkloads = getDailyWorkloads(sessions);
        if (dailyWorkloads.isEmpty()) {
            return 0;
        }

        double sum = 0;
        for (double workload : dailyWorkloads) {
            sum += workload;
        }
        return sum / dailyWorkloads.size();
    }

    public ArrayList<DailyWorkload> getDailyWorkloadSummaries(Collection<WorkoutSession> sessions, LocalDate prioritizedDate) {
        Map<LocalDate, Double> totalWorkloads = dailyWorkloadsByDate(sessions);
        Map<LocalDate, Integer> sessionCounts = dailySessionCountsByDate(sessions);
        ArrayList<DailyWorkload> result = new ArrayList<>();

        result.add(new DailyWorkload(
            prioritizedDate,
            totalWorkloads.getOrDefault(prioritizedDate, 0.0),
            sessionCounts.getOrDefault(prioritizedDate, 0)
        ));

        ArrayList<LocalDate> dates = new ArrayList<>(totalWorkloads.keySet());
        dates.sort(Comparator.reverseOrder());
        for (LocalDate date : dates) {
            if (!date.equals(prioritizedDate)) {
                result.add(new DailyWorkload(
                    date,
                    totalWorkloads.get(date),
                    sessionCounts.getOrDefault(date, 0)
                ));
            }
        }
        return result;
    }

    private Map<LocalDate, Double> dailyWorkloadsByDate(Collection<WorkoutSession> sessions) {
        Map<LocalDate, Double> dailyWorkloads = new TreeMap<>();
        for (WorkoutSession session : sessions) {
            dailyWorkloads.merge(LocalDate.parse(session.getDate()), session.getTotalWorkload(), Double::sum);
        }
        return dailyWorkloads;
    }

    private Map<LocalDate, Integer> dailySessionCountsByDate(Collection<WorkoutSession> sessions) {
        Map<LocalDate, Integer> sessionCounts = new TreeMap<>();
        for (WorkoutSession session : sessions) {
            sessionCounts.merge(LocalDate.parse(session.getDate()), 1, Integer::sum);
        }
        return sessionCounts;
    }

    public record DailyWorkload(LocalDate date, double totalWorkload, int sessionCount) {
    }
}
