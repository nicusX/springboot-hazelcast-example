package com.opencredo.examples.hzchat.domain;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.*;

public class ChatServiceHazelcastImplTest {
    private static Logger LOG = LoggerFactory.getLogger("TEST");


    private ChatService service;

    @Before
    public void setUp() {
        final HazelcastInstance instance = Hazelcast.newHazelcastInstance();
        service = new ChatServiceHazelcastImpl(instance);
    }

    @After
    public void shutdown( ) {
        LOG.info("Shutting down all Hazelcast instances");
        Hazelcast.shutdownAll();
    }


    @Test
    public void testSendAndReceive()  {
        final ChatMessage message = new ChatMessage("mssg-uid", "sender", "recipient", "text");

        service.send(message);

        final List<ChatMessage> received = service.receive("recipient");

        assertNotNull(received);
        assertEquals(1, received.size());
        assertEquals(message, received.get(0));
    }

    @Test
    public void testReceiveMessageInOrder() {
        final ChatMessage message1 = new ChatMessage("mssg-uid1", "sender-A", "recipient", "text");
        final ChatMessage message2 = new ChatMessage("mssg-uid2", "sender-B", "recipient", "text");

        service.send(message1);
        service.send(message2);

        final List<ChatMessage> received = service.receive("recipient");

        assertNotNull(received);
        assertEquals(2, received.size());
        assertEquals(message1, received.get(0));
        assertEquals(message2, received.get(1));

    }

    @Test
    public void testIgnoreDuplicateMessages() {
        final ChatMessage message = new ChatMessage("mssg-uid", "sender", "recipient", "text");

        service.send(message);
        service.send(message);

        final List<ChatMessage> received = service.receive("recipient");

        assertNotNull(received);
        assertEquals(1, received.size());
        assertEquals(message, received.get(0));
    }

    @Test
    public void testReceiveNoMessage() {
        final List<ChatMessage> received = service.receive("recipient");

        assertNotNull(received);
        assertTrue(received.isEmpty());

    }
}