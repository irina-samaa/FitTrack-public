package com.fittrack.controller;

import com.fittrack.model.Reminder;
import com.fittrack.model.WorkoutSession;
import com.fittrack.model.Exercise;
import com.fittrack.model.SetRecord;
import com.fittrack.service.FitnessTrackerService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardController {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter SHORT_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d");

    @FXML private Label sessionCountLabel;
    @FXML private Label lastSessionLabel;
    @FXML private Label bmiValueLabel;
    @FXML private Label bmiCategoryLabel;
    @FXML private Label reminderTitleLabel;
    @FXML private Label reminderTimeLabel;
    @FXML private Label todayDateLabel;
    @FXML private Label currentTimeLabel;
    @FXML private Label upcomingCountLabel;
    @FXML private ListView<String> exerciseWorkloadsListView;
    @FXML private ListView<String> sessionWorkloadsListView;

    private final FitnessTrackerService service = FitnessTrackerService.getInstance();
    private Timeline clockTimeline;

    @FXML
    private void initialize() {
        configureWorkloadLists();
        loadDateTimeHeader();
        loadWorkoutCard();
        loadBmiCard();
        loadReminderCard();
        loadPastExercises();
    }

    private void loadDateTimeHeader() {
        LocalDateTime now = LocalDateTime.now();
        todayDateLabel.setText(DATE_FORMATTER.format(now));
        updateCurrentTime();

        clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> updateCurrentTime()));
        clockTimeline.setCycleCount(Timeline.INDEFINITE);
        clockTimeline.play();
    }

    private void updateCurrentTime() {
        currentTimeLabel.setText("Current time: " + TIME_FORMATTER.format(LocalDateTime.now()));
    }

    private void loadWorkoutCard() {
        if (service.getCurrentUser() == null) {
            sessionCountLabel.setText("0 sessions logged");
            lastSessionLabel.setText("Latest: log in to view workout history.");
            return;
        }

        LocalDate today = LocalDate.now();
        ArrayList<WorkoutSession> todaySessions = new ArrayList<>();
        for (WorkoutSession session : service.getSessions()) {
            if (LocalDate.parse(session.getDate()).equals(today)) {
                todaySessions.add(session);
            }
        }

        sessionCountLabel.setText(todaySessions.size() + " sessions logged");
        if (todaySessions.isEmpty()) {
            lastSessionLabel.setText("Latest: no workouts logged today.");
            return;
        }

        WorkoutSession latest = todaySessions.get(todaySessions.size() - 1);
        LocalDate latestDate = LocalDate.parse(latest.getDate());
        lastSessionLabel.setText("Latest: " + latest.getSessionName() + " on " + latestDate);
    }

    private void loadBmiCard() {
        if (service.getCurrentUser() == null) {
            bmiValueLabel.setText("--");
            bmiCategoryLabel.setText("Log in to view BMI");
            bmiCategoryLabel.setStyle("-fx-text-fill: #AAAAAA;");
            return;
        }

        bmiValueLabel.setText(String.format("%.1f", service.calculateBMI()));
        String category = service.getBmiCategory();
        bmiCategoryLabel.setText(category);

        switch (category) {
            case "Normal" -> bmiCategoryLabel.setStyle("-fx-text-fill: #39FF14;");
            case "Underweight" -> bmiCategoryLabel.setStyle("-fx-text-fill: #00E5FF;");
            case "Overweight" -> bmiCategoryLabel.setStyle("-fx-text-fill: #FFD700;");
            case "Obese" -> bmiCategoryLabel.setStyle("-fx-text-fill: #FF4500;");
            default -> bmiCategoryLabel.setStyle("-fx-text-fill: #AAAAAA;");
        }
    }

    private void loadReminderCard() {
        if (service.getCurrentUser() == null) {
            reminderTitleLabel.setText("No reminder loaded");
            reminderTimeLabel.setText("Log in to see reminders");
            upcomingCountLabel.setText("No reminder queue yet");
            return;
        }

        Reminder next = service.getNextReminder();
        int reminderCount = service.getAllReminders().size();
        upcomingCountLabel.setText(reminderCount + " upcoming reminder" + (reminderCount == 1 ? "" : "s"));

        if (next == null) {
            reminderTitleLabel.setText("No upcoming reminders");
            reminderTimeLabel.setText("You're all caught up.");
            return;
        }

        reminderTitleLabel.setText(next.getLabel());
        reminderTimeLabel.setText(next.getScheduledTime().format(FORMATTER));
    }

    private void configureWorkloadLists() {
        sessionWorkloadsListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().remove("today-workload-cell");
                setWrapText(true);

                if (empty || item == null) {
                    setText(null);
                    return;
                }

                setText(item);
                if (item.startsWith("TODAY")) {
                    getStyleClass().add("today-workload-cell");
                }
            }
        });
    }

    private void loadPastExercises() {
        if (exerciseWorkloadsListView == null || sessionWorkloadsListView == null) return;

        if (service.getCurrentUser() == null) {
            exerciseWorkloadsListView.setItems(FXCollections.observableArrayList("Log in to view exercise history."));
            sessionWorkloadsListView.setItems(FXCollections.observableArrayList("Log in to view session history."));
            return;
        }
        
        ObservableList<String> exercises = FXCollections.observableArrayList();
        ObservableList<String> sessions = FXCollections.observableArrayList();
        Map<LocalDate, Double> dailyWorkloads = new LinkedHashMap<>();
        Map<LocalDate, Integer> dailySessionCounts = new LinkedHashMap<>();
        
        for (WorkoutSession session : service.getSessions()) {
            for (Exercise exercise : session.getExercises()) {
                double exerciseTotalWorkload = 0;
                for (SetRecord set : exercise.getSets()) {
                    exerciseTotalWorkload += set.getWorkloadScore();
                }
                exercises.add(exercise.getName() + " | Workload: " + String.format("%.1f", exerciseTotalWorkload));
            }

            LocalDate sessionDate = LocalDate.parse(session.getDate());
            dailyWorkloads.merge(sessionDate, session.getTotalWorkload(), Double::sum);
            dailySessionCounts.merge(sessionDate, 1, Integer::sum);
        }

        LocalDate today = LocalDate.now();
        sessions.add(buildDailyWorkloadItem(
            today,
            dailyWorkloads.getOrDefault(today, 0.0),
            dailySessionCounts.getOrDefault(today, 0),
            true
        ));
        dailyWorkloads.entrySet().stream()
            .filter(entry -> !entry.getKey().equals(today))
            .sorted(Map.Entry.<LocalDate, Double>comparingByKey(Comparator.reverseOrder()))
            .forEach(entry -> sessions.add(buildDailyWorkloadItem(
                entry.getKey(),
                entry.getValue(),
                dailySessionCounts.getOrDefault(entry.getKey(), 0),
                false
            )));

        if (exercises.isEmpty()) exercises.add("No past exercises found.");
        
        exerciseWorkloadsListView.setItems(exercises);
        sessionWorkloadsListView.setItems(sessions);
    }

    private String buildDailyWorkloadItem(LocalDate date, double totalWorkload, int sessionCount, boolean today) {
        String title = today ? "TODAY • " + SHORT_DATE_FORMATTER.format(date) : DATE_FORMATTER.format(date);
        return title
            + "\nDaily total: " + String.format("%.1f", totalWorkload)
            + "\nSessions: " + sessionCount;
    }
}
