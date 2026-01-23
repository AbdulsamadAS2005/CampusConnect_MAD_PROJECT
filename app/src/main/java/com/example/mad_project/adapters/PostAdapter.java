package com.example.mad_project.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mad_project.CommentsActivity;
import com.example.mad_project.R;
import com.example.mad_project.models.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private final Context context;
    private final List<Post> postList;
    private final FirebaseUser currentUser;

    public PostAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList != null ? postList : new ArrayList<>();
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = postList.get(position);

        // Handle null post
        if (post == null) return;

        // Set user info with null checks
        String username = post.getUsername() != null ? post.getUsername() : "User";
        String email = post.getUserEmail() != null ? post.getUserEmail() : "user@email.com";
        String timestamp = post.getFormattedDate() != null ? post.getFormattedDate() : "Just now";

        holder.tvUsername.setText(username);
        holder.tvEmail.setText(email);
        holder.tvTimestamp.setText(timestamp);

        // Set bio if available
        if (post.getUserBio() != null && !post.getUserBio().isEmpty()) {
            holder.tvUserBio.setText(post.getUserBio());
            holder.tvUserBio.setVisibility(View.VISIBLE);
        } else {
            holder.tvUserBio.setVisibility(View.GONE);
        }

        // Set category with color - handle null category
        String category = post.getCategory() != null ? post.getCategory() : "General";
        holder.tvCategory.setText(category);

        // Set category background color
        int categoryColor;
        switch (category) {
            case "Announcements":
                categoryColor = ContextCompat.getColor(context, R.color.category_announcement);
                break;
            case "Study Help":
                categoryColor = ContextCompat.getColor(context, R.color.category_study);
                break;
            case "Events":
                categoryColor = ContextCompat.getColor(context, R.color.category_event);
                break;
            default:
                categoryColor = ContextCompat.getColor(context, R.color.category_general);
        }
        holder.tvCategory.setBackgroundColor(categoryColor);

        // Set title and content with null checks
        String title = post.getTitle() != null ? post.getTitle() : "No Title";
        String content = post.getContent() != null ? post.getContent() : "";

        holder.tvTitle.setText(title);
        holder.tvContent.setText(content);

        // Set likes count
        holder.tvLikesCount.setText(String.valueOf(post.getLikesCount()));

        // Set comments count
        holder.tvCommentsCount.setText(post.getCommentsCount() + " comments");

        // Load post image if exists
        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            holder.ivPostImage.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(post.getImageUrl())
                    .placeholder(android.R.color.darker_gray)
                    .centerCrop()
                    .into(holder.ivPostImage);
        } else {
            holder.ivPostImage.setVisibility(View.GONE);
        }

        // Check if current user liked this post
        if (currentUser != null && post.getLikedBy() != null &&
                post.getLikedBy().contains(currentUser.getUid())) {
            holder.ivLike.setImageResource(R.drawable.ic_like_filled);
        } else {
            holder.ivLike.setImageResource(R.drawable.ic_like_outline);
        }

        // Set click listeners
        holder.ivLike.setOnClickListener(v -> handleLikeClick(post, holder, position));

        // FIXED: Pass the post parameter to handleCommentClick
        holder.ivComment.setOnClickListener(v -> handleCommentClick(post));

        holder.ivShare.setOnClickListener(v -> handleShareClick(post));

        // Optional: Make entire card clickable for post details
        holder.cardPost.setOnClickListener(v -> {
            // You can add post detail view here if needed
        });
    }

    private void handleLikeClick(Post post, ViewHolder holder, int position) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(context, "Please login to like posts", Toast.LENGTH_SHORT).show();
            return;
        }
        toggleLike(post, holder, position);
    }

    // FIXED: Added Post parameter
    private void handleCommentClick(Post post) {
        if (post == null || post.getPostId() == null) {
            Toast.makeText(context, "Error: Post not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Open CommentsActivity
        Intent intent = new Intent(context, CommentsActivity.class);
        intent.putExtra("postId", post.getPostId());
        context.startActivity(intent);
    }

    private void handleShareClick(Post post) {
        sharePost(post);
    }

    private void toggleLike(Post post, ViewHolder holder, int position) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(context, "Please login to like posts", Toast.LENGTH_SHORT).show();
            return;
        }

        if (post.getPostId() == null) {
            Toast.makeText(context, "Error: Post ID is null", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("posts").child(post.getPostId());

        // Initialize likedBy list if null
        if (post.getLikedBy() == null) {
            post.setLikedBy(new ArrayList<>());
        }

        List<String> likedBy = post.getLikedBy();
        String userId = currentUser.getUid();

        if (likedBy.contains(userId)) {
            // Unlike
            likedBy.remove(userId);
            post.setLikesCount(Math.max(0, post.getLikesCount() - 1));
        } else {
            // Like
            likedBy.add(userId);
            post.setLikesCount(post.getLikesCount() + 1);
        }

        // Update in Firebase
        HashMap<String, Object> updates = new HashMap<>();
        updates.put("likesCount", post.getLikesCount());
        updates.put("likedBy", likedBy);

        postRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    // Update UI immediately
                    if (likedBy.contains(userId)) {
                        holder.ivLike.setImageResource(R.drawable.ic_like_filled);
                    } else {
                        holder.ivLike.setImageResource(R.drawable.ic_like_outline);
                    }
                    holder.tvLikesCount.setText(String.valueOf(post.getLikesCount()));

                    // Also update the list
                    postList.set(position, post);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to update like: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void sharePost(Post post) {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");

            String shareTitle = post.getTitle() != null ? post.getTitle() : "CampusConnect Post";
            String shareContent = post.getContent() != null ? post.getContent() : "Check out this post";

            shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareTitle);
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    shareContent + "\n\nShared from CampusConnect App");

            context.startActivity(Intent.createChooser(shareIntent, "Share Post"));
        } catch (Exception e) {
            Toast.makeText(context, "Unable to share post", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    // Update method to refresh data
    public void updateData(List<Post> newPostList) {
        postList.clear();
        if (newPostList != null) {
            postList.addAll(newPostList);
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvEmail, tvUserBio, tvTimestamp, tvCategory;
        TextView tvTitle, tvContent, tvLikesCount, tvCommentsCount;
        ImageView ivPostImage, ivLike, ivComment, ivShare;
        CardView cardPost;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvUsername = itemView.findViewById(R.id.tv_username);
            tvEmail = itemView.findViewById(R.id.tv_email);
            tvUserBio = itemView.findViewById(R.id.tv_user_bio);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvLikesCount = itemView.findViewById(R.id.tv_likes_count);
            tvCommentsCount = itemView.findViewById(R.id.tv_comments_count);
            ivPostImage = itemView.findViewById(R.id.iv_post_image);
            ivLike = itemView.findViewById(R.id.iv_like);
            ivComment = itemView.findViewById(R.id.iv_comment);
            ivShare = itemView.findViewById(R.id.iv_share);
            cardPost = itemView.findViewById(R.id.card_post);
        }
    }
}