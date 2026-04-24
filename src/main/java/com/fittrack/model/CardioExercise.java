package com.fittrack.model;

public class CardioExercise extends Exercise {
    public CardioExercise(String name, BodyPart bodyPart) {
        super(name, bodyPart);
    }

    public void addSet(int durationMinutes, double distanceKm) {
        addSetRecord(new CardioSetRecord(durationMinutes, distanceKm));
    }

    @Override
    public ExerciseType getExerciseType() {
        return ExerciseType.CARDIO;
    }

    @Override
    protected Exercise createCopy() {
        return new CardioExercise(getName(), getBodyPart());
    }
}
