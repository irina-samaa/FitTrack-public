package com.fittrack.model;

public enum ExerciseType {
    STRENGTH("Strength"),
    CARDIO("Cardio"),
    ENDURANCE("Endurance");

    private final String displayName;

    ExerciseType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ExerciseType fromDisplayName(String value) {
        for (ExerciseType type : values()) {
            if (type.displayName.equalsIgnoreCase(value) || type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unsupported exercise type: " + value);
    }
}
