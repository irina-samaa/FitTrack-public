package com.fittrack.util;

import com.fittrack.model.BodyPart;
import com.fittrack.model.ExerciseType;
import com.fittrack.model.ReminderService;
import com.fittrack.model.User;
import com.fittrack.model.WorkoutSession;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DataStore {
    private static DataStore instance;

    private final User registeredUser;
    private User currentUser;
    private final ArrayList<BodyPart> bodyParts = new ArrayList<>();
    private final ArrayList<WorkoutSession> sessions = new ArrayList<>();
    private final ReminderService reminderService = new ReminderService();

    private DataStore() {
        registeredUser = new User("admin", "1234", 70, 175);
        registeredUser.seedWeightHistory(List.of(68.0, 69.0, 70.0, 70.5, 71.0, 70.0, 69.5));
        seedBodyParts();
        seedSessions();
        seedBodyPartReminderDemo();
        seedReminders();
    }

    public static DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    public boolean authenticate(String username, String password) {
        boolean valid = registeredUser.getUsername().equals(username) && registeredUser.matchesPassword(password);
        if (valid) {
            currentUser = registeredUser;
        }
        return valid;
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
        reminderService.ensureBodyPartRule(registeredUser, bodyPart.getName());
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

    private void seedBodyParts() {
        BodyPart chest = new BodyPart("Chest");
        chest.createExercise("Bench Press", ExerciseType.STRENGTH).addSet(8, 60);
        chest.createExercise("Push Up", ExerciseType.STRENGTH).addSet(20, 0);
        chest.createExercise("Chest Fly", ExerciseType.STRENGTH).addSet(12, 15);

        BodyPart legs = new BodyPart("Legs");
        legs.createExercise("Squat", ExerciseType.STRENGTH).addSet(6, 90);
        legs.createExercise("Treadmill Run", ExerciseType.CARDIO).addSet(20, 3.2);

        BodyPart back = new BodyPart("Back");
        back.createExercise("Deadlift", ExerciseType.STRENGTH).addSet(5, 100);
        back.createExercise("Rowing Machine", ExerciseType.ENDURANCE).addSet(18, 145);

        addBodyPart(chest);
        addBodyPart(legs);
        addBodyPart(back);
    }

    private void seedSessions() {
        WorkoutSession chestSession = new WorkoutSession(LocalDate.now().minusDays(2), "Chest Technique Day");
        BodyPart chest = findBodyPart("Chest");
        if (chest != null) {
            for (var exercise : chest.getExercises()) {
                chestSession.addExercise(exercise.copy());
            }
        }
        sessions.add(chestSession);
        registeredUser.addWorkoutSession(chestSession);

        WorkoutSession legsSession = new WorkoutSession(LocalDate.now().minusDays(6), "Leg Day Demo");
        BodyPart legs = findBodyPart("Legs");
        if (legs != null) {
            for (var exercise : legs.getExercises()) {
                legsSession.addExercise(exercise.copy());
            }
        }
        sessions.add(legsSession);
        registeredUser.addWorkoutSession(legsSession);
    }

    private void seedBodyPartReminderDemo() {
        reminderService.saveBodyPartRule(registeredUser, "Chest", 5, LocalDate.now().minusDays(5));
        reminderService.saveBodyPartRule(registeredUser, "Legs", 5, LocalDate.now().minusDays(8));
        reminderService.saveBodyPartRule(registeredUser, "Back", 5, LocalDate.now().minusDays(7));
    }

    private void seedReminders() {
        reminderService.scheduleReminder(registeredUser, "Hydration Check", LocalDateTime.now().plusHours(12), null, "Aim for 2.5L today");
        reminderService.scheduleReminder(registeredUser, "Sunday Stretch", LocalDateTime.now().plusDays(2), null, "10-minute mobility session");
    }

    private BodyPart findBodyPart(String name) {
        for (BodyPart bodyPart : bodyParts) {
            if (bodyPart.getName().equalsIgnoreCase(name)) {
                return bodyPart;
            }
        }
        return null;
    }
}
