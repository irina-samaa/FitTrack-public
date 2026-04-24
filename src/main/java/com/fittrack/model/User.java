package com.fittrack.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class User {
    private final String username;
    private final String password;
    private double weight;
    private double height;
    private final List<Double> weightHistory = new ArrayList<>();
    private final List<WorkoutSession> workoutHistory = new ArrayList<>();

    public User(String username, String password, double weight, double height) {
        this.username = username;
        this.password = password;
        this.weight = weight;
        this.height = height;
    }

    public String getUsername() {
        return username;
    }

    public boolean matchesPassword(String rawPassword) {
        return password.equals(rawPassword);
    }

    public void updateWeight(double newWeight) {
        if (newWeight <= 0) {
            throw new IllegalArgumentException("Weight must be greater than 0.");
        }
        weight = newWeight;
        weightHistory.add(newWeight);
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

    public void addWorkoutSession(WorkoutSession workoutSession) {
        if (workoutSession == null) {
            throw new IllegalArgumentException("Workout session cannot be null.");
        }
        workoutHistory.add(workoutSession);
    }

    public List<WorkoutSession> getWorkoutHistory() {
        return Collections.unmodifiableList(workoutHistory);
    }

    public List<Double> getWeightHistory() {
        return Collections.unmodifiableList(weightHistory);
    }

    public void seedWeightHistory(List<Double> historicalWeights) {
        weightHistory.clear();
        weightHistory.addAll(historicalWeights);
        if (!historicalWeights.isEmpty()) {
            weight = historicalWeights.get(historicalWeights.size() - 1);
        }
    }
}
