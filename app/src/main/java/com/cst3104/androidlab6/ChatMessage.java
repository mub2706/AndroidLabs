package com.cst3104.androidlab6;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ChatMessage {
    @ColumnInfo(name="message")
    protected String message;
    @ColumnInfo(name="TimeSent")
    protected String timeSent;
    @ColumnInfo(name="Send0rReceive")
    protected boolean isSentButton;

    @PrimaryKey (autoGenerate = true)
    @ColumnInfo (name="id")
    public int id;

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