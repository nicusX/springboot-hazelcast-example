package com.opencredo.examples.hzchat.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

public class ChatMessage implements Serializable {
    private final String messageUid;
    private final String recipient;
    private final String sender;
    private final String text;

    public ChatMessage(String messageUid, String sender, String recipient, String text) {
        this.messageUid = messageUid;
        this.recipient = recipient;
        this.sender = sender;
        this.text = text;
    }

    public String getMessageUid() {
        return messageUid;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getSender() {
        return sender;
    }

    public String getText() {
        return text;
    }

    @Override // Not strictly required by Hazelcast
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ChatMessage that = (ChatMessage) o;

        return new EqualsBuilder()
                .append(messageUid, that.messageUid)
                .append(recipient, that.recipient)
                .append(sender, that.sender)
                .append(text, that.text)
                .isEquals();
    }

    @Override // Not strictly required by Hazelcast
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(messageUid)
                .append(recipient)
                .append(sender)
                .append(text)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("messageUid", messageUid)
                .append("recipient", recipient)
                .append("sender", sender)
                .append("text", text)
                .toString();
    }
}
