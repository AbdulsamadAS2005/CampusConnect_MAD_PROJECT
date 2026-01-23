package com.example.mad_project.models;

import java.util.List;

public class Chat {
    private String chatId;
    private String participant1;
    private String participant2;
    private String participant1Name;
    private String participant2Name;
    private String lastMessage;
    private long lastMessageTime;
    private int unreadCount;
    private List<String> participants;

    public Chat() {
        // Default constructor for Firebase
    }

    public Chat(String chatId, String participant1, String participant2,
                String participant1Name, String participant2Name,
                String lastMessage, long lastMessageTime, int unreadCount) {
        this.chatId = chatId;
        this.participant1 = participant1;
        this.participant2 = participant2;
        this.participant1Name = participant1Name;
        this.participant2Name = participant2Name;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.unreadCount = unreadCount;
    }

    // Getters and Setters
    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }

    public String getParticipant1() { return participant1; }
    public void setParticipant1(String participant1) { this.participant1 = participant1; }

    public String getParticipant2() { return participant2; }
    public void setParticipant2(String participant2) { this.participant2 = participant2; }

    public String getParticipant1Name() { return participant1Name; }
    public void setParticipant1Name(String participant1Name) { this.participant1Name = participant1Name; }

    public String getParticipant2Name() { return participant2Name; }
    public void setParticipant2Name(String participant2Name) { this.participant2Name = participant2Name; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public long getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(long lastMessageTime) { this.lastMessageTime = lastMessageTime; }

    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }

    public List<String> getParticipants() { return participants; }
    public void setParticipants(List<String> participants) { this.participants = participants; }
}