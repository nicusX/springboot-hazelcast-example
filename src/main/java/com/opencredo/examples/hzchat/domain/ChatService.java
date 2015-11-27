package com.opencredo.examples.hzchat.domain;

import java.util.List;

public interface ChatService {

    void send(ChatMessage message);

    List<ChatMessage> receive(String recipient);
}
