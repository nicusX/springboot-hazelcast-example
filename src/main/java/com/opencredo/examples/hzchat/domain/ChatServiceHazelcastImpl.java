package com.opencredo.examples.hzchat.domain;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ChatServiceHazelcastImpl implements ChatService {
    private static final Logger LOG = LoggerFactory.getLogger(ChatServiceHazelcastImpl.class);

    private final HazelcastInstance hazelcastInstance;

    public static final String ACCEPTED_MESSAGES_TRACKING_MAP_NAME = "received";
    public static final String RECIPIENT_QUEUE_NAME_SUFFIX = "recipient-";

    @Autowired
    public ChatServiceHazelcastImpl(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    // Starting the HazelcastInstance is heavyweight, while retrieving a distributed object from it is not

    private IQueue<ChatMessage> recipientQueue(String user) {
        return hazelcastInstance.getQueue(RECIPIENT_QUEUE_NAME_SUFFIX + user);
    }


    private IMap<Object, Object> acceptedMessageUidMap() {
        return hazelcastInstance.getMap(ACCEPTED_MESSAGES_TRACKING_MAP_NAME);
    }


    @Override
    public void send(ChatMessage message)  {

        // Check if the message is duplicate. If duplicate, silently ignore it
        if( !isDuplicate(message)) {
            LOG.debug("Submitting the message id:{}", message.getMessageUid());
            recipientQueue(message.getRecipient()).offer(message);

            // Save UID as accepted
            markAsAccepted(message);
        }

    }

    @Override
    public List<ChatMessage> receive(String recipient) {
        LOG.debug("Polling message for recipient: {}", recipient);

         // Poll recipient's queue until empty
        final List<ChatMessage> messages = new ArrayList();
        while ( true ) {
            final ChatMessage message = recipientQueue(recipient).poll();
            if ( message == null ) break;
            LOG.debug("Polled message id:{}", message.getMessageUid());
            messages.add(message);
        }
        LOG.debug("Returning {} messages", messages.size());

        // This is not a transactional receiver: If something happens here, the messages are lost...

        // Return the received messages
        return Collections.unmodifiableList(messages);
    }



    private boolean isDuplicate(ChatMessage message) {
        // We just store and check the message UID. A distributed Set would suffice, but unfortunately
        // Hazelcast ISet doesn't support automatic eviction
        final boolean duplicate = acceptedMessageUidMap().containsKey(message.getMessageUid());
        LOG.debug("Message id:{} is duplicate? {}", message.getMessageUid(), duplicate);
        return duplicate;
    }

    private void markAsAccepted(ChatMessage message) {
        LOG.debug("Marking message id:{} as accepted", message.getMessageUid());
        acceptedMessageUidMap().put(message.getMessageUid(),"");
    }
}
