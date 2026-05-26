package com.fittrack.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class User {
    private final String username;
    private final String password;
    private double weight;
    private double height;
    private final List<Double> weightHistory = new ArrayList<>();
    private final List<LocalDate> weightHistoryDates = new ArrayList<>();

    public User(String username, String password, double weight, double height) {
        this.username = username;
        this.password = password;
        this.weight = weight;
        this.height = height;
    }

    public String getUsername() {
        return username;
    }

    public void updateWeight(double newWeight) {
        updateWeight(newWeight, LocalDate.now());
    }

    public void updateWeight(double newWeight, LocalDate recordDate) {
        if (newWeight <= 0) {
            throw new IllegalArgumentException("Weight must be greater than 0.");
        }
        if (recordDate == null) {
            throw new IllegalArgumentException("Record date cannot be null.");
        }
        weight = newWeight;
        for (int i = 0; i < weightHistoryDates.size(); i++) {
            if (weightHistoryDates.get(i).equals(recordDate)) {
                weightHistory.set(i, newWeight);
                return;
            }
        }
        weightHistory.add(newWeight);
        weightHistoryDates.add(recordDate);
    }

    public void updateHeight(double newHeight) {
        if (newHeight <= 0) {
            throw new IllegalArgumentException("Height must be greater than 0.");
        }
        height = newHeight;
    }

    public double getWeight() {
        return weight;
    }

    public double getHeight() {
        return height;
    }

    public List<Double> getWeightHistory() {
        return Collections.unmodifiableList(weightHistory);
    }

    public List<LocalDate> getWeightHistoryDates() {
        return Collections.unmodifiableList(weightHistoryDates);
    }

    public void seedWeightHistory(List<Double> historicalWeights, List<LocalDate> historicalDates) {
        if (historicalWeights.size() != historicalDates.size()) {
            throw new IllegalArgumentException("Weight history and date history must have the same size.");
        }
        weightHistory.clear();
        weightHistoryDates.clear();
        weightHistory.addAll(historicalWeights);
        weightHistoryDates.addAll(historicalDates);
        if (!historicalWeights.isEmpty()) {
            weight = historicalWeights.get(historicalWeights.size() - 1);
        }
    }
}
