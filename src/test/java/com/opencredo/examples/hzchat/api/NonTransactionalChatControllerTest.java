package com.opencredo.examples.hzchat.api;

import org.junit.Test;

import java.time.Instant;

import static com.jayway.restassured.RestAssured.when;
import static java.util.Arrays.asList;

// Runs the test with no special Spring Bean Profile
public class NonTransactionalChatControllerTest extends BaseControllerTest {

    @Test
    public void testExceptionWhilePolling() throws Exception {
        final String[] timestamps = { Instant.now().toString(), Instant.now().toString(), Instant.EPOCH.toString()};
        final String[] chatMessagesJson = {
                makeMessageJson(timestamps[0], "sender", "aRecipient", "This is the first message"),
                makeMessageJson(timestamps[1], "sender", "aRecipient", "This is the second message"),
                makeMessageJson(timestamps[2], "sender", "aRecipient", "This is the poison pill") };

        // Send messages
        asList(chatMessagesJson).stream().forEach( this::sendMessage );

        Thread.sleep(500);

        // Poll all messages (expect an error)
        when().get("/recipients/aRecipient/poll").
                then().assertThat().statusCode(500);

        Thread.sleep(500);

        /// As the implementation uses a non-transactional queue for polling
        /// expect not to find any message in the queue

        when().get("/recipients/aRecipient/poll-one").
                then().assertThat().statusCode(404);

    }

}
