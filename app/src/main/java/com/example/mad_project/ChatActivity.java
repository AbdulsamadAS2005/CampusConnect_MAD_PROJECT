package com.example.mad_project;

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

import com.example.mad_project.adapters.MessageAdapter;
import com.example.mad_project.models.Chat;
import com.example.mad_project.models.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private TextView tvChatName, tvOnlineStatus;
    private ImageView ivBack, ivSend;
    private EditText etMessage;
    private RecyclerView recyclerViewMessages;
    private ProgressBar progressBar;

    private MessageAdapter messageAdapter;
    private List<Message> messageList;

    private FirebaseUser currentUser;
    private DatabaseReference chatsRef, messagesRef, usersRef;

    private String chatId, otherUserId, otherUserName;
    private boolean isNewChat = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Get intent data
        chatId = getIntent().getStringExtra("chatId");
        otherUserId = getIntent().getStringExtra("otherUserId");
        otherUserName = getIntent().getStringExtra("otherUserName");

        if (otherUserId == null) {
            Toast.makeText(this, "Error: User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        chatsRef = FirebaseDatabase.getInstance().getReference("chats");
        messagesRef = FirebaseDatabase.getInstance().getReference("messages");
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Initialize views
        tvChatName = findViewById(R.id.tv_chat_name);
        tvOnlineStatus = findViewById(R.id.tv_online_status);
        ivBack = findViewById(R.id.iv_back);
        ivSend = findViewById(R.id.iv_send);
        etMessage = findViewById(R.id.et_message);
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        progressBar = findViewById(R.id.progressBar);

        // Set chat name
        tvChatName.setText(otherUserName != null ? otherUserName : "User");

        // Setup RecyclerView
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList, currentUser.getUid());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Start from bottom
        recyclerViewMessages.setLayoutManager(layoutManager);
        recyclerViewMessages.setAdapter(messageAdapter);

        // Back button
        ivBack.setOnClickListener(v -> finish());

        // Send button
        ivSend.setOnClickListener(v -> sendMessage());

        // Check if chat exists or create new
        if (chatId == null || chatId.isEmpty()) {
            isNewChat = true;
            checkAndCreateChat();
        } else {
            loadMessages();
        }

        // Load online status (simplified - you can implement real presence later)
        tvOnlineStatus.setText("Online");
    }

    private void checkAndCreateChat() {
        // Use arrays to work around the "effectively final" requirement
        final boolean[] chatExists = new boolean[1];
        final String[] foundChatId = new String[1];

        // Check if chat already exists between these users
        chatsRef.orderByChild("participant1").equalTo(currentUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        chatExists[0] = false;
                        foundChatId[0] = null;

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Chat chat = snapshot.getValue(Chat.class);
                            if (chat != null && chat.getParticipant2().equals(otherUserId)) {
                                foundChatId[0] = snapshot.getKey();
                                chatExists[0] = true;
                                break;
                            }
                        }

                        if (!chatExists[0]) {
                            // Also check reverse (current user as participant2)
                            chatsRef.orderByChild("participant2").equalTo(currentUser.getUid())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                Chat chat = snapshot.getValue(Chat.class);
                                                if (chat != null && chat.getParticipant1().equals(otherUserId)) {
                                                    chatId = snapshot.getKey();
                                                    loadMessages();
                                                    return;
                                                }
                                            }
                                            // Create new chat
                                            createNewChat();
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            createNewChat();
                                        }
                                    });
                        } else {
                            // Use the found chat ID
                            if (foundChatId[0] != null) {
                                chatId = foundChatId[0];
                            }
                            loadMessages();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        createNewChat();
                    }
                });
    }

    private void createNewChat() {
        // Get current user info
        usersRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String currentUserName = "User";
                if (dataSnapshot.exists()) {
                    currentUserName = dataSnapshot.child("username").getValue(String.class);
                    if (currentUserName == null) currentUserName = "User";
                }

                // Get other user info
                String finalCurrentUserName = currentUserName;
                usersRef.child(otherUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String otherUserNameFromDB = "User";
                        if (dataSnapshot.exists()) {
                            otherUserNameFromDB = dataSnapshot.child("username").getValue(String.class);
                            if (otherUserNameFromDB == null) otherUserNameFromDB = "User";
                        }

                        // Create chat
                        chatId = chatsRef.push().getKey();
                        Chat chat = new Chat(
                                chatId,
                                currentUser.getUid(),
                                otherUserId,
                                finalCurrentUserName,
                                otherUserNameFromDB,
                                "",
                                System.currentTimeMillis(),
                                0
                        );

                        if (chatId != null) {
                            chatsRef.child(chatId).setValue(chat)
                                    .addOnSuccessListener(aVoid -> {
                                        // Chat created successfully
                                        loadMessages();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(ChatActivity.this,
                                                "Failed to create chat", Toast.LENGTH_SHORT).show();
                                        finish();
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(ChatActivity.this,
                                "Failed to load user info", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ChatActivity.this,
                        "Failed to load user info", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMessages() {
        if (chatId == null) return;

        progressBar.setVisibility(View.VISIBLE);

        messagesRef.child(chatId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messageList.clear();

                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Message message = snapshot.getValue(Message.class);
                        if (message != null) {
                            if (message.getMessageId() == null) {
                                message.setMessageId(snapshot.getKey());
                            }
                            messageList.add(message);
                        }
                    }

                    messageAdapter.updateData(messageList);

                    // Scroll to bottom
                    if (messageList.size() > 0) {
                        recyclerViewMessages.scrollToPosition(messageList.size() - 1);
                    }
                }

                progressBar.setVisibility(View.GONE);

                // Mark messages as read
                markMessagesAsRead();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ChatActivity.this,
                        "Failed to load messages", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();

        if (TextUtils.isEmpty(messageText)) {
            return;
        }

        if (chatId == null) {
            Toast.makeText(this, "Please wait, creating chat...", Toast.LENGTH_SHORT).show();
            return;
        }

        // Clear input
        etMessage.setText("");

        // Create message
        String messageId = messagesRef.child(chatId).push().getKey();
        Message message = new Message(
                messageId,
                chatId,
                currentUser.getUid(),
                currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "User",
                messageText,
                System.currentTimeMillis(),
                false,
                "text"
        );

        // Save message
        if (messageId != null) {
            messagesRef.child(chatId).child(messageId).setValue(message)
                    .addOnSuccessListener(aVoid -> {
                        // Update chat last message
                        updateChatLastMessage(messageText);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateChatLastMessage(String lastMessage) {
        if (chatId == null) return;

        HashMap<String, Object> updates = new HashMap<>();
        updates.put("lastMessage", lastMessage);
        updates.put("lastMessageTime", System.currentTimeMillis());

        // Increment unread count for other user
        updates.put("unreadCount", 1); // You can make this dynamic

        chatsRef.child(chatId).updateChildren(updates);
    }

    private void markMessagesAsRead() {
        if (chatId == null) return;

        // Mark all messages from other user as read
        messagesRef.child(chatId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    if (message != null && !message.getSenderId().equals(currentUser.getUid()) && !message.isRead()) {
                        snapshot.getRef().child("isRead").setValue(true);
                    }
                }

                // Reset unread count for current user
                chatsRef.child(chatId).child("unreadCount").setValue(0);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Silent fail
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (chatId != null) {
            markMessagesAsRead();
        }
    }
}