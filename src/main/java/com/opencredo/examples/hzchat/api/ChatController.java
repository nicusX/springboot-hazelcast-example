package com.opencredo.examples.hzchat.api;

import com.opencredo.examples.hzchat.domain.ChatMessage;
import com.opencredo.examples.hzchat.domain.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;

@RestController
@RequestMapping
public class ChatController {

    @Autowired
    private ChatService chatService;

    @RequestMapping(value="/messages", method = RequestMethod.POST)
    public void send(@RequestBody ChatMessageResource messageResource) {

        // TODO Add validation and error handling

        chatService.send(map(messageResource));
    }

    @Transactional // Note this is not actually used, if no transaction manager is defined
    @RequestMapping(value = "/recipients/{recipient}/poll", method = RequestMethod.GET)
    public List<ChatMessageResource> receiveAll(@PathVariable("recipient") String recipient) {
        final List<ChatMessage> messages = chatService.receive(recipient);

        // To simulate a problem receiving the message, throws an exception finding the poison pill
        messages.stream().forEach( ChatController::throwExceptionIfFindPoisonPill );

        return messages.stream().map( ChatController::map ).collect(Collectors.toList());
    }

    @Transactional // Note this is not actually used, if no transaction manager is defined
    @RequestMapping(value = "/recipients/{recipient}/poll-one", method = RequestMethod.GET)
    public ChatMessageResource receiveOne(@PathVariable("recipient") String recipient, HttpServletResponse response) throws IOException {
        final Optional<ChatMessage> polled = chatService.receiveOne(recipient);
        if ( polled.isPresent()) {
            // Different from the receiveAll method, this method does not throw any exception finding the poision pill
            return map( polled.get() );
        } else {
            response.sendError(404);
            return null;
        }

    }



    private static void throwExceptionIfFindPoisonPill(ChatMessage message) {
        if ( message.getTimestamp().equals(Instant.EPOCH)) {
            throw new RuntimeException("Simulated Exception");
        }
    }



    private static ChatMessage map(ChatMessageResource messageResource) {
        return new ChatMessage(
                Instant.parse( messageResource.getTimestamp()),
                messageResource.getSender(),
                messageResource.getRecipient(),
                messageResource.getText());
    }

    private static ChatMessageResource map(ChatMessage message) {
        final ChatMessageResource messageResource = new ChatMessageResource();
        messageResource.setTimestamp( ISO_INSTANT.format(message.getTimestamp()) );
        messageResource.setSender(message.getSender());
        messageResource.setRecipient(message.getRecipient());
        messageResource.setText(message.getText());
        return messageResource;
    }
}
