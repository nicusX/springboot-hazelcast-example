package com.opencredo.examples.hzchat.domain;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Base implementation of ChatService
 * not implementing a transactional receiver on polling
 */
public class ChatServiceImpl implements ChatService {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    protected final HazelcastInstance hazelcastInstance;

    public static final String ACCEPTED_MESSAGES_TRACKING_MAP_NAME = "received";
    public static final String RECIPIENT_QUEUE_NAME_SUFFIX = "recipient-";

    public ChatServiceImpl(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    // Starting the HazelcastInstance is heavyweight, while retrieving a distributed object from it is not

    private IQueue<ChatMessage> recipientQueueForSeding(String recipient) {
        return hazelcastInstance.getQueue(RECIPIENT_QUEUE_NAME_SUFFIX + recipient);
    }

    protected IQueue<ChatMessage> recipientQueueForPolling(String recipient) {
        LOG.debug("Using non-transactional Queue for polling");
        return recipientQueueForSeding(recipient); // Same queue as for sending
    }

    private IMap<Integer, String> acceptedMessagesHash() {
        return hazelcastInstance.getMap(ACCEPTED_MESSAGES_TRACKING_MAP_NAME);
    }


    @Override
    public void send(ChatMessage message)  {

        // Check if the message is duplicate. If duplicate, silently ignore it
        if( !isDuplicate(message)) {
            LOG.debug("Submitting the message {}", message);
            recipientQueueForSeding(message.getRecipient()).offer(message);

            // Save UID as accepted
            markAsAccepted(message);
        }

    }

    @Override
    public List<ChatMessage> receive(String recipient) {
        final IQueue<ChatMessage> recipientQueue = recipientQueueForPolling(recipient);
        LOG.debug("Polling message for recipient: {}", recipient);

        // Poll recipient's queue until empty
        final List<ChatMessage> messages = new ArrayList<>();
        while ( true ) {
            final ChatMessage message = recipientQueue.poll();
            if ( message == null ) break;
            LOG.debug("Polled message {}", message);

            messages.add(message);
        }
        LOG.debug("Returning {} messages", messages.size());

        // Return the received messages
        return Collections.unmodifiableList(messages);
    }

    @Override
    public Optional<ChatMessage> receiveOne(String recipient) {
        LOG.debug("Polling one message for recipient: {}", recipient);
        return Optional.ofNullable(recipientQueueForPolling(recipient).poll());
    }

    private boolean isDuplicate(ChatMessage message) {
        // We just store and check the message UID. A distributed Set would suffice, but unfortunately
        // Hazelcast ISet doesn't support automatic eviction
        final boolean duplicate = acceptedMessagesHash().containsKey(message.hashCode());
        LOG.debug("Message {} (hash: {}) is duplicate? {}", message, message.hashCode(), duplicate);
        return duplicate;
    }

    private void markAsAccepted(ChatMessage message) {
        LOG.debug("Marking message {} (hash: {}) as accepted", message.hashCode());
        acceptedMessagesHash().put(message.hashCode(),"");
    }
}
