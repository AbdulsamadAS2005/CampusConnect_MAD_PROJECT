package com.example.mad_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class ProfileActivity extends AppCompatActivity {

    // UI Components
    private TextView tvUserId, tvEmail;
    private EditText etUsername, etBio;
    private Button btnUpdate, btnLogout;
    private ImageView ivBack;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Check if user is logged in
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Bind views
        tvUserId = findViewById(R.id.tv_userId);
        tvEmail = findViewById(R.id.tv_email);
        etUsername = findViewById(R.id.et_username);
        etBio = findViewById(R.id.et_bio);
        btnUpdate = findViewById(R.id.btn_update);
        btnLogout = findViewById(R.id.btn_logout);
        ivBack = findViewById(R.id.iv_back);
        progressBar = findViewById(R.id.progressBar);

        // Set user ID and email
        tvUserId.setText(currentUser.getUid());
        tvEmail.setText(currentUser.getEmail());

        // Load user profile data
        loadUserProfile();

        // Back button
        ivBack.setOnClickListener(v -> finish());

        // Update profile button
        btnUpdate.setOnClickListener(v -> updateProfile());

        // Logout button
        btnLogout.setOnClickListener(v -> logoutUser());
    }

    private void loadUserProfile() {
        progressBar.setVisibility(View.VISIBLE);

        usersRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);

                if (dataSnapshot.exists()) {
                    // Load username
                    String username = dataSnapshot.child("username").getValue(String.class);
                    if (username != null && !username.isEmpty()) {
                        etUsername.setText(username);
                    } else {
                        etUsername.setText(currentUser.getDisplayName() != null ?
                                currentUser.getDisplayName() : "User" + currentUser.getUid().substring(0, 6));
                    }

                    // Load bio
                    String bio = dataSnapshot.child("bio").getValue(String.class);
                    if (bio != null && !bio.isEmpty()) {
                        etBio.setText(bio);
                    }
                } else {
                    // First time user - set default username
                    String defaultUsername = currentUser.getDisplayName() != null ?
                            currentUser.getDisplayName() : "User" + currentUser.getUid().substring(0, 6);
                    etUsername.setText(defaultUsername);

                    // Save initial user data
                    saveUserProfile();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ProfileActivity.this, "Failed to load profile: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProfile() {
        String username = etUsername.getText().toString().trim();
        String bio = etBio.getText().toString().trim();

        if (username.isEmpty()) {
            Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnUpdate.setEnabled(false);

        // Create user data object
        UserProfile userProfile = new UserProfile(
                currentUser.getUid(),
                username,
                currentUser.getEmail(),
                bio,
                System.currentTimeMillis()
        );

        // Save to Firebase
        usersRef.child(currentUser.getUid()).setValue(userProfile)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    btnUpdate.setEnabled(true);
                    Toast.makeText(ProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnUpdate.setEnabled(true);
                    Toast.makeText(ProfileActivity.this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveUserProfile() {
        String username = etUsername.getText().toString().trim();
        String bio = etBio.getText().toString().trim();

        UserProfile userProfile = new UserProfile(
                currentUser.getUid(),
                username,
                currentUser.getEmail(),
                bio,
                System.currentTimeMillis()
        );

        usersRef.child(currentUser.getUid()).setValue(userProfile)
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void logoutUser() {
        auth.signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    // User Profile Model Class
    public static class UserProfile {
        private String userId;
        private String username;
        private String email;
        private String bio;
        private long timestamp;

        public UserProfile() {
            // Default constructor required for Firebase
        }

        public UserProfile(String userId, String username, String email, String bio, long timestamp) {
            this.userId = userId;
            this.username = username;
            this.email = email;
            this.bio = bio;
            this.timestamp = timestamp;
        }

        // Getters and Setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getBio() { return bio; }
        public void setBio(String bio) { this.bio = bio; }

        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
}