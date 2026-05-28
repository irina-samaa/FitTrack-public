package com.fittrack.firebase;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Handles Firebase Authentication using the Firebase Auth REST API.
 * This is needed for desktop apps since the Firebase Admin SDK
 * does not support client-side sign-in directly.
 *
 * Replace YOUR_FIREBASE_WEB_API_KEY with your actual key from:
 * Firebase Console → Project Settings → General → Web API Key
 */
public class FirebaseAuthService {

    private static final String API_KEY = "YOUR_FIREBASE_WEB_API_KEY";
    private static final String SIGN_IN_URL =
        "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + API_KEY;
    private static final String SIGN_UP_URL =
        "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=" + API_KEY;

    private String currentUserId;
    private String currentEmail;
    private String idToken;

    private static FirebaseAuthService instance;

    public static FirebaseAuthService getInstance() {
        if (instance == null) instance = new FirebaseAuthService();
        return instance;
    }

    /**
     * Signs in an existing user with email and password.
     * @return true if login succeeded
     */
    public boolean signIn(String email, String password) {
        try {
            String body = String.format(
                "{\"email\":\"%s\",\"password\":\"%s\",\"returnSecureToken\":true}",
                email, password
            );
            String response = post(SIGN_IN_URL, body);
            if (response != null && response.contains("idToken")) {
                this.idToken = extractField(response, "idToken");
                this.currentUserId = extractField(response, "localId");
                this.currentEmail = extractField(response, "email");
                return true;
            }
        } catch (Exception e) {
            System.err.println("Sign-in error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Registers a new user with email and password.
     * @return true if registration succeeded
     */
    public boolean signUp(String email, String password) {
        try {
            String body = String.format(
                "{\"email\":\"%s\",\"password\":\"%s\",\"returnSecureToken\":true}",
                email, password
            );
            String response = post(SIGN_UP_URL, body);
            if (response != null && response.contains("idToken")) {
                this.idToken = extractField(response, "idToken");
                this.currentUserId = extractField(response, "localId");
                this.currentEmail = extractField(response, "email");
                return true;
            }
        } catch (Exception e) {
            System.err.println("Sign-up error: " + e.getMessage());
        }
        return false;
    }

    public void signOut() {
        this.idToken = null;
        this.currentUserId = null;
        this.currentEmail = null;
    }

    public boolean isSignedIn() {
        return idToken != null;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public String getCurrentEmail() {
        return currentEmail;
    }

    public String getIdToken() {
        return idToken;
    }

    private String post(String urlStr, String body) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        int status = conn.getResponseCode();
        var stream = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();
        Scanner scanner = new Scanner(stream, StandardCharsets.UTF_8);
        String response = scanner.useDelimiter("\\A").next();
        scanner.close();

        if (status >= 400) {
            System.err.println("Firebase Auth error response: " + response);
            return null;
        }
        return response;
    }

    private String extractField(String json, String field) {
        String key = "\"" + field + "\":\"";
        int start = json.indexOf(key);
        if (start == -1) return null;
        start += key.length();
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }
}
