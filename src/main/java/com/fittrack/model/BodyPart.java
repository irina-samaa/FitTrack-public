package com.fittrack.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BodyPart {
    private final String name;
    private final List<Exercise> exercises = new ArrayList<>();
    private final Reminder reminder;

    public BodyPart(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Body part name cannot be blank.");
        }
        this.name = name.trim();
        this.reminder = new Reminder(this.name, null, null);
    }

    public String getName() {
        return name;
    }

    public void addExercise(Exercise exercise) {
        if (exercise == null) {
            throw new IllegalArgumentException("Exercise cannot be null.");
        }
        exercises.add(exercise);
    }

    public void removeExercise(Exercise exercise) {
        exercises.remove(exercise);
    }

    public List<Exercise> getExercises() {
        return Collections.unmodifiableList(exercises);
    }

    public Reminder getReminder() {
        return reminder;
    }

    public void updateReminder(Integer thresholdDays, String note) {
        reminder.update(thresholdDays, note);
    }

    public Exercise findExercise(String exerciseName) {
        for (Exercise exercise : exercises) {
            if (exercise.getName().equalsIgnoreCase(exerciseName)) {
                return exercise;
            }
        }
        return null;
    }

    public void sortExercises() {
        for (int i = 1; i < exercises.size(); i++) {
            Exercise current = exercises.get(i);
            int j = i - 1;
            while (j >= 0 && exercises.get(j).getName().compareToIgnoreCase(current.getName()) > 0) {
                exercises.set(j + 1, exercises.get(j));
                j--;
            }
            exercises.set(j + 1, current);
        }
    }

    public Exercise createExercise(String exerciseName, ExerciseType exerciseType) {
        Exercise exercise = switch (exerciseType) {
            case STRENGTH -> new StrengthExercise(exerciseName, this);
            case CARDIO -> new CardioExercise(exerciseName, this);
            case ENDURANCE -> new EnduranceExercise(exerciseName, this);
        };
        addExercise(exercise);
        sortExercises();
        return exercise;
    }
}
