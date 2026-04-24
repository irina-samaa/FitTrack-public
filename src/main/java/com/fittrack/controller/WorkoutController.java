package com.fittrack.controller;

import com.fittrack.model.BodyPart;
import com.fittrack.model.Exercise;
import com.fittrack.model.ExerciseType;
import com.fittrack.model.SetRecord;
import com.fittrack.model.WorkoutSession;
import com.fittrack.service.FitnessTrackerService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.time.LocalDate;

public class WorkoutController {
    @FXML private ListView<String> bodyPartListView;
    @FXML private TextField newBodyPartField;
    @FXML private ListView<String> exerciseListView;
    @FXML private TextField newExerciseField;
    @FXML private ComboBox<String> exerciseTypeComboBox;
    @FXML private TextField sessionNameField;
    @FXML private Label selectedBodyPartLabel;
    @FXML private TableView<SetRecord> setTableView;
    @FXML private TableColumn<SetRecord, String> setTypeColumn;
    @FXML private TableColumn<SetRecord, Number> primaryMetricColumn;
    @FXML private TableColumn<SetRecord, Number> secondaryMetricColumn;
    @FXML private TableColumn<SetRecord, String> workloadColumn;
    @FXML private TextField firstMetricField;
    @FXML private TextField secondMetricField;
    @FXML private Label firstMetricLabel;
    @FXML private Label secondMetricLabel;
    @FXML private Label selectedExerciseLabel;
    @FXML private Label totalVolumeLabel;

    private final FitnessTrackerService service = FitnessTrackerService.getInstance();
    private final ObservableList<String> bodyPartNames = FXCollections.observableArrayList();
    private final ObservableList<String> exerciseNames = FXCollections.observableArrayList();
    private final ObservableList<SetRecord> setItems = FXCollections.observableArrayList();

    private BodyPart selectedBodyPart;
    private Exercise selectedExercise;

    @FXML
    private void initialize() {
        setTypeColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getType()));
        primaryMetricColumn.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getPrimaryMetricValue().doubleValue()));
        secondaryMetricColumn.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getSecondaryMetricValue().doubleValue()));
        workloadColumn.setCellValueFactory(cell -> new SimpleStringProperty(formatSelectedExerciseWorkload()));

        bodyPartListView.setItems(bodyPartNames);
        exerciseListView.setItems(exerciseNames);
        setTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setTableView.setItems(setItems);

        exerciseTypeComboBox.getItems().addAll(
            ExerciseType.STRENGTH.getDisplayName(),
            ExerciseType.CARDIO.getDisplayName(),
            ExerciseType.ENDURANCE.getDisplayName()
        );
        exerciseTypeComboBox.setValue(ExerciseType.STRENGTH.getDisplayName());
        exerciseTypeComboBox.setOnAction(event -> updateMetricInputs(ExerciseType.fromDisplayName(exerciseTypeComboBox.getValue())));

        bodyPartListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> onBodyPartSelected(newValue));
        exerciseListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> onExerciseSelected(newValue));

        sessionNameField.setText(buildDefaultSessionName());
        updateMetricInputs(ExerciseType.STRENGTH);
        loadBodyParts();
    }

    private void loadBodyParts() {
        bodyPartNames.clear();
        for (BodyPart bodyPart : service.getBodyParts()) {
            bodyPartNames.add(bodyPart.getName());
        }
    }

    private void onBodyPartSelected(String bodyPartName) {
        exerciseNames.clear();
        setItems.clear();
        selectedExercise = null;
        selectedBodyPart = null;
        selectedExerciseLabel.setText("SETS");
        refreshSessionWorkloadLabel();

        if (bodyPartName == null) {
            return;
        }

        selectedBodyPart = service.findBodyPart(bodyPartName);
        if (selectedBodyPart == null) {
            return;
        }

        selectedBodyPartLabel.setText("Exercises - " + bodyPartName);
        for (Exercise exercise : selectedBodyPart.getExercises()) {
            exerciseNames.add(buildExerciseDisplayName(exercise));
        }
    }

    private void onExerciseSelected(String exerciseDisplayName) {
        setItems.clear();
        selectedExercise = null;
        refreshSessionWorkloadLabel();

        if (exerciseDisplayName == null || selectedBodyPart == null) {
            return;
        }

        String exerciseName = extractExerciseName(exerciseDisplayName);
        selectedExercise = service.findExercise(selectedBodyPart.getName(), exerciseName);
        if (selectedExercise == null) {
            return;
        }

        selectedExerciseLabel.setText("Sets - " + exerciseName + " [" + selectedExercise.getExerciseType().getDisplayName() + "]");
        setItems.setAll(selectedExercise.getSets());
        updateMetricInputs(selectedExercise.getExerciseType());
        refreshSessionWorkloadLabel();
        setTableView.refresh();
    }

    @FXML
    private void addBodyPart() {
        String name = newBodyPartField.getText().trim();
        if (name.isEmpty()) {
            showAlert("Enter a body part name.");
            return;
        }

        service.createBodyPart(name);
        newBodyPartField.clear();
        loadBodyParts();
    }

    @FXML
    private void addExercise() {
        if (selectedBodyPart == null) {
            showAlert("Select a body part first.");
            return;
        }

        String name = newExerciseField.getText().trim();
        if (name.isEmpty()) {
            showAlert("Enter an exercise name.");
            return;
        }

        ExerciseType type = ExerciseType.fromDisplayName(exerciseTypeComboBox.getValue());
        service.createExercise(selectedBodyPart.getName(), name, type);
        newExerciseField.clear();
        onBodyPartSelected(selectedBodyPart.getName());
        exerciseListView.getSelectionModel().select(buildExerciseDisplayName(service.findExercise(selectedBodyPart.getName(), name)));
    }

    @FXML
    private void addSet() {
        if (selectedExercise == null || selectedBodyPart == null) {
            showAlert("Select an exercise first.");
            return;
        }

        try {
            int firstMetric = Integer.parseInt(firstMetricField.getText().trim());
            double secondMetric = Double.parseDouble(secondMetricField.getText().trim());

            service.addSet(selectedBodyPart.getName(), selectedExercise.getName(), firstMetric, secondMetric);
            setItems.setAll(selectedExercise.getSets());
            refreshSessionWorkloadLabel();
            setTableView.refresh();
            firstMetricField.clear();
            secondMetricField.clear();
        } catch (NumberFormatException e) {
            showAlert("Metric 1 must be a whole number and Metric 2 must be numeric.");
        } catch (IllegalArgumentException e) {
            showAlert(e.getMessage());
        }
    }

    @FXML
    private void startWorkoutSession() {
        String selectedBodyPartName = selectedBodyPart == null ? null : selectedBodyPart.getName();
        String selectedExerciseName = selectedExercise == null ? null : selectedExercise.getName();

        try {
            WorkoutSession session = service.startWorkoutSession(sessionNameField.getText());
            loadBodyParts();
            restoreSelection(selectedBodyPartName, selectedExerciseName);
            sessionNameField.setText(buildDefaultSessionName());
            showAlert("Session logged!\n" + session.getSummary());
        } catch (IllegalStateException e) {
            showAlert(e.getMessage());
        }
    }

    @FXML
    private void deleteSet() {
        if (selectedExercise == null || selectedBodyPart == null) {
            showAlert("Select an exercise first.");
            return;
        }

        SetRecord selectedSet = setTableView.getSelectionModel().getSelectedItem();
        if (selectedSet == null) {
            showAlert("Select a set to delete.");
            return;
        }

        service.deleteSet(selectedBodyPart.getName(), selectedExercise.getName(), selectedSet);
        setItems.setAll(selectedExercise.getSets());
        refreshSessionWorkloadLabel();
        setTableView.refresh();
        setTableView.getSelectionModel().clearSelection();
        if (selectedExercise.getSets().isEmpty()) {
            refreshSessionWorkloadLabel();
        }
    }

    private void updateMetricInputs(ExerciseType type) {
        switch (type) {
            case STRENGTH -> {
                firstMetricLabel.setText("Reps");
                secondMetricLabel.setText("Weight (kg)");
                firstMetricField.setPromptText("e.g. 8");
                secondMetricField.setPromptText("e.g. 60");
            }
            case CARDIO -> {
                firstMetricLabel.setText("Duration (min)");
                secondMetricLabel.setText("Distance (km)");
                firstMetricField.setPromptText("e.g. 20");
                secondMetricField.setPromptText("e.g. 3.5");
            }
            case ENDURANCE -> {
                firstMetricLabel.setText("Duration (min)");
                secondMetricLabel.setText("Heart Rate");
                firstMetricField.setPromptText("e.g. 18");
                secondMetricField.setPromptText("e.g. 145");
            }
        }
        primaryMetricColumn.setText(firstMetricLabel.getText());
        secondaryMetricColumn.setText(secondMetricLabel.getText());
        refreshSessionWorkloadLabel();
    }

    private void refreshSessionWorkloadLabel() {
        double sessionWorkload = calculateCurrentSessionWorkload();
        if (sessionWorkload <= 0) {
            totalVolumeLabel.setText("Session workload: -");
            return;
        }
        totalVolumeLabel.setText(String.format("Session workload: %.2f", sessionWorkload));
    }

    private double calculateCurrentSessionWorkload() {
        double total = 0;
        for (BodyPart bodyPart : service.getBodyParts()) {
            for (Exercise exercise : bodyPart.getExercises()) {
                total += exercise.getTotalVolume();
            }
        }
        return total;
    }

    private String formatSelectedExerciseWorkload() {
        if (selectedExercise == null) {
            return "-";
        }
        return String.format("%.2f", selectedExercise.getTotalVolume());
    }

    private String buildExerciseDisplayName(Exercise exercise) {
        return exercise.getName() + " (" + exercise.getExerciseType().getDisplayName() + ")";
    }

    private String extractExerciseName(String exerciseDisplayName) {
        return exerciseDisplayName.replaceAll("\\s+\\(.+\\)$", "");
    }

    private String buildDefaultSessionName() {
        return "Workout - " + LocalDate.now();
    }

    private void restoreSelection(String bodyPartName, String exerciseName) {
        if (bodyPartName == null) {
            onBodyPartSelected(null);
            return;
        }

        bodyPartListView.getSelectionModel().select(bodyPartName);
        onBodyPartSelected(bodyPartName);

        if (exerciseName != null) {
            Exercise exercise = service.findExercise(bodyPartName, exerciseName);
            if (exercise != null) {
                String exerciseDisplayName = buildExerciseDisplayName(exercise);
                exerciseListView.getSelectionModel().select(exerciseDisplayName);
                onExerciseSelected(exerciseDisplayName);
            }
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
