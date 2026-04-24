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
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class DashboardController {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

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
    @FXML private Label heroStatusLabel;
    @FXML private Label upcomingCountLabel;
    @FXML private Label readinessLabel;
    @FXML private Label consistencyLabel;
    @FXML private ListView<String> exerciseWorkloadsListView;
    @FXML private ListView<String> sessionWorkloadsListView;
    @FXML private ProgressBar readinessProgressBar;
    @FXML private ProgressBar consistencyProgressBar;

    private final FitnessTrackerService service = FitnessTrackerService.getInstance();
    private Timeline clockTimeline;

    @FXML
    private void initialize() {
        loadDateTimeHeader();
        loadWorkoutCard();
        loadBmiCard();
        loadReminderCard();
        loadMotivationQuote();
        loadVisualSummary();
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
            heroStatusLabel.setText("Log in to unlock today's workout overview.");
            return;
        }

        ArrayList<WorkoutSession> sessions = new ArrayList<>(service.getSessions());
        sessionCountLabel.setText(sessions.size() + " sessions logged");
        if (sessions.isEmpty()) {
            lastSessionLabel.setText("Latest: no workouts yet - go crush it!");
            heroStatusLabel.setText("Fresh start today. Your next session will set the tone.");
            return;
        }

        WorkoutSession latest = sessions.get(sessions.size() - 1);
        LocalDate latestDate = LocalDate.parse(latest.getDate());
        lastSessionLabel.setText("Latest: " + latest.getSessionName() + " on " + latestDate);

        long workoutsThisWeek = sessions.stream()
            .map(session -> LocalDate.parse(session.getDate()))
            .filter(date -> !date.isBefore(LocalDate.now().minusDays(6)))
            .count();
        heroStatusLabel.setText(workoutsThisWeek + " workout" + (workoutsThisWeek == 1 ? "" : "s") + " tracked in the last 7 days.");
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

    private void loadMotivationQuote() {
        int index = (int) (Math.random() * QUOTES.length);
        motivationLabel.setText("\"" + QUOTES[index] + "\"");
    }

    private void loadVisualSummary() {
        if (service.getCurrentUser() == null) {
            readinessLabel.setText("Log in to see your training readiness.");
            consistencyLabel.setText("Log in to see your weekly consistency.");
            readinessProgressBar.setProgress(0.0);
            consistencyProgressBar.setProgress(0.0);
            return;
        }

        String bmiCategory = service.getBmiCategory();
        double readinessScore = switch (bmiCategory) {
            case "Normal" -> 0.88;
            case "Underweight" -> 0.62;
            case "Overweight" -> 0.58;
            case "Obese" -> 0.42;
            default -> 0.5;
        };

        boolean hasUpcomingReminder = service.getNextReminder() != null;
        if (hasUpcomingReminder) {
            readinessScore = Math.min(1.0, readinessScore + 0.07);
        }

        long recentSessions = service.getSessions().stream()
            .map(session -> LocalDate.parse(session.getDate()))
            .filter(date -> !date.isBefore(LocalDate.now().minusDays(6)))
            .count();
        double consistencyScore = Math.min(1.0, recentSessions / 4.0);

        readinessProgressBar.setProgress(readinessScore);
        consistencyProgressBar.setProgress(consistencyScore);
        readinessLabel.setText(String.format("Body status looks %s. Readiness score: %d%%.", bmiCategory.toLowerCase(), Math.round(readinessScore * 100)));
        consistencyLabel.setText(String.format("%d workout%s recorded in the last 7 days.", recentSessions, recentSessions == 1 ? "" : "s"));
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
        
        for (WorkoutSession session : service.getSessions()) {
            double sessionTotalWorkload = 0;
            for (Exercise exercise : session.getExercises()) {
                double exerciseTotalWorkload = 0;
                for (SetRecord set : exercise.getSets()) {
                    exerciseTotalWorkload += set.getWorkloadScore();
                }
                sessionTotalWorkload += exerciseTotalWorkload;
                exercises.add(exercise.getName() + " | Workload: " + String.format("%.1f", exerciseTotalWorkload));
            }
            sessions.add(session.getDate() + " - " + session.getSessionName() + "\nTotal: " + String.format("%.1f", sessionTotalWorkload));
        }
        
        if (exercises.isEmpty()) exercises.add("No past exercises found.");
        if (sessions.isEmpty()) sessions.add("No session records found.");
        
        exerciseWorkloadsListView.setItems(exercises);
        sessionWorkloadsListView.setItems(sessions);
    }
}
