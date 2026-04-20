package com.fittrack.model;

import java.util.ArrayList;

/**
 * ProgressTracker.java — STUB cho UI team.
 * Backend team implement generateWeightGraph() và getMovingAverage().
 */
public class ProgressTracker {
    private HealthMetrics healthMetrics;

    public ProgressTracker(HealthMetrics healthMetrics) {
        this.healthMetrics = healthMetrics;
    }

    /**
     * Trả về ArrayList moving averages cho toàn bộ weight history.
     * TODO (Backend): implement theo hint trong vibe doc.
     *
     * Hint từ vibe doc:
     * for (int i = 0; i < history.size(); i++) {
     *     int start = Math.max(0, i - window + 1);
     *     double sum = 0;
     *     for (int j = start; j <= i; j++) sum += history.get(j);
     *     result.add(sum / (i - start + 1));
     * }
     */
    public ArrayList<Double> getMovingAverage(int window) {
        ArrayList<Double> history = healthMetrics.getWeightHistory();
        ArrayList<Double> result = new ArrayList<>();
        // TODO (Backend): implement moving average
        for (int i = 0; i < history.size(); i++) {
            int start = Math.max(0, i - window + 1);
            double sum = 0;
            for (int j = start; j <= i; j++) sum += history.get(j);
            result.add(sum / (i - start + 1));
        }
        return result;
    }

    /**
     * Trả về ArrayList toàn bộ moving averages (dùng window mặc định = 3).
     * TODO (Backend): implement.
     */
    public ArrayList<Double> generateWeightGraph() {
        return getMovingAverage(3); // TODO (Backend): có thể dùng window khác
    }

    /**
     * Trả về nhãn cho từng điểm dữ liệu: ["Week 1", "Week 2", ...].
     * TODO (Backend): implement.
     */
    public ArrayList<String> getLabels() {
        ArrayList<String> labels = new ArrayList<>();
        int size = healthMetrics.getWeightHistory().size();
        for (int i = 1; i <= size; i++) {
            labels.add("Week " + i); // TODO (Backend): có thể dùng ngày thực tế
        }
        return labels;
    }
}
