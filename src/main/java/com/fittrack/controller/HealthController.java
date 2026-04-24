package com.fittrack.controller;

import com.fittrack.service.FitnessTrackerService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class HealthController {
    @FXML private TextField weightField;
    @FXML private TextField heightField;
    @FXML private Label bmiResultLabel;
    @FXML private Label bmiCategoryBadge;
    @FXML private Label recommendationLabel;
    @FXML private Label currentWeightLabel;
    @FXML private Label currentHeightLabel;

    private final FitnessTrackerService service = FitnessTrackerService.getInstance();

    @FXML
    private void initialize() {
        var user = service.getCurrentUser();
        if (user == null) {
            return;
        }

        currentWeightLabel.setText("Current weight: " + user.getWeight() + " kg");
        currentHeightLabel.setText("Current height: " + user.getHeight() + " cm");
        weightField.setText(String.valueOf(user.getWeight()));
        heightField.setText(String.valueOf(user.getHeight()));
        calculateAndDisplay();
    }

    @FXML
    private void handleUpdate() {
        try {
            double weight = Double.parseDouble(weightField.getText().trim());
            double height = Double.parseDouble(heightField.getText().trim());

            if (weight <= 0 || height <= 0) {
                showError("Weight and height must be greater than 0.");
                return;
            }

            service.updateHealth(weight, height);
            currentWeightLabel.setText("Current weight: " + weight + " kg");
            currentHeightLabel.setText("Current height: " + height + " cm");
            calculateAndDisplay();
        } catch (IllegalArgumentException e) {
            showError("Enter valid numbers for weight and height.");
        }
    }

    private void calculateAndDisplay() {
        double bmi = service.calculateBMI();
        bmiResultLabel.setText(String.format("%.2f", bmi));

        String category = service.getBmiCategory();
        bmiCategoryBadge.setText(category);

        String color = switch (category) {
            case "Normal" -> "#39FF14";
            case "Underweight" -> "#00E5FF";
            case "Overweight" -> "#FFD700";
            case "Obese" -> "#FF4500";
            default -> "#AAAAAA";
        };
        bmiCategoryBadge.setStyle(
            "-fx-background-color: " + color + "22;" +
            "-fx-text-fill: " + color + ";" +
            "-fx-border-color: " + color + ";" +
            "-fx-border-radius: 6; -fx-background-radius: 6;" +
            "-fx-padding: 4 12;"
        );

        recommendationLabel.setStyle("-fx-text-fill: #AAAAAA;");
        recommendationLabel.setText(service.getHealthRecommendation());
    }

    private void showError(String message) {
        recommendationLabel.setText("! " + message);
        recommendationLabel.setStyle("-fx-text-fill: #FF4500;");
    }
}
