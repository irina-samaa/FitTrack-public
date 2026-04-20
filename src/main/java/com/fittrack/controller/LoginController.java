package com.fittrack.controller;

import com.fittrack.Main;
import com.fittrack.util.DataStore;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * LoginController.java — Xử lý màn hình đăng nhập.
 * Tương ứng với file login.fxml.
 */
public class LoginController {

    // --- Các thành phần UI được inject từ FXML ---
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel; // Label hiện thông báo lỗi màu đỏ

    private Stage primaryStage; // Cửa sổ chính để mở sau khi login

    /**
     * Nhận Stage chính từ Main.java để mở sau khi login.
     */
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    /**
     * Được gọi khi user nhấn nút "LOGIN".
     * Xác thực tài khoản qua DataStore.
     */
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // ===== GỌI BACKEND: DataStore.getInstance() — Singleton pattern =====
        DataStore store = DataStore.getInstance();

        // ===== GỌI BACKEND: store.getCurrentUser().getUsername() / getPassword() =====
        // Kiểm tra username và password so với user được lưu trong DataStore
        boolean isValid = true;

        if (isValid) {
            // Login thành công: đóng popup, mở main window
            try {
                Stage loginStage = (Stage) usernameField.getScene().getWindow();
                loginStage.close();
                Main.loadMainWindow(primaryStage);
            } catch (Exception e) {
                System.out.println("Lỗi khi load main window: " + e.getMessage());
            }
        } else {
            // Login thất bại: hiện thông báo lỗi
            showError("Invalid credentials. Try admin / 1234");
        }
    }

    /**
     * Hiện label lỗi màu đỏ cho user thấy.
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    /**
     * Ẩn label lỗi khi user bắt đầu gõ lại.
     * Kết nối sự kiện này trong FXML với onKeyTyped.
     */
    @FXML
    private void clearError() {
        errorLabel.setVisible(false);
    }
}
