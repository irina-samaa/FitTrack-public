package com.fittrack.controller;

import com.fittrack.model.Reminder;
import com.fittrack.model.WorkoutSession;
import com.fittrack.model.Exercise;
import com.fittrack.model.SetRecord;
import com.fittrack.service.FitnessTrackerService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class DashboardController {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

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
    @FXML private Label nextReminderLabel;
    @FXML private Label motivationLabel;
    @FXML private Label encapsulationLabel;
    @FXML private Label inheritanceLabel;
    @FXML private Label polymorphismLabel;
    @FXML private Label abstractionLabel;
    @FXML private ListView<String> exerciseWorkloadsListView;
    @FXML private ListView<String> sessionWorkloadsListView;

    private final FitnessTrackerService service = FitnessTrackerService.getInstance();

    @FXML
    private void initialize() {
        loadWorkoutCard();
        loadBmiCard();
        loadReminderCard();
        loadOopCard();
        loadMotivationQuote();
        loadPastExercises();
    }

    private void loadWorkoutCard() {
        ArrayList<WorkoutSession> sessions = new ArrayList<>(service.getSessions());
        sessionCountLabel.setText(sessions.size() + " sessions logged");
        if (sessions.isEmpty()) {
            lastSessionLabel.setText("No workouts yet - go crush it!");
            return;
        }
        WorkoutSession last = sessions.get(sessions.size() - 1);
        lastSessionLabel.setText("Last: " + last.getSessionName());
    }

    private void loadBmiCard() {
        if (service.getCurrentUser() == null) {
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
        Reminder next = service.getNextReminder();
        if (next == null) {
            nextReminderLabel.setText("No upcoming reminders");
            return;
        }
        nextReminderLabel.setText(next.getLabel() + "\n" + next.getScheduledTime().format(FORMATTER));
    }

    private void loadOopCard() {
        encapsulationLabel.setText("Encapsulation: User dong goi weight, height, history va chi cho phep cap nhat qua method.");
        inheritanceLabel.setText("Inheritance: StrengthExercise, CardioExercise, EnduranceExercise ke thua Exercise.");
        polymorphismLabel.setText("Polymorphism: UI lam viec qua Exercise va SetRecord chung, runtime tu chon hanh vi dung cho tung subclass.");
        abstractionLabel.setText("Abstraction: Exercise va SetRecord la abstract class, controller goi nghiep vu qua FitnessTrackerService.");
    }

    private void loadMotivationQuote() {
        int index = (int) (Math.random() * QUOTES.length);
        motivationLabel.setText("\"" + QUOTES[index] + "\"");
    }

    private void loadPastExercises() {
        if (exerciseWorkloadsListView == null || sessionWorkloadsListView == null) return;
        
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
