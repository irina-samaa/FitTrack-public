package com.fittrack.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WorkoutSession {
    private final LocalDate date;
    private final String sessionName;
    private final List<Exercise> exercises = new ArrayList<>();

    public WorkoutSession(String date, String sessionName) {
        this(LocalDate.parse(date), sessionName);
    }

    public WorkoutSession(LocalDate date, String sessionName) {
        this.date = date;
        this.sessionName = sessionName;
    }

    public void addExercise(Exercise exercise) {
        if (exercise == null) {
            throw new IllegalArgumentException("Exercise cannot be null.");
        }
        exercises.add(exercise);
    }

    public List<Exercise> getExercises() {
        return Collections.unmodifiableList(exercises);
    }

    public String getDate() {
        return date.toString();
    }

    public String getSessionName() {
        return sessionName;
    }

    public double getTotalWorkload() {
        double total = 0;
        for (Exercise exercise : exercises) {
            total += exercise.getTotalVolume();
        }
        return total;
    }

    public String getSummary() {
        return String.format("%s on %s with %d exercises", sessionName, date, exercises.size());
    }
}
