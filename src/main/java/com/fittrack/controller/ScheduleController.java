package com.fittrack.controller;

import com.fittrack.model.Reminder;
import com.fittrack.service.FitnessTrackerService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ScheduleController {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private TableView<Reminder> reminderTable;
    @FXML
    private TableColumn<Reminder, String> labelColumn;
    @FXML
    private TableColumn<Reminder, String> timeColumn;
    @FXML
    private TextField reminderLabelField;
    @FXML
    private TextField reminderTimeField;
    @FXML
    private ComboBox<String> reminderTypeComboBox;
    @FXML
    private Label reminderInputLabel;
    @FXML
    private Label nextReminderLabel;

    private final FitnessTrackerService service = FitnessTrackerService.getInstance();
    private final ObservableList<Reminder> reminderList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        labelColumn.setCellValueFactory(
                cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getLabel()));
        timeColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getScheduledTime().format(FORMATTER)));
        reminderTable.setItems(reminderList);

        reminderTypeComboBox.getItems().addAll("Specific Date & Time", "Repeat (Inactivity)");
        reminderTypeComboBox.setValue("Specific Date & Time");
        reminderTypeComboBox.setOnAction(e -> updateReminderInputMode());

        refreshReminderList();
    }

    private void updateReminderInputMode() {
        if ("Repeat (Inactivity)".equals(reminderTypeComboBox.getValue())) {
            reminderInputLabel.setText("REPEAT EVERY (DAYS)");
            reminderTimeField.setPromptText("e.g. 3");
            reminderTimeField.clear();
        } else {
            reminderInputLabel.setText("DATE & TIME");
            reminderTimeField.setPromptText("yyyy-MM-dd HH:mm");
            reminderTimeField.clear();
        }
    }

    @FXML
    private void addReminder() {
        if (service.getCurrentUser() == null) {
            showAlert("Please log in before adding reminders.");
            return;
        }

        String label = reminderLabelField.getText().trim();
        String inputText = reminderTimeField.getText().trim();
        if (label.isEmpty() || inputText.isEmpty()) {
            showAlert("Vui long nhap day du label va thoi gian/so ngay!");
            return;
        }

        if ("Repeat (Inactivity)".equals(reminderTypeComboBox.getValue())) {
            try {
                int days = Integer.parseInt(inputText);
                if (days <= 0) {
                    showAlert("So ngay phai lon hon 0!");
                    return;
                }
                service.scheduleReminder(label, LocalDateTime.now().plusDays(days), days);
            } catch (NumberFormatException e) {
                showAlert("So ngay phai la so nguyen!");
                return;
            }
        } else {
            try {
                LocalDateTime time = LocalDateTime.parse(inputText, FORMATTER);
                service.scheduleReminder(label, time, null);
            } catch (DateTimeParseException e) {
                showAlert("Sai dinh dang thoi gian. Dung yyyy-MM-dd HH:mm");
                return;
            }
        }

        reminderLabelField.clear();
        reminderTimeField.clear();
        refreshReminderList();
    }

    @FXML
    private void removeNextReminder() {
        if (service.getCurrentUser() == null) {
            showAlert("Please log in before removing reminders.");
            return;
        }

        Reminder next = service.getNextReminder();
        if (next == null) {
            showAlert("Khong co reminder nao!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("Xoa reminder tiep theo?");
        confirm.setContentText(next.toString());
        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                service.removeNextReminder();
                refreshReminderList();
            }
        });
    }

    private void refreshReminderList() {
        if (service.getCurrentUser() == null) {
            reminderList.clear();
            nextReminderLabel.setText("Log in to manage reminders");
            return;
        }

        reminderList.setAll(service.getAllReminders());
        Reminder next = service.getNextReminder();
        if (next == null) {
            nextReminderLabel.setText("No upcoming reminders");
            return;
        }
        nextReminderLabel.setText("Next: " + next.getLabel() + " - " + next.getScheduledTime().format(FORMATTER));
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
