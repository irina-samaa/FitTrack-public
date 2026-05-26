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
    @FXML private LineChart<String, Number> workloadChart;
    @FXML private ComboBox<Integer> windowComboBox;
    @FXML private Label averageWeightLabel;
    @FXML private Label averageWorkloadLabel;

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
        workloadChart.getData().clear();
        if (service.getCurrentUser() == null) {
            return;
        }

        ArrayList<String> labels = service.getProgressLabels();
        ArrayList<Double> rawWeights = service.getWeightHistory();
        if (rawWeights.isEmpty()) {
            averageWeightLabel.setText("No data");
        } else {
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
            updateAverageWeightLabel(rawWeights);
        }

        refreshWorkloadChart();
    }

    private void refreshWorkloadChart() {
        ArrayList<String> labels = service.getWorkloadProgressLabels();
        ArrayList<Double> workloads = service.getDailyWorkloads();
        if (workloads.isEmpty()) {
            averageWorkloadLabel.setText("No data");
            return;
        }

        XYChart.Series<String, Number> workloadSeries = new XYChart.Series<>();
        workloadSeries.setName("Daily Workload");
        for (int i = 0; i < workloads.size(); i++) {
            workloadSeries.getData().add(new XYChart.Data<>(labels.get(i), workloads.get(i)));
        }
        workloadChart.getData().add(workloadSeries);
        averageWorkloadLabel.setText(String.format("%.1f", service.getAverageWorkload()));
    }

    private void updateAverageWeightLabel(ArrayList<Double> weights) {
        double sum = 0;

        for (double weight : weights) {
            sum += weight;
        }

        averageWeightLabel.setText(String.format("%.1f kg", sum / weights.size()));
    }

}
