package com.opencredo.examples.hzchat.domain;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;
import org.mpilone.hazelcastmq.spring.tx.HazelcastUtils;

/**
 * Extension of ChatService base implementation,
 * using transactional queue on polling
 */
public class TransactionalChatServiceImpl extends ChatServiceImpl {

    public TransactionalChatServiceImpl(HazelcastInstance hazelcastInstance) {
        super(hazelcastInstance);
    }

    @Override
    protected IQueue<ChatMessage> recipientQueueForPolling(String recipient) {
        // For Polling, uses a transactional queue, from HazelcastUtils
        LOG.debug("Try to use transactional Queue for polling");
        return HazelcastUtils.getTransactionalQueue( RECIPIENT_QUEUE_NAME_SUFFIX + recipient, hazelcastInstance, true);
    }
}
