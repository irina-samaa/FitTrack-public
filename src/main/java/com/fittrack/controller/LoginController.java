package com.fittrack.controller;

import com.fittrack.Main;
import com.fittrack.service.FitnessTrackerService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final FitnessTrackerService service = FitnessTrackerService.getInstance();
    private Stage primaryStage;

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        clearError();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Enter both username and password.");
            return;
        }

        if (service.login(username, password)) {
            openMainWindow();
            return;
        }

        showError("Invalid credentials. Try admin / 1234 or testuser / 1234");
    }

    @FXML
    private void handleCreateAccount() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        clearError();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Enter username and password to create an account.");
            return;
        }

        if (service.createAccount(username, password)) {
            openMainWindow();
            return;
        }

        showError("Username already exists or input is invalid.");
    }

    private void openMainWindow() {
        try {
            Main.loadMainWindow(primaryStage);
            Stage loginStage = (Stage) usernameField.getScene().getWindow();
            loginStage.close();
        } catch (Exception e) {
            System.out.println("Error loading main window: " + e.getMessage());
            showError("Could not open the main window.");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    @FXML
    private void clearError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
