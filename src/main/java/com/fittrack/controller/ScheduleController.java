package com.fittrack.controller;

import com.fittrack.model.BodyPart;
import com.fittrack.model.Reminder;
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

public class ScheduleController {
    @FXML
    private TableView<Reminder> reminderTable;
    @FXML
    private TableColumn<Reminder, String> labelColumn;
    @FXML
    private TableColumn<Reminder, String> timeColumn;
    @FXML
    private TableColumn<Reminder, String> noteColumn;
    @FXML
    private ComboBox<String> bodyPartComboBox;
    @FXML
    private TextField thresholdDaysField;
    @FXML
    private TextField reminderNoteField;
    @FXML
    private Label nextReminderLabel;

    private final FitnessTrackerService service = FitnessTrackerService.getInstance();
    private final ObservableList<Reminder> reminderList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        labelColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getBodyPartName()));
        timeColumn.setCellValueFactory(cell -> new SimpleStringProperty(buildStatusText(cell.getValue())));
        noteColumn.setCellValueFactory(cell -> new SimpleStringProperty(buildNoteText(cell.getValue())));
        reminderTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        reminderTable.setItems(reminderList);
        reminderTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                populateForm(newValue);
            }
        });

        bodyPartComboBox.setOnAction(event -> loadSelectedReminder());

        loadBodyPartOptions();
        refreshReminderList();
    }

    @FXML
    private void saveReminder() {
        if (service.getCurrentUser() == null) {
            showAlert("Please log in before updating reminders.");
            return;
        }

        String bodyPartName = bodyPartComboBox.getValue();
        if (bodyPartName == null || bodyPartName.isBlank()) {
            showAlert("Select a body part first.");
            return;
        }

        Integer thresholdDays;
        try {
            thresholdDays = parseThresholdDays();
        } catch (NumberFormatException e) {
            showAlert("Threshold days must be a whole number greater than 0.");
            return;
        }

        try {
            service.createOrUpdateReminder(bodyPartName, thresholdDays, reminderNoteField.getText());
        } catch (IllegalArgumentException e) {
            showAlert(e.getMessage());
            return;
        }

        refreshReminderList();
        Reminder updatedReminder = service.getReminder(bodyPartName);
        if (updatedReminder != null && reminderList.contains(updatedReminder)) {
            reminderTable.getSelectionModel().select(updatedReminder);
            reminderTable.scrollTo(updatedReminder);
            populateForm(updatedReminder);
        }
    }

    private void refreshReminderList() {
        if (service.getCurrentUser() == null) {
            reminderList.clear();
            nextReminderLabel.setText("Log in to manage reminders");
            clearForm();
            return;
        }

        reminderList.clear();
        for (Reminder reminder : service.getReminders()) {
            if (service.hasReminderStarted(reminder)) {
                reminderList.add(reminder);
            }
        }
        updateNextReminderLabel();
        if (bodyPartComboBox.getValue() == null && !bodyPartComboBox.getItems().isEmpty()) {
            bodyPartComboBox.getSelectionModel().selectFirst();
            loadSelectedReminder();
            return;
        }
        loadSelectedReminder();
    }

    private void loadBodyPartOptions() {
        String selectedBodyPart = bodyPartComboBox.getValue();
        bodyPartComboBox.getItems().clear();
        for (BodyPart bodyPart : service.getBodyParts()) {
            bodyPartComboBox.getItems().add(bodyPart.getName());
        }

        if (selectedBodyPart != null && bodyPartComboBox.getItems().contains(selectedBodyPart)) {
            bodyPartComboBox.setValue(selectedBodyPart);
        } else if (!bodyPartComboBox.getItems().isEmpty()) {
            bodyPartComboBox.getSelectionModel().selectFirst();
        }

        loadSelectedReminder();
    }

    private void loadSelectedReminder() {
        if (service.getCurrentUser() == null) {
            clearForm();
            return;
        }

        String bodyPartName = bodyPartComboBox.getValue();
        if (bodyPartName == null || bodyPartName.isBlank()) {
            clearForm();
            return;
        }

        Reminder reminder = service.getReminder(bodyPartName);
        if (reminder == null) {
            thresholdDaysField.setText(String.valueOf(Reminder.DEFAULT_THRESHOLD_DAYS));
            reminderNoteField.clear();
            return;
        }

        populateForm(reminder);
    }

    private void populateForm(Reminder reminder) {
        bodyPartComboBox.setValue(reminder.getBodyPartName());
        thresholdDaysField.setText(String.valueOf(reminder.getThresholdDays()));
        reminderNoteField.setText(reminder.getNote() == null ? "" : reminder.getNote());
    }

    private void clearForm() {
        thresholdDaysField.clear();
        reminderNoteField.clear();
    }

    private Integer parseThresholdDays() {
        String thresholdText = thresholdDaysField.getText().trim();
        if (thresholdText.isEmpty()) {
            return null;
        }

        int thresholdDays = Integer.parseInt(thresholdText);
        if (thresholdDays <= 0) {
            throw new NumberFormatException("Threshold days must be positive.");
        }
        return thresholdDays;
    }

    private void updateNextReminderLabel() {
        Reminder nextReminder = service.getNextReminder();
        if (nextReminder == null) {
            nextReminderLabel.setText("No reminder started yet");
            return;
        }

        if (service.isReminderDue(nextReminder)) {
            nextReminderLabel.setText("Dashboard: " + nextReminder.getBodyPartName() + " is due now");
            return;
        }

        int daysRemaining = Math.max(service.getDaysRemaining(nextReminder), 0);
        nextReminderLabel.setText(
            "Dashboard: " + nextReminder.getBodyPartName() + " in "
                + daysRemaining + " day" + (daysRemaining == 1 ? "" : "s")
        );
    }

    private String buildStatusText(Reminder reminder) {
        if (!service.hasReminderStarted(reminder)) {
            return "Waiting for first logged workout";
        }

        int inactiveDays = service.getInactiveDays(reminder);
        if (service.isReminderDue(reminder)) {
            return inactiveDays + " inactive day" + (inactiveDays == 1 ? "" : "s") + " | due now";
        }

        int remainingDays = Math.max(service.getDaysRemaining(reminder), 0);
        return inactiveDays + " inactive day" + (inactiveDays == 1 ? "" : "s")
            + " | " + remainingDays + " day" + (remainingDays == 1 ? "" : "s") + " left";
    }

    private String buildNoteText(Reminder reminder) {
        return reminder.getNote() == null ? "-" : reminder.getNote();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
