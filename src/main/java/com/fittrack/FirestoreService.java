package com.fittrack.firebase;

import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * FirestoreService handles all reads/writes to Cloud Firestore.
 *
 * Firestore structure:
 *   users/{userId}/
 *     profile          → { email, weight, height, weightHistory[] }
 *     workouts/{id}    → { name, date, exercises[] }
 *     reminders/{id}   → { bodyPart, thresholdDays, note }
 */
public class FirestoreService {

    private static FirestoreService instance;
    private final Firestore db;

    private FirestoreService() {
        db = FirestoreClient.getFirestore();
    }

    public static FirestoreService getInstance() {
        if (instance == null) instance = new FirestoreService();
        return instance;
    }

    // ─── USER PROFILE ────────────────────────────────────────────────────────

    public void saveUserProfile(String userId, Map<String, Object> profileData) {
        db.collection("users").document(userId)
          .collection("data").document("profile")
          .set(profileData);
    }

    public Map<String, Object> loadUserProfile(String userId) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = db.collection("users").document(userId)
            .collection("data").document("profile")
            .get().get();
        return doc.exists() ? doc.getData() : new HashMap<>();
    }

    // ─── WEIGHT HISTORY ──────────────────────────────────────────────────────

    public void saveWeightHistory(String userId, List<Double> weightHistory) {
        Map<String, Object> data = new HashMap<>();
        data.put("weightHistory", weightHistory);
        db.collection("users").document(userId)
          .collection("data").document("profile")
          .set(data, SetOptions.merge());
    }

    // ─── WORKOUT SESSIONS ────────────────────────────────────────────────────

    public void saveWorkoutSession(String userId, Map<String, Object> sessionData) {
        db.collection("users").document(userId)
          .collection("workouts")
          .add(sessionData);
    }

    public List<Map<String, Object>> loadWorkoutSessions(String userId)
            throws ExecutionException, InterruptedException {
        List<Map<String, Object>> sessions = new ArrayList<>();
        QuerySnapshot snapshot = db.collection("users").document(userId)
            .collection("workouts")
            .orderBy("date", Query.Direction.DESCENDING)
            .get().get();
        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            Map<String, Object> data = doc.getData();
            if (data != null) {
                data.put("id", doc.getId());
                sessions.add(data);
            }
        }
        return sessions;
    }

    // ─── REMINDERS ───────────────────────────────────────────────────────────

    public void saveReminder(String userId, String bodyPartName, Map<String, Object> reminderData) {
        db.collection("users").document(userId)
          .collection("reminders").document(bodyPartName)
          .set(reminderData);
    }

    public Map<String, Object> loadReminder(String userId, String bodyPartName)
            throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = db.collection("users").document(userId)
            .collection("reminders").document(bodyPartName)
            .get().get();
        return doc.exists() ? doc.getData() : null;
    }

    public List<Map<String, Object>> loadAllReminders(String userId)
            throws ExecutionException, InterruptedException {
        List<Map<String, Object>> reminders = new ArrayList<>();
        QuerySnapshot snapshot = db.collection("users").document(userId)
            .collection("reminders").get().get();
        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            Map<String, Object> data = doc.getData();
            if (data != null) {
                data.put("bodyPart", doc.getId());
                reminders.add(data);
            }
        }
        return reminders;
    }

    // ─── BODY PARTS & EXERCISES ──────────────────────────────────────────────

    public void saveBodyParts(String userId, List<Map<String, Object>> bodyPartsData) {
        Map<String, Object> data = new HashMap<>();
        data.put("bodyParts", bodyPartsData);
        db.collection("users").document(userId)
          .collection("data").document("bodyParts")
          .set(data);
    }

    public List<Map<String, Object>> loadBodyParts(String userId)
            throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = db.collection("users").document(userId)
            .collection("data").document("bodyParts")
            .get().get();
        if (doc.exists() && doc.getData() != null) {
            Object raw = doc.getData().get("bodyParts");
            if (raw instanceof List<?> list) {
                List<Map<String, Object>> result = new ArrayList<>();
                for (Object item : list) {
                    if (item instanceof Map<?, ?> map) {
                        result.add((Map<String, Object>) map);
                    }
                }
                return result;
            }
        }
        return new ArrayList<>();
    }
}
