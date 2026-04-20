package com.fittrack.model;

import java.util.ArrayList;

/**
 * BodyPart.java — STUB cho UI team.
 * Backend team implement sortExercises() (Insertion Sort) và findExercise() (Linear Search).
 */
public class BodyPart {
    private String name;
    private ArrayList<Exercise> exercises = new ArrayList<>();

    public BodyPart(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    public void addExercise(Exercise exercise) {
        exercises.add(exercise); // TODO (Backend): implement
    }

    public ArrayList<Exercise> getExercises() { return exercises; }

    /**
     * Tìm Exercise theo tên — Linear Search.
     * TODO (Backend): implement vòng lặp tìm kiếm tuyến tính.
     */
    public Exercise findExercise(String name) {
        for (Exercise ex : exercises) {
            if (ex.getName().equals(name)) return ex;
        }
        return null;
    }

    /**
     * Sắp xếp exercises theo tên — Insertion Sort.
     * TODO (Backend): implement Insertion Sort theo vibe doc.
     */
    public void sortExercises() {
        // TODO (Backend): Insertion Sort
        // Xem hint trong vibe doc:
        // for (int i = 1; i < exercises.size(); i++) { ... }
        System.out.println("TODO: sortExercises() — Backend team implement Insertion Sort");
    }
}
