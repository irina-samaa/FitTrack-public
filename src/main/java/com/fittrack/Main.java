package com.fittrack;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.fittrack.controller.LoginController;

import com.fittrack.service.FitnessTrackerService;

/**
 * Main.java — Entry point của ứng dụng FitTrack.
 * Chạy file này để khởi động app.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Auto-login directly to the main page for testing/convenience
        FitnessTrackerService.getInstance().login("admin", "1234");
        loadMainWindow(primaryStage);
    }

    /**
     * Hiện cửa sổ Login dạng modal (chặn window chính).
     * Sau khi login thành công, LoginController sẽ gọi loadMainWindow().
     */
    public static void showLoginPopup(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                Main.class.getResource("/com/fittrack/login.fxml"));
        Parent root = loader.load();

        // Truyền primaryStage vào controller để sau khi login xong có thể load main
        // window
        LoginController loginCtrl = loader.getController();
        loginCtrl.setPrimaryStage(primaryStage);

        Stage loginStage = new Stage();
        loginStage.initModality(Modality.APPLICATION_MODAL); // Chặn user tương tác nơi khác
        loginStage.setTitle("FitTrack — Login");
        loginStage.setScene(new Scene(root, 400, 300));
        loginStage.setResizable(false);
        loginStage.show();
    }

    /**
     * Load cửa sổ chính sau khi login thành công.
     * Được gọi từ LoginController.
     */
    public static void loadMainWindow(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                Main.class.getResource("/com/fittrack/main.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1000, 650);
        scene.getStylesheets().add(
                Main.class.getResource("/com/fittrack/styles.css").toExternalForm());

        primaryStage.setTitle("FitTrack");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
