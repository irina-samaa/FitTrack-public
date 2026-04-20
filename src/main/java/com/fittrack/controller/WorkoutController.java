package com.fittrack.controller;

import com.fittrack.model.BodyPart;
import com.fittrack.model.Exercise;
import com.fittrack.model.ExerciseSet;
import com.fittrack.model.WorkoutSession;
import com.fittrack.util.DataStore;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;

/**
 * WorkoutController.java — Màn hình quản lý bài tập.
 * Layout 3 cột: BodyParts → Exercises → Sets
 */
public class WorkoutController {

    // --- Cột trái: danh sách BodyPart ---
    @FXML private ListView<String> bodyPartListView;
    @FXML private TextField newBodyPartField;

    // --- Cột giữa: danh sách Exercise theo BodyPart đang chọn ---
    @FXML private ListView<String> exerciseListView;
    @FXML private TextField newExerciseField;
    @FXML private Label selectedBodyPartLabel; // Tiêu đề hiện tên body part đang chọn

    // --- Cột phải: bảng Set của Exercise đang chọn ---
    @FXML private TableView<ExerciseSet> setTableView;
    @FXML private TableColumn<ExerciseSet, Number> repsColumn;
    @FXML private TableColumn<ExerciseSet, Number> weightColumn;
    @FXML private TextField newRepsField;
    @FXML private TextField newWeightField;
    @FXML private Label selectedExerciseLabel;  // Tiêu đề hiện tên exercise đang chọn
    @FXML private Label totalVolumeLabel;        // Tổng volume (reps × weight)

    // Lưu đối tượng đang được chọn
    private BodyPart selectedBodyPart;
    private Exercise selectedExercise;

    // ObservableList để ListView/TableView tự cập nhật UI
    private ObservableList<String> bodyPartNames = FXCollections.observableArrayList();
    private ObservableList<String> exerciseNames = FXCollections.observableArrayList();
    private ObservableList<ExerciseSet> setItems = FXCollections.observableArrayList();

    /**
     * Khởi tạo: bind dữ liệu từ DataStore vào các list và table.
     */
    @FXML
    private void initialize() {
        // Thiết lập TableView columns
        // ===== GỌI BACKEND: ExerciseSet.getReps() / getWeightKg() =====
        repsColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getReps()));
        weightColumn.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getWeightKg()));

        // Bind ObservableList vào ListView/TableView
        bodyPartListView.setItems(bodyPartNames);
        exerciseListView.setItems(exerciseNames);
        setTableView.setItems(setItems);

        // Lắng nghe khi user chọn BodyPart
        bodyPartListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> onBodyPartSelected(newVal)
        );

        // Lắng nghe khi user chọn Exercise
        exerciseListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> onExerciseSelected(newVal)
        );

        // Load danh sách BodyPart ban đầu
        loadBodyParts();
    }

    /**
     * Load tất cả BodyPart từ DataStore vào cột trái.
     */
    private void loadBodyParts() {
        bodyPartNames.clear();
        // ===== GỌI BACKEND: DataStore.getBodyParts() =====
        for (BodyPart bp : DataStore.getInstance().getBodyParts()) {
            // ===== GỌI BACKEND: BodyPart.getName() =====
            bodyPartNames.add(bp.getName());
        }
    }

    /**
     * Khi user chọn một BodyPart, load danh sách Exercise của nó vào cột giữa.
     */
    private void onBodyPartSelected(String bodyPartName) {
        exerciseNames.clear();
        setItems.clear();
        selectedBodyPart = null;
        selectedExercise = null;

        if (bodyPartName == null) return;

        // Tìm BodyPart object tương ứng
        for (BodyPart bp : DataStore.getInstance().getBodyParts()) {
            if (bp.getName().equals(bodyPartName)) {
                selectedBodyPart = bp;
                break;
            }
        }

        if (selectedBodyPart == null) return;

        selectedBodyPartLabel.setText("Exercises — " + bodyPartName);

        // ===== GỌI BACKEND: BodyPart.getExercises() =====
        for (Exercise ex : selectedBodyPart.getExercises()) {
            // ===== GỌI BACKEND: Exercise.getName() =====
            exerciseNames.add(ex.getName());
        }
    }

    /**
     * Khi user chọn một Exercise, load danh sách Set vào cột phải.
     */
    private void onExerciseSelected(String exerciseName) {
        setItems.clear();
        selectedExercise = null;

        if (exerciseName == null || selectedBodyPart == null) return;

        // ===== GỌI BACKEND: BodyPart.findExercise(name) — Linear Search =====
        selectedExercise = selectedBodyPart.findExercise(exerciseName);

        if (selectedExercise == null) return;

        selectedExerciseLabel.setText("Sets — " + exerciseName);

        // ===== GỌI BACKEND: Exercise.getSets() =====
        setItems.addAll(selectedExercise.getSets());

        // ===== GỌI BACKEND: Exercise.getTotalVolume() =====
        totalVolumeLabel.setText("Total volume: " + selectedExercise.getTotalVolume() + " kg·reps");
    }

    /**
     * Nút "+ Add Body Part": thêm BodyPart mới vào DataStore.
     */
    @FXML
    private void addBodyPart() {
        String name = newBodyPartField.getText().trim();
        if (name.isEmpty()) {
            showAlert("Nhập tên body part!");
            return;
        }

        // ===== GỌI BACKEND: new BodyPart(name) — TODO: backend team tạo constructor =====
        BodyPart newBP = new BodyPart(name);

        // ===== GỌI BACKEND: DataStore.addBodyPart(bodyPart) =====
        DataStore.getInstance().addBodyPart(newBP);

        newBodyPartField.clear();
        loadBodyParts(); // Refresh list
        System.out.println("Đã thêm BodyPart: " + name);
    }

    /**
     * Nút "+ Add Exercise": thêm Exercise vào BodyPart đang chọn.
     */
    @FXML
    private void addExercise() {
        if (selectedBodyPart == null) {
            showAlert("Chọn một Body Part trước!");
            return;
        }

        String name = newExerciseField.getText().trim();
        if (name.isEmpty()) {
            showAlert("Nhập tên exercise!");
            return;
        }

        // ===== GỌI BACKEND: new Exercise(name) — TODO: backend team tạo constructor =====
        Exercise newEx = new Exercise(name);

        // ===== GỌI BACKEND: BodyPart.addExercise(exercise) =====
        selectedBodyPart.addExercise(newEx);

        // ===== GỌI BACKEND: BodyPart.sortExercises() — Insertion Sort by name =====
        selectedBodyPart.sortExercises();

        newExerciseField.clear();
        onBodyPartSelected(selectedBodyPart.getName()); // Refresh exercise list
        System.out.println("Đã thêm Exercise: " + name);
    }

    /**
     * Nút "+ Add Set": thêm Set vào Exercise đang chọn.
     */
    @FXML
    private void addSet() {
        if (selectedExercise == null) {
            showAlert("Chọn một Exercise trước!");
            return;
        }

        try {
            int reps = Integer.parseInt(newRepsField.getText().trim());
            double weight = Double.parseDouble(newWeightField.getText().trim());

            // ===== GỌI BACKEND: new ExerciseSet(reps, weight) — TODO: backend team =====
            ExerciseSet newSet = new ExerciseSet(reps, weight);

            // ===== GỌI BACKEND: Exercise.addSet(exerciseSet) =====
            selectedExercise.addSet(newSet);

            // Refresh TableView
            setItems.add(newSet);

            // ===== GỌI BACKEND: Exercise.getTotalVolume() =====
            totalVolumeLabel.setText("Total volume: " + selectedExercise.getTotalVolume() + " kg·reps");

            newRepsField.clear();
            newWeightField.clear();
            System.out.println("Đã thêm Set: " + reps + " reps x " + weight + "kg");

        } catch (NumberFormatException e) {
            showAlert("Reps phải là số nguyên, Weight phải là số thực!");
        }
    }

    /**
     * Nút "Start Workout Session": tạo session mới từ các BodyPart đã log.
     */
    @FXML
    private void startWorkoutSession() {
        String date = LocalDate.now().toString();
        String sessionName = "Workout — " + date;

        // ===== GỌI BACKEND: new WorkoutSession(date, sessionName) — TODO: backend team =====
        WorkoutSession session = new WorkoutSession(date, sessionName);

        // Thêm tất cả exercise của tất cả BodyPart vào session
        for (BodyPart bp : DataStore.getInstance().getBodyParts()) {
            // ===== GỌI BACKEND: BodyPart.getExercises() =====
            for (Exercise ex : bp.getExercises()) {
                // ===== GỌI BACKEND: WorkoutSession.addExercise(exercise) =====
                session.addExercise(ex);
            }
        }

        // ===== GỌI BACKEND: DataStore.addSession(session) =====
        DataStore.getInstance().addSession(session);

        // ===== GỌI BACKEND: WorkoutSession.getSummary() =====
        showAlert("Session logged!\n" + session.getSummary());
        System.out.println("Session logged: " + sessionName);
    }

    /**
     * Tiện ích hiện hộp thông báo.
     */
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
