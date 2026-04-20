package com.fittrack.model;

import java.util.ArrayList;

/**
 * WorkoutSession.java — STUB cho UI team.
 */
public class WorkoutSession {
    private String date;
    private String sessionName;
    private ArrayList<Exercise> exercises = new ArrayList<>();

    public WorkoutSession(String date, String sessionName) {
        this.date = date;
        this.sessionName = sessionName;
    }

    public String getDate() { return date; }
    public String getSessionName() { return sessionName; }

    public void addExercise(Exercise exercise) {
        exercises.add(exercise); // TODO (Backend): implement
    }

    public ArrayList<Exercise> getExercises() { return exercises; }

    /**
     * Linear Search tìm exercise theo tên.
     * TODO (Backend): implement.
     */
    public Exercise findExercise(String name) {
        for (Exercise ex : exercises) {
            if (ex.getName().equals(name)) return ex;
        }
        return null;
    }

    /**
     * Trả về chuỗi tóm tắt session.
     * TODO (Backend): format đẹp hơn.
     */
    public String getSummary() {
        return "Session: " + sessionName + " | Date: " + date
             + " | Exercises: " + exercises.size();
    }
}
