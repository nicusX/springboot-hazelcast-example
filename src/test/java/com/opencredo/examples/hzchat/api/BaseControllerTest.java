package com.opencredo.examples.hzchat.api;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.opencredo.examples.hzchat.ChatApplication;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.Instant;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ChatApplication.class)
@WebIntegrationTest("server.port=0") // Use a random free port
@DirtiesContext
public abstract class BaseControllerTest {


    //////////////////////////////////////////////////////////////
    // The same Hazelcast instance is reused by every test method
    // See https://github.com/hazelcast/hazelcast/issues/6339
    //////////////////////////////////////////////////////////////


    @Value("${local.server.port}")
    private int port;

    @Before
    public void setUp() {
        RestAssured.port = port;
    }



    @Test
    public void testSend() {
        final String timestamp = Instant.now().toString();

        final String chatMessageJson = makeMessageJson(timestamp, "sender", "aRecipient", "This is the text"  );

        given().contentType(ContentType.JSON).body(chatMessageJson).
                when().post("/messages").
                then().assertThat().statusCode(200);

    }

    @Test
    public void testReceive() throws Exception {
        final String timestamp = Instant.now().toString();
        final String chatMessageJson = makeMessageJson(timestamp, "sender", "bRecipient", "This is the text"  );

        sendMessage(chatMessageJson);

        Thread.sleep(500);

        when().get("/recipients/bRecipient/poll").
                then().assertThat().statusCode(200).
                and().contentType(ContentType.JSON).
                and().body("size()", equalTo(1)).
                and().body("[0].timestamp", equalTo(timestamp)).
                and().body("[0].sender", equalTo("sender")).
                and().body("[0].recipient", equalTo("bRecipient"));
    }

    @Test
    public void testReceiveNoMessage() {
        when().get("/recipients/cRecipient/poll").
                then().assertThat().statusCode(200).
                and().contentType(ContentType.JSON).
                and().body("size()", equalTo(0));
    }

    @Test
    public void testDiscardDuplicateMessages() {
        final String timestamp = Instant.now().toString();
        final String chatMessageJson = makeMessageJson(timestamp, "sender", "dRecipient", "This is the text"  );

        // Send the same message twice
        sendMessage(chatMessageJson);
        sendMessage(chatMessageJson);

        when().get("/recipients/dRecipient/poll").
                then().assertThat().statusCode(200).
                and().contentType(ContentType.JSON).
                and().body("size()", equalTo(1));
    }

    @Test
    public void testPollOne()  throws Exception {
        final String timestamp = Instant.now().toString();
        final String chatMessageJson = makeMessageJson(timestamp, "sender", "eRecipient", "This is the text"  );

        sendMessage(chatMessageJson);

        Thread.sleep(500);

        when().get("/recipients/eRecipient/poll-one").
                then().assertThat().statusCode(200).
                and().contentType(ContentType.JSON).
                and().body("timestamp", equalTo(timestamp)).
                and().body("sender", equalTo("sender")).
                and().body("recipient", equalTo("eRecipient"));

    }

    protected String makeMessageJson(String timestamp, String sender, String recipient, String text) {
        return "{" +
                "\"timestamp\" : \"" + timestamp + "\", " +
                "\"sender\" : \"" + sender + "\", " +
                "\"recipient\" : \"" + recipient + "\", " +
                "\"text\" : \"" + text + "\" " +
                "}";
    }


    protected void sendMessage(String messageJson) {
        given().contentType(ContentType.JSON).body(messageJson).
                when().post("/messages").
                then().assertThat().statusCode(200);
    }

}
