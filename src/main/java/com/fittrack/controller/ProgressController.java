package com.fittrack.controller;

import com.fittrack.service.FitnessTrackerService;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
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

        ArrayList<String> labels = service.getChartWeightLabels();
        ArrayList<Double> weights = service.getChartWeightValues();
        if (weights.isEmpty()) {
            averageWeightLabel.setText("No data");
        } else {
            XYChart.Series<String, Number> rawSeries = new XYChart.Series<>();
            rawSeries.setName("Weight (kg)");
            for (int i = 0; i < weights.size(); i++) {
                rawSeries.getData().add(new XYChart.Data<>(labels.get(i), weights.get(i)));
            }

            int window = windowComboBox.getValue();
            ArrayList<Double> movingAverage = service.getChartWeightMovingAverage(window);
            XYChart.Series<String, Number> movingSeries = new XYChart.Series<>();
            movingSeries.setName("Moving Avg (" + window + ")");
            for (int i = 0; i < movingAverage.size(); i++) {
                movingSeries.getData().add(new XYChart.Data<>(labels.get(i), movingAverage.get(i)));
            }

            weightChart.getData().addAll(rawSeries, movingSeries);
            focusWeightAxis(weights);
            updateAverageWeightLabel(weights);
        }

        refreshWorkloadChart();
    }

    private void refreshWorkloadChart() {
        ArrayList<String> labels = service.getChartWorkloadLabels();
        ArrayList<Double> workloads = service.getChartWorkloadValues();
        if (workloads.isEmpty()) {
            averageWorkloadLabel.setText("No data");
            return;
        }

        XYChart.Series<String, Number> workloadSeries = new XYChart.Series<>();
        workloadSeries.setName("Daily Workload");
        for (int i = 0; i < workloads.size(); i++) {
            workloadSeries.getData().add(new XYChart.Data<>(labels.get(i), workloads.get(i)));
        }

        int window = windowComboBox.getValue();
        ArrayList<Double> movingAverage = service.getChartWorkloadMovingAverage(window);
        XYChart.Series<String, Number> movingSeries = new XYChart.Series<>();
        movingSeries.setName("Moving Avg (" + window + ")");
        for (int i = 0; i < movingAverage.size(); i++) {
            movingSeries.getData().add(new XYChart.Data<>(labels.get(i), movingAverage.get(i)));
        }

        workloadChart.getData().addAll(workloadSeries, movingSeries);
        updateAverageWorkloadLabel(workloads);
    }

    private void updateAverageWeightLabel(ArrayList<Double> weights) {
        double sum = 0;

        for (double weight : weights) {
            sum += weight;
        }

        averageWeightLabel.setText(String.format("%.1f kg", sum / weights.size()));
    }

    private void updateAverageWorkloadLabel(ArrayList<Double> workloads) {
        double sum = 0;

        for (double workload : workloads) {
            sum += workload;
        }

        averageWorkloadLabel.setText(String.format("%.1f", sum / workloads.size()));
    }

    private void focusWeightAxis(ArrayList<Double> weights) {
        NumberAxis weightAxis = (NumberAxis) weightChart.getYAxis();
        double min = weights.get(0);
        double max = weights.get(0);

        for (double weight : weights) {
            min = Math.min(min, weight);
            max = Math.max(max, weight);
        }

        double range = max - min;
        if (range == 0) {
            weightAxis.setAutoRanging(false);
            weightAxis.setLowerBound(min - 1);
            weightAxis.setUpperBound(max + 1);
            weightAxis.setTickUnit(0.5);
            return;
        }

        double padding = Math.max(0.5, range * 0.25);
        double lowerBound = min - padding;
        double upperBound = max + padding;
        double visibleRange = upperBound - lowerBound;

        weightAxis.setAutoRanging(false);
        weightAxis.setLowerBound(lowerBound);
        weightAxis.setUpperBound(upperBound);
        weightAxis.setTickUnit(Math.max(0.5, visibleRange / 5));
    }

}
