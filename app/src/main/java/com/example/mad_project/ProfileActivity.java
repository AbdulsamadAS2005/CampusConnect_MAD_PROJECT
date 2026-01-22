package com.example.mad_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mad_project.auth.AuthManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    // UI Components
    private TextView tvUserId, tvEmail;
    private EditText etUsername, etBio;
    private Button btnUpdate, btnLogout;
    private ImageView ivBack;
    private ProgressBar progressBar;

    // Auth & Database
    private AuthManager authManager;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference firebaseUsersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize
        authManager = new AuthManager(this);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        // Check if user is logged in
        if (!authManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Initialize Firebase
        try {
            firebaseUsersRef = FirebaseDatabase.getInstance().getReference("users");
        } catch (Exception e) {
            Toast.makeText(this, "Firebase database unavailable", Toast.LENGTH_SHORT).show();
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

        // Set current email
        String currentEmail = authManager.getCurrentUserEmail();
        if (currentEmail != null) {
            tvEmail.setText(currentEmail);
        }

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

        if (firebaseUser == null) {
            // No Firebase user, show basic info
            progressBar.setVisibility(View.GONE);
            tvUserId.setText("Not Available");
            etUsername.setText(authManager.getCurrentUsername());
            etBio.setText("User profile");
            return;
        }

        // Always load from Firebase (since we're using Firebase Auth)
        firebaseUsersRef.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);

                if (dataSnapshot.exists()) {
                    // User exists in database
                    String userId = dataSnapshot.child("userId").getValue(String.class);
                    String username = dataSnapshot.child("username").getValue(String.class);
                    String bio = dataSnapshot.child("bio").getValue(String.class);

                    // Set the user ID (use Firebase UID if not in database)
                    tvUserId.setText(userId != null ? userId : firebaseUser.getUid());

                    // Set username (use stored or fallback to auth manager)
                    String displayUsername = username != null ? username : authManager.getCurrentUsername();
                    etUsername.setText(displayUsername);

                    // Set bio (or default if empty)
                    String displayBio = bio != null ? bio : "Hey there! I'm using CampusConnect";
                    etBio.setText(displayBio);
                } else {
                    // User doesn't exist in database, create default
                    createDefaultProfile();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                // Show basic info on error
                tvUserId.setText(firebaseUser != null ? firebaseUser.getUid() : "N/A");
                etUsername.setText(authManager.getCurrentUsername());
                etBio.setText("Unable to load profile");
                Toast.makeText(ProfileActivity.this, "Failed to load profile data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createDefaultProfile() {
        if (firebaseUser == null) return;

        String defaultUsername = authManager.getCurrentUsername();
        String defaultBio = "Hey there! I'm using CampusConnect";

        tvUserId.setText(firebaseUser.getUid());
        etUsername.setText(defaultUsername);
        etBio.setText(defaultBio);

        // Save default profile to Firebase
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("userId", firebaseUser.getUid());
        userMap.put("username", defaultUsername);
        userMap.put("email", firebaseUser.getEmail());
        userMap.put("bio", defaultBio);
        userMap.put("profileImageUrl", "");
        userMap.put("timestamp", System.currentTimeMillis());

        firebaseUsersRef.child(firebaseUser.getUid()).setValue(userMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ProfileActivity.this, "Default profile created", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfileActivity.this, "Note: Profile saved locally only", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateProfile() {
        String username = etUsername.getText().toString().trim();
        String bio = etBio.getText().toString().trim();

        if (username.isEmpty()) {
            Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (firebaseUser == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnUpdate.setEnabled(false);

        // Update in Firebase
        HashMap<String, Object> updates = new HashMap<>();
        updates.put("username", username);
        updates.put("bio", bio);

        firebaseUsersRef.child(firebaseUser.getUid()).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    btnUpdate.setEnabled(true);

                    // Update username in AuthManager preferences
                    authManager.updateUsername(username);

                    Toast.makeText(ProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnUpdate.setEnabled(true);
                    Toast.makeText(ProfileActivity.this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void logoutUser() {
        authManager.logout();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh user data when coming back to profile
        if (authManager.isLoggedIn()) {
            String email = authManager.getCurrentUserEmail();
            if (email != null) {
                tvEmail.setText(email);
            }
        }
    }
}