package com.opencredo.examples.hzchat.domain;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class ChatServiceImplTest {
    private static Logger LOG = LoggerFactory.getLogger("TEST");


    private ChatService service;

    @Before
    public void setUp() {
        final HazelcastInstance instance = Hazelcast.newHazelcastInstance();
        service = new ChatServiceImpl(instance);
    }

    @After
    public void shutdown( ) {
        LOG.info("Shutting down all Hazelcast instances");
        Hazelcast.shutdownAll();
    }


    @Test(timeout = 10000L)
    public void testSendAndReceive()  {
        final ChatMessage message = new ChatMessage(Instant.now(), "sender", "recipient", "text");

        service.send(message);

        List<ChatMessage> received = new ArrayList<>();
        while(received.isEmpty()) {
            received = service.receive("recipient");
        }

        assertNotNull(received);
        assertEquals(1, received.size());
        assertEquals(message, received.get(0));
    }

    @Test(timeout = 10000L)
    public void testReceiveMessageInOrder() {
        final ChatMessage message1 = new ChatMessage(Instant.now(), "sender-A", "recipient", "text");
        final ChatMessage message2 = new ChatMessage(Instant.now(), "sender-B", "recipient", "text");

        service.send(message1);
        service.send(message2);

        List<ChatMessage> received = new ArrayList<>();
        while(received.isEmpty()) {
            received = service.receive("recipient");
        }

        assertNotNull(received);
        assertEquals(2, received.size());
        assertEquals(message1, received.get(0));
        assertEquals(message2, received.get(1));

    }

    @Test(timeout = 10000L)
    public void testIgnoreDuplicateMessages() {
        final ChatMessage message = new ChatMessage(Instant.now(), "sender", "recipient", "text");

        service.send(message);
        service.send(message);

        List<ChatMessage> received = new ArrayList<>();
        while(received.isEmpty()) {
            received = service.receive("recipient");
        }

        assertNotNull(received);
        assertEquals(1, received.size());
        assertEquals(message, received.get(0));
    }


    @Test(timeout = 10000L)
    public void testReceiveNoMessage() {
        final List<ChatMessage> received = service.receive("recipient");

        assertNotNull(received);
        assertTrue(received.isEmpty());

    }

    @Test(timeout = 10000L)
    public void testReceiveOne() throws Exception {
        final Instant timestamps1 = Instant.now();
        final ChatMessage message1 = new ChatMessage(timestamps1, "sender-A", "recipient", "text");
        final Instant timestamps2 = Instant.now();
        final ChatMessage message2 = new ChatMessage(timestamps2, "sender-B", "recipient", "text");
        service.send(message1);
        service.send(message2);

        Optional<ChatMessage> received1 = Optional.empty();
        while (!received1.isPresent()) {
            received1 = service.receiveOne("recipient");
        }
        assertTrue(received1.isPresent());
        assertEquals(timestamps1, received1.get().getTimestamp());

        Optional<ChatMessage> received2 = Optional.empty();
        while(!received2.isPresent()) {
            received2 = service.receiveOne("recipient");
        }
        assertTrue(received2.isPresent());
        assertEquals(timestamps2, received2.get().getTimestamp());

        Optional<ChatMessage> received3 = service.receiveOne("recipient");
        assertFalse(received3.isPresent());
    }
}