package com.fittrack.service;

import com.fittrack.model.BodyPart;
import com.fittrack.model.Exercise;
import com.fittrack.model.ExerciseType;
import com.fittrack.model.HealthMetrics;
import com.fittrack.model.ProgressTracker;
import com.fittrack.model.Reminder;
import com.fittrack.model.SetRecord;
import com.fittrack.model.User;
import com.fittrack.model.WorkoutSession;
import com.fittrack.util.DataStore;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FitnessTrackerService {
    private static FitnessTrackerService instance;

    private final DataStore dataStore;
    private final HealthMetrics healthMetrics;
    private final ProgressTracker progressTracker;

    private FitnessTrackerService() {
        dataStore = DataStore.getInstance();
        healthMetrics = new HealthMetrics();
        progressTracker = new ProgressTracker();
    }

    public static FitnessTrackerService getInstance() {
        if (instance == null) {
            instance = new FitnessTrackerService();
        }
        return instance;
    }

    public boolean login(String username, String password) {
        return dataStore.authenticate(username, password);
    }

    public boolean createAccount(String username, String password) {
        return dataStore.createAccount(username, password);
    }

    public void logout() {
        dataStore.logout();
    }

    public User getCurrentUser() {
        return dataStore.getCurrentUser();
    }

    public List<BodyPart> getBodyParts() {
        return dataStore.getBodyParts();
    }

    public BodyPart createBodyPart(String name) {
        BodyPart bodyPart = new BodyPart(name);
        dataStore.addBodyPart(bodyPart);
        return bodyPart;
    }

    public BodyPart findBodyPart(String name) {
        for (BodyPart bodyPart : dataStore.getBodyParts()) {
            if (bodyPart.getName().equalsIgnoreCase(name)) {
                return bodyPart;
            }
        }
        return null;
    }

    public Exercise createExercise(String bodyPartName, String exerciseName, ExerciseType type) {
        BodyPart bodyPart = requireBodyPart(bodyPartName);
        Exercise exercise = bodyPart.createExercise(exerciseName, type);
        dataStore.saveCurrentUserWorkoutDraft();
        return exercise;
    }

    public Exercise findExercise(String bodyPartName, String exerciseName) {
        return requireBodyPart(bodyPartName).findExercise(exerciseName);
    }

    public void addSet(String bodyPartName, String exerciseName, int firstMetric, double secondMetric) {
        Exercise exercise = requireExercise(bodyPartName, exerciseName);
        exercise.addSet(firstMetric, secondMetric);
        dataStore.saveCurrentUserWorkoutDraft();
    }

    public void deleteSet(String bodyPartName, String exerciseName, SetRecord setRecord) {
        Exercise exercise = requireExercise(bodyPartName, exerciseName);
        exercise.removeSet(setRecord);
        dataStore.saveCurrentUserWorkoutDraft();
    }

    public WorkoutSession startWorkoutSession(String sessionName) {
        User currentUser = requireCurrentUser();
        String resolvedSessionName = (sessionName == null || sessionName.isBlank())
            ? "Workout - " + LocalDate.now()
            : sessionName.trim();
        WorkoutSession session = new WorkoutSession(LocalDate.now(), resolvedSessionName);
        List<Exercise> exercisesToClear = new ArrayList<>();
        for (BodyPart bodyPart : dataStore.getBodyParts()) {
            for (Exercise exercise : bodyPart.getExercises()) {
                if (exercise.hasSets()) {
                    session.addExercise(exercise.copy());
                    exercisesToClear.add(exercise);
                }
            }
        }
        if (session.getExercises().isEmpty()) {
            throw new IllegalStateException("Add at least one exercise set before logging a session.");
        }
        dataStore.addSession(session);
        for (Exercise exercise : exercisesToClear) {
            exercise.clearSets();
        }

        // Reset repeating reminders since a workout was completed
        dataStore.getReminderService().resetRepeatingReminders(currentUser);

        dataStore.saveCurrentUserSessions();
        dataStore.saveCurrentUserWorkoutDraft();
        dataStore.saveCurrentUserReminders();
        return session;
    }

    public List<WorkoutSession> getSessions() {
        return dataStore.getSessions();
    }

    public void updateHealth(double weight, double height) {
        User user = requireCurrentUser();
        user.updateWeight(weight);
        user.updateHeight(height);
        dataStore.saveCurrentUserProfile();
    }

    public double calculateBMI() {
        User user = requireCurrentUser();
        return healthMetrics.calculateBMI(user.getWeight(), user.getHeight());
    }

    public String getBmiCategory() {
        return healthMetrics.getBMICategory(calculateBMI());
    }

    public String getHealthRecommendation() {
        return healthMetrics.getHealthSuggestion(calculateBMI());
    }

    public ArrayList<Double> getWeightHistory() {
        return new ArrayList<>(requireCurrentUser().getWeightHistory());
    }

    public ArrayList<Double> getMovingAverage(int window) {
        return progressTracker.getMovingAverage(requireCurrentUser(), window);
    }

    public ArrayList<Double> generateWeightGraph() {
        return progressTracker.generateWeightGraph(requireCurrentUser());
    }

    public ArrayList<String> getProgressLabels() {
        return progressTracker.getLabels(requireCurrentUser());
    }

    public void scheduleReminder(String label, LocalDateTime time, Integer repeatIntervalDays) {
        scheduleReminder(label, time, repeatIntervalDays, null);
    }

    public void scheduleReminder(String label, LocalDateTime time, Integer repeatIntervalDays, String note) {
        dataStore.getReminderService().scheduleReminder(requireCurrentUser(), label, time, repeatIntervalDays, note);
        dataStore.saveCurrentUserReminders();
    }

    public Reminder getNextReminder() {
        return dataStore.getReminderService().getNextReminder(requireCurrentUser());
    }

    public Reminder removeNextReminder() {
        Reminder reminder = dataStore.getReminderService().removeNextReminder(requireCurrentUser());
        dataStore.saveCurrentUserReminders();
        return reminder;
    }

    public ArrayList<Reminder> getAllReminders() {
        return dataStore.getReminderService().getAllReminders(requireCurrentUser());
    }



    public List<SetRecord> getSets(String bodyPartName, String exerciseName) {
        return requireExercise(bodyPartName, exerciseName).getSets();
    }

    private User requireCurrentUser() {
        User user = dataStore.getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("No user is logged in.");
        }
        return user;
    }

    private BodyPart requireBodyPart(String bodyPartName) {
        BodyPart bodyPart = findBodyPart(bodyPartName);
        if (bodyPart == null) {
            throw new IllegalArgumentException("Body part not found: " + bodyPartName);
        }
        return bodyPart;
    }

    private Exercise requireExercise(String bodyPartName, String exerciseName) {
        Exercise exercise = findExercise(bodyPartName, exerciseName);
        if (exercise == null) {
            throw new IllegalArgumentException("Exercise not found: " + exerciseName);
        }
        return exercise;
    }
}
