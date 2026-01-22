package com.example.mad_project.models;

public class User {
    private String userId;
    private String username;
    private String email;
    private String bio;
    private String profileImageUrl;
    private long timestamp;

    // Empty constructor for Firebase
    public User() {
    }

    // Constructor with parameters
    public User(String userId, String username, String email, String bio, String profileImageUrl, long timestamp) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.bio = bio;
        this.profileImageUrl = profileImageUrl;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}