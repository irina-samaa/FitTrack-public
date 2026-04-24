package com.fittrack.model;

import java.util.ArrayList;
import java.util.List;

public class ProgressTracker {
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
        for (int i = 0; i < user.getWeightHistory().size(); i++) {
            labels.add("Entry " + (i + 1));
        }
        return labels;
    }
}
