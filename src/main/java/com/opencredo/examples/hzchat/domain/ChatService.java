package com.opencredo.examples.hzchat.domain;

import java.util.List;
import java.util.Optional;

public interface ChatService {

    void send(ChatMessage message);

    List<ChatMessage> receive(String recipient);

    Optional<ChatMessage> receiveOne(String recipient);
}
