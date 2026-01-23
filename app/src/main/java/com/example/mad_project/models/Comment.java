package com.example.mad_project.models;

public class Comment {
    private String commentId;
    private String postId;
    private String userId;
    private String username;
    private String userEmail;
    private String commentText;
    private long timestamp;
    private String formattedDate;

    // Empty constructor for Firebase
    public Comment() {
    }

    // Constructor
    public Comment(String commentId, String postId, String userId, String username,
                   String userEmail, String commentText, long timestamp, String formattedDate) {
        this.commentId = commentId;
        this.postId = postId;
        this.userId = userId;
        this.username = username;
        this.userEmail = userEmail;
        this.commentText = commentText;
        this.timestamp = timestamp;
        this.formattedDate = formattedDate;
    }

    // Getters and Setters
    public String getCommentId() { return commentId; }
    public void setCommentId(String commentId) { this.commentId = commentId; }

    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getCommentText() { return commentText; }
    public void setCommentText(String commentText) { this.commentText = commentText; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getFormattedDate() { return formattedDate; }
    public void setFormattedDate(String formattedDate) { this.formattedDate = formattedDate; }
}