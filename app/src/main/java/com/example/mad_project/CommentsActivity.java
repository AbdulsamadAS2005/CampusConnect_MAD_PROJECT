package com.example.mad_project;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mad_project.adapters.CommentAdapter;
import com.example.mad_project.models.Comment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewComments;
    private EditText etComment;
    private TextView btnPostComment, tvNoComments;
    private ImageView ivBack;
    private ProgressBar progressBar;

    private CommentAdapter commentAdapter;
    private List<Comment> commentList;

    private DatabaseReference commentsRef, usersRef;
    private FirebaseUser currentUser;
    private String postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        // Get postId from intent
        postId = getIntent().getStringExtra("postId");
        if (postId == null || postId.isEmpty()) {
            Toast.makeText(this, "Error: Post not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please login to comment", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase
        commentsRef = FirebaseDatabase.getInstance().getReference("comments").child(postId);
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Initialize views
        recyclerViewComments = findViewById(R.id.recyclerViewComments);
        etComment = findViewById(R.id.et_comment);
        btnPostComment = findViewById(R.id.btn_post_comment);
        tvNoComments = findViewById(R.id.tv_no_comments);
        ivBack = findViewById(R.id.iv_back);
        progressBar = findViewById(R.id.progressBar);

        // Setup RecyclerView
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, commentList);
        recyclerViewComments.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewComments.setAdapter(commentAdapter);

        // Load comments
        loadComments();

        // Back button
        ivBack.setOnClickListener(v -> finish());

        // Post comment button
        btnPostComment.setOnClickListener(v -> postComment());
    }

    private void loadComments() {
        progressBar.setVisibility(View.VISIBLE);

        commentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Comment comment = snapshot.getValue(Comment.class);
                    if (comment != null) {
                        if (comment.getCommentId() == null) {
                            comment.setCommentId(snapshot.getKey());
                        }
                        commentList.add(comment);
                    }
                }

                // Sort by timestamp (newest first)
                commentList.sort((c1, c2) -> Long.compare(c2.getTimestamp(), c1.getTimestamp()));
                commentAdapter.notifyDataSetChanged();

                // Show/hide empty state
                if (commentList.isEmpty()) {
                    tvNoComments.setVisibility(View.VISIBLE);
                } else {
                    tvNoComments.setVisibility(View.GONE);
                }

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(CommentsActivity.this, "Failed to load comments", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void postComment() {
        String commentText = etComment.getText().toString().trim();

        if (TextUtils.isEmpty(commentText)) {
            Toast.makeText(this, "Please enter a comment", Toast.LENGTH_SHORT).show();
            return;
        }

        btnPostComment.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        // Get user info
        usersRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String username = "User";
                String userEmail = currentUser.getEmail() != null ? currentUser.getEmail() : "";

                if (dataSnapshot.exists()) {
                    username = dataSnapshot.child("username").getValue(String.class);
                    if (username == null || username.isEmpty()) {
                        username = currentUser.getDisplayName() != null ?
                                currentUser.getDisplayName() : "User";
                    }
                }

                // Create comment
                String commentId = commentsRef.push().getKey();
                long timestamp = System.currentTimeMillis();

                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
                String formattedDate = sdf.format(new Date(timestamp));

                Comment comment = new Comment(
                        commentId,
                        postId,
                        currentUser.getUid(),
                        username,
                        userEmail,
                        commentText,
                        timestamp,
                        formattedDate
                );

                // Save to Firebase
                if (commentId != null) {
                    commentsRef.child(commentId).setValue(comment)
                            .addOnSuccessListener(aVoid -> {
                                etComment.setText("");
                                btnPostComment.setEnabled(true);
                                progressBar.setVisibility(View.GONE);

                                // Update post comments count
                                updatePostCommentsCount();
                            })
                            .addOnFailureListener(e -> {
                                btnPostComment.setEnabled(true);
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(CommentsActivity.this,
                                        "Failed to post comment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                btnPostComment.setEnabled(true);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(CommentsActivity.this,
                        "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePostCommentsCount() {
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("posts").child(postId);

        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Integer currentCount = dataSnapshot.child("commentsCount").getValue(Integer.class);
                    int newCount = (currentCount != null ? currentCount : 0) + 1;
                    postRef.child("commentsCount").setValue(newCount);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Silent fail
            }
        });
    }
}