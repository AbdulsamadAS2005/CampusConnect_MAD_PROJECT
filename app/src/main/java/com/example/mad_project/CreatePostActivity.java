package com.example.mad_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mad_project.models.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreatePostActivity extends AppCompatActivity {

    // UI Components
    private Spinner spinnerCategory;
    private EditText etTitle, etContent;
    private Button btnPost;
    private ImageView ivBack;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private DatabaseReference postsRef, usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        postsRef = FirebaseDatabase.getInstance().getReference("posts");
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Bind views
        spinnerCategory = findViewById(R.id.spinner_category);
        etTitle = findViewById(R.id.et_title);
        etContent = findViewById(R.id.et_content);
        btnPost = findViewById(R.id.btn_post);
        ivBack = findViewById(R.id.iv_back);
        progressBar = findViewById(R.id.progressBar);

        // Remove image-related components (they won't exist in layout)
        // Or keep them but don't use them

        // Setup category spinner
        setupCategorySpinner();

        // Back button
        ivBack.setOnClickListener(v -> finish());

        // Post button
        btnPost.setOnClickListener(v -> createPost());
    }

    private void setupCategorySpinner() {
        // Create adapter for categories
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.post_categories,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void createPost() {
        String category = spinnerCategory.getSelectedItem().toString();
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        // Validation
        if (content.isEmpty()) {
            Toast.makeText(this, "Please enter post content", Toast.LENGTH_SHORT).show();
            return;
        }

        if (title.isEmpty()) {
            title = "No Title";
        }

        // Disable button and show progress
        btnPost.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        // Create post without image
        createPostInDatabase(category, title, content);
    }

    private void createPostInDatabase(String category, String title, String content) {
        String postId = postsRef.push().getKey();
        long timestamp = System.currentTimeMillis();

        // Format date for display
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        String formattedDate = sdf.format(new Date(timestamp));

        // First, get user info
        usersRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                String username = "User";
                String userBio = "";
                String userEmail = currentUser.getEmail() != null ? currentUser.getEmail() : "";

                if (dataSnapshot.exists()) {
                    username = dataSnapshot.child("username").getValue(String.class);
                    if (username == null || username.isEmpty()) {
                        username = currentUser.getDisplayName() != null ?
                                currentUser.getDisplayName() : "User";
                    }
                    userBio = dataSnapshot.child("bio").getValue(String.class);
                    if (userBio == null) userBio = "";
                }

                // Create post object WITHOUT image
                Post post = new Post(
                        postId,
                        currentUser.getUid(),
                        username,
                        userBio,
                        userEmail,
                        title,
                        content,
                        category,
                        "",  // No image URL
                        0,   // likes count
                        0,   // comments count
                        timestamp,
                        formattedDate
                );

                // Save to Firebase
                if (postId != null) {
                    postsRef.child(postId).setValue(post)
                            .addOnSuccessListener(aVoid -> {
                                progressBar.setVisibility(View.GONE);
                                btnPost.setEnabled(true);
                                Toast.makeText(CreatePostActivity.this,
                                        "Post created successfully!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                btnPost.setEnabled(true);
                                Toast.makeText(CreatePostActivity.this,
                                        "Failed to create post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            }

            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                btnPost.setEnabled(true);
                Toast.makeText(CreatePostActivity.this,
                        "Failed to load user data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}