package com.fittrack.controller;

import com.fittrack.service.FitnessTrackerService;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import java.util.ArrayList;

public class ProgressController {
    @FXML private LineChart<String, Number> weightChart;
    @FXML private ComboBox<Integer> windowComboBox;
    @FXML private Label minWeightLabel;
    @FXML private Label maxWeightLabel;
    @FXML private Label avgWeightLabel;
    @FXML private Label totalEntriesLabel;

    private final FitnessTrackerService service = FitnessTrackerService.getInstance();

    @FXML
    private void initialize() {
        windowComboBox.getItems().addAll(2, 3, 5);
        windowComboBox.setValue(3);
        windowComboBox.setOnAction(event -> refreshChart());
        refreshChart();
    }

    @FXML
    private void refreshChart() {
        weightChart.getData().clear();
        if (service.getCurrentUser() == null) {
            return;
        }

        ArrayList<String> labels = service.getProgressLabels();
        ArrayList<Double> rawWeights = service.getWeightHistory();
        if (rawWeights.isEmpty()) {
            totalEntriesLabel.setText("No data yet. Update your weight in Health tab.");
            return;
        }

        XYChart.Series<String, Number> rawSeries = new XYChart.Series<>();
        rawSeries.setName("Weight (kg)");
        for (int i = 0; i < rawWeights.size(); i++) {
            rawSeries.getData().add(new XYChart.Data<>(labels.get(i), rawWeights.get(i)));
        }

        int window = windowComboBox.getValue();
        ArrayList<Double> movingAverage = service.getMovingAverage(window);
        XYChart.Series<String, Number> movingSeries = new XYChart.Series<>();
        movingSeries.setName("Moving Avg (" + window + ")");
        for (int i = 0; i < movingAverage.size(); i++) {
            movingSeries.getData().add(new XYChart.Data<>(labels.get(i), movingAverage.get(i)));
        }

        weightChart.getData().addAll(rawSeries, movingSeries);
        updateStatLabels(rawWeights);
    }

    private void updateStatLabels(ArrayList<Double> weights) {
        double min = weights.get(0);
        double max = weights.get(0);
        double sum = 0;

        for (double weight : weights) {
            min = Math.min(min, weight);
            max = Math.max(max, weight);
            sum += weight;
        }

        minWeightLabel.setText(String.format("Min: %.1f kg", min));
        maxWeightLabel.setText(String.format("Max: %.1f kg", max));
        avgWeightLabel.setText(String.format("Avg: %.1f kg", sum / weights.size()));
        totalEntriesLabel.setText("Entries: " + weights.size());
    }
}
