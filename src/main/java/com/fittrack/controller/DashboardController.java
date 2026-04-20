package com.fittrack.controller;

import com.fittrack.model.Reminder;
import com.fittrack.model.WorkoutSession;
import com.fittrack.util.DataStore;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.ArrayList;

/**
 * DashboardController.java — Màn hình Home tổng quan.
 * Hiện 3 card tóm tắt + câu động lực.
 */
public class DashboardController {

    // --- Labels trong 3 card tóm tắt ---
    @FXML private Label sessionCountLabel;    // Card 1: số buổi tập hôm nay
    @FXML private Label lastSessionLabel;     // Card 1: tên buổi tập gần nhất
    @FXML private Label bmiValueLabel;        // Card 2: chỉ số BMI
    @FXML private Label bmiCategoryLabel;     // Card 2: phân loại BMI
    @FXML private Label nextReminderLabel;    // Card 3: nhắc nhở tiếp theo
    @FXML private Label motivationLabel;      // Câu động lực phía dưới

    // Một vài câu động lực hardcode
    private static final String[] QUOTES = {
        "No pain, no gain. Push through!",
        "Your only competition is yesterday's you.",
        "Consistency beats motivation every time.",
        "Train hard, recover smart.",
        "One more rep. Always one more rep."
    };

    /**
     * Tự động chạy khi Dashboard được load.
     * Load dữ liệu từ DataStore và hiện lên các card.
     */
    @FXML
    private void initialize() {
        DataStore store = DataStore.getInstance();

        loadWorkoutCard(store);
        loadBMICard(store);
        loadReminderCard(store);
        loadMotivationQuote();
    }

    /**
     * Card 1: Thông tin buổi tập.
     * Hiện số session và tên session gần nhất.
     */
    private void loadWorkoutCard(DataStore store) {
        // ===== GỌI BACKEND: DataStore.getSessions() =====
        ArrayList<WorkoutSession> sessions = store.getSessions();

        sessionCountLabel.setText(sessions.size() + " sessions logged");

        if (sessions.isEmpty()) {
            lastSessionLabel.setText("No workouts yet — go crush it!");
        } else {
            // ===== GỌI BACKEND: WorkoutSession.getSessionName() =====
            WorkoutSession last = sessions.get(sessions.size() - 1);
            lastSessionLabel.setText("Last: " + last.getSessionName());
        }
    }

    /**
     * Card 2: Thông tin BMI.
     * Lấy từ HealthMetrics của user hiện tại.
     */
    private void loadBMICard(DataStore store) {
        // ===== GỌI BACKEND: DataStore.getCurrentUser().getHealthMetrics() =====
        if (store.getCurrentUser() == null) return;

        var health = store.getCurrentUser().getHealthMetrics();
        if (health == null) return;

        // ===== GỌI BACKEND: HealthMetrics.calculateBMI() =====
        double bmi = health.calculateBMI();
        bmiValueLabel.setText(String.format("%.1f", bmi));

        // ===== GỌI BACKEND: HealthMetrics.getBMICategory() =====
        String category = health.getBMICategory();
        bmiCategoryLabel.setText(category);

        // Đổi màu category badge dựa trên kết quả
        switch (category) {
            case "Normal"      -> bmiCategoryLabel.setStyle("-fx-text-fill: #39FF14;");
            case "Underweight" -> bmiCategoryLabel.setStyle("-fx-text-fill: #00E5FF;");
            case "Overweight"  -> bmiCategoryLabel.setStyle("-fx-text-fill: #FFD700;");
            case "Obese"       -> bmiCategoryLabel.setStyle("-fx-text-fill: #FF4500;");
        }
    }

    /**
     * Card 3: Nhắc nhở sắp tới nhất.
     */
    private void loadReminderCard(DataStore store) {
        // ===== GỌI BACKEND: DataStore.getReminderService().getNextReminder() =====
        Reminder next = store.getReminderService().getNextReminder();

        if (next == null) {
            nextReminderLabel.setText("No upcoming reminders");
        } else {
            // ===== GỌI BACKEND: Reminder.getLabel() + getScheduledTime() =====
            nextReminderLabel.setText(next.getLabel() + "\n" + next.getScheduledTime().toString());
        }
    }

    /**
     * Chọn ngẫu nhiên một câu động lực và hiện lên.
     */
    private void loadMotivationQuote() {
        int index = (int) (Math.random() * QUOTES.length);
        motivationLabel.setText("\"" + QUOTES[index] + "\"");
    }
}
