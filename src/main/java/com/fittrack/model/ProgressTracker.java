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

    public ArrayList<Double> getMovingAverage(User user, int window) {
        return calculateMovingAverage(user.getWeightHistory(), window);
    }

    public ArrayList<Double> getChartWeightValues(User user) {
        ArrayList<Double> weights = new ArrayList<>();
        Map<LocalDate, Double> weightsByDate = weightsByDate(user);
        if (weightsByDate.isEmpty()) {
            return weights;
        }

        double currentWeight = weightsByDate.get(weightsByDate.keySet().iterator().next());
        for (LocalDate date = weightsByDate.keySet().iterator().next(); !date.isAfter(LocalDate.now()); date = date.plusDays(1)) {
            if (weightsByDate.containsKey(date)) {
                currentWeight = weightsByDate.get(date);
            }
            weights.add(currentWeight);
        }
        return weights;
    }

    public ArrayList<Double> getChartWeightMovingAverage(User user, int window) {
        return calculateMovingAverage(getChartWeightValues(user), window);
    }

    public ArrayList<Double> getWorkloadMovingAverage(Collection<WorkoutSession> sessions, int window) {
        return calculateMovingAverage(getDailyWorkloads(sessions), window);
    }

    public ArrayList<Double> getChartWorkloadMovingAverage(Collection<WorkoutSession> sessions, int window) {
        return calculateMovingAverage(getChartDailyWorkloads(sessions), window);
    }

    private ArrayList<Double> calculateMovingAverage(List<Double> values, int window) {
        ArrayList<Double> result = new ArrayList<>();
        if (window <= 0) {
            throw new IllegalArgumentException("Window must be greater than 0.");
        }

        for (int i = 0; i < values.size(); i++) {
            int start = Math.max(0, i - window + 1);
            double sum = 0;
            for (int j = start; j <= i; j++) {
                sum += values.get(j);
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

    public ArrayList<String> getChartWeightLabels(User user) {
        return labelsForDates(getContinuousWeightDates(user));
    }

    public ArrayList<String> getWorkloadLabels(Collection<WorkoutSession> sessions) {
        ArrayList<String> labels = new ArrayList<>();
        for (var date : dailyWorkloadsByDate(sessions).keySet()) {
            labels.add(LABEL_FORMATTER.format(date));
        }
        return labels;
    }

    public ArrayList<String> getChartWorkloadLabels(Collection<WorkoutSession> sessions) {
        return labelsForDates(getContinuousWorkloadDates(sessions));
    }

    public ArrayList<Double> getDailyWorkloads(Collection<WorkoutSession> sessions) {
        return new ArrayList<>(dailyWorkloadsByDate(sessions).values());
    }

    public ArrayList<Double> getChartDailyWorkloads(Collection<WorkoutSession> sessions) {
        ArrayList<Double> dailyWorkloads = new ArrayList<>();
        Map<LocalDate, Double> workloadsByDate = dailyWorkloadsByDate(sessions);
        if (workloadsByDate.isEmpty()) {
            return dailyWorkloads;
        }

        for (LocalDate date = workloadsByDate.keySet().iterator().next(); !date.isAfter(LocalDate.now()); date = date.plusDays(1)) {
            dailyWorkloads.add(workloadsByDate.getOrDefault(date, 0.0));
        }
        return dailyWorkloads;
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

    private Map<LocalDate, Double> weightsByDate(User user) {
        Map<LocalDate, Double> weightsByDate = new TreeMap<>();
        List<Double> weights = user.getWeightHistory();
        List<LocalDate> dates = user.getWeightHistoryDates();
        for (int i = 0; i < weights.size(); i++) {
            weightsByDate.put(dates.get(i), weights.get(i));
        }
        return weightsByDate;
    }

    private ArrayList<LocalDate> getContinuousWeightDates(User user) {
        Map<LocalDate, Double> weightsByDate = weightsByDate(user);
        if (weightsByDate.isEmpty()) {
            return new ArrayList<>();
        }
        return continuousDatesFrom(weightsByDate.keySet().iterator().next());
    }

    private ArrayList<LocalDate> getContinuousWorkloadDates(Collection<WorkoutSession> sessions) {
        Map<LocalDate, Double> workloadsByDate = dailyWorkloadsByDate(sessions);
        if (workloadsByDate.isEmpty()) {
            return new ArrayList<>();
        }
        return continuousDatesFrom(workloadsByDate.keySet().iterator().next());
    }

    private ArrayList<LocalDate> continuousDatesFrom(LocalDate startDate) {
        ArrayList<LocalDate> dates = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(LocalDate.now()); date = date.plusDays(1)) {
            dates.add(date);
        }
        return dates;
    }

    private ArrayList<String> labelsForDates(List<LocalDate> dates) {
        ArrayList<String> labels = new ArrayList<>();
        for (LocalDate date : dates) {
            labels.add(LABEL_FORMATTER.format(date));
        }
        return labels;
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
