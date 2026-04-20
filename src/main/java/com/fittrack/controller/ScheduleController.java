package com.fittrack.controller;

import com.fittrack.model.Reminder;
import com.fittrack.util.DataStore;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * ScheduleController.java — Màn hình quản lý nhắc nhở (Reminder).
 * Hiện danh sách reminder, thêm/xóa, và chỉnh recovery days.
 */
public class ScheduleController {

    // --- TableView hiện danh sách reminder ---
    @FXML private TableView<Reminder> reminderTable;
    @FXML private TableColumn<Reminder, String> labelColumn;
    @FXML private TableColumn<Reminder, String> timeColumn;

    // --- Form thêm reminder mới ---
    @FXML private TextField reminderLabelField;   // Tên nhắc nhở
    @FXML private TextField reminderTimeField;    // Thời gian (định dạng: yyyy-MM-ddTHH:mm)

    // --- Recovery days setting ---
    @FXML private TextField recoveryDaysField;
    @FXML private Label recoveryDaysInfo;

    // --- Label hiện reminder tiếp theo ---
    @FXML private Label nextReminderLabel;

    private ObservableList<Reminder> reminderList = FXCollections.observableArrayList();

    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Khởi tạo: setup TableView và load dữ liệu từ DataStore.
     */
    @FXML
    private void initialize() {
        // Setup cột "Label"
        // ===== GỌI BACKEND: Reminder.getLabel() =====
        labelColumn.setCellValueFactory(cell ->
            new javafx.beans.property.SimpleStringProperty(cell.getValue().getLabel())
        );

        // Setup cột "Scheduled Time"
        // ===== GỌI BACKEND: Reminder.getScheduledTime() =====
        timeColumn.setCellValueFactory(cell ->
            new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getScheduledTime().format(FORMATTER)
            )
        );

        reminderTable.setItems(reminderList);

        // Load recovery days hiện tại
        // ===== GỌI BACKEND: DataStore.getReminderService().getRecoveryDays() =====
        int days = DataStore.getInstance().getReminderService().getRecoveryDays();
        recoveryDaysField.setText(String.valueOf(days));
        recoveryDaysInfo.setText("Rest " + days + " days between sessions");

        // Load danh sách reminders
        refreshReminderList();
    }

    /**
     * Nút "Add Reminder": thêm reminder mới vào hàng đợi PriorityQueue.
     * Format thời gian: yyyy-MM-dd HH:mm (ví dụ: 2025-06-15 09:00)
     */
    @FXML
    private void addReminder() {
        String label = reminderLabelField.getText().trim();
        String timeText = reminderTimeField.getText().trim();

        if (label.isEmpty() || timeText.isEmpty()) {
            showAlert("Vui lòng nhập đầy đủ label và thời gian!");
            return;
        }

        try {
            LocalDateTime time = LocalDateTime.parse(timeText, FORMATTER);

            // ===== GỌI BACKEND: ReminderService.scheduleReminder(label, time) =====
            // Method này tạo Reminder mới và thêm vào PriorityQueue (min-heap)
            DataStore.getInstance().getReminderService().scheduleReminder(label, time);

            reminderLabelField.clear();
            reminderTimeField.clear();
            refreshReminderList();
            System.out.println("Đã thêm reminder: " + label + " lúc " + timeText);

        } catch (DateTimeParseException e) {
            showAlert("Sai định dạng thời gian!\nDùng: yyyy-MM-dd HH:mm\nVí dụ: 2025-06-15 09:00");
        }
    }

    /**
     * Nút "Remove Next": xóa reminder sớm nhất (đầu min-heap).
     */
    @FXML
    private void removeNextReminder() {
        // ===== GỌI BACKEND: ReminderService.getNextReminder() — peek tại min-heap =====
        Reminder next = DataStore.getInstance().getReminderService().getNextReminder();

        if (next == null) {
            showAlert("Không có reminder nào!");
            return;
        }

        // Confirm trước khi xóa
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("Xóa reminder tiếp theo?");
        // ===== GỌI BACKEND: Reminder.toString() =====
        confirm.setContentText(next.toString());
        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                // ===== GỌI BACKEND: ReminderService.removeNextReminder() — poll từ min-heap =====
                DataStore.getInstance().getReminderService().removeNextReminder();
                refreshReminderList();
                System.out.println("Đã xóa reminder: " + next.getLabel());
            }
        });
    }

    /**
     * Nút "Save" recovery days: cập nhật số ngày nghỉ.
     */
    @FXML
    private void saveRecoveryDays() {
        try {
            int days = Integer.parseInt(recoveryDaysField.getText().trim());
            if (days < 0) throw new NumberFormatException();

            // ===== GỌI BACKEND: ReminderService.setRecoveryDays(days) =====
            DataStore.getInstance().getReminderService().setRecoveryDays(days);

            recoveryDaysInfo.setText("Rest " + days + " days between sessions");
            System.out.println("Recovery days set: " + days);

        } catch (NumberFormatException e) {
            showAlert("Recovery days phải là số nguyên không âm!");
        }
    }

    /**
     * Refresh bảng reminder từ PriorityQueue.
     * getAllReminders() trả về sorted list copy của PriorityQueue.
     */
    private void refreshReminderList() {
        reminderList.clear();

        // ===== GỌI BACKEND: ReminderService.getAllReminders() — sorted list từ min-heap =====
        reminderList.addAll(DataStore.getInstance().getReminderService().getAllReminders());

        // Cập nhật label "Next Reminder"
        // ===== GỌI BACKEND: ReminderService.getNextReminder() =====
        Reminder next = DataStore.getInstance().getReminderService().getNextReminder();
        if (next != null) {
            nextReminderLabel.setText("Next: " + next.getLabel()
                + " — " + next.getScheduledTime().format(FORMATTER));
        } else {
            nextReminderLabel.setText("No upcoming reminders");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
