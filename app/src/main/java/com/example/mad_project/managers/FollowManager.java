package com.example.mad_project.managers;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class FollowManager {

    private DatabaseReference usersRef;
    private FirebaseUser currentUser;

    public FollowManager() {
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    public void followUser(String targetUserId) {
        if (currentUser == null || targetUserId == null) return;

        String currentUserId = currentUser.getUid();

        // Update current user's following list
        usersRef.child(currentUserId).child("following").addListenerForSingleValueEvent(
                new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                        List<String> following = new ArrayList<>();
                        if (dataSnapshot.exists()) {
                            for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String userId = snapshot.getValue(String.class);
                                if (userId != null) {
                                    following.add(userId);
                                }
                            }
                        }

                        // Add target user if not already following
                        if (!following.contains(targetUserId)) {
                            following.add(targetUserId);
                        }

                        usersRef.child(currentUserId).child("following").setValue(following);

                        // Update target user's followers list
                        updateTargetFollowers(targetUserId, currentUserId, true);
                    }

                    @Override
                    public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                    }
                });
    }

    public void unfollowUser(String targetUserId) {
        if (currentUser == null || targetUserId == null) return;

        String currentUserId = currentUser.getUid();

        // Update current user's following list
        usersRef.child(currentUserId).child("following").addListenerForSingleValueEvent(
                new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                        List<String> following = new ArrayList<>();
                        if (dataSnapshot.exists()) {
                            for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String userId = snapshot.getValue(String.class);
                                if (userId != null && !userId.equals(targetUserId)) {
                                    following.add(userId);
                                }
                            }
                        }

                        usersRef.child(currentUserId).child("following").setValue(following);

                        // Update target user's followers list
                        updateTargetFollowers(targetUserId, currentUserId, false);
                    }

                    @Override
                    public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                    }
                });
    }

    private void updateTargetFollowers(String targetUserId, String followerId, boolean isFollow) {
        usersRef.child(targetUserId).child("followers").addListenerForSingleValueEvent(
                new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                        List<String> followers = new ArrayList<>();
                        if (dataSnapshot.exists()) {
                            for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String userId = snapshot.getValue(String.class);
                                if (userId != null) {
                                    followers.add(userId);
                                }
                            }
                        }

                        if (isFollow) {
                            // Add follower
                            if (!followers.contains(followerId)) {
                                followers.add(followerId);
                            }
                        } else {
                            // Remove follower
                            followers.remove(followerId);
                        }

                        usersRef.child(targetUserId).child("followers").setValue(followers);
                    }

                    @Override
                    public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                    }
                });
    }

    public void checkFollowingStatus(String targetUserId, FollowStatusCallback callback) {
        if (currentUser == null) {
            callback.onResult(false);
            return;
        }

        String currentUserId = currentUser.getUid();

        usersRef.child(currentUserId).child("following").addListenerForSingleValueEvent(
                new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                        boolean isFollowing = false;
                        if (dataSnapshot.exists()) {
                            for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String userId = snapshot.getValue(String.class);
                                if (userId != null && userId.equals(targetUserId)) {
                                    isFollowing = true;
                                    break;
                                }
                            }
                        }
                        callback.onResult(isFollowing);
                    }

                    @Override
                    public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                        callback.onResult(false);
                    }
                });
    }

    public interface FollowStatusCallback {
        void onResult(boolean isFollowing);
    }
}