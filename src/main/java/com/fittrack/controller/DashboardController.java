package com.fittrack.controller;

import com.fittrack.model.Exercise;
import com.fittrack.model.Reminder;
import com.fittrack.model.SetRecord;
import com.fittrack.model.WorkoutSession;
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
import java.util.Set;
import java.util.TreeSet;

public class DashboardController {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter SHORT_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d");

    private static final String[] QUOTES = {
        "No pain, no gain. Push through!",
        "Your only competition is yesterday's you.",
        "Consistency beats motivation every time.",
        "Train hard, recover smart.",
        "One more rep. Always one more rep."
    };

    @FXML private Label sessionCountLabel;
    @FXML private Label lastSessionLabel;
    @FXML private Label bmiValueLabel;
    @FXML private Label bmiCategoryLabel;
    @FXML private Label reminderTitleLabel;
    @FXML private Label reminderTimeLabel;
    @FXML private Label motivationLabel;
    @FXML private Label todayDateLabel;
    @FXML private Label currentTimeLabel;
    @FXML private Label upcomingCountLabel;
    @FXML private Label streakCountLabel;
    @FXML private Label streakDetailLabel;
    @FXML private Label streakSupportLabel;
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
        loadMotivationQuote();
        loadStreakCard();
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

        ArrayList<WorkoutSession> sessions = new ArrayList<>(service.getSessions());
        sessionCountLabel.setText(sessions.size() + " sessions logged");
        if (sessions.isEmpty()) {
            lastSessionLabel.setText("Latest: no workouts yet - go crush it!");
            return;
        }

        WorkoutSession latest = sessions.get(sessions.size() - 1);
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
            upcomingCountLabel.setText("0 reminders due");
            return;
        }

        int dueReminderCount = service.getDueReminderCount();
        upcomingCountLabel.setText(dueReminderCount + " reminder" + (dueReminderCount == 1 ? "" : "s") + " due");

        Reminder topReminder = service.getTopDueReminder();
        if (topReminder == null) {
            reminderTitleLabel.setText("No reminders due");
            reminderTimeLabel.setText("Threshold countdown starts after the first logged workout for each body part.");
            return;
        }

        int inactiveDays = service.getInactiveDays(topReminder);
        reminderTitleLabel.setText(topReminder.getTitle());
        reminderTimeLabel.setText(topReminder.formatStatusMessage(inactiveDays));
    }

    private void loadMotivationQuote() {
        int index = (int) (Math.random() * QUOTES.length);
        motivationLabel.setText("\"" + QUOTES[index] + "\"");
    }

    private void loadStreakCard() {
        if (service.getCurrentUser() == null) {
            streakCountLabel.setText("\uD83D\uDD25 --");
            streakDetailLabel.setText("Log in to see your current workout streak.");
            streakSupportLabel.setText("Your streak summary will appear here.");
            return;
        }

        Set<LocalDate> workoutDays = new TreeSet<>();
        for (WorkoutSession session : service.getSessions()) {
            workoutDays.add(LocalDate.parse(session.getDate()));
        }

        long recentSessions = workoutDays.stream()
            .filter(date -> !date.isBefore(LocalDate.now().minusDays(6)))
            .count();
        int streak = calculateStreak(workoutDays);

        streakCountLabel.setText("\uD83D\uDD25 " + streak + " day" + (streak == 1 ? "" : "s"));
        if (streak == 0) {
            streakDetailLabel.setText("No active streak yet. Your next workout can light it up.");
        } else {
            streakDetailLabel.setText("You have trained on " + streak + " consecutive day" + (streak == 1 ? "" : "s") + ". Keep the fire going.");
        }

        if (workoutDays.isEmpty()) {
            streakSupportLabel.setText("No workout history yet");
            return;
        }

        LocalDate latestWorkout = workoutDays.stream().max(LocalDate::compareTo).orElse(LocalDate.now());
        streakSupportLabel.setText(recentSessions + " active day" + (recentSessions == 1 ? "" : "s")
            + " in the last 7 days | Last workout " + SHORT_DATE_FORMATTER.format(latestWorkout));
    }

    private int calculateStreak(Set<LocalDate> workoutDays) {
        if (workoutDays.isEmpty()) {
            return 0;
        }

        LocalDate cursor = LocalDate.now();
        if (!workoutDays.contains(cursor)) {
            cursor = cursor.minusDays(1);
            if (!workoutDays.contains(cursor)) {
                return 0;
            }
        }

        int streak = 0;
        while (workoutDays.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
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
