package com.fittrack.controller;

import com.fittrack.service.FitnessTrackerService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainController {
    @FXML private BorderPane mainPane;
    @FXML private Label usernameLabel;

    @FXML
    private void initialize() {
        var currentUser = FitnessTrackerService.getInstance().getCurrentUser();
        if (currentUser != null) {
            usernameLabel.setText(currentUser.getUsername());
        }
        navigateTo("dashboard");
    }

    @FXML
    private void goHome() {
        navigateTo("dashboard");
    }

    @FXML
    private void goWorkout() {
        navigateTo("workout");
    }

    @FXML
    private void goSchedule() {
        navigateTo("schedule");
    }

    @FXML
    private void goHealth() {
        navigateTo("health");
    }

    @FXML
    private void goProgress() {
        navigateTo("progress");
    }

    private void navigateTo(String viewName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fittrack/" + viewName + ".fxml"));
            Parent view = loader.load();
            mainPane.setCenter(view);
        } catch (Exception e) {
            System.out.println("Error loading view " + viewName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        try {
            FitnessTrackerService.getInstance().logout();
            Stage stage = (Stage) mainPane.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            System.out.println("Logout error: " + e.getMessage());
        }
    }

    @FXML
    private void openSettings() {
        System.out.println("TODO: Open Settings dialog");
    }
}
