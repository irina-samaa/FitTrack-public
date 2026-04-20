package com.fittrack.controller;

import com.fittrack.model.HealthMetrics;
import com.fittrack.util.DataStore;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * HealthController.java — Màn hình nhập chỉ số sức khỏe và tính BMI.
 * User nhập cân nặng + chiều cao → xem BMI + khuyến nghị.
 */
public class HealthController {

    // --- Input fields ---
    @FXML private TextField weightField;   // Cân nặng (kg)
    @FXML private TextField heightField;   // Chiều cao (cm)

    // --- Result display ---
    @FXML private Label bmiResultLabel;         // Con số BMI lớn, đậm
    @FXML private Label bmiCategoryBadge;       // Badge phân loại (color-coded)
    @FXML private Label recommendationLabel;    // Lời khuyên dựa trên BMI
    @FXML private Label currentWeightLabel;     // Hiện cân nặng hiện tại
    @FXML private Label currentHeightLabel;     // Hiện chiều cao hiện tại

    /**
     * Khởi tạo: hiện giá trị cân nặng/chiều cao hiện tại của user.
     */
    @FXML
    private void initialize() {
        // ===== GỌI BACKEND: DataStore.getCurrentUser().getHealthMetrics() =====
        HealthMetrics health = getHealthMetrics();
        if (health == null) return;

        // ===== GỌI BACKEND: HealthMetrics.getWeight() / getHeight() =====
        currentWeightLabel.setText("Current weight: " + health.getWeight() + " kg");
        currentHeightLabel.setText("Current height: " + health.getHeight() + " cm");

        // Pre-fill input fields với giá trị hiện tại
        weightField.setText(String.valueOf(health.getWeight()));
        heightField.setText(String.valueOf(health.getHeight()));

        // Tính và hiện BMI ngay khi load
        calculateAndDisplay(health);
    }

    /**
     * Nút "Update": lưu cân nặng/chiều cao mới và tính lại BMI.
     */
    @FXML
    private void handleUpdate() {
        HealthMetrics health = getHealthMetrics();
        if (health == null) return;

        try {
            double weight = Double.parseDouble(weightField.getText().trim());
            double height = Double.parseDouble(heightField.getText().trim());

            if (weight <= 0 || height <= 0) {
                showError("Cân nặng và chiều cao phải > 0!");
                return;
            }

            // ===== GỌI BACKEND: HealthMetrics.updateWeight(weightKg) =====
            // Lưu ý: updateWeight() vừa set weight vừa append vào weightHistory!
            health.updateWeight(weight);

            // ===== GỌI BACKEND: HealthMetrics.updateHeight(heightCm) =====
            health.updateHeight(height);

            // Cập nhật label hiện tại
            currentWeightLabel.setText("Current weight: " + weight + " kg");
            currentHeightLabel.setText("Current height: " + height + " cm");

            // Tính lại BMI
            calculateAndDisplay(health);
            System.out.println("Updated: " + weight + "kg / " + height + "cm");

        } catch (NumberFormatException e) {
            showError("Nhập số hợp lệ cho cân nặng và chiều cao!");
        }
    }

    /**
     * Tính BMI và cập nhật toàn bộ kết quả lên UI.
     */
    private void calculateAndDisplay(HealthMetrics health) {
        // ===== GỌI BACKEND: HealthMetrics.calculateBMI() =====
        double bmi = health.calculateBMI();
        bmiResultLabel.setText(String.format("%.2f", bmi));

        // ===== GỌI BACKEND: HealthMetrics.getBMICategory() =====
        String category = health.getBMICategory();
        bmiCategoryBadge.setText(category);

        // Đổi màu badge theo category
        String color = switch (category) {
            case "Normal"      -> "#39FF14"; // neon green
            case "Underweight" -> "#00E5FF"; // electric cyan
            case "Overweight"  -> "#FFD700"; // warning yellow
            case "Obese"       -> "#FF4500"; // danger red
            default            -> "#AAAAAA";
        };
        bmiCategoryBadge.setStyle(
            "-fx-background-color: " + color + "22;" +
            "-fx-text-fill: " + color + ";" +
            "-fx-border-color: " + color + ";" +
            "-fx-border-radius: 6; -fx-background-radius: 6;" +
            "-fx-padding: 4 12;"
        );

        // ===== GỌI BACKEND: HealthMetrics.getHealthRecommendation() =====
        String recommendation = health.getHealthRecommendation();
        recommendationLabel.setText(recommendation);
    }

    /**
     * Tiện ích: lấy HealthMetrics của user hiện tại.
     */
    private HealthMetrics getHealthMetrics() {
        // ===== GỌI BACKEND: DataStore.getCurrentUser().getHealthMetrics() =====
        if (DataStore.getInstance().getCurrentUser() == null) return null;
        return DataStore.getInstance().getCurrentUser().getHealthMetrics();
    }

    /**
     * Hiện thông báo lỗi trong recommendationLabel (tái sử dụng chỗ hiện text).
     */
    private void showError(String message) {
        recommendationLabel.setText("⚠ " + message);
        recommendationLabel.setStyle("-fx-text-fill: #FF4500;");
    }
}
