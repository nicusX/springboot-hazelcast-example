package com.opencredo.examples.hzchat.domain;

import com.hazelcast.core.BaseQueue;
import com.hazelcast.core.HazelcastInstance;
import com.opencredo.hazelcast.HazelcastUtils;

/**
 * Extension of ChatService base implementation,
 * using transactional queue on polling
 */
public class TransactionalChatServiceImpl extends ChatServiceImpl {

    public TransactionalChatServiceImpl(HazelcastInstance hazelcastInstance) {
        super(hazelcastInstance);
    }

    @Override
    protected BaseQueue<ChatMessage> recipientQueueForPolling(String recipient) {
        // For Polling, uses a transactional queue, from HazelcastUtils
        LOG.debug("Try to use transactional Queue for polling");
        return HazelcastUtils.getTransactionalQueue( RECIPIENT_QUEUE_NAME_SUFFIX + recipient, hazelcastInstance, true);
    }
}
