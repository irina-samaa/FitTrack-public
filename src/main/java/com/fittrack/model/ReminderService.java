package com.fittrack.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.PriorityQueue;

/**
 * ReminderService.java — STUB cho UI team.
 * Dùng PriorityQueue (min-heap) để luôn lấy reminder sớm nhất trước.
 */
public class ReminderService {
    private PriorityQueue<Reminder> reminders = new PriorityQueue<>();
    private int recoveryDays = 5; // Default

    /**
     * Thêm reminder mới vào PriorityQueue.
     * TODO (Backend): implement đầy đủ.
     */
    public void scheduleReminder(String label, LocalDateTime time) {
        Reminder r = new Reminder(label, time);
        reminders.add(r); // PriorityQueue tự sort theo compareTo()
    }

    /**
     * Xem reminder sớm nhất (đầu min-heap) — KHÔNG xóa.
     * TODO (Backend): implement.
     */
    public Reminder getNextReminder() {
        return reminders.peek(); // peek = xem mà không xóa
    }

    /**
     * Xóa và trả về reminder sớm nhất (poll từ min-heap).
     * TODO (Backend): implement.
     */
    public Reminder removeNextReminder() {
        return reminders.poll(); // poll = xóa và trả về
    }

    /**
     * Trả về sorted list copy của toàn bộ reminders.
     * Dùng để hiển thị trong TableView.
     * TODO (Backend): implement — cần tạo copy để không xóa khỏi queue gốc.
     */
    public ArrayList<Reminder> getAllReminders() {
        // Tạo PriorityQueue copy để drain mà không ảnh hưởng original
        PriorityQueue<Reminder> copy = new PriorityQueue<>(reminders);
        ArrayList<Reminder> result = new ArrayList<>();
        while (!copy.isEmpty()) {
            result.add(copy.poll()); // Poll theo thứ tự min-heap (sớm nhất trước)
        }
        return result;
    }

    public void setRecoveryDays(int days) {
        this.recoveryDays = days; // TODO (Backend): implement
    }

    public int getRecoveryDays() { return recoveryDays; }
}
