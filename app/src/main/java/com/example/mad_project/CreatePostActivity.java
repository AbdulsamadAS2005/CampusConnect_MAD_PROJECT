package com.example.mad_project;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.bumptech.glide.Glide;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class CreatePostActivity extends AppCompatActivity {

    // UI Components
    private Spinner spinnerCategory;
    private EditText etTitle, etContent;
    private Button btnAttachImage, btnPost, btnRemoveImage;
    private ImageView ivBack, ivImagePreview;
    private TextView tvImageName;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private DatabaseReference postsRef, usersRef;
    private StorageReference storageRef;

    // Image Handling
    private static final int PICK_IMAGE_REQUEST = 100;
    private Uri imageUri = null;
    private String imageUrl = "";

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
        storageRef = FirebaseStorage.getInstance().getReference("post_images");

        // Bind views
        spinnerCategory = findViewById(R.id.spinner_category);
        etTitle = findViewById(R.id.et_title);
        etContent = findViewById(R.id.et_content);
        btnAttachImage = findViewById(R.id.btn_attach_image);
        btnPost = findViewById(R.id.btn_post);
        btnRemoveImage = findViewById(R.id.btn_remove_image);
        ivBack = findViewById(R.id.iv_back);
        ivImagePreview = findViewById(R.id.iv_image_preview);
        tvImageName = findViewById(R.id.tv_image_name);
        progressBar = findViewById(R.id.progressBar);

        // Setup category spinner
        setupCategorySpinner();

        // Back button
        ivBack.setOnClickListener(v -> finish());

        // Attach image button
        btnAttachImage.setOnClickListener(v -> openImageChooser());

        // Remove image button
        btnRemoveImage.setOnClickListener(v -> removeSelectedImage());

        // Post button
        btnPost.setOnClickListener(v -> createPost());

        // Initially hide remove button
        btnRemoveImage.setVisibility(View.GONE);
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

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void removeSelectedImage() {
        imageUri = null;
        imageUrl = "";
        ivImagePreview.setVisibility(View.GONE);
        tvImageName.setText("No image selected");
        btnRemoveImage.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            // Get file name
            String fileName = getFileNameFromUri(imageUri);
            tvImageName.setText(fileName);

            // Show image preview
            ivImagePreview.setVisibility(View.VISIBLE);
            Glide.with(this).load(imageUri).into(ivImagePreview);

            // Show remove button
            btnRemoveImage.setVisibility(View.VISIBLE);
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(android.provider.OpenableColumns.DISPLAY_NAME));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result != null ? result : "image.jpg";
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

        // If image is selected, upload it first
        if (imageUri != null) {
            uploadImageAndCreatePost(category, title, content);
        } else {
            createPostInDatabase(category, title, content, "");
        }
    }

    private void uploadImageAndCreatePost(String category, String title, String content) {
        // Create unique filename
        String filename = UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storageRef.child(filename);

        // Upload image
        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get download URL
                    imageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                imageUrl = uri.toString();
                                createPostInDatabase(category, title, content, imageUrl);
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                btnPost.setEnabled(true);
                                Toast.makeText(CreatePostActivity.this,
                                        "Failed to get image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnPost.setEnabled(true);
                    Toast.makeText(CreatePostActivity.this,
                            "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void createPostInDatabase(String category, String title, String content, String imageUrl) {
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

                if (dataSnapshot.exists()) {
                    username = dataSnapshot.child("username").getValue(String.class);
                    if (username == null || username.isEmpty()) {
                        username = currentUser.getDisplayName() != null ?
                                currentUser.getDisplayName() : "User";
                    }
                    userBio = dataSnapshot.child("bio").getValue(String.class);
                    if (userBio == null) userBio = "";
                }

                // Create post object
                Post post = new Post(
                        postId,
                        currentUser.getUid(),
                        username,
                        userBio,
                        currentUser.getEmail(),
                        title,
                        content,
                        category,
                        imageUrl,
                        0,  // likes count
                        0,  // comments count
                        timestamp,
                        formattedDate
                );

                // Save to Firebase
