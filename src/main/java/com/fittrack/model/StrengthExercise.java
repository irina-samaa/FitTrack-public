package com.fittrack.model;

public class StrengthExercise extends Exercise {
    public StrengthExercise(String name, BodyPart bodyPart) {
        super(name, bodyPart);
    }

    public void addSet(int reps, double weight) {
        addSetRecord(new StrengthSetRecord(reps, weight));
    }

    @Override
    public ExerciseType getExerciseType() {
        return ExerciseType.STRENGTH;
    }

    @Override
    protected Exercise createCopy() {
        return new StrengthExercise(getName(), getBodyPart());
    }
}
