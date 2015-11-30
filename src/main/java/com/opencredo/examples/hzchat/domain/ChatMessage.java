package com.opencredo.examples.hzchat.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.time.Instant;

public class ChatMessage implements Serializable {
    private static final long serialVersionUID = 6664167798628462875L;

    private final Instant timestamp;
    private final String recipient;
    private final String sender;
    private final String text;

    public ChatMessage(Instant timestamp, String sender, String recipient, String text) {
        this.timestamp = timestamp;
        this.recipient = recipient;
        this.sender = sender;
        this.text = text;
    }

    public Instant getTimestamp() {
        return timestamp;
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
                .append(timestamp, that.timestamp)
                .append(recipient, that.recipient)
                .append(sender, that.sender)
                .append(text, that.text)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(timestamp)
                .append(recipient)
                .append(sender)
                .append(text)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("timestamp", timestamp)
                .append("recipient", recipient)
                .append("sender", sender)
                .append("text", text)
                .toString();
    }
}
