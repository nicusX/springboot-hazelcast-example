package com.opencredo.examples.hzchat.api;

public class ChatMessageResource {
    private String messageUid;
    private String recipient;
    private String sender;
    private String text;

    public String getMessageUid() { return messageUid; }

    public void setMessageUid(String messageUid) {
        this.messageUid = messageUid;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
