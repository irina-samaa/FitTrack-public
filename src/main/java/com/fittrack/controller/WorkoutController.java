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

public class WorkoutController {
    @FXML private ListView<String> bodyPartListView;
    @FXML private TextField newBodyPartField;
    @FXML private ListView<String> exerciseListView;
    @FXML private TextField newExerciseField;
    @FXML private ComboBox<String> exerciseTypeComboBox;
    @FXML private Label selectedBodyPartLabel;
    @FXML private TableView<SetRecord> setTableView;
    @FXML private TableColumn<SetRecord, String> setTypeColumn;
    @FXML private TableColumn<SetRecord, Number> primaryMetricColumn;
    @FXML private TableColumn<SetRecord, Number> secondaryMetricColumn;
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

        bodyPartListView.setItems(bodyPartNames);
        exerciseListView.setItems(exerciseNames);
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
        totalVolumeLabel.setText(String.format("Total workload: %.2f", selectedExercise.getTotalVolume()));
    }

    @FXML
    private void addBodyPart() {
        String name = newBodyPartField.getText().trim();
        if (name.isEmpty()) {
            showAlert("Nhap ten body part!");
            return;
        }

        service.createBodyPart(name);
        newBodyPartField.clear();
        loadBodyParts();
    }

    @FXML
    private void addExercise() {
        if (selectedBodyPart == null) {
            showAlert("Chon mot Body Part truoc!");
            return;
        }

        String name = newExerciseField.getText().trim();
        if (name.isEmpty()) {
            showAlert("Nhap ten exercise!");
            return;
        }

        ExerciseType type = ExerciseType.fromDisplayName(exerciseTypeComboBox.getValue());
        service.createExercise(selectedBodyPart.getName(), name, type);
        newExerciseField.clear();
        onBodyPartSelected(selectedBodyPart.getName());
    }

    @FXML
    private void addSet() {
        if (selectedExercise == null || selectedBodyPart == null) {
            showAlert("Chon mot Exercise truoc!");
            return;
        }

        try {
            int firstMetric = Integer.parseInt(firstMetricField.getText().trim());
            double secondMetric = Double.parseDouble(secondMetricField.getText().trim());

            service.addSet(selectedBodyPart.getName(), selectedExercise.getName(), firstMetric, secondMetric);
            setItems.setAll(selectedExercise.getSets());
            totalVolumeLabel.setText(String.format("Total workload: %.2f", selectedExercise.getTotalVolume()));
            firstMetricField.clear();
            secondMetricField.clear();
        } catch (NumberFormatException e) {
            showAlert("Metric 1 phai la so nguyen, Metric 2 phai la so!");
        }
    }

    @FXML
    private void startWorkoutSession() {
        WorkoutSession session = service.startWorkoutSession();
        showAlert("Session logged!\n" + session.getSummary());
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
        if (selectedExercise == null) {
            totalVolumeLabel.setText("Total workload: -");
        }
    }

    private String buildExerciseDisplayName(Exercise exercise) {
        return exercise.getName() + " (" + exercise.getExerciseType().getDisplayName() + ")";
    }

    private String extractExerciseName(String exerciseDisplayName) {
        return exerciseDisplayName.replaceAll("\\s+\\(.+\\)$", "");
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
