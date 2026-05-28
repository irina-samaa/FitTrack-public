package com.fittrack;
 
import com.fittrack.controller.LoginController;
import com.fittrack.firebase.FirebaseConfig;
import com.fittrack.service.FitnessTrackerService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
 
/**
 * Main.java - Entry point of the FitTrack application.
 * Run this file to start the app.
 */
public class Main extends Application {
 
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialize Firebase before anything else
        try {
            FirebaseConfig.init();
        } catch (Exception e) {
            System.err.println("Failed to initialize Firebase: " + e.getMessage());
        }
 
        FitnessTrackerService.getInstance();
        showLoginPopup(primaryStage);
    }
 
    /**
     * Shows the login window as a modal dialog that blocks the main window.
     * After a successful login, LoginController calls loadMainWindow().
     */
    public static void showLoginPopup(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                Main.class.getResource("/com/fittrack/login.fxml"));
        Parent root = loader.load();
 
        LoginController loginCtrl = loader.getController();
        loginCtrl.setPrimaryStage(primaryStage);
 
        Stage loginStage = new Stage();
        loginStage.initModality(Modality.APPLICATION_MODAL);
        loginStage.setTitle("FitTrack - Login");
        Scene scene = new Scene(root, 400, 380); // slightly taller for register button
        scene.getStylesheets().add(
            Main.class.getResource("/com/fittrack/styles.css").toExternalForm());
        loginStage.setScene(scene);
        loginStage.setResizable(false);
        loginStage.setOnHidden(event -> {
            if (primaryStage.getScene() == null && FitnessTrackerService.getInstance().getCurrentUser() == null) {
                Platform.exit();
            }
        });
        loginStage.show();
    }
 
    /**
     * Loads the main window after a successful login.
     * Called from LoginController.
     */
    public static void loadMainWindow(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                Main.class.getResource("/com/fittrack/main.fxml"));
        Parent root = loader.load();
 
        Scene scene = new Scene(root, 1280, 760);
        scene.getStylesheets().add(
                Main.class.getResource("/com/fittrack/styles.css").toExternalForm());
 
        primaryStage.setTitle("FitTrack");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1180);
        primaryStage.setMinHeight(680);
        primaryStage.show();
    }
 
    public static void main(String[] args) {
        launch(args);
    }
}
