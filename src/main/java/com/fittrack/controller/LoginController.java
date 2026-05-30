package com.fittrack.controller;

import com.fittrack.Main;
import com.fittrack.firebase.FirebaseAuthService;
import com.fittrack.service.FitnessTrackerService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {
    @FXML private TextField usernameField;     // used as email field
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final FirebaseAuthService authService = FirebaseAuthService.getInstance();
    private final FitnessTrackerService service = FitnessTrackerService.getInstance();
    private Stage primaryStage;

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    @FXML
    private void handleLogin() {
        String email = usernameField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter your email and password.");
            return;
        }

        boolean success = authService.signIn(email, password);

        if (success) {
            service.setCurrentUserId(authService.getCurrentUserId());
            try {
                Stage loginStage = (Stage) usernameField.getScene().getWindow();
                loginStage.close();
                Main.loadMainWindow(primaryStage);
            } catch (Exception e) {
                showError("Error loading main window: " + e.getMessage());
            }
        } else {
            showError("Invalid email or password. Please try again.");
        }
    }

    @FXML
    private void handleCreateAccount() {
        String email = usernameField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter an email and password to create an account.");
            return;
        }

        if (password.length() < 6) {
            showError("Password must be at least 6 characters.");
            return;
        }

        boolean success = authService.signUp(email, password);

        if (success) {
            service.setCurrentUserId(authService.getCurrentUserId());
            try {
                Stage loginStage = (Stage) usernameField.getScene().getWindow();
                loginStage.close();
                Main.loadMainWindow(primaryStage);
            } catch (Exception e) {
                showError("Error loading main window: " + e.getMessage());
            }
        } else {
            showError("Registration failed. Email may already be in use.");
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
    private void clearError() {
        errorLabel.setVisible(false);
    }
}
