package com.fittrack.model;

/**
 * ExerciseSet.java — STUB cho UI team.
 * Backend team implement đầy đủ.
 */
public class ExerciseSet {
    private int reps;
    private double weightKg;

    public ExerciseSet(int reps, double weightKg) {
        this.reps = reps;
        this.weightKg = weightKg;
    }

    public int getReps() { return reps; }
    public double getWeightKg() { return weightKg; }

    @Override
    public String toString() {
        return reps + " reps × " + weightKg + " kg"; // TODO (Backend): format chính xác
    }
}
