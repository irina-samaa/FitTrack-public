package com.fittrack.model;

import java.util.ArrayList;

/**
 * Exercise.java — STUB cho UI team.
 */
public class Exercise {
    private String name;
    private ArrayList<ExerciseSet> sets = new ArrayList<>();

    public Exercise(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    public void addSet(ExerciseSet set) {
        sets.add(set); // TODO (Backend): implement
    }

    public ArrayList<ExerciseSet> getSets() { return sets; }

    /** Tổng volume = sum(reps × weight) qua tất cả set */
    public double getTotalVolume() {
        double total = 0;
        for (ExerciseSet s : sets) {
            total += s.getReps() * s.getWeightKg(); // TODO (Backend): implement
        }
        return total;
    }
}
