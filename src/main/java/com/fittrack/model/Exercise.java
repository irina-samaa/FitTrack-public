package com.fittrack.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Exercise {
    private final String name;
    private final BodyPart bodyPart;
    private final List<SetRecord> sets = new ArrayList<>();

    protected Exercise(String name, BodyPart bodyPart) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Exercise name cannot be blank.");
        }
        this.name = name.trim();
        this.bodyPart = bodyPart;
    }

    public String getName() {
        return name;
    }

    public BodyPart getBodyPart() {
        return bodyPart;
    }

    public List<SetRecord> getSets() {
        return Collections.unmodifiableList(sets);
    }

    protected void addSetRecord(SetRecord setRecord) {
        if (setRecord == null) {
            throw new IllegalArgumentException("Set record cannot be null.");
        }
        sets.add(setRecord);
    }

    public double getTotalVolume() {
        double total = 0;
        for (SetRecord set : sets) {
            total += set.getWorkloadScore();
        }
        return total;
    }

    public abstract void addSet(int firstMetric, double secondMetric);

    public abstract ExerciseType getExerciseType();
}
