```java
package com.fittrack.util;

import com.fittrack.firebase.FirestoreService;
import com.fittrack.model.BodyPart;
import com.fittrack.model.Exercise;
import com.fittrack.model.ExerciseType;
import com.fittrack.model.Reminder;
import com.fittrack.model.ReminderService;
import com.fittrack.model.User;
import com.fittrack.model.WorkoutSession;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataStore {

    private static final double DEFAULT_WEIGHT = 70.0;
    private static final double DEFAULT_HEIGHT = 170.0;

    private static DataStore instance;

    private final FirestoreService firestoreService;

    private final ArrayList<BodyPart> bodyParts = new ArrayList<>();
    private final ArrayList<WorkoutSession> sessions = new ArrayList<>();

    private User currentUser;
    private ReminderService reminderService = new ReminderService();

    private DataStore() {
        firestoreService = FirestoreService.getInstance();
    }

    public static DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    public boolean authenticate(String username, String password) {
        return true;
    }

    public boolean createAccount(String username, String password) {
        return true;
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

    public void setCurrentUser(User user) {
        this.currentUser = user;
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
        saveSessionToFirestore(session);
    }

    public ReminderService getReminderService() {
        return reminderService;
    }

    public void saveCurrentUserProfile() {

        User user = requireCurrentUser();

        Map<String, Object> profile = new HashMap<>();

        profile.put("username", user.getUsername());
        profile.put("weight", user.getWeight());
        profile.put("height", user.getHeight());
        profile.put("weightHistory", user.getWeightHistory());

        firestoreService.saveUserProfile(
            user.getUsername(),
            profile
        );
    }

    public void saveCurrentUserWorkoutDraft() {

        User user = requireCurrentUser();

        List<Map<String, Object>> bodyPartsData = new ArrayList<>();

        for (BodyPart bodyPart : bodyParts) {

            Map<String, Object> bodyPartMap = new HashMap<>();

            bodyPartMap.put("name", bodyPart.getName());

            List<Map<String, Object>> exercises = new ArrayList<>();

            for (Exercise exercise : bodyPart.getExercises()) {

                Map<String, Object> exerciseMap = new HashMap<>();

                exerciseMap.put("name", exercise.getName());
                exerciseMap.put("type", exercise.getType().toString());

                exercises.add(exerciseMap);
            }

            bodyPartMap.put("exercises", exercises);

            bodyPartsData.add(bodyPartMap);
        }

        firestoreService.saveBodyParts(
            user.getUsername(),
            bodyPartsData
        );
    }

    public void saveCurrentUserSessions() {

        for (WorkoutSession session : sessions) {
            saveSessionToFirestore(session);
        }
    }

    public void saveCurrentUserReminders() {

        User user = requireCurrentUser();

        for (Reminder reminder : reminderService.getReminders(bodyParts)) {

            Map<String, Object> reminderData = new HashMap<>();

            reminderData.put(
                "intervalDays",
                reminder.getIntervalDays()
            );

            reminderData.put(
                "scheduledTime",
                reminder.getScheduledTime().toString()
            );

            firestoreService.saveReminder(
                user.getUsername(),
                reminder.getBodyPart().getName(),
                reminderData
            );
        }
    }

    private void saveSessionToFirestore(WorkoutSession session) {

        User user = requireCurrentUser();

        Map<String, Object> sessionData = new HashMap<>();

        sessionData.put("name", session.getSessionName());

        sessionData.put(
            "date",
            session.getWorkoutDate().toString()
        );

        List<Map<String, Object>> exerciseList = new ArrayList<>();

        for (Exercise exercise : session.getExercises()) {

            Map<String, Object> exerciseMap = new HashMap<>();

            exerciseMap.put("name", exercise.getName());
            exerciseMap.put("type", exercise.getType().toString());

            exerciseList.add(exerciseMap);
        }

        sessionData.put("exercises", exerciseList);

        firestoreService.saveWorkoutSession(
            user.getUsername(),
            sessionData
        );
    }

    public void loadUserData(String userId) {

        try {

            Map<String, Object> profile =
                firestoreService.loadUserProfile(userId);

            if (profile == null || profile.isEmpty()) {

                currentUser = new User(
                    userId,
                    DEFAULT_WEIGHT,
                    DEFAULT_HEIGHT
                );

                saveCurrentUserProfile();

                ensureDefaultWorkoutDraft();

                return;
            }

            double weight =
                ((Number) profile.getOrDefault("weight", DEFAULT_WEIGHT))
                    .doubleValue();

            double height =
                ((Number) profile.getOrDefault("height", DEFAULT_HEIGHT))
                    .doubleValue();

            currentUser = new User(
                userId,
                weight,
                height
            );

            ensureDefaultWorkoutDraft();

        } catch (Exception e) {

            System.out.println(
                "Error loading Firebase user data: "
                    + e.getMessage()
            );
        }
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

    private boolean addDefaultExercise(
        BodyPart bodyPart,
        String name,
        ExerciseType type
    ) {

        if (bodyPart.findExercise(name) != null) {
            return false;
        }

        bodyPart.createExercise(name, type);

        return true;
    }

    private User requireCurrentUser() {

        if (currentUser == null) {
            throw new IllegalStateException(
                "No user is logged in."
            );
        }

        return currentUser;
    }

    private BodyPart findBodyPart(String name) {

        if (name == null) {
            return null;
        }

        for (BodyPart bodyPart : bodyParts) {

            if (bodyPart.getName().equalsIgnoreCase(name.trim())) {
                return bodyPart;
            }
        }

        return null;
    }
}

