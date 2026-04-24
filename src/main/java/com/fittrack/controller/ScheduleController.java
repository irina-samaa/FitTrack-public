package com.fittrack.controller;

import com.fittrack.model.BodyPartInactivityAlert;
import com.fittrack.model.ReminderDisplayItem;
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

    @FXML
    private TableView<ReminderDisplayItem> reminderTable;
    @FXML
    private TableColumn<ReminderDisplayItem, String> labelColumn;
    @FXML
    private TableColumn<ReminderDisplayItem, String> timeColumn;
    @FXML
    private TableColumn<ReminderDisplayItem, String> noteColumn;
    @FXML
    private TextField reminderLabelField;
    @FXML
    private TextField reminderTimeField;
    @FXML
    private TextField reminderNoteField;
    @FXML
    private Label nextReminderLabel;

    private final FitnessTrackerService service = FitnessTrackerService.getInstance();
    private final ObservableList<ReminderDisplayItem> reminderList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        labelColumn.setCellValueFactory(
                cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getTitle()));
        timeColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getMessage()));
        noteColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                formatType(cell.getValue())));
        reminderTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        reminderTable.setItems(reminderList);
        refreshReminderList();
    }

    @FXML
    private void addReminder() {
        if (service.getCurrentUser() == null) {
            showAlert("Please log in before adding reminders.");
            return;
        }

        String label = reminderLabelField.getText().trim();
        String inputText = reminderTimeField.getText().trim();
        String note = reminderNoteField.getText().trim();
        if (label.isEmpty() || inputText.isEmpty()) {
            showAlert("Please enter both the label and the date/time.");
            return;
        }
        try {
            LocalDateTime time = LocalDateTime.parse(inputText, FORMATTER);
            service.scheduleReminder(label, time, note);
        } catch (DateTimeParseException e) {
            showAlert("Invalid time format. Use yyyy-MM-dd HH:mm.");
            return;
        }

        reminderLabelField.clear();
        reminderTimeField.clear();
        reminderNoteField.clear();
        refreshReminderList();
    }

    @FXML
    private void removeSelectedReminder() {
        if (service.getCurrentUser() == null) {
            showAlert("Please log in before removing reminder items.");
            return;
        }

        ReminderDisplayItem selectedItem = reminderTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert("Select a reminder item first.");
            return;
        }
        if (selectedItem instanceof BodyPartInactivityAlert) {
            showAlert("Body part inactivity reminders are automatic. Log that body part in a workout to clear it.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("Remove the selected reminder item?");
        confirm.setContentText(selectedItem.getTitle() + "\n" + selectedItem.getMessage());
        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                service.removeReminderDisplayItem(selectedItem);
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

        reminderList.setAll(service.getReminderDisplayItems());
        ReminderDisplayItem next = service.getNextReminderDisplayItem();
        if (next == null) {
            nextReminderLabel.setText("No upcoming reminders");
            return;
        }
        nextReminderLabel.setText("Up next: " + next.getTitle() + " - " + next.getMessage());
    }

    private String formatType(ReminderDisplayItem item) {
        return item instanceof BodyPartInactivityAlert ? "Body Part Inactivity" : "User Defined";
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
