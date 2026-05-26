package com.fittrack.controller;

import com.fittrack.model.BodyPart;
import com.fittrack.service.FitnessTrackerService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.time.format.DateTimeFormatter;

public class ScheduleController {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private TableView<BodyPart> reminderTable;
    @FXML
    private TableColumn<BodyPart, String> bodyPartColumn;
    @FXML
    private TableColumn<BodyPart, String> intervalColumn;
    @FXML
    private TableColumn<BodyPart, String> timeColumn;
    @FXML
    private ComboBox<String> bodyPartComboBox;
    @FXML
    private TextField reminderDaysField;
    @FXML
    private Label nextReminderLabel;

    private final FitnessTrackerService service = FitnessTrackerService.getInstance();
    private final ObservableList<BodyPart> reminderList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        bodyPartColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
        intervalColumn.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getReminder().getIntervalDays() + " days"));
        timeColumn.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getReminder().getScheduledTime().format(FORMATTER)));
        reminderTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        reminderTable.setItems(reminderList);

        refreshBodyPartChoices();
        refreshReminderList();
    }

    @FXML
    private void updateReminderDays() {
        if (service.getCurrentUser() == null) {
            showAlert("Please log in before changing reminders.");
            return;
        }

        String bodyPartName = getSelectedBodyPartName();
        if (bodyPartName.isEmpty()) {
            showAlert("Please choose a body part.");
            return;
        }
        int days;
        String daysText = reminderDaysField.getText().trim();
        if (daysText.isEmpty()) {
            days = service.getDefaultReminderDays();
        } else {
            try {
                days = Integer.parseInt(daysText);
            } catch (NumberFormatException e) {
                showAlert("The number of days must be a whole number.");
                return;
            }
        }
        if (days <= 0) {
            showAlert("The number of days must be greater than 0.");
            return;
        }

        try {
            service.updateBodyPartReminderDays(bodyPartName, days);
            reminderDaysField.clear();
            refreshReminderList();
        } catch (IllegalStateException | IllegalArgumentException e) {
            showAlert(e.getMessage());
        }
    }

    private void refreshBodyPartChoices() {
        bodyPartComboBox.getItems().clear();
        for (BodyPart bodyPart : service.getBodyParts()) {
            bodyPartComboBox.getItems().add(bodyPart.getName());
        }
    }

    private void refreshReminderList() {
        if (service.getCurrentUser() == null) {
            reminderList.clear();
            nextReminderLabel.setText("Log in to manage reminders");
            return;
        }

        reminderList.setAll(service.getBodyPartsWithReminders());
        BodyPart next = service.getNextReminderBodyPart();
        if (next == null) {
            nextReminderLabel.setText("No body part reminders yet");
            return;
        }
        nextReminderLabel.setText(
            "Next: " + next.getName() + " - " + next.getReminder().getScheduledTime().format(FORMATTER));
    }

    private String getSelectedBodyPartName() {
        if (bodyPartComboBox.isEditable()) {
            return bodyPartComboBox.getEditor().getText().trim();
        }
        String value = bodyPartComboBox.getValue();
        return value == null ? "" : value.trim();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
