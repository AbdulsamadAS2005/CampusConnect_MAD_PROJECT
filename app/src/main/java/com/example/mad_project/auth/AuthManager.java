package com.example.mad_project.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class AuthManager {

    private static final String PREF_NAME = "CampusConnectPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USERNAME = "username";

    private final Context context;
    private final FirebaseAuth firebaseAuth;
    private final SharedPreferences preferences;

    public AuthManager(Context context) {
        this.context = context;
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void login(String email, String password, AuthCallback callback) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        saveUserSession(user.getUid(), user.getEmail(), user.getDisplayName());
                        callback.onSuccess("Login successful!");
                    } else {
                        callback.onFailure("User not found");
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onFailure("Login failed: " + e.getMessage());
                });
    }

    public void register(String username, String email, String password, AuthCallback callback) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        saveUserSession(user.getUid(), user.getEmail(), username);
                        createUserInDatabase(user.getUid(), username, email, callback);
                    } else {
                        callback.onFailure("User creation failed");
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onFailure("Registration failed: " + e.getMessage());
                });
    }

    private void createUserInDatabase(String userId, String username, String email, AuthCallback callback) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("userId", userId);
        userMap.put("username", username);
        userMap.put("email", email);
        userMap.put("bio", "Hey there! I'm using CampusConnect");
        userMap.put("profileImageUrl", "");
        userMap.put("timestamp", System.currentTimeMillis());

        usersRef.setValue(userMap)
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess("Registration successful!");
                })
                .addOnFailureListener(e -> {
                    // Still success even if database fails
                    callback.onSuccess("Registration successful! (User created in auth)");
                });
    }

    private void saveUserSession(String userId, String email, String username) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USERNAME, username != null ? username : "User");
        editor.apply();
    }

    public boolean isLoggedIn() {
        // Check both preferences AND Firebase user
        boolean hasPrefs = preferences.getBoolean(KEY_IS_LOGGED_IN, false);
        boolean hasFirebaseUser = firebaseAuth.getCurrentUser() != null;
        return hasPrefs && hasFirebaseUser;
    }

    public String getCurrentUserId() {
        return preferences.getString(KEY_USER_ID, null);
    }

    public String getCurrentUserEmail() {
        return preferences.getString(KEY_USER_EMAIL, null);
    }

    public String getCurrentUsername() {
        return preferences.getString(KEY_USERNAME, "User");
    }

    public void logout() {
        firebaseAuth.signOut();
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }

    public FirebaseUser getFirebaseUser() {
        return firebaseAuth.getCurrentUser();
    }

    // Add this method to update username in preferences (used by ProfileActivity)
    public void updateUsername(String newUsername) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_USERNAME, newUsername);
        editor.apply();
    }

    public interface AuthCallback {
        void onSuccess(String message);
        void onFailure(String error);
    }
}