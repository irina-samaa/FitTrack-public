package com.fittrack.model;

import java.util.ArrayList;

/**
 * HealthMetrics.java — STUB cho UI team.
 * Backend team implement đầy đủ logic calculateBMI, getBMICategory, v.v.
 */
public class HealthMetrics {
    private double weightKg;
    private double heightCm;
    private ArrayList<Double> weightHistory = new ArrayList<>();

    public HealthMetrics(double weightKg, double heightCm) {
        this.weightKg = weightKg;
        this.heightCm = heightCm;
    }

    /** Cập nhật cân nặng VÀ lưu vào lịch sử */
    public void updateWeight(double weightKg) {
        this.weightKg = weightKg;
        weightHistory.add(weightKg); // TODO (Backend): implement
    }

    public void updateHeight(double heightCm) {
        this.heightCm = heightCm; // TODO (Backend): implement
    }

    public double getWeight() { return weightKg; }
    public double getHeight() { return heightCm; }

    /** Tính BMI = weight / (height_m)^2 */
    public double calculateBMI() {
        // TODO (Backend): implement công thức
        double heightM = heightCm / 100.0;
        return weightKg / (heightM * heightM);
    }

    /** Trả về "Underweight" / "Normal" / "Overweight" / "Obese" */
    public String getBMICategory() {
        // TODO (Backend): implement theo threshold trong vibe doc
        double bmi = calculateBMI();
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25.0) return "Normal";
        if (bmi < 30.0) return "Overweight";
        return "Obese";
    }

    /** Trả về lời khuyên ngắn dựa trên BMI */
    public String getHealthRecommendation() {
        // TODO (Backend): implement
        return "TODO: Backend team thêm lời khuyên theo getBMICategory()";
    }

    public ArrayList<Double> getWeightHistory() { return weightHistory; }
}
