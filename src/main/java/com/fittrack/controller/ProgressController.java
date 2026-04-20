package com.fittrack.controller;

import com.fittrack.model.HealthMetrics;
import com.fittrack.model.ProgressTracker;
import com.fittrack.util.DataStore;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import java.util.ArrayList;

/**
 * ProgressController.java — Màn hình biểu đồ cân nặng theo thời gian.
 * Hiện 2 đường: cân nặng thực tế + đường trung bình trượt (moving average).
 */
public class ProgressController {

    // --- Biểu đồ JavaFX LineChart ---
    @FXML private LineChart<String, Number> weightChart;

    // --- ComboBox chọn cửa sổ moving average ---
    @FXML private ComboBox<Integer> windowComboBox;

    // --- Label thống kê ---
    @FXML private Label minWeightLabel;
    @FXML private Label maxWeightLabel;
    @FXML private Label avgWeightLabel;
    @FXML private Label totalEntriesLabel;

    /**
     * Khởi tạo: setup ComboBox và load biểu đồ.
     */
    @FXML
    private void initialize() {
        // Setup ComboBox cho moving average window: 2, 3, 5 entries
        windowComboBox.getItems().addAll(2, 3, 5);
        windowComboBox.setValue(3); // Mặc định window = 3

        // Khi thay đổi window size → refresh chart
        windowComboBox.setOnAction(e -> refreshChart());

        // Load chart lần đầu
        refreshChart();
    }

    /**
     * Refresh toàn bộ biểu đồ dựa trên window size đang chọn.
     * Được gọi khi: lần đầu load, hoặc khi user đổi ComboBox.
     */
    @FXML
    private void refreshChart() {
        weightChart.getData().clear(); // Xóa data cũ

        // ===== GỌI BACKEND: DataStore.getCurrentUser().getHealthMetrics() =====
        HealthMetrics health = DataStore.getInstance().getCurrentUser().getHealthMetrics();
        if (health == null) return;

        // ===== GỌI BACKEND: new ProgressTracker(healthMetrics) — TODO: backend team =====
        ProgressTracker tracker = new ProgressTracker(health);

        // ===== GỌI BACKEND: ProgressTracker.getLabels() — ["Week 1", "Week 2", ...] =====
        ArrayList<String> labels = tracker.getLabels();

        // ===== GỌI BACKEND: HealthMetrics.getWeightHistory() =====
        ArrayList<Double> rawWeights = health.getWeightHistory();

        if (rawWeights.isEmpty()) {
            totalEntriesLabel.setText("No data yet. Update your weight in Health tab.");
            return;
        }

        // === Series 1: Cân nặng thực tế ===
        XYChart.Series<String, Number> rawSeries = new XYChart.Series<>();
        rawSeries.setName("Weight (kg)");

        for (int i = 0; i < rawWeights.size(); i++) {
            String label = (i < labels.size()) ? labels.get(i) : "Entry " + (i + 1);
            rawSeries.getData().add(new XYChart.Data<>(label, rawWeights.get(i)));
        }

        // === Series 2: Đường trung bình trượt (moving average) ===
        int window = windowComboBox.getValue();

        // ===== GỌI BACKEND: ProgressTracker.getMovingAverage(window) =====
        // Tính moving average với cửa sổ window entries
        ArrayList<Double> movingAvg = tracker.getMovingAverage(window);

        XYChart.Series<String, Number> avgSeries = new XYChart.Series<>();
        avgSeries.setName("Moving Avg (" + window + ")");

        for (int i = 0; i < movingAvg.size(); i++) {
            String label = (i < labels.size()) ? labels.get(i) : "Entry " + (i + 1);
            avgSeries.getData().add(new XYChart.Data<>(label, movingAvg.get(i)));
        }

        // Thêm cả 2 series vào chart
        weightChart.getData().addAll(rawSeries, avgSeries);

        // Cập nhật các label thống kê
        updateStatLabels(rawWeights);
    }

    /**
     * Tính và hiện các thống kê cơ bản: min, max, trung bình.
     */
    private void updateStatLabels(ArrayList<Double> weights) {
        if (weights.isEmpty()) return;

        double min = weights.get(0);
        double max = weights.get(0);
        double sum = 0;

        for (double w : weights) {
            if (w < min) min = w;
            if (w > max) max = w;
            sum += w;
        }

        double avg = sum / weights.size();

        minWeightLabel.setText(String.format("Min: %.1f kg", min));
        maxWeightLabel.setText(String.format("Max: %.1f kg", max));
        avgWeightLabel.setText(String.format("Avg: %.1f kg", avg));
        totalEntriesLabel.setText("Entries: " + weights.size());

        // ===== GỌI BACKEND: ProgressTracker.generateWeightGraph() =====
        // (Phương thức này backend dùng nội bộ để generate dữ liệu,
        //  UI đã tự lấy từ getMovingAverage() phía trên)
    }
}
