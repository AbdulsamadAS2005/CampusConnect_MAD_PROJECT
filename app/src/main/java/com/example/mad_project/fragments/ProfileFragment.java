package com.example.mad_project.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mad_project.LoginActivity;
import com.example.mad_project.R;
import com.example.mad_project.auth.AuthManager;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class ProfileFragment extends Fragment {

    private TextView tvUserId, tvEmail;
    private EditText etUsername, etBio;
    private Button btnUpdate, btnLogout;
    private ProgressBar progressBar;

    private AuthManager authManager;
    private DatabaseReference usersRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        authManager = new AuthManager(requireContext());
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Initialize views
        tvUserId = view.findViewById(R.id.tv_userId);
        tvEmail = view.findViewById(R.id.tv_email);
        etUsername = view.findViewById(R.id.et_username);
        etBio = view.findViewById(R.id.et_bio);
        btnUpdate = view.findViewById(R.id.btn_update);
        btnLogout = view.findViewById(R.id.btn_logout);
        progressBar = view.findViewById(R.id.progressBar);

        // Set current email
        String email = authManager.getCurrentUserEmail();
        if (email != null) {
            tvEmail.setText(email);
        }

        // Load user data
        loadUserData();

        // Update button
        btnUpdate.setOnClickListener(v -> updateProfile());

        // Logout button
        btnLogout.setOnClickListener(v -> {
            authManager.logout();
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
        });

        return view;
    }

    private void loadUserData() {
        FirebaseUser firebaseUser = authManager.getFirebaseUser();
        if (firebaseUser == null) return;

        progressBar.setVisibility(View.VISIBLE);

        usersRef.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);

                if (dataSnapshot.exists()) {
                    String userId = dataSnapshot.child("userId").getValue(String.class);
                    String username = dataSnapshot.child("username").getValue(String.class);
                    String bio = dataSnapshot.child("bio").getValue(String.class);

                    tvUserId.setText(userId != null ? userId : "N/A");
                    etUsername.setText(username != null ? username : "User");
                    etBio.setText(bio != null ? bio : "");
                } else {
                    // Create default profile
                    createDefaultProfile(firebaseUser);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createDefaultProfile(FirebaseUser user) {
        tvUserId.setText(user.getUid());
        etUsername.setText(user.getDisplayName() != null ? user.getDisplayName() : "User");
        etBio.setText("Hey there! I'm using CampusConnect");

        // Save to database
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("userId", user.getUid());
        userMap.put("username", etUsername.getText().toString());
        userMap.put("email", user.getEmail());
        userMap.put("bio", etBio.getText().toString());
        userMap.put("profileImageUrl", "");
        userMap.put("timestamp", System.currentTimeMillis());

        usersRef.child(user.getUid()).setValue(userMap);
    }

    private void updateProfile() {
        String username = etUsername.getText().toString().trim();
        String bio = etBio.getText().toString().trim();
        FirebaseUser user = authManager.getFirebaseUser();

        if (user == null) return;

        if (username.isEmpty()) {
            etUsername.setError("Username is required");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnUpdate.setEnabled(false);

        HashMap<String, Object> updates = new HashMap<>();
        updates.put("username", username);
        updates.put("bio", bio);

        usersRef.child(user.getUid()).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    btnUpdate.setEnabled(true);
                    Toast.makeText(requireContext(), "Profile updated!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnUpdate.setEnabled(true);
                    Toast.makeText(requireContext(), "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}