package com.example.mad_project.models;

import java.util.ArrayList;
import java.util.List;

public class Post {
    private String postId;
    private String userId;
    private String username;
    private String userBio;
    private String userEmail;
    private String title;
    private String content;
    private String category;
    private String imageUrl; // Keep but will be empty
    private int likesCount;
    private int commentsCount;
    private long timestamp;
    private String formattedDate;
    private List<String> likedBy;

    // Empty constructor for Firebase
    public Post() {
        likedBy = new ArrayList<>();
    }

    // Full constructor (imageUrl will be empty string)
    public Post(String postId, String userId, String username, String userBio,
                String userEmail, String title, String content, String category,
                String imageUrl, int likesCount, int commentsCount,
                long timestamp, String formattedDate) {
        this.postId = postId;
        this.userId = userId;
        this.username = username;
        this.userBio = userBio;
        this.userEmail = userEmail;
        this.title = title;
        this.content = content;
        this.category = category;
        this.imageUrl = imageUrl != null ? imageUrl : "";
        this.likesCount = likesCount;
        this.commentsCount = commentsCount;
        this.timestamp = timestamp;
        this.formattedDate = formattedDate;
        this.likedBy = new ArrayList<>();
    }

    // Getters and Setters (keep as is)
    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getUserBio() { return userBio; }
    public void setUserBio(String userBio) { this.userBio = userBio; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getLikesCount() { return likesCount; }
    public void setLikesCount(int likesCount) { this.likesCount = likesCount; }

    public int getCommentsCount() { return commentsCount; }
    public void setCommentsCount(int commentsCount) { this.commentsCount = commentsCount; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getFormattedDate() { return formattedDate; }
    public void setFormattedDate(String formattedDate) { this.formattedDate = formattedDate; }

    public List<String> getLikedBy() { return likedBy; }
    public void setLikedBy(List<String> likedBy) { this.likedBy = likedBy; }
}