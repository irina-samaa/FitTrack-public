package com.fittrack.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import java.io.InputStream;

public class FirebaseConfig {

    public static void init() throws Exception {
        if (!FirebaseApp.getApps().isEmpty()) return;

        InputStream serviceAccount =
            FirebaseConfig.class.getResourceAsStream("/firebase-service-account.json");

        if (serviceAccount == null) {
            throw new RuntimeException("firebase-service-account.json not found in resources.");
        }

        FirebaseOptions options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build();

        FirebaseApp.initializeApp(options);
        System.out.println("Firebase initialized successfully.");
    }
}
