package com.example.mad_project.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mad_project.R;
import com.example.mad_project.models.Comment;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private Context context;
    private List<Comment> commentList;

    public CommentAdapter(Context context, List<Comment> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = commentList.get(position);

        if (comment == null) return;

        holder.tvCommenterName.setText(comment.getUsername() != null ? comment.getUsername() : "User");
        holder.tvCommentText.setText(comment.getCommentText() != null ? comment.getCommentText() : "");

        if (comment.getFormattedDate() != null) {
            holder.tvCommentTime.setText(comment.getFormattedDate());
        } else {
            holder.tvCommentTime.setText("Just now");
        }
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public void updateData(List<Comment> newCommentList) {
        commentList.clear();
        if (newCommentList != null) {
            commentList.addAll(newCommentList);
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCommenterName, tvCommentText, tvCommentTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCommenterName = itemView.findViewById(R.id.tv_commenter_name);
            tvCommentText = itemView.findViewById(R.id.tv_comment_text);
            tvCommentTime = itemView.findViewById(R.id.tv_comment_time);
        }
    }
}