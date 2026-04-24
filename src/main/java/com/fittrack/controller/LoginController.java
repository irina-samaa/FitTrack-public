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

        if (service.login(username, password)) {
            try {
                Stage loginStage = (Stage) usernameField.getScene().getWindow();
                loginStage.close();
                Main.loadMainWindow(primaryStage);
            } catch (Exception e) {
                System.out.println("Loi khi load main window: " + e.getMessage());
            }
            return;
        }

        showError("Invalid credentials. Try admin / 1234");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    @FXML
    private void clearError() {
        errorLabel.setVisible(false);
    }
}
