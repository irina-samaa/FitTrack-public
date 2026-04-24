package com.fittrack.model;

public class HealthMetrics {
    public double calculateBMI(double weight, double heightCm) {
        double heightM = heightCm / 100.0;
        if (heightM <= 0) {
            throw new IllegalArgumentException("Height must be greater than 0.");
        }
        return weight / (heightM * heightM);
    }

    public String getBMICategory(double bmi) {
        if (bmi < 18.5) {
            return "Underweight";
        }
        if (bmi < 25.0) {
            return "Normal";
        }
        if (bmi < 30.0) {
            return "Overweight";
        }
        return "Obese";
    }

    public String getHealthSuggestion(double bmi) {
        String category = getBMICategory(bmi);
        return switch (category) {
            case "Underweight" -> "Increase nutrient-dense calories and prioritize strength training.";
            case "Normal" -> "Maintain your current routine with consistent sleep, protein, and hydration.";
            case "Overweight" -> "Add more daily activity and control calorie intake with steady cardio.";
            default -> "Consult a healthcare professional and build a gradual plan for weight reduction.";
        };
    }
}
