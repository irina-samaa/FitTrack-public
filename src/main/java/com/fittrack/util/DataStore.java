package com.fittrack.util;

import com.fittrack.model.BodyPart;
import com.fittrack.model.ExerciseType;
import com.fittrack.model.Reminder;
import com.fittrack.model.ReminderService;
import com.fittrack.model.User;
import com.fittrack.model.WorkoutSession;

import java.util.ArrayList;

public class DataStore {
    private static final double DEFAULT_WEIGHT = 70.0;
    private static final double DEFAULT_HEIGHT = 170.0;

    private static DataStore instance;

    private final SQLiteRepository repository;
    private final ArrayList<BodyPart> bodyParts = new ArrayList<>();
    private final ArrayList<WorkoutSession> sessions = new ArrayList<>();

    private User currentUser;
    private ReminderService reminderService = new ReminderService();

    private DataStore() {
        repository = new SQLiteRepository();
    }

    public static DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    public boolean authenticate(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return false;
        }

        SQLiteRepository.UserCredentials credentials = repository.findUserCredentials(username.trim());
        if (credentials == null || !credentials.password().equals(password)) {
            return false;
        }

        hydrateCurrentUser(repository.loadUserData(credentials.username()));
        return true;
    }

    public boolean createAccount(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return false;
        }

        boolean created = repository.createUser(username.trim(), password, DEFAULT_WEIGHT, DEFAULT_HEIGHT);
        if (!created) {
            return false;
        }
        return authenticate(username.trim(), password);
    }

    public void logout() {
        currentUser = null;
        bodyParts.clear();
        sessions.clear();
        reminderService = new ReminderService();
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public ArrayList<BodyPart> getBodyParts() {
        return bodyParts;
    }

    public void addBodyPart(BodyPart bodyPart) {
        bodyParts.add(bodyPart);
        saveCurrentUserWorkoutDraft();
    }

    public ArrayList<WorkoutSession> getSessions() {
        return sessions;
    }

    public void addSession(WorkoutSession session) {
        sessions.add(session);
        if (currentUser != null) {
            currentUser.addWorkoutSession(session);
        }
    }

    public ReminderService getReminderService() {
        return reminderService;
    }

    public void saveCurrentUserProfile() {
        User user = requireCurrentUser();
        repository.saveUserProfile(user);
        repository.saveWeightHistory(user);
    }

    public void saveCurrentUserWorkoutDraft() {
        repository.saveWorkoutDraft(requireCurrentUser().getUsername(), bodyParts);
    }

    public void saveCurrentUserSessions() {
        repository.saveSessions(requireCurrentUser().getUsername(), sessions);
    }

    public void saveCurrentUserReminders() {
        repository.saveReminders(requireCurrentUser().getUsername(), getAllCurrentReminders());
    }

    private void hydrateCurrentUser(SQLiteRepository.LoadedUserData loadedUserData) {
        currentUser = loadedUserData.user();
        bodyParts.clear();
        bodyParts.addAll(loadedUserData.bodyParts());
        ensureDefaultWorkoutDraft();
        sessions.clear();
        sessions.addAll(loadedUserData.sessions());
        for (WorkoutSession session : sessions) {
            currentUser.addWorkoutSession(session);
        }
        reminderService = new ReminderService();
        for (Reminder reminder : loadedUserData.reminders()) {
            reminderService.loadReminder(currentUser, reminder);
        }
        reminderService.syncRemindersFromLoggedHistory(currentUser, sessions);
        saveCurrentUserReminders();
    }

    private void ensureDefaultWorkoutDraft() {
        boolean changed = false;

        BodyPart chest = findOrCreateBodyPart("Chest");
        changed |= addDefaultExercise(chest, "Bench Press", ExerciseType.STRENGTH);
        changed |= addDefaultExercise(chest, "Push Up", ExerciseType.STRENGTH);
        changed |= addDefaultExercise(chest, "Chest Fly", ExerciseType.STRENGTH);

        BodyPart back = findOrCreateBodyPart("Back");
        changed |= addDefaultExercise(back, "Deadlift", ExerciseType.STRENGTH);
        changed |= addDefaultExercise(back, "Dumbbell Row", ExerciseType.STRENGTH);
        changed |= addDefaultExercise(back, "Rowing Machine", ExerciseType.ENDURANCE);

        BodyPart legs = findOrCreateBodyPart("Legs");
        changed |= addDefaultExercise(legs, "Squat", ExerciseType.STRENGTH);
        changed |= addDefaultExercise(legs, "Lunge", ExerciseType.STRENGTH);
        changed |= addDefaultExercise(legs, "Treadmill Run", ExerciseType.CARDIO);

        BodyPart core = findOrCreateBodyPart("Core");
        changed |= addDefaultExercise(core, "Plank Hold", ExerciseType.ENDURANCE);
        changed |= addDefaultExercise(core, "Crunch", ExerciseType.STRENGTH);
        changed |= addDefaultExercise(core, "Cycling", ExerciseType.CARDIO);

        if (changed) {
            saveCurrentUserWorkoutDraft();
        }
    }

    private BodyPart findOrCreateBodyPart(String name) {
        for (BodyPart bodyPart : bodyParts) {
            if (bodyPart.getName().equalsIgnoreCase(name)) {
                return bodyPart;
            }
        }

        BodyPart bodyPart = new BodyPart(name);
        bodyParts.add(bodyPart);
        return bodyPart;
    }

    private boolean addDefaultExercise(BodyPart bodyPart, String name, ExerciseType type) {
        if (bodyPart.findExercise(name) != null) {
            return false;
        }

        bodyPart.createExercise(name, type);
        return true;
    }

    private ArrayList<Reminder> getAllCurrentReminders() {
        User user = requireCurrentUser();
        return reminderService.getAllReminders(user);
    }

    private User requireCurrentUser() {
        if (currentUser == null) {
            throw new IllegalStateException("No user is logged in.");
        }
        return currentUser;
    }
}
