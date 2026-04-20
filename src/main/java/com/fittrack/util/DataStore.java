package com.fittrack.util;

import com.fittrack.model.*;

import java.util.ArrayList;

/**
 * DataStore.java — Singleton chứa toàn bộ dữ liệu của app.
 *
 * ⚠️ FILE NÀY LÀ STUB CHO TEAM UI ⚠️
 * Backend team sẽ implement đầy đủ logic bên trong.
 * UI team chỉ cần biết các method signature để compile và test UI.
 *
 * Pattern: Singleton — chỉ có 1 instance duy nhất trong toàn app.
 */
public class DataStore {

    // Instance duy nhất (Singleton)
    private static DataStore instance;

    // Dữ liệu của app
    private User currentUser;
    private ArrayList<BodyPart> bodyParts;
    private ArrayList<WorkoutSession> sessions;
    private ReminderService reminderService;

    /**
     * Constructor private — không ai new DataStore() được từ bên ngoài.
     * TODO (Backend): Seed dữ liệu test ở đây.
     */
    private DataStore() {
        bodyParts = new ArrayList<>();
        sessions = new ArrayList<>();
        reminderService = new ReminderService();

        // TODO (Backend): Tạo test user và seed data theo vibe doc:
        // User: admin / 1234
        // HealthMetrics: weight=70, height=175
        // weightHistory: [68, 69, 70, 70.5, 71, 70, 69.5]
        // BodyParts: Chest (Bench Press, Push Up, Chest Fly), Legs, Back
        // Reminders: Chest Day (tomorrow), Leg Day (in 3 days)

        System.out.println("DataStore initialized (stub)");
    }

    /**
     * Lấy instance duy nhất của DataStore.
     * Nếu chưa tồn tại thì tạo mới.
     */
    public static DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    // ===== USER =====

    /** Lấy user đang đăng nhập */
    public User getCurrentUser() {
        return currentUser; // TODO (Backend): return currentUser
    }

    /** Set user sau khi login thành công */
    public void setCurrentUser(User user) {
        this.currentUser = user; // TODO (Backend): implement
    }

    // ===== BODY PARTS =====

    /** Lấy toàn bộ danh sách BodyPart */
    public ArrayList<BodyPart> getBodyParts() {
        return bodyParts; // TODO (Backend): return list
    }

    /** Thêm một BodyPart mới */
    public void addBodyPart(BodyPart bodyPart) {
        bodyParts.add(bodyPart); // TODO (Backend): implement
    }

    // ===== WORKOUT SESSIONS =====

    /** Lấy toàn bộ danh sách WorkoutSession */
    public ArrayList<WorkoutSession> getSessions() {
        return sessions; // TODO (Backend): return list
    }

    /** Thêm một WorkoutSession mới vào lịch sử */
    public void addSession(WorkoutSession session) {
        sessions.add(session); // TODO (Backend): implement
    }

    // ===== REMINDER SERVICE =====

    /** Lấy ReminderService (quản lý PriorityQueue reminders) */
    public ReminderService getReminderService() {
        return reminderService; // TODO (Backend): return service
    }
}
