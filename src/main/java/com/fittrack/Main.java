package com.fittrack;

import com.fittrack.controller.LoginController;
import com.fittrack.service.FitnessTrackerService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Main.java - Entry point cua ung dung FitTrack.
 * Chay file nay de khoi dong app.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FitnessTrackerService.getInstance().login("admin", "1234");
        loadMainWindow(primaryStage);
    }

    /**
     * Hien cua so Login dang modal (chan window chinh).
     * Sau khi login thanh cong, LoginController se goi loadMainWindow().
     */
    public static void showLoginPopup(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                Main.class.getResource("/com/fittrack/login.fxml"));
        Parent root = loader.load();

        // Truyen primaryStage vao controller de sau khi login xong co the load main window.
        LoginController loginCtrl = loader.getController();
        loginCtrl.setPrimaryStage(primaryStage);

        Stage loginStage = new Stage();
        loginStage.initModality(Modality.APPLICATION_MODAL);
        loginStage.setTitle("FitTrack - Login");
        loginStage.setScene(new Scene(root, 400, 300));
        loginStage.setResizable(false);
        loginStage.show();
    }

    /**
     * Load cua so chinh sau khi login thanh cong.
     * Duoc goi tu LoginController.
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
