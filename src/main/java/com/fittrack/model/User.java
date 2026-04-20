package com.fittrack.model;

/**
 * User.java — STUB cho UI team.
 * Backend team implement đầy đủ.
 */
public class User {
    private String username;
    private String password;
    private HealthMetrics healthMetrics;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public HealthMetrics getHealthMetrics() { return healthMetrics; }
    public void setHealthMetrics(HealthMetrics hm) { this.healthMetrics = hm; }
}
