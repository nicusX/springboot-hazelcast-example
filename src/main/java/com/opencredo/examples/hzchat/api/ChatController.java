package com.opencredo.examples.hzchat.api;

import com.opencredo.examples.hzchat.domain.ChatMessage;
import com.opencredo.examples.hzchat.domain.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/messages")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @RequestMapping(method = RequestMethod.POST)
    public void send(@RequestBody ChatMessageResource messageResource) {

        // TODO Add validation and error handling

        chatService.send(map(messageResource));
    }


    @RequestMapping(value = "/{receiver}", method = RequestMethod.GET)
    public List<ChatMessageResource> receive(@PathVariable("receiver") String receiver) {
        return chatService.receive(receiver).stream().map( ChatController::map ).collect(Collectors.toList());
    }

    private static ChatMessage map(ChatMessageResource messageResource) {
        return new ChatMessage(
                messageResource.getMessageUid(),
                messageResource.getSender(),
                messageResource.getRecipient(),
                messageResource.getText());
    }

    private static ChatMessageResource map(ChatMessage message) {
        final ChatMessageResource messageResource = new ChatMessageResource();
        messageResource.setMessageUid(message.getMessageUid());
        messageResource.setSender(message.getSender());
        messageResource.setRecipient(message.getRecipient());
        messageResource.setText(message.getText());
        return messageResource;
    }
}
