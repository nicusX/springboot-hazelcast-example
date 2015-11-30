package com.opencredo.examples.hzchat.api;

import com.jayway.restassured.http.ContentType;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static com.jayway.restassured.RestAssured.when;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;


@ActiveProfiles("transactional-polling") // Run tests with "transactional-polling" profile
public class TransactionalChatControllerTest extends BaseControllerTest {

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

        /// As the implementation uses a transactional queue for polling
        /// expect to find the messages in the queue

        // Poll one message at a time
        when().get("/recipients/aRecipient/poll-one").
        then().assertThat().statusCode(200).
        and().contentType(ContentType.JSON).
        and().body("timestamp", equalTo(timestamps[0]));

        when().get("/recipients/aRecipient/poll-one").
        then().assertThat().statusCode(200).
        and().contentType(ContentType.JSON).
        and().body("timestamp", equalTo(timestamps[1]));

        when().get("/recipients/aRecipient/poll-one").
        then().assertThat().statusCode(200).
        and().contentType(ContentType.JSON).
        and().body("timestamp", equalTo(timestamps[2]));

        when().get("/recipients/aRecipient/poll-one").
        then().assertThat().statusCode(404);

    }


}
