package com.example.mad_project.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mad_project.ChatActivity;
import com.example.mad_project.R;
import com.example.mad_project.models.Chat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private Context context;
    private List<Chat> chatList;
    private String currentUserId;

    public ChatAdapter(Context context, List<Chat> chatList, String currentUserId) {
        this.context = context;
        this.chatList = chatList;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chat chat = chatList.get(position);

        // Determine the other participant
        String otherUserId = chat.getParticipant1().equals(currentUserId) ?
                chat.getParticipant2() : chat.getParticipant1();
        String otherUserName = chat.getParticipant1().equals(currentUserId) ?
                chat.getParticipant2Name() : chat.getParticipant1Name();

        // Set user name
        holder.tvUserName.setText(otherUserName != null ? otherUserName : "User");

        // Set last message
        holder.tvLastMessage.setText(chat.getLastMessage() != null ? chat.getLastMessage() : "");

        // Set time
        if (chat.getLastMessageTime() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            String time = sdf.format(new Date(chat.getLastMessageTime()));
            holder.tvTime.setText(time);
        } else {
            holder.tvTime.setText("");
        }

        // Set unread count
        if (chat.getUnreadCount() > 0) {
            holder.tvUnreadCount.setText(String.valueOf(chat.getUnreadCount()));
            holder.tvUnreadCount.setVisibility(View.VISIBLE);
        } else {
            holder.tvUnreadCount.setVisibility(View.GONE);
        }

        // Open chat on click
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("chatId", chat.getChatId());
            intent.putExtra("otherUserId", otherUserId);
            intent.putExtra("otherUserName", otherUserName);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public void updateData(List<Chat> newChatList) {
        chatList.clear();
        if (newChatList != null) {
            chatList.addAll(newChatList);
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvLastMessage, tvTime, tvUnreadCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvLastMessage = itemView.findViewById(R.id.tv_last_message);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvUnreadCount = itemView.findViewById(R.id.tv_unread_count);
        }
    }
}