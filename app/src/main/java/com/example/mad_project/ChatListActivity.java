package com.example.mad_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mad_project.adapters.ChatAdapter;
import com.example.mad_project.models.Chat;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ChatListActivity extends AppCompatActivity {

    private RecyclerView recyclerViewChats;
    private TextView tvEmpty;
    private ProgressBar progressBar;

    private ChatAdapter chatAdapter;
    private List<Chat> chatList;

    private FirebaseUser currentUser;
    private DatabaseReference chatsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        // Initialize Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        chatsRef = FirebaseDatabase.getInstance().getReference("chats");

        // Initialize views
        recyclerViewChats = findViewById(R.id.recyclerViewChats);
        tvEmpty = findViewById(R.id.tv_empty);
        progressBar = findViewById(R.id.progressBar);

        FloatingActionButton fabNewChat = findViewById(R.id.fab_new_chat);
        fabNewChat.setOnClickListener(v -> {
            startActivity(new Intent(ChatListActivity.this, UsersListActivity.class));
        });

        // Setup RecyclerView
        chatList = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, chatList, currentUser.getUid());
        recyclerViewChats.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChats.setAdapter(chatAdapter);

        // Load chats
        loadChats();
    }

    private void loadChats() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        // Query chats where current user is participant1 or participant2
        chatsRef.orderByChild("lastMessageTime")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        chatList.clear();

                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Chat chat = snapshot.getValue(Chat.class);
                                if (chat != null) {
                                    // Check if current user is in this chat
                                    if (currentUser.getUid().equals(chat.getParticipant1()) ||
                                            currentUser.getUid().equals(chat.getParticipant2())) {

                                        // Set chat ID
                                        if (chat.getChatId() == null) {
                                            chat.setChatId(snapshot.getKey());
                                        }
                                        chatList.add(chat);
                                    }
                                }
                            }

                            // Sort by last message time (newest first)
                            Collections.sort(chatList, (c1, c2) ->
                                    Long.compare(c2.getLastMessageTime(), c1.getLastMessageTime()));

                            chatAdapter.updateData(chatList);
                        }

                        // Show/hide empty state
                        if (chatList.isEmpty()) {
                            tvEmpty.setVisibility(View.VISIBLE);
                        } else {
                            tvEmpty.setVisibility(View.GONE);
                        }

                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        progressBar.setVisibility(View.GONE);
                        tvEmpty.setVisibility(View.VISIBLE);
                        Toast.makeText(ChatListActivity.this,
                                "Failed to load chats", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentUser != null) {
            loadChats();
        }
    }
}