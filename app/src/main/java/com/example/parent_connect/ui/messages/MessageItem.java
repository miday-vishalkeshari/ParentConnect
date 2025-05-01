package com.example.parent_connect.ui.messages;

public class MessageItem {
    private String messageId; // Field for messageId
    private String messageName; // Field for messageName
    private String messageDescription; // Field for messageDescription

    // Constructor
    public MessageItem(String messageId, String messageName, String messageDescription) {
        this.messageId = messageId; // Initialize messageId
        this.messageName = messageName;
        this.messageDescription = messageDescription;
    }

    // Default Constructor (required for Firebase)
    public MessageItem() {
    }

    // Getter for messageId
    public String getMessageId() {
        return messageId;
    }

    // Setter for messageId
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    // Getter for messageName
    public String getMessageName() {
        return messageName;
    }

    // Setter for messageName
    public void setMessageName(String messageName) {
        this.messageName = messageName;
    }

    // Getter for messageDescription
    public String getMessageDescription() {
        return messageDescription;
    }

    // Setter for messageDescription
    public void setMessageDescription(String messageDescription) {
        this.messageDescription = messageDescription;
    }
}
