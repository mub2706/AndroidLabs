package com.cst3104.androidlab6;

public class ChatMessage {
    private String message;
    private String timeSent;
    private boolean isSentButton;

    // Constructor
    public ChatMessage(String message, String timeSent, boolean isSentButton) {
        this.message = message;
        this.timeSent = timeSent;
        this.isSentButton = isSentButton;
    }

    // Getter for message
    public String getMessage() {
        return message;
    }

    // Getter for timeSent
    public String getTimeSent() {
        return timeSent;
    }

    // Getter for isSentButton
    public boolean isSentButton() {
        return isSentButton;
    }
}