package com.example.mad_project.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mad_project.ChatActivity;
import com.example.mad_project.ProfileActivity;
import com.example.mad_project.R;
import com.example.mad_project.adapters.PostAdapter;
import com.example.mad_project.auth.AuthManager;
import com.example.mad_project.managers.FollowManager;
import com.example.mad_project.models.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SocialProfileFragment extends Fragment {

    private TextView tvProfileName, tvProfileEmail, tvProfileBio;
    private TextView tvPostsCount, tvFollowingCount, tvFollowersCount;
    private TextView tvNoPosts;
    private Button btnEditProfile, btnMessage, btnFollow;
    private ProgressBar progressBar;
    private RecyclerView recyclerViewMyPosts;

    private AuthManager authManager;
    private FollowManager followManager;
    private DatabaseReference usersRef, postsRef;
    private PostAdapter postAdapter;
    private List<Post> myPostsList;

    private FirebaseUser currentUser;
    private String profileUserId;
    private boolean isFollowing = false;
    private boolean isOwnProfile = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_social_profile, container, false);

        // Initialize Firebase Auth
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return view;
        }

        authManager = new AuthManager(requireContext());
        followManager = new FollowManager();

        // Default to current user's profile
        if (getArguments() != null) {
            // If viewing another user's profile
            profileUserId = getArguments().getString("userId", currentUser.getUid());
            isOwnProfile = profileUserId.equals(currentUser.getUid());
        } else {
            // Default to current user's profile
            profileUserId = currentUser.getUid();
            isOwnProfile = true;
        }

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        postsRef = FirebaseDatabase.getInstance().getReference("posts");

        // Initialize ALL views
        tvProfileName = view.findViewById(R.id.tv_profile_name);
        tvProfileEmail = view.findViewById(R.id.tv_profile_email);
        tvProfileBio = view.findViewById(R.id.tv_profile_bio);
        tvPostsCount = view.findViewById(R.id.tv_posts_count);
        tvFollowingCount = view.findViewById(R.id.tv_following_count);
        tvFollowersCount = view.findViewById(R.id.tv_followers_count);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        btnMessage = view.findViewById(R.id.btn_message);
        btnFollow = view.findViewById(R.id.btn_follow);
        tvNoPosts = view.findViewById(R.id.tv_no_posts);
        progressBar = view.findViewById(R.id.progressBar);
        recyclerViewMyPosts = view.findViewById(R.id.recyclerViewMyPosts);

        // Setup RecyclerView for posts
        myPostsList = new ArrayList<>();
        postAdapter = new PostAdapter(requireContext(), myPostsList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        recyclerViewMyPosts.setLayoutManager(layoutManager);
        recyclerViewMyPosts.setAdapter(postAdapter);

        // Button visibility logic
        if (isOwnProfile) {
            // Own profile: Show Edit, Hide Message and Follow
            btnEditProfile.setVisibility(View.VISIBLE);
            btnMessage.setVisibility(View.GONE);
            btnFollow.setVisibility(View.GONE);
            btnEditProfile.setText("Edit Profile");
        } else {
            // Other user's profile: Hide Edit, Show Message and Follow
            btnEditProfile.setVisibility(View.GONE);
            btnMessage.setVisibility(View.VISIBLE);
            btnFollow.setVisibility(View.VISIBLE);
            checkFollowStatus();
        }

        // Load user profile data
        loadUserProfile();

        // Load user's posts
        loadUserPosts();

        // Load follower/following counts
        loadFollowCounts();

        // Edit Profile button
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ProfileActivity.class);
            startActivity(intent);
        });

        // Message button
        btnMessage.setOnClickListener(v -> {
            // Start chat with this user
            Intent intent = new Intent(requireContext(), ChatActivity.class);
            intent.putExtra("otherUserId", profileUserId);
            intent.putExtra("otherUserName", tvProfileName.getText().toString());
            startActivity(intent);
        });

        // Follow button
        btnFollow.setOnClickListener(v -> {
            toggleFollow();
        });

        return view;
    }

    private void loadUserProfile() {
        if (profileUserId == null) return;

        progressBar.setVisibility(View.VISIBLE);

        usersRef.child(profileUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String username = dataSnapshot.child("username").getValue(String.class);
                    String email = dataSnapshot.child("email").getValue(String.class);
                    String bio = dataSnapshot.child("bio").getValue(String.class);

                    tvProfileName.setText(username != null ? username : "User");
                    tvProfileEmail.setText(email != null ? email : "No email");
                    tvProfileBio.setText(bio != null ? bio : "No bio yet");
                } else {
                    // Default values
                    tvProfileName.setText("User");
                    tvProfileEmail.setText("No email");
                    tvProfileBio.setText("No bio yet");
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFollowCounts() {
        if (profileUserId == null) return;

        usersRef.child(profileUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Get following count
                    long followingCount = 0;
                    if (dataSnapshot.child("following").exists()) {
                        followingCount = dataSnapshot.child("following").getChildrenCount();
                    }
                    tvFollowingCount.setText(String.valueOf(followingCount));

                    // Get followers count
                    long followersCount = 0;
                    if (dataSnapshot.child("followers").exists()) {
                        followersCount = dataSnapshot.child("followers").getChildrenCount();
                    }
                    tvFollowersCount.setText(String.valueOf(followersCount));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Silent fail
            }
        });
    }

    private void loadUserPosts() {
        if (profileUserId == null) return;

        progressBar.setVisibility(View.VISIBLE);
        tvNoPosts.setVisibility(View.GONE);
        recyclerViewMyPosts.setVisibility(View.VISIBLE);

        postsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myPostsList.clear();
                int postCount = 0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);
                    if (post != null) {
                        // Check if this post belongs to the current user
                        if (profileUserId.equals(post.getUserId())) {
                            // Set post ID from snapshot key
                            if (post.getPostId() == null) {
                                post.setPostId(snapshot.getKey());
                            }
                            myPostsList.add(post);
                            postCount++;
                        }
                    }
                }

                // Update posts count
                tvPostsCount.setText(String.valueOf(postCount));

                // Sort by timestamp (newest first)
                Collections.sort(myPostsList, (p1, p2) ->
                        Long.compare(p2.getTimestamp(), p1.getTimestamp()));

                // Update adapter
                postAdapter.notifyDataSetChanged();

                // Show/hide empty state
                if (myPostsList.isEmpty()) {
                    tvNoPosts.setText("No posts yet. Create your first post!");
                    tvNoPosts.setVisibility(View.VISIBLE);
                    recyclerViewMyPosts.setVisibility(View.GONE);
                } else {
                    tvNoPosts.setVisibility(View.GONE);
                    recyclerViewMyPosts.setVisibility(View.VISIBLE);
                }

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                tvNoPosts.setText("Failed to load posts");
                tvNoPosts.setVisibility(View.VISIBLE);
                recyclerViewMyPosts.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Failed to load posts", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkFollowStatus() {
        if (currentUser == null || profileUserId.equals(currentUser.getUid())) return;

        followManager.checkFollowingStatus(profileUserId, new FollowManager.FollowStatusCallback() {
            @Override
            public void onResult(boolean isFollowing) {
                SocialProfileFragment.this.isFollowing = isFollowing;
                updateFollowButtonUI();
            }
        });
    }

    private void updateFollowButtonUI() {
        if (isFollowing) {
            btnFollow.setText("Following");
            btnFollow.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
            btnFollow.setTextColor(getResources().getColor(R.color.white));
        } else {
            btnFollow.setText("Follow");
            btnFollow.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
            btnFollow.setTextColor(getResources().getColor(R.color.black));
        }
    }

    private void toggleFollow() {
        if (currentUser == null || profileUserId.equals(currentUser.getUid())) return;

        if (isFollowing) {
            // Unfollow
            followManager.unfollowUser(profileUserId);
            isFollowing = false;
            Toast.makeText(requireContext(), "Unfollowed", Toast.LENGTH_SHORT).show();
        } else {
            // Follow
            followManager.followUser(profileUserId);
            isFollowing = true;
            Toast.makeText(requireContext(), "Followed", Toast.LENGTH_SHORT).show();
        }

        updateFollowButtonUI();
        loadFollowCounts(); // Refresh counts
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when fragment resumes
        if (profileUserId != null) {
            loadUserProfile();
            // Don't reload posts here since we're using addValueEventListener
            loadFollowCounts();
            if (!isOwnProfile) {
                checkFollowStatus();
            }
        }
    }
}