package com.fittrack.model;

public class EnduranceExercise extends Exercise {
    public EnduranceExercise(String name, BodyPart bodyPart) {
        super(name, bodyPart);
    }

    public void addSet(int durationMinutes, int heartRate) {
        addSetRecord(new EnduranceSetRecord(durationMinutes, heartRate));
    }

    @Override
    public void addSet(int firstMetric, double secondMetric) {
        addSet(firstMetric, (int) Math.round(secondMetric));
    }

    @Override
    public ExerciseType getExerciseType() {
        return ExerciseType.ENDURANCE;
    }

    @Override
    protected Exercise createCopy() {
        return new EnduranceExercise(getName(), getBodyPart());
    }
}
