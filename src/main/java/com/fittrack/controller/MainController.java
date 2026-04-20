package com.fittrack.controller;

import com.fittrack.Main;
import com.fittrack.util.DataStore;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * MainController.java — Quản lý cửa sổ chính: sidebar + vùng nội dung.
 * Tương ứng với main.fxml (layout BorderPane).
 */
public class MainController {

    @FXML private BorderPane mainPane;   // Layout chính: LEFT = sidebar, CENTER = content
    @FXML private Label usernameLabel;   // Hiện tên user ở top bar

    /**
     * Chạy tự động khi FXML được load xong.
     * Hiện tên user và load Dashboard mặc định.
     */
    @FXML
    private void initialize() {
        // ===== GỌI BACKEND: DataStore → getCurrentUser() → getUsername() =====
        DataStore store = DataStore.getInstance();
        if (store.getCurrentUser() != null) {
            usernameLabel.setText(store.getCurrentUser().getUsername());
        }

        // Load Dashboard mặc định khi mở app
        navigateTo("dashboard");
    }

    // ===== CÁC NÚT SIDEBAR =====

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

    /**
     * Load màn hình tương ứng vào vùng CENTER của BorderPane.
     * @param viewName tên file fxml (không có extension)
     */
    private void navigateTo(String viewName) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/fittrack/" + viewName + ".fxml")
            );
            Parent view = loader.load();
            mainPane.setCenter(view); // Thay nội dung vùng trung tâm
            System.out.println("Đã chuyển sang: " + viewName);
        } catch (Exception e) {
            System.out.println("Lỗi khi load view " + viewName + ": " + e.getMessage());
        }
    }

    /**
     * Xử lý nút Logout: đóng main window, hiện lại Login popup.
     */
    @FXML
    private void handleLogout() {
        try {
            Stage stage = (Stage) mainPane.getScene().getWindow();
            stage.hide();
            Main.showLoginPopup(stage);
        } catch (Exception e) {
            System.out.println("Lỗi logout: " + e.getMessage());
        }
    }

    /**
     * Mở cửa sổ Settings (có thể mở rộng sau).
     */
    @FXML
    private void openSettings() {
        System.out.println("TODO: Mở Settings dialog");
    }
}
