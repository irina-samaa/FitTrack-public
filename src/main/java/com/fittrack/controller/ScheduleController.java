package com.fittrack.controller;

import com.fittrack.model.Reminder;
import com.fittrack.service.FitnessTrackerService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ScheduleController {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML private TableView<Reminder> reminderTable;
    @FXML private TableColumn<Reminder, String> labelColumn;
    @FXML private TableColumn<Reminder, String> timeColumn;
    @FXML private TextField reminderLabelField;
    @FXML private TextField reminderTimeField;
    @FXML private TextField recoveryDaysField;
    @FXML private Label recoveryDaysInfo;
    @FXML private Label nextReminderLabel;

    private final FitnessTrackerService service = FitnessTrackerService.getInstance();
    private final ObservableList<Reminder> reminderList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        labelColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getLabel()));
        timeColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getScheduledTime().format(FORMATTER)));
        reminderTable.setItems(reminderList);

        int days = service.getRecoveryDays();
        recoveryDaysField.setText(String.valueOf(days));
        recoveryDaysInfo.setText("Rest " + days + " days between sessions");
        refreshReminderList();
    }

    @FXML
    private void addReminder() {
        String label = reminderLabelField.getText().trim();
        String timeText = reminderTimeField.getText().trim();
        if (label.isEmpty() || timeText.isEmpty()) {
            showAlert("Vui long nhap day du label va thoi gian!");
            return;
        }

        try {
            LocalDateTime time = LocalDateTime.parse(timeText, FORMATTER);
            service.scheduleReminder(label, time);
            reminderLabelField.clear();
            reminderTimeField.clear();
            refreshReminderList();
        } catch (DateTimeParseException e) {
            showAlert("Sai dinh dang thoi gian. Dung yyyy-MM-dd HH:mm");
        }
    }

    @FXML
    private void removeNextReminder() {
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

    @FXML
    private void saveRecoveryDays() {
        try {
            int days = Integer.parseInt(recoveryDaysField.getText().trim());
            service.setRecoveryDays(days);
            recoveryDaysInfo.setText("Rest " + days + " days between sessions");
        } catch (IllegalArgumentException | NumberFormatException e) {
            showAlert("Recovery days phai la so nguyen khong am!");
        }
    }

    private void refreshReminderList() {
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
